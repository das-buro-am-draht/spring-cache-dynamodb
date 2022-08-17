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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DynamoCacheManager}.
 *
 * @author Georg Zimmermann
 */
@ExtendWith({SpringExtension.class, TestDbCreationExtension.class})
@ContextConfiguration(classes = TestConfiguration.class)
public class DynamoCacheManagerTest {

  private static final String CACHE_NAME = "cache";

  @Autowired
  private DynamoDbClient dynamoTemplate;

  private DynamoCacheManager manager;

  @BeforeEach
  public void setup() {
    DynamoCacheBuilder defaultCacheBuilder = DynamoCacheBuilder.newInstance(CACHE_NAME, dynamoTemplate);
    this.manager = new DynamoCacheManager(Collections.singletonList(defaultCacheBuilder));
    manager.afterPropertiesSet();
  }

  /**
   * Test for {@link DynamoCacheManager#loadCaches()}
   */
  @Test
  public void loadCaches() {
    final Collection<DynamoCacheBuilder> initialCaches = new ArrayList<>();
    final DynamoCacheBuilder cache = DynamoCacheBuilder.newInstance("cache", dynamoTemplate);
    initialCaches.add(cache);

    final DynamoCacheManager manager = new DynamoCacheManager(initialCaches);
    final Collection<? extends Cache> caches = manager.loadCaches();
    assertNotNull(caches);
    assertEquals(1, caches.size());
  }

  /**
   * Test for {@link DynamoCacheManager#getCache(String)}
   */
  @Test
  public void cache() {
    final Cache cache = manager.getCache(CACHE_NAME);
    assertNotNull(cache);
    assertEquals(CACHE_NAME, cache.getName());
    assertThat(cache, instanceOf(DynamoCache.class));

    final DynamoCache dynamoCache = (DynamoCache) cache;
    assertEquals(dynamoCache.getNativeCache(), dynamoTemplate);
  }

  /**
   * Test for {@link DynamoCacheManager#getCache(String)}
   */
  @Test
  public void getCache() {
    final Cache cache = manager.getCache(CACHE_NAME);
    assertNotNull(cache);

    final Cache invalidCache = manager.getCache("invalid");
    assertNull(invalidCache);
  }

  /**
   * Test for {@link DynamoCacheManager#getCacheNames()}
   */
  @Test
  public void getCacheNames() {
    assertNotNull(manager.getCacheNames());
    assertEquals(1, manager.getCacheNames().size());
    assertThat(manager.getCacheNames(), hasItem(CACHE_NAME));
  }

}
