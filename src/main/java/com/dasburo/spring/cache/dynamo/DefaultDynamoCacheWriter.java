/*
 * Copyright 2019 the original author or authors.
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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.dasburo.spring.cache.dynamo.util.ByteUtils;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

/**
 * {@link DynamoCacheWriter} implementation capable of reading/writing binary data from/to DynamoDB in {@literal standalone}
 * and {@literal cluster} environments. Works upon a given {@link AmazonDynamoDB} holds the actual connection.
 * <p>
 * {@link DefaultDynamoCacheWriter} can be used in
 * {@link DynamoCacheWriter#lockingDynamoCacheWriter(AmazonDynamoDB) locking} or
 * {@link DynamoCacheWriter#nonLockingDynamoCacheWriter(AmazonDynamoDB) non-locking} mode. While
 * {@literal non-locking} aims for maximum performance it may result in overlapping, non atomic, command execution for
 * operations spanning multiple DynamoDB interactions like {@code putIfAbsent}. The {@literal locking} counterpart prevents
 * command overlap by setting an explicit lock key and checking against presence of this key which leads to additional
 * requests and potential command wait times.
 *
 * @author Georg Zimmermann
 */
class DefaultDynamoCacheWriter implements DynamoCacheWriter {

  private static final String ATTRIBUTE_KEY = "key";
  private static final String ATTRIBUTE_VALUE = "value";
  private static final String ATTRIBUTE_TTL = "ttl";

  private final AmazonDynamoDB dynamoTemplate;
  private final Duration sleepTime;

  /**
   * @param dynamoTemplate must not be {@literal null}.
   */
  DefaultDynamoCacheWriter(AmazonDynamoDB dynamoTemplate) {
    this(dynamoTemplate, Duration.ZERO);
  }

  /**
   * @param dynamoTemplate must not be {@literal null}.
   * @param sleepTime      sleep time between lock request attempts. Must not be {@literal null}. Use {@link Duration#ZERO}
   *                       to disable locking.
   */
  DefaultDynamoCacheWriter(AmazonDynamoDB dynamoTemplate, Duration sleepTime) {
    Assert.notNull(dynamoTemplate, "ConnectionFactory must not be null!");
    Assert.notNull(sleepTime, "SleepTime must not be null!");

    this.dynamoTemplate = dynamoTemplate;
    this.sleepTime = sleepTime;
  }

  @Override
  public AmazonDynamoDB getNativeCacheWriter() {
    return dynamoTemplate;
  }

  @Override
  public void put(String name, String key, byte[] value, @Nullable Duration ttl) {
    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(key, "Key must not be null!");

    execute(name, connection -> {
      putInternal(name, key, value, ttl);

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
  public byte[] putIfAbsent(String name, String key, @Nullable byte[] value, @Nullable Duration ttl) {
    Assert.notNull(name, "Name must not be null!");
    Assert.notNull(key, "Key must not be null!");

    return execute(name, connection -> {

      if (isLockingCacheWriter()) {
        doLock(name);
      }

      try {
        return getInternal(name, key);
      } catch (NoSuchElementException e) {
        putInternal(name, key, value, ttl);
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

        ScanRequest req = new ScanRequest(name);
        List<Map<String, AttributeValue>> items = dynamoTemplate.scan(req).getItems();

        items.parallelStream()
          .forEach(map -> {
            Map<String, AttributeValue> keyToDelete = new HashMap<>();
            keyToDelete.put(ATTRIBUTE_KEY, map.get(ATTRIBUTE_KEY));
            DeleteItemRequest delReq = new DeleteItemRequest(name, keyToDelete);
            dynamoTemplate.deleteItem(delReq);
          });
      } catch(ResourceNotFoundException ignored) {
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

    boolean created = TableUtils.createTableIfNotExists(dynamoTemplate, createTableRequest(name, readCapacityUnits, writeCapacityUnits));
    if (created && !ttl.isZero()) {
      dynamoTemplate.updateTimeToLive(updateTimeToLiveRequest(name));
    }
    return created;
  }

  private byte[] getInternal(String name, String key) {
    final GetItemRequest request = new GetItemRequest()
      .withAttributesToGet(ATTRIBUTE_VALUE)
      .withTableName(name)
      .withKey(Collections.singletonMap(ATTRIBUTE_KEY, new AttributeValue(key)));

    final GetItemResult result = dynamoTemplate.getItem(request);
    if (result.getItem() != null) {
      return getAttributeValue(result);
    } else {
      throw new NoSuchElementException(String.format("No entry found for '%s'.", key));
    }
  }

  private byte[] getAttributeValue(GetItemResult result) {
    final AttributeValue attribute = result.getItem().get(ATTRIBUTE_VALUE);
    if (attribute == null) {
      throw new IllegalStateException(String.format("Attribute value does not match the expected '%s'.", ATTRIBUTE_VALUE));
    }

    ByteBuffer element = attribute.getB();
    if (element == null && attribute.getNULL()) {
      // TODO to return null is bad style, but how to distinct between null value from getInternal and no entry at all?
      return null;
    } else {
      return ByteUtils.getBytes(element);
    }
  }

  private void putInternal(String name, String key, @Nullable byte[] value, @Nullable Duration ttl) {
    Map<String, AttributeValue> attributeValues = new HashMap<>();
    attributeValues.put(ATTRIBUTE_KEY, new AttributeValue().withS(key));

    if (value == null) {
      attributeValues.put(ATTRIBUTE_VALUE, new AttributeValue().withNULL(true));
    } else {
      attributeValues.put(ATTRIBUTE_VALUE, new AttributeValue().withB(ByteBuffer.wrap(value)));
    }

    if (shouldExpireWithin(ttl)) {
      attributeValues.put(ATTRIBUTE_TTL, new AttributeValue().withS(String.valueOf(Instant.now().toEpochMilli() + ttl.toMillis())));
    }

    PutItemRequest putItemRequest = new PutItemRequest()
      .withTableName(name)
      .withItem(attributeValues);
    dynamoTemplate.putItem(putItemRequest);
  }

  private void removeInternal(String name, String key) {
    dynamoTemplate.deleteItem(new DeleteItemRequest()
      .withTableName(name)
      .withKey(Collections.singletonMap(ATTRIBUTE_KEY, new AttributeValue(key))));
  }

  private void doLock(String name) {
    // TODO should a ttl be provided for locking?
    putInternal(name, createCacheLockKey(name), "1".getBytes(), null);
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

  private <T> T execute(String name, Function<AmazonDynamoDB, T> callback) {
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
    List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
    attributeDefinitions.add(new AttributeDefinition().withAttributeName(ATTRIBUTE_KEY).withAttributeType(ScalarAttributeType.S));

    List<KeySchemaElement> keySchema = new ArrayList<>();
    keySchema.add(new KeySchemaElement().withAttributeName(ATTRIBUTE_KEY).withKeyType(KeyType.HASH));

    return new CreateTableRequest()
      .withTableName(name)
      .withKeySchema(keySchema)
      .withAttributeDefinitions(attributeDefinitions)
      .withProvisionedThroughput(new ProvisionedThroughput(readCapacityUnits, writeCapacityUnits));
  }

  // TODO to be tested (not implemented in AmazonDynamoDB local)
  private UpdateTimeToLiveRequest updateTimeToLiveRequest(String name) {
    return new UpdateTimeToLiveRequest()
      .withTableName(name)
      .withTimeToLiveSpecification(new TimeToLiveSpecification()
        .withEnabled(true)
        .withAttributeName(ATTRIBUTE_TTL));
  }
}
