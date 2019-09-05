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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * {@link DynamoSerializer} that can read and write the given object with the parent serializer
 * and compress or uncompress the resulting data using {@link GZIPInputStream}
 *
 * @author Georg Zimmermann
 */
public class GZipSerializer<T> implements InitializingBean, DynamoSerializer<T> {

  private @Nullable
  DynamoSerializer<T> parent;

  /**
   * Creates a new {@link GZipSerializer} for the given class
   *
   * @param parent the parent {@link DynamoSerializer}
   */
  public GZipSerializer(DynamoSerializer<T> parent) {
    setParent(parent);
  }

  /**
   * @param parent The parent serializer to set.
   */
  public void setParent(DynamoSerializer<T> parent) {
    Assert.notNull(parent, "Parent serializer must not be null!");

    this.parent = parent;
  }

  @Override
  public byte[] serialize(@Nullable T t) throws SerializationException {
    byte[] data = parent.serialize(t);

    if (SerializationUtils.isEmpty(data)) {
      return null;
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);

    try (GZIPOutputStream zipOut = new GZIPOutputStream(out)) {
      zipOut.write(data);
    } catch (IOException e) {
      throw new SerializationException("Failed to zip value.", e);
    }

    return out.toByteArray();
  }

  @Override
  public T deserialize(@Nullable byte[] zippedBytes) throws SerializationException {

    if (SerializationUtils.isEmpty(zippedBytes)) {
      return null;
    }

    ByteArrayInputStream bais = new ByteArrayInputStream(zippedBytes);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (GZIPInputStream is = new GZIPInputStream(bais)) {
      int chunkSize = 1024;
      byte[] buffer = new byte[chunkSize];
      int length = 0;
      while ((length = is.read(buffer, 0, chunkSize)) != -1) {
        baos.write(buffer, 0, length);
      }

      baos.close();
      bais.close();

      return parent.deserialize(baos.toByteArray());
    } catch (IOException e) {
      throw new SerializationException("Failed to unzip value.", e);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() {
    Assert.notNull(parent, "non-null parent serializer required");
  }

}
