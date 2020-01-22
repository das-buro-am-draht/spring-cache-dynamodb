package com.dasburo.spring.cache.dynamo.rootattribute;

import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import org.junit.Test;

import static com.dasburo.spring.cache.dynamo.DefaultDynamoCacheWriter.ATTRIBUTE_KEY;
import static com.dasburo.spring.cache.dynamo.DefaultDynamoCacheWriter.ATTRIBUTE_TTL;
import static com.dasburo.spring.cache.dynamo.DefaultDynamoCacheWriter.ATTRIBUTE_VALUE;

public class RootAttributeConfigTest {

  @Test(expected = IllegalArgumentException.class)
  public void RootAttributeConfigTest_TypeMustNotBeNull() {
    new RootAttributeConfig("sampleField", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void RootAttributeConfigTest_NameMustNotBeNull() {
    new RootAttributeConfig(null, ScalarAttributeType.S);
  }

  @Test(expected = IllegalArgumentException.class)
  public void RootAttributeConfigTest_NameMustNotBeEmpty() {
    new RootAttributeConfig("", ScalarAttributeType.S);
  }

  @Test(expected = IllegalArgumentException.class)
  public void RootAttributeConfigTest_NameMustNotEqualATTRIBUTE_KEY() {
    new RootAttributeConfig(ATTRIBUTE_KEY, ScalarAttributeType.S);
  }

  @Test(expected = IllegalArgumentException.class)
  public void RootAttributeConfigTest_NameMustNotEqualATTRIBUTE_VALUE() {
    new RootAttributeConfig(ATTRIBUTE_VALUE, ScalarAttributeType.S);
  }

  @Test(expected = IllegalArgumentException.class)
  public void RootAttributeConfigTest_NameMustNotEqualATTRIBUTE_TTL() {
    new RootAttributeConfig(ATTRIBUTE_TTL, ScalarAttributeType.S);
  }
}