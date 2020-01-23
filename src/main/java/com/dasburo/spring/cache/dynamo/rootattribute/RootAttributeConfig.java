package com.dasburo.spring.cache.dynamo.rootattribute;

import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import org.springframework.util.Assert;

import static com.dasburo.spring.cache.dynamo.DefaultDynamoCacheWriter.ATTRIBUTE_KEY;
import static com.dasburo.spring.cache.dynamo.DefaultDynamoCacheWriter.ATTRIBUTE_TTL;
import static com.dasburo.spring.cache.dynamo.DefaultDynamoCacheWriter.ATTRIBUTE_VALUE;

public class RootAttributeConfig {

  private String name;
  private ScalarAttributeType type;

  /* default constructor required for property binding */
  public RootAttributeConfig(){
  }

  public RootAttributeConfig(String name, ScalarAttributeType type) {
    setName(name);
    setType(type);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    Assert.notNull(name, "name must not be null!");
    Assert.isTrue(name.length() > 0, "name must not be empty!");
    Assert.isTrue(!ATTRIBUTE_KEY.equalsIgnoreCase(name), "name must not equal '" + ATTRIBUTE_KEY+"'");
    Assert.isTrue(!ATTRIBUTE_VALUE.equalsIgnoreCase(name), "name must not equal '" + ATTRIBUTE_VALUE+"'");
    Assert.isTrue(!ATTRIBUTE_TTL.equalsIgnoreCase(name), "name must not equal '" + ATTRIBUTE_TTL+"'");
    this.name = name;
  }

  public ScalarAttributeType getType() {
    return type;
  }

  public void setType(ScalarAttributeType type) {
    Assert.notNull(type, "type must not be null!");
    this.type = type;
  }
}
