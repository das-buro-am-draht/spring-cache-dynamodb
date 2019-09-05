package com.dasburo.spring.cache.dynamo.serializer;

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
  public void testSerializableSerializer_ShouldReturnNullWhenSerializingNull() {
    Assert.assertNull(serializer.deserialize(serializer.serialize(null)));
  }

}
