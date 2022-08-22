/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dasburo.spring.cache.dynamo;

import com.dasburo.spring.cache.dynamo.rootattribute.RootAttribute;
import com.dasburo.spring.cache.dynamo.util.TableUtils;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.TimeToLiveSpecification;
import software.amazon.awssdk.services.dynamodb.model.UpdateTimeToLiveRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

import static software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType.S;

/**
 * {@link DynamoCacheWriter} implementation capable of reading/writing binary data from/to DynamoDB in {@literal standalone}
 * and {@literal cluster} environments. Works upon a given {@link DynamoDbClient} holds the actual connection.
 * <p>
 * {@link DefaultDynamoCacheWriter} can be used in
 * {@link DynamoCacheWriter#lockingDynamoCacheWriter(DynamoDbClient) locking} or
 * {@link DynamoCacheWriter#nonLockingDynamoCacheWriter(DynamoDbClient) non-locking} mode. While
 * {@literal non-locking} aims for maximum performance it may result in overlapping, non-atomic, command execution for
 * operations spanning multiple DynamoDB interactions like {@code putIfAbsent}. The {@literal locking} counterpart prevents
 * command overlap by setting an explicit lock key and checking against presence of this key which leads to additional
 * requests and potential command wait times.
 *
 * @author Georg Zimmermann
 */
public class DefaultDynamoCacheWriter implements DynamoCacheWriter {

  public static final String ATTRIBUTE_KEY = "key";
  public static final String ATTRIBUTE_VALUE = "value";
  public static final String ATTRIBUTE_TTL = "ttl";

  private final DynamoDbClient dynamoTemplate;
  private final Duration sleepTime;

  /**
   * @param dynamoTemplate must not be {@literal null}.
   */
  DefaultDynamoCacheWriter(DynamoDbClient dynamoTemplate) {
    this(dynamoTemplate, Duration.ZERO);
  }

  /**
   * @param dynamoTemplate must not be {@literal null}.
   * @param sleepTime      sleep time between lock request attempts. Must not be {@literal null}. Use {@link Duration#ZERO}
   *                       to disable locking.
   */
  DefaultDynamoCacheWriter(DynamoDbClient dynamoTemplate, Duration sleepTime) {
    Assert.notNull(dynamoTemplate, "ConnectionFactory must not be null!");
    Assert.notNull(sleepTime, "SleepTime must not be null!");

    this.dynamoTemplate = dynamoTemplate;
    this.sleepTime = sleepTime;
  }

  @Override
  public DynamoDbClient getNativeCacheWriter() {
    return dynamoTemplate;
  }

  @Override
  public void put(String name, String key, byte[] value, @Nullable Duration ttl, @Nullable List<RootAttribute> rootAttributes) {
    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(key, "Key must not be null!");

    execute(name, connection -> {
      putInternal(name, key, value, ttl, rootAttributes);

      return "OK";
    });
  }

  @Override
  public byte[] get(String name, String key) {
    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(key, "Key must not be null!");

    return execute(name, connection -> getInternal(name, key));
  }

  @Override
  public byte[] putIfAbsent(String name, String key, @Nullable byte[] value, @Nullable Duration ttl, @Nullable List<RootAttribute> rootAttributes) {
    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(key, "Key must not be null!");

    return execute(name, connection -> {

      if (isLockingCacheWriter()) {
        doLock(name);
      }

      try {
        return getInternal(name, key);
      } catch (NoSuchElementException e) {
        putInternal(name, key, value, ttl, rootAttributes);
      } finally {
        if (isLockingCacheWriter()) {
          doUnlock(name);
        }
      }
      return null;
    });
  }

  @Override
  public void remove(String name, String key) {
    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(key, "Key must not be null!");

    execute(name, connection -> {
      removeInternal(name, key);
      return "OK";
    });
  }

  @Override
  public void clear(String name) {
    Assert.notNull(name, "Name must not be null!");

    execute(name, connection -> {
      try {
        if (isLockingCacheWriter()) {
          doLock(name);
        }

        List<Map<String, AttributeValue>> items = dynamoTemplate.scan(req -> req.tableName(name)).items();

        items.parallelStream()
          .forEach(map -> {
            Map<String, AttributeValue> keyToDelete = new HashMap<>();
            keyToDelete.put(ATTRIBUTE_KEY, map.get(ATTRIBUTE_KEY));
            DeleteItemRequest delReq = DeleteItemRequest.builder()
              .tableName(name)
              .key(keyToDelete)
              .build();
            dynamoTemplate.deleteItem(delReq);
          });
      } catch (ResourceNotFoundException ignored) {
        // ignore table not found
      } finally {
        if (isLockingCacheWriter()) {
          doUnlock(name);
        }
      }
      return "OK";
    });
  }

  @Override
  public boolean createIfNotExists(String name, Duration ttl, Long readCapacityUnits, Long writeCapacityUnits) {
    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(ttl, "TTL must not be null! Use Duration.ZERO to disable TTL.");

    boolean created = false;
    try {
      dynamoTemplate.describeTable(DescribeTableRequest.builder()
        .tableName(name)
        .build());
    } catch (ResourceNotFoundException e) {
      created = TableUtils.createTableIfNotExists(dynamoTemplate, createTableRequest(name, readCapacityUnits, writeCapacityUnits));
      if (created && !ttl.isZero()) {
        dynamoTemplate.updateTimeToLive(updateTimeToLiveRequest(name));
      }
    }
    return created;
  }

