package com.dasburo.spring.cache.dynamo.rootattribute;

import org.junit.jupiter.api.Test;

import static com.dasburo.spring.cache.dynamo.DefaultDynamoCacheWriter.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType.S;

public class RootAttributeConfigTest {

  @Test
  public void RootAttributeConfigTest_TypeMustNotBeNull() {
    assertThrows(IllegalArgumentException.class, () ->
      new RootAttributeConfig("sampleField", null)
    );
  }

  @Test
  public void RootAttributeConfigTest_NameMustNotBeNull() {
    assertThrows(IllegalArgumentException.class, () ->
      new RootAttributeConfig(null, S)
    );
  }

  @Test
  public void RootAttributeConfigTest_NameMustNotBeEmpty() {
    assertThrows(IllegalArgumentException.class, () ->
      new RootAttributeConfig("", S)
    );
  }

  @Test
  public void RootAttributeConfigTest_NameMustNotEqualATTRIBUTE_KEY() {
    assertThrows(IllegalArgumentException.class, () ->
      new RootAttributeConfig(ATTRIBUTE_KEY, S)
    );
  }

  @Test
  public void RootAttributeConfigTest_NameMustNotEqualATTRIBUTE_VALUE() {
    assertThrows(IllegalArgumentException.class, () ->
      new RootAttributeConfig(ATTRIBUTE_VALUE, S)
    );
  }

  @Test
  public void RootAttributeConfigTest_NameMustNotEqualATTRIBUTE_TTL() {
    assertThrows(IllegalArgumentException.class, () ->
      new RootAttributeConfig(ATTRIBUTE_TTL, S)
    );
  }
}
