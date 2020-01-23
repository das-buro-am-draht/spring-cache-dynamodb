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

import com.dasburo.spring.cache.dynamo.DynamoCache;
import com.dasburo.spring.cache.dynamo.rootattribute.RootAttributeConfig;

import java.time.Duration;
import java.util.List;

/**
 * Properties for {@link DynamoCache}.
 *
 * @author BaD Georg Zimmermann
 */
public class DynamoCacheProperties {

  private String cacheName;
  private boolean flushOnBoot;
  private Duration ttl;
  private List<RootAttributeConfig> rootAttributes;
  private Long readCapacityUnits = 1L;
  private Long writeCapacityUnits = 1L;

  public String getCacheName() {
    return cacheName;
  }

  public void setCacheName(String cacheName) {
    this.cacheName = cacheName;
  }

  public boolean isFlushOnBoot() {
    return flushOnBoot;
  }

  public void setFlushOnBoot(boolean flushOnBoot) {
    this.flushOnBoot = flushOnBoot;
  }

  public Duration getTtl() {
    return ttl;
  }

  public void setTtl(Duration ttl) {
    this.ttl = ttl;
  }

  public List<RootAttributeConfig> getRootAttributes() {
    return rootAttributes;
  }

  public void setRootAttributes(List<RootAttributeConfig> rootAttributes) {
    this.rootAttributes = rootAttributes;
  }

  public Long getReadCapacityUnits() {
    return readCapacityUnits;
  }

  public void setReadCapacityUnits(Long readCapacityUnits) {
    this.readCapacityUnits = readCapacityUnits;
  }

  public Long getWriteCapacityUnits() {
    return writeCapacityUnits;
  }

  public void setWriteCapacityUnits(Long writeCapacityUnits) {
    this.writeCapacityUnits = writeCapacityUnits;
  }

}
