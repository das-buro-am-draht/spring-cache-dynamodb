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
package com.dasburo.spring.cache.dynamo;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration for basic integration tests.
 *
 * @author BaD Georg Zimmermann
 */
@Configuration
public class TestConfiguration {

  private static final String ENDPOINT = "http://localhost:8090";

  @Bean
  public AWSCredentialsProvider amazonAWSCredentialsProvider() {
    return new AWSStaticCredentialsProvider(new BasicAWSCredentials("accessKey", "secretKey"));
  }

  /**
   * Gets a {@link com.amazonaws.services.dynamodbv2.AmazonDynamoDB} instance.
   *
   * @return the {@link com.amazonaws.services.dynamodbv2.AmazonDynamoDB} instance.
   */
  @Bean
  public AmazonDynamoDB amazonDynamoDB(AWSCredentialsProvider amazonAWSCredentialsProvider) {
    return AmazonDynamoDBClientBuilder.standard()
      .withCredentials(amazonAWSCredentialsProvider)
      .withClientConfiguration(
        new ClientConfiguration()
          .withRequestTimeout(5000)
          .withConnectionTimeout(5000))
      .withEndpointConfiguration(
        new AwsClientBuilder
          .EndpointConfiguration(ENDPOINT, Regions.EU_CENTRAL_1.getName()))
      .build();
  }

  @Bean
  public DynamoCacheWriter dynamoCacheWriter(AmazonDynamoDB amazonDynamoDB) {
    return new DefaultDynamoCacheWriter(amazonDynamoDB);
  }

}