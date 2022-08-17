package com.dasburo.spring.cache.dynamo.rootattribute;

import org.junit.Test;

import static com.dasburo.spring.cache.dynamo.DefaultDynamoCacheWriter.ATTRIBUTE_KEY;
import static com.dasburo.spring.cache.dynamo.DefaultDynamoCacheWriter.ATTRIBUTE_TTL;
import static com.dasburo.spring.cache.dynamo.DefaultDynamoCacheWriter.ATTRIBUTE_VALUE;
import static software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType.S;

public class RootAttributeConfigTest {

  @Test(expected = IllegalArgumentException.class)
  public void RootAttributeConfigTest_TypeMustNotBeNull() {
    new RootAttributeConfig("sampleField", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void RootAttributeConfigTest_NameMustNotBeNull() {
    new RootAttributeConfig(null, S);
  }

  @Test(expected = IllegalArgumentException.class)
  public void RootAttributeConfigTest_NameMustNotBeEmpty() {
    new RootAttributeConfig("", S);
  }

  @Test(expected = IllegalArgumentException.class)
  public void RootAttributeConfigTest_NameMustNotEqualATTRIBUTE_KEY() {
    new RootAttributeConfig(ATTRIBUTE_KEY, S);
  }

  @Test(expected = IllegalArgumentException.class)
  public void RootAttributeConfigTest_NameMustNotEqualATTRIBUTE_VALUE() {
    new RootAttributeConfig(ATTRIBUTE_VALUE, S);
  }

  @Test(expected = IllegalArgumentException.class)
  public void RootAttributeConfigTest_NameMustNotEqualATTRIBUTE_TTL() {
    new RootAttributeConfig(ATTRIBUTE_TTL, S);
  }
}
