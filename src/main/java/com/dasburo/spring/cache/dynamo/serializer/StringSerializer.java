/*
 * Copyright 2019 the original author or authors.
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
package com.dasburo.spring.cache.dynamo.serializer;

import org.springframework.lang.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * {@link DynamoSerializer} that can read and write {@link String}.
 * <b>Note:</b>
 * Does not perform any {@literal null} conversion.
 *
 * @author Georg Zimmermann
 */
public class StringSerializer implements DynamoSerializer<String> {

  private final Charset charset;

  public StringSerializer() {
    charset = StandardCharsets.UTF_8;
  }

  @Override
  public byte[] serialize(@Nullable String string) throws SerializationException {
    return (string == null ? null : string.getBytes(charset));
  }

  @Override
  public String deserialize(@Nullable byte[] bytes) throws SerializationException {
    return (bytes == null ? null : new String(bytes, charset));
  }
}
