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
