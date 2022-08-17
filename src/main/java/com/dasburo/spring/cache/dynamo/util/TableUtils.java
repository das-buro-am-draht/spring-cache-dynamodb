/*
 * Copyright 2022 the original author or authors.
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
package com.dasburo.spring.cache.dynamo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;

/**
 * Utility methods for working with DynamoDB tables.
 */
public class TableUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(TableUtils.class);

  private TableUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Creates the table if possible and ignores any errors if it already exists.
   *
   * @param dynamoTemplate     The {@literal DynamoDbClient} to use.
   * @param createTableRequest The create table request.
   * @return True if created, false otherwise.
   */
  public static boolean createTableIfNotExists(final DynamoDbClient dynamoTemplate, final CreateTableRequest createTableRequest) {
    try {
      dynamoTemplate.createTable(createTableRequest);
      return true;
    } catch (final ResourceInUseException e) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Table " + createTableRequest.tableName() + " already exists", e);
      }
    }
    return false;
  }
}
