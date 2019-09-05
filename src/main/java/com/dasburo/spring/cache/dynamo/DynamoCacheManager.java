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

import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * CacheManager implementation that lazily builds {@link DynamoCache}
 * instances for each {@link #getCache} request.
 *
 * @author Georg Zimmermann
 */
public class DynamoCacheManager extends AbstractCacheManager {

  private final Collection<DynamoCacheBuilder> initialCaches;

  /**
   * Constructor.
   *
   * @param initialCaches the caches to make available on startup.
   */
  public DynamoCacheManager(final Collection<DynamoCacheBuilder> initialCaches) {
    Assert.notEmpty(initialCaches, "At least one cache builder must be specified.");
    this.initialCaches = new ArrayList<>(initialCaches);
  }

  @Override
  protected Collection<? extends Cache> loadCaches() {
    final Collection<Cache> caches = new LinkedHashSet<>(initialCaches.size());
    for (final DynamoCacheBuilder cacheBuilder : initialCaches) {
      final DynamoCache cache = cacheBuilder.build();
      caches.add(cache);
    }

    return caches;
  }

}