  private byte[] getInternal(String name, String key) {
    final GetItemRequest request = GetItemRequest.builder()
      .attributesToGet(ATTRIBUTE_VALUE, ATTRIBUTE_TTL)
      .tableName(name)
      .key(Collections.singletonMap(ATTRIBUTE_KEY, AttributeValue.fromS(key)))
      .build();

    final GetItemResponse result = dynamoTemplate.getItem(request);
    if (result.hasItem() && !isPastTtl(result)) {
      return getAttributeValue(result);
    } else {
      throw new NoSuchElementException(String.format("No entry found for '%s'.", key));
    }
  }

  private byte[] getAttributeValue(GetItemResponse result) {
    final AttributeValue attribute = result.item().get(ATTRIBUTE_VALUE);
    if (attribute == null) {
      throw new IllegalStateException(String.format("Attribute value does not match the expected '%s'.", ATTRIBUTE_VALUE));
    }

    SdkBytes element = attribute.b();
    if (element == null && attribute.nul()) {
      // TODO to return null is bad style, but how to distinct between null value from getInternal and no entry at all?
      return null;
    } else {
      return Objects.requireNonNull(element).asByteArray();
    }
  }

  private boolean isPastTtl(GetItemResponse result) {
    final AttributeValue attributeTtl = result.item().get(ATTRIBUTE_TTL);
    if (attributeTtl != null && attributeTtl.n() != null) {
      Instant ttlInstant = Instant.ofEpochSecond(Long.parseLong(attributeTtl.n()));
      return Instant.now().isAfter(ttlInstant);
    }
    return false;
  }

  private void putInternal(String name, String key, @Nullable byte[] value, @Nullable Duration ttl, @Nullable List<RootAttribute> rootAttributes) {
    Map<String, AttributeValue> attributeValues = new HashMap<>();
    attributeValues.put(ATTRIBUTE_KEY, AttributeValue.fromS(key));

    if (value == null) {
      attributeValues.put(ATTRIBUTE_VALUE, AttributeValue.fromNul(true));
    } else {
      attributeValues.put(ATTRIBUTE_VALUE, AttributeValue.fromB(SdkBytes.fromByteArray(value)));
    }

    if (shouldExpireWithin(ttl)) {
      attributeValues.put(ATTRIBUTE_TTL, AttributeValue.fromN(String.valueOf(Instant.now().plus(ttl).getEpochSecond())));
    }

    if (rootAttributes != null) {
      rootAttributes.forEach(rootAttribute -> attributeValues.put(rootAttribute.getName(), rootAttribute.getAttributeValue()));
    }

    PutItemRequest putItemRequest = PutItemRequest.builder()
      .tableName(name)
      .item(attributeValues)
      .build();
    dynamoTemplate.putItem(putItemRequest);
  }

  private void removeInternal(String name, String key) {
    dynamoTemplate.deleteItem(DeleteItemRequest.builder()
      .tableName(name)
      .key(Collections.singletonMap(ATTRIBUTE_KEY, AttributeValue.fromS(key)))
      .build());
  }

  private void doLock(String name) {
    // TODO should a ttl be provided for locking?
    putInternal(name, createCacheLockKey(name), "1".getBytes(), null, null);
  }

  private void doUnlock(String name) {
    try {
      removeInternal(name, createCacheLockKey(name));
    } catch (ResourceNotFoundException e) {
      // ignore
    }
  }

  private boolean doCheckLock(String name) {
    try {
      getInternal(name, createCacheLockKey(name));
    } catch (NoSuchElementException | ResourceNotFoundException e) {
      return false;
    }
    return true;
  }

  /**
   * @return {@literal true} if {@link DynamoCacheWriter} uses locks.
   */
  private boolean isLockingCacheWriter() {
    return !sleepTime.isZero() && !sleepTime.isNegative();
  }

  private <T> T execute(String name, Function<DynamoDbClient, T> callback) {
    checkAndPotentiallyWaitUntilUnlocked(name);
    return callback.apply(dynamoTemplate);
  }

  private void checkAndPotentiallyWaitUntilUnlocked(String name) {
    if (!isLockingCacheWriter()) {
      return;
    }

    try {
      while (doCheckLock(name)) {
        Thread.sleep(sleepTime.toMillis());
      }
    } catch (InterruptedException ex) {
      // Re-interrupt current thread, to allow other participants to react.
      Thread.currentThread().interrupt();

      throw new PessimisticLockingFailureException(String.format("Interrupted while waiting to unlock cache %s", name),
        ex);
    }
  }

  private static boolean shouldExpireWithin(@Nullable Duration ttl) {
    return ttl != null && !ttl.isZero() && !ttl.isNegative();
  }

  private static String createCacheLockKey(String name) {
    return (name + "~lock");
  }

  private CreateTableRequest createTableRequest(String name, Long readCapacityUnits, Long writeCapacityUnits) {
    return CreateTableRequest.builder()
      .tableName(name)
      .attributeDefinitions(AttributeDefinition.builder()
        .attributeName(ATTRIBUTE_KEY)
        .attributeType(S)
        .build())
      .keySchema(KeySchemaElement.builder()
        .attributeName(ATTRIBUTE_KEY)
        .keyType(KeyType.HASH)
        .build())
      .provisionedThroughput(ProvisionedThroughput.builder()
        .readCapacityUnits(readCapacityUnits)
        .writeCapacityUnits(writeCapacityUnits)
        .build())
      .build();
  }

  // TODO to be tested (not implemented in AmazonDynamoDB local)
  private UpdateTimeToLiveRequest updateTimeToLiveRequest(String name) {
    return UpdateTimeToLiveRequest.builder()
      .tableName(name)
      .timeToLiveSpecification(TimeToLiveSpecification.builder()
        .enabled(true)
        .attributeName(ATTRIBUTE_TTL)
        .build())
      .build();
  }
}
