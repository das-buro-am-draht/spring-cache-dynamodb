/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dasburo.spring.cache.dynamo;

import com.dasburo.spring.cache.dynamo.helper.Address;
import com.dasburo.spring.cache.dynamo.rootattribute.RootAttributeConfig;
import com.dasburo.spring.cache.dynamo.serializer.DynamoSerializer;
import com.dasburo.spring.cache.dynamo.serializer.Jackson2JsonSerializer;
import com.dasburo.spring.cache.dynamo.serializer.StringSerializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.time.Duration;
import java.util.HashMap;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType.S;

/**
 * Unit tests for {@link DynamoCache}.
 *
 * @author Georg Zimmermann
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class DynamoCacheTest {

  private static final String CACHE_NAME = "cache";
  private static final Duration TTL = Duration.ofSeconds(10);

  @ClassRule
  public static TestDbCreationRule dynamoDB = new TestDbCreationRule();

  @Autowired
  private DynamoCacheWriter writer;

  @Autowired
  private DynamoDbClient ddbClient;

  private DynamoCache cache;

  private DynamoSerializer serializer;

  @Before
  public void setup() {
    writer = spy(writer);
    serializer = new StringSerializer();

    DynamoCacheConfiguration config = DynamoCacheConfiguration.defaultCacheConfig();
    config.setTtl(TTL);
    config.setFlushOnBoot(true);
    config.setSerializer(serializer);

    cache = new DynamoCache(CACHE_NAME, writer, config);
    reset(writer);
  }

  /**
   * Test for {@link DynamoCache#DynamoCache(String, DynamoCacheWriter)}.
   */
  @Test
  public void constructor() {
    final String cacheName = cache.getName();
    Assert.assertEquals(CACHE_NAME, cacheName);

    final Object nativeCache = cache.getNativeCache();
    Assert.assertEquals(nativeCache, writer.getNativeCacheWriter());

    final DynamoCache dynamoCache = new DynamoCache(CACHE_NAME, writer);
    Assert.assertNotEquals(0, dynamoCache.getTtl());
  }

  /**
   * Test for {@link DynamoCache#clear()}.
   */
  @Test
  public void clear() {
    cache.clear();
    verify(writer, times(1)).clear(cache.getName());
  }

  /**
   * Test for {@link DynamoCache#getName()}.
   */
  @Test
  public void getName() {
    final String name = cache.getName();
    Assert.assertEquals(CACHE_NAME, name);
  }

  /**
   * Test for {@link DynamoCache#getNativeCache()}.
   */
  @Test
  public void getNativeCache() {
    final Object nativeCache = cache.getNativeCache();
    Assert.assertEquals(nativeCache, writer.getNativeCacheWriter());
  }

  /**
   * Test for {@link DynamoCache#getTtl()}.
   */
  @Test
  public void getTtl() {
    final Duration ttl = cache.getTtl();
    Assert.assertEquals(TTL, ttl);
  }

  /**
   * Test for {@link DynamoCache#getWriter()}.
   */
  @Test
  public void getCacheWriter() {
    final Object cacheWriter = cache.getWriter();
    Assert.assertEquals(writer, cacheWriter);
  }

  /**
   * Test for {@link DynamoCache#getSerializer()}.
   */
  @Test
  public void getSerializer() {
    final DynamoSerializer serializer = cache.getSerializer();
    Assert.assertEquals(this.serializer, serializer);
  }

  /**
   * Test for {@link DynamoCache#get(Object)}.
   */
  @Test
  public void get() {
    final String key = "key";
    final String value = "value";

    cache.put(key, value);
    Cache.ValueWrapper wrapper = cache.get(key);

    Assert.assertNotNull(wrapper);
    Assert.assertNotNull(value, wrapper.get());
  }

  /**
   * Test for {@link DynamoCache#get(Object)}.
   */
  @Test
  public void getNullValue() {
    final String key = "key";
    final String value = null;

    cache.put(key, value);

    final Cache.ValueWrapper wrapper = cache.get(key);
    Assert.assertNotNull(wrapper);
    Assert.assertNull(value, wrapper.get());
  }

  /**
   * Test for {@link DynamoCache#get{T}(Object)}.
   */
  @Test(expected = IllegalStateException.class)
  public void getWithCast() {
    final String key = "key";
    final String value = "value";

    cache.put(key, value);
    cache.get(key, Double.class);
  }

  /**
   * Test for {@link DynamoCache#get{T}(Object)}.
   */
  @Test
  public void getNullValueWithCast() {
    final String key = "key";
    final String value = null;

    cache.put(key, value);

    String valueInCache = cache.get(key, String.class);
    Assert.assertEquals(value, valueInCache);

    valueInCache = cache.get("key1", String.class);
    Assert.assertNull(valueInCache);

    cache.get(key, Double.class);
  }

  /**
   * Test for {@link DynamoCache#evict(Object)}.
   */
  @Test
  public void evict() {
    final String key = "evictKey";

    cache.put(key, "value");
    cache.evict(key);

    final Cache.ValueWrapper wrapper = cache.get(key);
    Assert.assertNull(wrapper);
  }

  /**
   * Test for {@link DynamoCache#put(Object, Object)}.
   */
  @Test
  public void putShouldAddEntry() {
    final String key = "key";
    final String value = "value";

    cache.put(key, value);
    Assert.assertNotNull(cache.get(key));
    Assert.assertEquals(value, cache.get(key).get());
  }

  /**
   * Test for {@link DynamoCache#put(Object, Object)} with {@literal null} value.
   */
  @Test
  public void putNullShouldAddNullValueEntry() {
    final String key = "key";
    final String value = null;

    cache.put(key, value);
    Assert.assertNotNull(cache.get(key));
    Assert.assertEquals(value, cache.get(key).get());
  }

  /**
   * Test for {@link DynamoCache#putIfAbsent(Object, Object)}.
   */
  @Test
  public void putIfAbsentShouldAddEntryIfNotExists() {
    final String key = "key";
    final String value = "value";

    Cache.ValueWrapper wrapper = cache.putIfAbsent(key, value);

    Assert.assertNull(wrapper);
    Assert.assertNotNull(cache.get(key));
    Assert.assertEquals(value, cache.get(key).get());
  }

  /**
   * Test for {@link DynamoCache#putIfAbsent(Object, Object)}.
   */
  @Test
  public void putIfAbsentWithNullShouldAddNullValueEntryIfNotExists() {
    final String key = "key";
    final String value = null;

    Cache.ValueWrapper wrapper = cache.putIfAbsent(key, value);

    Assert.assertNull(wrapper);
    Assert.assertNotNull(cache.get(key));
    Assert.assertEquals(value, cache.get(key).get());
  }

  /**
   * Test for {@link DynamoCache#putIfAbsent(Object, Object)}.
   */
  @Test
  public void putIfAbsentShouldReturnExistingEntryIfNotExists() {
    final String key = "key";
    final String value = "value";

    cache.put(key, value);

    Cache.ValueWrapper wrapper = cache.putIfAbsent(key, value);

    Assert.assertNotNull(wrapper);
    Assert.assertEquals(value, wrapper.get());
  }

  @Test
  public void getWithCallableShouldResolveValueIfNotPresent() {
    final String key = "key";

    Address address = new Address("someStreet", 1);
    DynamoCacheConfiguration config = DynamoCacheConfiguration.defaultCacheConfig();
    config.setSerializer(new Jackson2JsonSerializer<>(Address.class));

    Cache addressCache = new DynamoCache(CACHE_NAME, writer, config);

    Assert.assertEquals(address, addressCache.get(key, () -> address));
  }

  @Test
  public void getWithCallableShouldNotResolveValueIfPresent() {
    final String key = "key";

    Address address = new Address("someStreet", 1);
    DynamoCacheConfiguration config = DynamoCacheConfiguration.defaultCacheConfig();
    config.setSerializer(new Jackson2JsonSerializer<>(Address.class));

    Cache addressCache = new DynamoCache(CACHE_NAME, writer, config);

    addressCache.put(key, address);

    addressCache.get(key, () -> {
      throw new IllegalStateException("Why call the value loader when we've got a cache entry?");
    });

    Assert.assertNotNull(addressCache.get(key));
    Assert.assertEquals(addressCache.get(key).get(), address);
  }

  @Test
  public void putWithRootAttributeConfigDoesNotInfluenceTheCoreFunctionality() {
    //given
    final String itemKey = "key";
    final RootAttributeConfig streetRootAttribute = new RootAttributeConfig("street", S);
    Address address = new Address("someStreet", 1);

    DynamoCacheConfiguration config = DynamoCacheConfiguration.defaultCacheConfig();
    config.setSerializer(new Jackson2JsonSerializer<>(Address.class));
    config.setRootAttributes(singletonList(streetRootAttribute));

    Cache addressCache = new DynamoCache(CACHE_NAME, writer, config);

    //when
    addressCache.put(itemKey, address);

    //then
    Assert.assertNotNull(addressCache.get(itemKey));
    Assert.assertEquals(addressCache.get(itemKey).get(), address);
  }

  @Test
  public void putWithRootAttributePersistsAdditionalRootAttributeOnDynamoDbTable() {
    //given
    final String itemKey = "key";
    final RootAttributeConfig streetRootAttribute = new RootAttributeConfig("street", S);
    Address address = new Address("someStreet", 1);

    DynamoCacheConfiguration config = DynamoCacheConfiguration.defaultCacheConfig();
    config.setSerializer(new Jackson2JsonSerializer<>(Address.class));
    config.setRootAttributes(singletonList(streetRootAttribute));

    Cache addressCache = new DynamoCache(CACHE_NAME, writer, config);

    //when
    addressCache.put(itemKey, address);

    //then
    HashMap<String, AttributeValue> ddbKey = new HashMap<>();
    ddbKey.put(DefaultDynamoCacheWriter.ATTRIBUTE_KEY, AttributeValue.fromS(itemKey));

    GetItemResponse ddbItem = ddbClient.getItem(GetItemRequest.builder()
      .tableName(CACHE_NAME)
      .key(ddbKey)
      .build());
    AttributeValue storedRootAttribute = ddbItem.item().get(streetRootAttribute.getName());

    Assert.assertEquals(storedRootAttribute.s(), address.getStreet());
  }
}
