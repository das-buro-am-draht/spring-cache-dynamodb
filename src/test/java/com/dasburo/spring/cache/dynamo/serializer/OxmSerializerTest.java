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

import com.dasburo.spring.cache.dynamo.helper.Address;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@RunWith(MockitoJUnitRunner.class)
public class OxmSerializerTest {

  @Mock
  private Marshaller marshaller;

  @Mock
  private Unmarshaller unmarshaller;

  OxmSerializer sut;

  @Before
  public void setup() {
    Jaxb2Marshaller m = new Jaxb2Marshaller();
    sut = new OxmSerializer(m, m);
  }

  @Test
  public void testOxmSerializer_WithNullValueShouldReturnNullObject() {
    Assert.assertEquals(null, sut.serialize(null));
  }

  @Test(expected = SerializationException.class)
  public void testOxmSerializer_WithWrongValueShouldThrowException() {
    Address test = new Address();
    sut.serialize(test);
  }

  @Test
  public void testOxmSerializer_WithNullValueShouldReturnNullObjectWhenDeserializing() {
    Assert.assertEquals(null, sut.deserialize(null));
  }
}