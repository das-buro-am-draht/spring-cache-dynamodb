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

import com.dasburo.spring.cache.dynamo.helper.Address;
import com.dasburo.spring.cache.dynamo.helper.Company;
import com.dasburo.spring.cache.dynamo.helper.NotSerializeable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class Jackson2JsonSerializerTest {

  private Jackson2JsonSerializer<Company> serializer;

  @BeforeEach
  public void setup() {
    this.serializer = new Jackson2JsonSerializer<>(Company.class);
  }

  @Test
  public void testJackson2JsonSerializer() {
    Company company = new Company("company", "IT", 2019, new Address("street", 1));
    assertEquals(company, serializer.deserialize(serializer.serialize(company)));
  }

  @Test
  public void testJackson2JsonSerializer_ShouldReturnNullWhenSerializingNull() {
    assertEquals(null, serializer.serialize(null));
  }

  @Test
  public void testJackson2JsonSerializer_ShouldReturnNullWhenDeserializingEmptyByteArray() {
    assertNull(serializer.deserialize(new byte[0]));
  }

  @Test
  public void testSerializableSerializer_ShouldThrowExceptionWhenSerializingNonSerializableObject() {
    Assertions.assertThrows(SerializationException.class, () ->
      serializer.serialize(new NotSerializeable())
    );
  }

  @Test
  public void testJackson2JsonSerializer_ShouldThrowExceptionWhenDeserializingInvalidByteArray() {
    Company company = new Company("company", "IT", 2019, new Address("street", 1));
    byte[] serializedValue = serializer.serialize(company);
    Arrays.sort(serializedValue); // corrupt serialization result

    Assertions.assertThrows(SerializationException.class, () ->
      serializer.deserialize(serializedValue)
    );
  }

  @Test
  public void testJackson2JsonSerializer_ThrowsExceptionWhenSettingNullObjectMapper() {
    Assertions.assertThrows(IllegalArgumentException.class, () ->
      serializer.setObjectMapper(null)
    );
  }

  @Test
  public void testJackson2JsonSerializer_ShouldUSeCustomObjectMapper() throws JsonProcessingException {
    ObjectMapper mockObjMapper = mock(ObjectMapper.class);
    serializer.setObjectMapper(mockObjMapper);

    serializer.serialize(new Address());
    verify(mockObjMapper, times(1)).writeValueAsBytes(any());
  }

}
