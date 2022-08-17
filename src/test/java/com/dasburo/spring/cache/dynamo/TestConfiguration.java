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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Spring Configuration for basic integration tests.
 *
 * @author Georg Zimmermann
 */
@Configuration
public class TestConfiguration {

  private static final String ENDPOINT = "http://localhost:8090";

  @Bean
  public AwsCredentialsProvider awsCredentialsProvider() {
    return StaticCredentialsProvider.create(
      AwsBasicCredentials.create("accessKey", "secretKey"));
  }

  /**
   * Gets a {@link DynamoDbClient} instance.
   *
   * @return the {@link DynamoDbClient} instance.
   */
  @Bean
  public DynamoDbClient amazonDynamoDB(AwsCredentialsProvider awsCredentialsProvider) {
    ProxyConfiguration.Builder proxyConfig = ProxyConfiguration.builder();

    ApacheHttpClient.Builder httpClientBuilder =
      ApacheHttpClient.builder()
        .proxyConfiguration(proxyConfig.build())
        .connectionTimeout(Duration.of(5, ChronoUnit.SECONDS));

    ClientOverrideConfiguration.Builder overrideConfig =
      ClientOverrideConfiguration.builder();

    return DynamoDbClient.builder()
      .credentialsProvider(awsCredentialsProvider)
      .httpClientBuilder(httpClientBuilder)
      .overrideConfiguration(overrideConfig.build())
      .endpointOverride(URI.create(ENDPOINT))
      .region(Region.EU_CENTRAL_1)
      .build();
  }

  @Bean
  public DynamoCacheWriter dynamoCacheWriter(DynamoDbClient amazonDynamoDB) {
    return DynamoCacheWriter.lockingDynamoCacheWriter(amazonDynamoDB);
  }

}
