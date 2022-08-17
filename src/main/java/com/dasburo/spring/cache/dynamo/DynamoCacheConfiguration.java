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

import com.dasburo.spring.cache.dynamo.rootattribute.RootAttributeConfig;
import com.dasburo.spring.cache.dynamo.serializer.DynamoSerializer;
import com.dasburo.spring.cache.dynamo.serializer.StringSerializer;

import java.time.Duration;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * A configuration container for a {@link DynamoCache} instance.
 *
 * @author Georg Zimmermann
 */
public class DynamoCacheConfiguration {

  private Duration ttl;
  private boolean flushOnBoot;
  private Long readCapacityUnits;
  private Long writeCapacityUnits;
  private DynamoSerializer serializer;
  private List<RootAttributeConfig> rootAttributes;

  private DynamoCacheConfiguration(Duration ttl, boolean flushOnBoot, Long readCapacityUnits, Long writeCapacityUnits, DynamoSerializer serializer, List<RootAttributeConfig> rootAttributes) {
    this.ttl = ttl;
    this.flushOnBoot = flushOnBoot;
    this.readCapacityUnits = readCapacityUnits;
    this.writeCapacityUnits = writeCapacityUnits;
    this.serializer = serializer;
    this.rootAttributes = rootAttributes;
  }

  public static DynamoCacheConfiguration defaultCacheConfig() {
    return new DynamoCacheConfiguration(Duration.ZERO, false, 1L, 1L, new StringSerializer(), emptyList());
  }

  public Duration getTtl() {
    return ttl;
  }

  public void setTtl(Duration ttl) {
    this.ttl = ttl;
  }

  public boolean isFlushOnBoot() {
    return flushOnBoot;
  }

  public void setFlushOnBoot(boolean flushOnBoot) {
    this.flushOnBoot = flushOnBoot;
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

  public DynamoSerializer getSerializer() {
    return serializer;
  }

  public void setSerializer(DynamoSerializer serializer) {
    this.serializer = serializer;
  }

  public List<RootAttributeConfig> getRootAttributes() {
    return rootAttributes;
  }

  public void setRootAttributes(List<RootAttributeConfig> rootAttributes) {
    this.rootAttributes = rootAttributes;
  }
}
