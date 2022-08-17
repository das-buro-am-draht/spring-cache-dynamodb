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
package com.dasburo.spring.cache.dynamo.util;

import org.springframework.util.Assert;

import java.nio.ByteBuffer;

public class ByteUtils {

  private ByteUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Extract a byte array from {@link ByteBuffer} without consuming it.
   *
   * @param byteBuffer must not be {@literal null}.
   * @return Return byte[]
   * @deprecated By using {@literal SdkBytes} this is not necessary anymore and will be removed in future versions.
   */
  @Deprecated
  public static byte[] getBytes(ByteBuffer byteBuffer) {
    Assert.notNull(byteBuffer, "ByteBuffer must not be null!");

    ByteBuffer duplicate = byteBuffer.duplicate();
    byte[] bytes = new byte[duplicate.remaining()];
    duplicate.get(bytes);
    return bytes;
  }
}
