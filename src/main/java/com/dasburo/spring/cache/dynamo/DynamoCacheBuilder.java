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
import com.dasburo.spring.cache.dynamo.serializer.DynamoSerializer;
import org.springframework.util.Assert;

import java.time.Duration;

/**
 * A builder for {@link DynamoCache} instance.
 *
 * @author Georg Zimmermann
 */
public class DynamoCacheBuilder {

  private String cacheName;
  private DynamoCacheWriter writer;

  private DynamoCacheConfiguration cacheConfig;

  /**
   * Constructor.
   *
   * @param cacheName      a name of the cache.
   * @param dynamoTemplate a {@link AmazonDynamoDB} instance.
   */
  protected DynamoCacheBuilder(final String cacheName, final AmazonDynamoDB dynamoTemplate) {
    Assert.notNull(dynamoTemplate, "'dynamoTemplate' must not be null.");
    Assert.hasText(cacheName, "'cacheName' must not be null and must contain at least one non-whitespace character.");

    this.cacheName = cacheName;
    this.writer = DynamoCacheWriter.nonLockingDynamoCacheWriter(dynamoTemplate);
    this.cacheConfig = DynamoCacheConfiguration.defaultCacheConfig();
  }

  /**
   * Create a new builder instance with the given cache name.
   *
   * @param cacheName      a name of the cache.
   * @param dynamoTemplate a {@link AmazonDynamoDB} instance.
   * @return a new builder
   */
  public static DynamoCacheBuilder newInstance(String cacheName, AmazonDynamoDB dynamoTemplate) {
    return new DynamoCacheBuilder(cacheName, dynamoTemplate);
  }

  /**
   * Build a new {@link DynamoCache} with the specified name.
   *
   * @return a {@link DynamoCache} instance.
   */
  public DynamoCache build() {
    return new DynamoCache(cacheName, writer, cacheConfig);
  }

  /**
   * Give a value that indicates if the collection must be always flush.
   *
   * @param flushOnBoot a value that indicates if the collection must be always flush.
   * @return this builder for chaining.
   */
  public DynamoCacheBuilder withFlushOnBoot(boolean flushOnBoot) {
    cacheConfig.setFlushOnBoot(flushOnBoot);
    return this;
  }

  /**
   * Give a TTL to the cache to be built.
   *
   * @param ttl a time-to-live (in seconds).
   * @return this builder for chaining.
   */
  public DynamoCacheBuilder withTTL(Duration ttl) {
    cacheConfig.setTtl(ttl);
    return this;
  }

  /**
   * Give a value that indicates the reads per second to be built.
   *
   * @param readCapacityUnit amount of strongly consistent reads per second.
   * @return this builder for chaining.
   */
  public DynamoCacheBuilder withReadCapacityUnit(Long readCapacityUnit) {
    cacheConfig.setReadCapacityUnits(readCapacityUnit);
    return this;
  }

  /**
   * Give a value that indicates the writes per second to be built.
   *
   * @param writeCapacityUnit amount of strongly consistent reads per second.
   * @return this builder for chaining.
   */
  public DynamoCacheBuilder withWriteCapacityUnit(Long writeCapacityUnit) {
    cacheConfig.setWriteCapacityUnits(writeCapacityUnit);
    return this;
  }

  /**
   * Give a {@link DynamoSerializer} to the cache to be built.
   * Defaults to {@link com.dasburo.spring.cache.dynamo.serializer.StringSerializer}.
   *
   * @param serializer a serializer to serialize/deserialize the data
   * @return this builder for chaining.
   */
  public DynamoCacheBuilder withSerializer(DynamoSerializer serializer) {
    cacheConfig.setSerializer(serializer);
    return this;
  }

  /**
   * Give a {@link DynamoCacheWriter} to the cache to be built.
   * Defaults to {@link DefaultDynamoCacheWriter}.
   *
   * @param writer a writer doing the actual cache operations.
   * @return this buzilder for chaining.
   */
  public DynamoCacheBuilder withWriter(DynamoCacheWriter writer) {
    this.writer = writer;
    return this;
  }

}