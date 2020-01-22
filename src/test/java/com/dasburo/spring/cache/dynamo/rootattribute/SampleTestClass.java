package com.dasburo.spring.cache.dynamo.rootattribute;

import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

import java.nio.ByteBuffer;

public class SampleTestClass {

  private String stringField;
  private Boolean booleanField;
  private ScalarAttributeType enumField;
  private Integer integerField;
  private Double doubleField;
  private ByteBuffer byteBufferField;

  public String getStringField() {
    return stringField;
  }

  public void setStringField(String stringField) {
    this.stringField = stringField;
  }

  public Boolean getBooleanField() {
    return booleanField;
  }

  public void setBooleanField(Boolean booleanField) {
    this.booleanField = booleanField;
  }

  public ScalarAttributeType getEnumField() {
    return enumField;
  }

  public void setEnumField(ScalarAttributeType enumField) {
    this.enumField = enumField;
  }

  public Integer getIntegerField() {
    return integerField;
  }

  public void setIntegerField(Integer integerField) {
    this.integerField = integerField;
  }

  public Double getDoubleField() {
    return doubleField;
  }

  public void setDoubleField(Double doubleField) {
    this.doubleField = doubleField;
  }

  public ByteBuffer getByteBufferField() {
    return byteBufferField;
  }

  public void setByteBufferField(ByteBuffer byteBufferField) {
    this.byteBufferField = byteBufferField;
  }
}
