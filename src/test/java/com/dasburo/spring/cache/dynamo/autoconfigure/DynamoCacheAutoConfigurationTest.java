/*
 * Copyright 2019 the original author or authors.
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
package com.dasburo.spring.cache.dynamo.autoconfigure;

import com.dasburo.spring.cache.dynamo.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;

/**
 * Unit tests for {@code DynamoCacheAutoConfiguration} class.
 *
 * @author BaD Georg Zimmermann
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class DynamoCacheAutoConfigurationTest extends UnitTestBase {

  private static final String CACHE_NAME = "cache";
  private static final boolean FLUSH_ON_BOOT = false;
  private static final Duration TTL = Duration.ofSeconds(30);
  private static final Long READ_CAPACITY_UNITS = 1L;
  private static final Long WRITE_CAPACITY_UNITS = 1L;

  @ClassRule
  public static TestDbCreationRule dynamoDB = new TestDbCreationRule();

  /**
   * Executes before the execution of tests.
   */
  @Before
  public void load() {
    context = load(
      new Class<?>[]{DynamoCacheAutoConfiguration.class},
      "spring.cache.dynamo.caches[0].ttl:" + TTL,
      "spring.cache.dynamo.caches[0].cacheName:" + CACHE_NAME,
      "spring.cache.dynamo.caches[0].flushOnBoot:" + FLUSH_ON_BOOT,
      "spring.cache.dynamo.caches[0].readCapacityUnits:" + READ_CAPACITY_UNITS,
      "spring.cache.dynamo.caches[0].writeCapacityUnits:" + WRITE_CAPACITY_UNITS
    );
  }

  /**
   * Test for using of {@code DynamoCacheManager} instance.
   */
  @Test
  public void testInstance() {
    load();
    assertBeanExists(DynamoCache.class);

    final DynamoCacheManager manager = context.getBean(DynamoCacheManager.class);
    Assert.assertNotNull(manager);
  }

  /**
   * Test for properties of {@code DynamoCacheManager} instance.
   */
  @Test
  public void testProperties() {
    load();
    assertBeanExists(DynamoCacheManager.class);

    final DynamoCacheManager manager = context.getBean(DynamoCacheManager.class);
    Assert.assertNotNull(manager);

    final DynamoCache cache = (DynamoCache) manager.getCache(CACHE_NAME);
    Assert.assertNotNull(cache);
    Assert.assertEquals(TTL, cache.getTtl());
    Assert.assertEquals(CACHE_NAME, cache.getName());
    Assert.assertEquals(FLUSH_ON_BOOT, cache.isFlushOnBoot());
  }

}
