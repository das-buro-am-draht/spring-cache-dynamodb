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
package com.dasburo.spring.cache.dynamo.serializer;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

public interface DynamoSerializer<T> {

  /**
   * Serialize the given object to binary data.
   *
   * @param t object to serialize. Can be {@literal null}.
   * @return the equivalent binary data. Can be {@literal null}.
   */
  @Nullable
  byte[] serialize(@Nullable T t) throws SerializationException;

  /**
   * Deserialize an object from the given binary data.
   *
   * @param bytes object binary representation. Can be {@literal null}.
   * @return the equivalent object instance. Can be {@literal null}.
   */
  @Nullable
  T deserialize(@Nullable byte[] bytes) throws SerializationException;

  default boolean canSerialize(Class<?> type) {
    return ClassUtils.isAssignable(getTargetType(), type);
  }

  default Class<?> getTargetType() {
    return Object.class;
  }
}
