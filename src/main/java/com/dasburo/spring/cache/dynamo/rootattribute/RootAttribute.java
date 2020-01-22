package com.dasburo.spring.cache.dynamo.rootattribute;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public class RootAttribute {

  private String name;
  private AttributeValue value;

  public RootAttribute(String name, AttributeValue value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public AttributeValue getAttributeValue() {
    return value;
  }

}
