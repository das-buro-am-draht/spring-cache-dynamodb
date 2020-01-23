package com.dasburo.spring.cache.dynamo.rootattribute;

import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RootAttributeReaderTest {

  private RootAttributeReader rootAttributeReader;

  @Before
  public void setup() {
    rootAttributeReader = new RootAttributeReader();
  }

  @Test
  public void testRootAttributeReader_StringFieldCanBeHandled() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("stringField", ScalarAttributeType.S);
    SampleTestClass sampleInstance = new SampleTestClass();
    sampleInstance.setStringField("dummy-value");

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, sampleInstance);

    //then
    assertEquals("stringField", rootAttribute.getName());
    assertEquals("dummy-value", rootAttribute.getAttributeValue().getS());
  }

  @Test
  public void testRootAttributeReader_BooleanFieldCanBeHandledAsString() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("booleanField", ScalarAttributeType.S);
    SampleTestClass sampleInstance = new SampleTestClass();
    sampleInstance.setBooleanField(true);

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, sampleInstance);

    //then
    assertEquals("booleanField", rootAttribute.getName());
    assertEquals("true", rootAttribute.getAttributeValue().getS());
  }

  @Test
  public void testRootAttributeReader_EnumFieldCanBeHandledAsString() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("enumField", ScalarAttributeType.N);
    SampleTestClass sampleInstance = new SampleTestClass();
    sampleInstance.setEnumField(ScalarAttributeType.S);

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, sampleInstance);

    //then
    assertEquals("enumField", rootAttribute.getName());
    assertEquals("S", rootAttribute.getAttributeValue().getN());
  }

  @Test
  public void testRootAttributeReader_IntegerFieldCanBeHandled() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("integerField", ScalarAttributeType.N);
    SampleTestClass sampleInstance = new SampleTestClass();
    sampleInstance.setIntegerField(123);

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, sampleInstance);

    //then
    assertEquals("integerField", rootAttribute.getName());
    assertEquals("123", rootAttribute.getAttributeValue().getN());
  }

  @Test
  public void testRootAttributeReader_DoubleFieldCanBeHandled() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("doubleField", ScalarAttributeType.N);
    SampleTestClass sampleInstance = new SampleTestClass();
    sampleInstance.setDoubleField(123.0);

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, sampleInstance);

    //then
    assertEquals("doubleField", rootAttribute.getName());
    assertEquals("123.0", rootAttribute.getAttributeValue().getN());
  }

  @Test
  public void testRootAttributeReader_ByteBufferFieldCanBeHandled() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("byteBufferField", ScalarAttributeType.B);
    SampleTestClass sampleInstance = new SampleTestClass();
    sampleInstance.setByteBufferField(ByteBuffer.wrap("dummy-text".getBytes()));

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, sampleInstance);

    //then
    assertEquals("byteBufferField", rootAttribute.getName());
    assertEquals(ByteBuffer.wrap("dummy-text".getBytes()), rootAttribute.getAttributeValue().getB());
  }

  @Test
  public void testRootAttributeReader_HashMapCanBeHandled() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("stringField", ScalarAttributeType.S);
    HashMap<String, String> map = new HashMap<>();
    map.put("stringField", "dummy-text");

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, map);

    //then
    assertEquals("stringField", rootAttribute.getName());
    assertEquals("dummy-text", rootAttribute.getAttributeValue().getS());
  }

  @Test
  public void testRootAttributeReader_LinkedHashMapCanBeHandled() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("stringField", ScalarAttributeType.S);
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("stringField", "dummy-text");

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, map);

    //then
    assertEquals("stringField", rootAttribute.getName());
    assertEquals("dummy-text", rootAttribute.getAttributeValue().getS());
  }

  @Test
  public void testRootAttributeReader_UnknownFieldIsGetsIgnored() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("unknownField", ScalarAttributeType.S);
    SampleTestClass sampleInstance = new SampleTestClass();

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, sampleInstance);

    //then
    assertNull(rootAttribute);
  }

  @Test
  public void testRootAttributeReader_NullValueOfKnownFieldsGetsIgnored() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("stringField", ScalarAttributeType.S);
    SampleTestClass sampleInstance = new SampleTestClass();
    sampleInstance.setStringField(null);

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, sampleInstance);

    //then
    assertNull(rootAttribute);
  }
}