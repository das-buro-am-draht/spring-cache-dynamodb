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

import com.dasburo.spring.cache.dynamo.helper.NotSerializeable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SerializableSerializerTest {

  private SerializableSerializer serializer;

  @Before
  public void setup() {
    serializer = new SerializableSerializer();
  }

  @Test
  public void testSerializableSerializer() {
    Assert.assertEquals("test", serializer.deserialize(serializer.serialize("test")));
  }

  @Test
  public void testSerializableSerializer_ShouldReturnNullWhenDeSerializingNullObject() {
    Assert.assertNull(serializer.deserialize(null));
  }

  @Test
  public void testSerializableSerializer_ShouldReturnNullWhenSerializingNull() {
    Assert.assertNull(serializer.deserialize(serializer.serialize(null)));
  }

  @Test(expected = SerializationException.class)
  public void testSerializableSerializer_ShouldThrowExceptionWhenSerializingNonSerializableObject() {
    serializer.serialize(new NotSerializeable());
  }
}
