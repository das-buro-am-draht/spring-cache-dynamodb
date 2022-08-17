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
package com.dasburo.spring.cache.dynamo.autoconfigure;

import com.dasburo.spring.cache.dynamo.DynamoCache;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Properties for {@link DynamoCache}.
 *
 * @author Georg Zimmermann
 */
@ConfigurationProperties(prefix = "spring.cache.dynamo")
public class DynamoCachePropertiesList {

  private List<DynamoCacheProperties> caches;

  public List<DynamoCacheProperties> getCaches() {
    return caches;
  }

  public void setCaches(List<DynamoCacheProperties> caches) {
    this.caches = caches;
  }

}
