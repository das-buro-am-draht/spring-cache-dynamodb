package com.dasburo.spring.cache.dynamo.serializer;

import com.dasburo.spring.cache.dynamo.Address;
import com.dasburo.spring.cache.dynamo.Company;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;


public class Jackson2JsonSerializerTest {

  private Jackson2JsonSerializer<Company> serializer;

  @Before
  public void setup() {
    this.serializer = new Jackson2JsonSerializer<>(Company.class);
  }

  @Test
  public void testJackson2JsonSerializer() {
    Company company = new Company("company", "IT", 2019, new Address("street", 1));
    Assert.assertEquals(company, serializer.deserialize(serializer.serialize(company)));
  }

  @Test
  public void testJackson2JsonSerializer_ShouldReturnNullWhenSerializingNull() {
    Assert.assertEquals(null, serializer.serialize(null));
  }

  @Test
  public void testJackson2JsonSerializer_ShouldReturnNullWhenDeserializingEmptyByteArray() {
    Assert.assertNull(serializer.deserialize(new byte[0]));
  }

  @Test(expected = SerializationException.class)
  public void testJackson2JsonSerilizer_ShouldThrowExceptionWhenDeserializingInvalidByteArray() {
    Company company = new Company("company", "IT", 2019, new Address("street", 1));
    byte[] serializedValue = serializer.serialize(company);
    Arrays.sort(serializedValue); // corrupt serialization result

    serializer.deserialize(serializedValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testJackson2JsonSerializer_ThrowsExceptionWhenSettingNullObjectMapper() {
    serializer.setObjectMapper(null);
  }

}
