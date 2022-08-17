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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GZipSerializerTest {

  private GZipSerializer serializer;

  @Before
  public void setup() {
    serializer = new GZipSerializer<>(new StringSerializer());
  }

  @Test
  public void testGZipSerializer() {
    Assert.assertEquals("test", serializer.deserialize(serializer.serialize("test")));
  }

  @Test
  public void testGZipSerializer_ShouldReturnNullWhenSerializingNull() {
    Assert.assertNull(serializer.deserialize(serializer.serialize(null)));
  }
}
