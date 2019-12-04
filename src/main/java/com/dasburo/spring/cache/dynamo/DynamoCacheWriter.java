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
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.time.Duration;

/**
 * {@link DynamoCacheWriter} provides low level access to DynamoDB commands ({@code PUT, GET, ...}) used for
 * caching.
 * <p>
 * The {@link DynamoCacheWriter} is responsible for writing / reading binary data to / from DynamoDB.
 *
 * @author Georg Zimmermann
 */
public interface DynamoCacheWriter {

  /**
   * Create new {@link DynamoCacheWriter} without locking behavior.
   *
   * @return new instance of {@link DefaultDynamoCacheWriter}.
   */
  static DynamoCacheWriter nonLockingDynamoCacheWriter(AmazonDynamoDB dynamoTemplate) {

    Assert.notNull(dynamoTemplate, "AmazonDynamoDB must not be null!");

    return new DefaultDynamoCacheWriter(dynamoTemplate);
  }

  /**
   * Create new {@link DynamoCacheWriter} with locking behavior.
   *
   * @param dynamoTemplate must not be {@literal null}.
   * @return new instance of {@link DefaultDynamoCacheWriter}.
   */
  static DynamoCacheWriter lockingDynamoCacheWriter(AmazonDynamoDB dynamoTemplate) {

    Assert.notNull(dynamoTemplate, "AmazonDynamoDB must not be null!");

    return new DefaultDynamoCacheWriter(dynamoTemplate, Duration.ofMillis(50));
  }

  /**
   * Returns the native connection library for the cache.
   *
   * @return {@link AmazonDynamoDB}
   */
  AmazonDynamoDB getNativeCacheWriter();

  /**
   * Create a cache table for the given name.
   *
   * @param name               The cache name must not be {@literal null}.
   * @param ttl                Optional expiration time. Must not be {@literal null}. Use {@code Duration.ZERO} to declare an eternal cache.
   * @param readCapacityUnits  Amount of strongly consistent reads per second must not be {@literal null}.
   * @param writeCapacityUnits Amount of strongly consistent writes per second must not be {@literal null}.
   * @return {@literal true} if table had to be created.
   */
  boolean createIfNotExists(String name, Duration ttl, Long readCapacityUnits, Long writeCapacityUnits);

  /**
   * Write the given key/value pair to Dynamo an set the expiration time if defined.
   * <br><b>Note:</b> The values size must be less than 400 KB.
   * As this is the maximum element size of a binary in Amazons DynamoDB.
   *
   * @param name  The cache name must not be {@literal null}.
   * @param key   The key for the cache entry. Must not be {@literal null}.
   * @param value The value stored for the key. Must not be {@literal null}.
   * @param ttl   Optional expiration time. Can be {@literal null}.
   */
  void put(String name, String key, byte[] value, @Nullable Duration ttl);

  /**
   * Get the binary value representation from Dynamo stored for the given key.
   *
   * @param name must not be {@literal null}.
   * @param key  must not be {@literal null}.
   * @return {@literal null} if key does not exist.
   */
  @Nullable
  byte[] get(String name, String key);

  /**
   * Write the given value to Dynamo if the key does not already exist.
   * <br><b>Note:</b> The values size must be less than 400 KB.
   * As this is the maximum element size of a binary in Amazons DynamoDB.
   *
   * @param name  The cache name must not be {@literal null}.
   * @param key   The key for the cache entry. Must not be {@literal null}.
   * @param value The value stored for the key. Must not be {@literal null}.
   * @param ttl   Optional expiration time. Can be {@literal null}.
   * @return {@literal null} if the value has been written, the value stored for the key if it already exists.
   */
  @Nullable
  byte[] putIfAbsent(String name, String key, byte[] value, @Nullable Duration ttl);

  /**
   * Remove the given key from Dynamo.
   *
   * @param name The cache name must not be {@literal null}.
   * @param key  The key for the cache entry. Must not be {@literal null}.
   */
  void remove(String name, String key);

  /**
   * Remove all keys from the given cache name.
   * <br><b>Note:</b> Clear is actually a table scann followed by per Item deletion.
   * This could lead to performance issues on very large data sets.
   *
   * @param name The cache name must not be {@literal null}.
   */
  void clear(String name);
}