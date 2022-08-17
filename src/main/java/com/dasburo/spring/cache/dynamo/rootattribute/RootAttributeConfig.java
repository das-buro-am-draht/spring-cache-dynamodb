/*
 * Copyright 2019-2022 the original author or authors.
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
package com.dasburo.spring.cache.dynamo.rootattribute;

import org.springframework.util.Assert;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import static com.dasburo.spring.cache.dynamo.DefaultDynamoCacheWriter.ATTRIBUTE_KEY;
import static com.dasburo.spring.cache.dynamo.DefaultDynamoCacheWriter.ATTRIBUTE_TTL;
import static com.dasburo.spring.cache.dynamo.DefaultDynamoCacheWriter.ATTRIBUTE_VALUE;

public class RootAttributeConfig {

  private String name;
  private ScalarAttributeType type;

  /* default constructor required for property binding */
  public RootAttributeConfig(){
  }

  public RootAttributeConfig(String name, ScalarAttributeType type) {
    setName(name);
    setType(type);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    Assert.notNull(name, "name must not be null!");
    Assert.isTrue(name.length() > 0, "name must not be empty!");
    Assert.isTrue(!ATTRIBUTE_KEY.equalsIgnoreCase(name), "name must not equal '" + ATTRIBUTE_KEY+"'");
    Assert.isTrue(!ATTRIBUTE_VALUE.equalsIgnoreCase(name), "name must not equal '" + ATTRIBUTE_VALUE+"'");
    Assert.isTrue(!ATTRIBUTE_TTL.equalsIgnoreCase(name), "name must not equal '" + ATTRIBUTE_TTL+"'");
    this.name = name;
  }

  public ScalarAttributeType getType() {
    return type;
  }

  public void setType(ScalarAttributeType type) {
    Assert.notNull(type, "type must not be null!");
    this.type = type;
  }
}
