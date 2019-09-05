package com.dasburo.spring.cache.dynamo.serializer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StringSerializerTest {

  private StringSerializer serializer;

  @Before
  public void setup() {
    serializer = new StringSerializer();
  }

  @Test
  public void serialize() {
    String expectedValue = "test";

    byte[] serializedValue = serializer.serialize(expectedValue);
    String actualValue = serializer.deserialize(serializedValue);

    Assert.assertEquals(expectedValue, actualValue);
  }

  @Test
  public void serializeWithNull() {
    String expectedValue = null;

    byte[] serializedValue = serializer.serialize(expectedValue);
    Object actualValue = serializer.deserialize(serializedValue);

    Assert.assertEquals(expectedValue, actualValue);
  }

}
