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

import com.dasburo.spring.cache.dynamo.rootattribute.RootAttribute;
import com.dasburo.spring.cache.dynamo.rootattribute.RootAttributeConfig;
import com.dasburo.spring.cache.dynamo.rootattribute.RootAttributeReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Spring {@link Cache} adapter implementation
 * on top of Amazons DynamoDB.
 *
 * @author Georg Zimmermann
 */
public class DynamoCache implements Cache {

  private static final Logger LOGGER = LoggerFactory.getLogger(DynamoCache.class);

  private final String cacheName;
  private final DynamoCacheWriter writer;
  private final DynamoCacheConfiguration cacheConfig;

  private RootAttributeReader rootAttributeReader = new RootAttributeReader();

  /**
   * Constructor.
   *
   * @param cacheName a cache name.
   * @param writer    a {@link DynamoCacheWriter} instance.
   */
  public DynamoCache(String cacheName, DynamoCacheWriter writer) {
    this(cacheName, writer, DynamoCacheConfiguration.defaultCacheConfig());
  }

  /**
   * Constructor.
   *
   * @param cacheName   a cache name.
   * @param writer      a {@link DynamoCacheWriter} instance.
   * @param cacheConfig a {@link DynamoCacheConfiguration} instance. Must not be {@literal null}.
   */
  public DynamoCache(String cacheName, DynamoCacheWriter writer, DynamoCacheConfiguration cacheConfig) {
    Assert.hasText(cacheName, "'cacheName' must be not null and not empty.");
    Assert.notNull(writer, "'writer' must not be null.");

    this.cacheName = cacheName;
    this.writer = writer;
    this.cacheConfig = cacheConfig;

    initialize();
  }

  @Override
  public void clear() {
    writer.clear(cacheName);
  }

  @Override
  public void evict(Object key) {
    Assert.isTrue(key instanceof String, "'key' must be an instance of 'java.lang.String'.");

    writer.remove(cacheName, (String) key);
  }

  @Override
  public ValueWrapper get(Object key) {
    try {
      Object value = getFromCache(key);
      return new SimpleValueWrapper(value);
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @Override
  public <T> T get(Object key, Class<T> type) {
    try {
      final Object value = getFromCache(key);
      return type.cast(value);
    } catch (ClassCastException e) {
      throw new IllegalStateException("Unable to cast the object.", e);
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @Override
  public <T> T get(Object key, Callable<T> valueLoader) {
    Assert.isTrue(key instanceof String, "'key' must be an instance of 'java.lang.String'.");
    Assert.notNull(valueLoader, "'valueLoader' must not be null.");

    ValueWrapper cached = get(key);

    if (cached != null) {
      return (T) cached.get();
    }

    try {
      T value = valueLoader.call();
      put(key, value);
      return value;
    } catch (Exception e) {
      throw new ValueRetrievalException(key, valueLoader, e);
    }
  }

  /**
   * Gets whether the cache should delete all elements on boot.
   *
   * @return returns whether the cache should delete all elements on boot.
   */
  public final boolean isFlushOnBoot() {
    return cacheConfig.isFlushOnBoot();
  }

  @Override
  public String getName() {
    return cacheName;
  }

  @Override
  public Object getNativeCache() {
    return writer.getNativeCacheWriter();
  }

  /**
   * Returns the time to live value for this cache.
   *
   * @return the ttl value.
   */
  public final Duration getTtl() {
    return cacheConfig.getTtl();
  }

  /**
   * Returns the configuration of additional root attributes for this cache
   *
   * @return the rootAttributeConfigs value.
   */
  public final List<RootAttributeConfig> getRootAttributes() {
    return cacheConfig.getRootAttributes();
  }

  @Override
  public void put(Object key, Object value) {
    Assert.isTrue(key instanceof String, "'key' must be an instance of 'java.lang.String'.");
    writer.put(cacheName, (String) key, serialize(value), cacheConfig.getTtl(), readRootAttributes(cacheConfig.getRootAttributes(), value));
  }

  @Override
  public ValueWrapper putIfAbsent(Object key, Object value) {
    Assert.isTrue(key instanceof String, "'key' must be an instance of 'java.lang.String'.");

    byte[] result = writer.putIfAbsent(cacheName, (String) key, serialize(value), cacheConfig.getTtl(), readRootAttributes(cacheConfig.getRootAttributes(), value));
    if (result != null) {
      LOGGER.debug(String.format("Key: %s already exists in the cache. Element will not be replaced.", key));
      return new SimpleValueWrapper(deserialize(result));
    }

    return null;
  }

  private Object getFromCache(Object key) {
    Assert.isTrue(key instanceof String, "'key' must be an instance of 'java.lang.String'.");

    byte[] element = writer.get(cacheName, (String) key);
    return deserialize(element);
  }

  private void initialize() {
    if (cacheConfig.isFlushOnBoot()) {
      clear();
    }

    writer.createIfNotExists(cacheName, cacheConfig.getTtl(), cacheConfig.getReadCapacityUnits(), cacheConfig.getWriteCapacityUnits());
  }

  private Object deserialize(byte[] value) {
    return cacheConfig.getSerializer().deserialize(value);
  }

  private byte[] serialize(Object value) {
    return cacheConfig.getSerializer().serialize(value);
  }

  private List<RootAttribute> readRootAttributes(List<RootAttributeConfig> rootAttributeConfigs, Object value) {
    return rootAttributeConfigs.stream()
        .map(rootAttributeConfig -> rootAttributeReader.readRootAttribute(rootAttributeConfig, value))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
