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

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public class RootAttributeReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(RootAttributeReader.class);

  @Nullable
  public RootAttribute readRootAttribute(@NonNull RootAttributeConfig rootAttributeConfig, @NonNull Object object) {
    try {
      Object value = PropertyUtils.getProperty(object, rootAttributeConfig.getName());
      if (value == null) {
        return null;
      }
      AttributeValue attributeValue = mapValueToAttributeValue(value, rootAttributeConfig.getType());
      return new RootAttribute(rootAttributeConfig.getName(), attributeValue);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      LOGGER.trace("Unable to access attribute {} on instance of class {}", rootAttributeConfig.getName(), object.getClass());
      return null;
    }
  }

  @Nullable
  private AttributeValue mapValueToAttributeValue(@NonNull Object value, @NonNull ScalarAttributeType type) {
    switch (type) {
      case N:
        return AttributeValue.fromN(String.valueOf(value));
      case B:
        return AttributeValue.fromB(SdkBytes.fromByteBuffer((ByteBuffer) value));
      case S:
      default:
        return AttributeValue.fromS(String.valueOf(value));
    }
  }
}
