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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.dasburo.spring.cache.dynamo.DynamoCacheBuilder;
import com.dasburo.spring.cache.dynamo.DynamoCacheManager;
import com.dasburo.spring.cache.dynamo.DynamoCacheWriter;
import com.dasburo.spring.cache.dynamo.serializer.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration Auto configuration} for {@code DynamoCacheManager} support.
 *
 * @author BaD Georg Zimmermann
 */
@Configuration
@ConditionalOnClass(AmazonDynamoDB.class)
@ConditionalOnMissingBean(CacheManager.class)
@EnableConfigurationProperties(DynamoCachePropertiesList.class)
public class DynamoCacheAutoConfiguration {

  private final AmazonDynamoDB dynamoTemplate;

  private final DynamoCachePropertiesList properties;

  @Autowired
  public DynamoCacheAutoConfiguration(AmazonDynamoDB dynamoTemplate, DynamoCachePropertiesList properties) {
    this.dynamoTemplate = dynamoTemplate;
    this.properties = properties;
  }

  /**
   * Creates an instance of the {@code CacheManager} class.
   * Only instantiates if there is at least one cache defined.
   *
   * @return the instance of {@code CacheManager} class.
   */
  @Bean
  @ConditionalOnProperty("spring.cache.dynamo.caches[0].cacheName")
  public CacheManager dynamoCacheManager() {
    return new DynamoCacheManager(dynamoCacheBuilders());
  }

  private List<DynamoCacheBuilder> dynamoCacheBuilders() {

    List<DynamoCacheBuilder> builders = new ArrayList<>();

    if (properties.getCaches() != null) {
      for (DynamoCacheProperties dynamoCacheProperties : properties.getCaches()) {
        builders.add(
          DynamoCacheBuilder
            .newInstance(
              dynamoCacheProperties.getCacheName(), dynamoTemplate
            )
            .withTTL(dynamoCacheProperties.getTtl())
            .withFlushOnBoot(dynamoCacheProperties.isFlushOnBoot())
            .withReadCapacityUnit(dynamoCacheProperties.getReadCapacityUnits())
            .withWriteCapacityUnit(dynamoCacheProperties.getWriteCapacityUnits())
            .withSerializer(new StringSerializer())
            .withWriter(DynamoCacheWriter.nonLockingDynamoCacheWriter(dynamoTemplate))
        );
      }
    }

    return builders;
  }

}
