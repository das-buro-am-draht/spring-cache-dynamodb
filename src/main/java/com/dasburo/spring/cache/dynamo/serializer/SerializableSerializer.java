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
package com.dasburo.spring.cache.dynamo.serializer;

import org.springframework.lang.Nullable;

import java.io.*;

/**
 * {@link DynamoSerializer} that can read and write {@link Object} that implement {@link Serializable}.
 * <b>Note:</b>Null objects are serialized as empty arrays and vice versa.
 *
 * @author Georg Zimmermann
 */
public class SerializableSerializer implements DynamoSerializer<Object> {

  @Override
  public byte[] serialize(@Nullable Object object) throws SerializationException {
    try (final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
         final ObjectOutputStream output = new ObjectOutputStream(buffer)) {
      output.writeObject(object);
      return buffer.toByteArray();
    } catch (IOException e) {
      throw new SerializationException("Cannot serialize value.", e);
    }
  }

  @Override
  public Object deserialize(@Nullable byte[] bytes) throws SerializationException {
    if (bytes == null) {
      return null;
    }

    try (final ByteArrayInputStream buffer = new ByteArrayInputStream(bytes);
         final ObjectInputStream output = new ObjectInputStream(buffer)) {
      return output.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new SerializationException("Cannot deserialize the value.", e);
    }
  }
}
