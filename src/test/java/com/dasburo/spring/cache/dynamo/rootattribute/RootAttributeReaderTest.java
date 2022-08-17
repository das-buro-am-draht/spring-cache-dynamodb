package com.dasburo.spring.cache.dynamo.rootattribute;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType.S;

public class RootAttributeReaderTest {

  private RootAttributeReader rootAttributeReader;

  @BeforeEach
  public void setup() {
    rootAttributeReader = new RootAttributeReader();
  }

  @Test
  public void testRootAttributeReader_StringFieldCanBeHandled() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("stringField", S);
    SampleTestClass sampleInstance = new SampleTestClass();
    sampleInstance.setStringField("dummy-value");

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, sampleInstance);

    //then
    assertEquals("stringField", rootAttribute.getName());
    assertEquals("dummy-value", rootAttribute.getAttributeValue().s());
  }

  @Test
  public void testRootAttributeReader_BooleanFieldCanBeHandledAsString() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("booleanField", S);
    SampleTestClass sampleInstance = new SampleTestClass();
    sampleInstance.setBooleanField(true);

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, sampleInstance);

    //then
    assertEquals("booleanField", rootAttribute.getName());
    assertEquals("true", rootAttribute.getAttributeValue().s());
  }

  @Test
  public void testRootAttributeReader_EnumFieldCanBeHandledAsString() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("enumField", ScalarAttributeType.N);
    SampleTestClass sampleInstance = new SampleTestClass();
    sampleInstance.setEnumField(S);

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, sampleInstance);

    //then
    assertEquals("enumField", rootAttribute.getName());
    assertEquals("S", rootAttribute.getAttributeValue().n());
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
    assertEquals("123", rootAttribute.getAttributeValue().n());
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
    assertEquals("123.0", rootAttribute.getAttributeValue().n());
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
    assertEquals(SdkBytes.fromByteArray("dummy-text".getBytes()), rootAttribute.getAttributeValue().b());
  }

  @Test
  public void testRootAttributeReader_HashMapCanBeHandled() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("stringField", S);
    HashMap<String, String> map = new HashMap<>();
    map.put("stringField", "dummy-text");

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, map);

    //then
    assertEquals("stringField", rootAttribute.getName());
    assertEquals("dummy-text", rootAttribute.getAttributeValue().s());
  }

  @Test
  public void testRootAttributeReader_LinkedHashMapCanBeHandled() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("stringField", S);
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("stringField", "dummy-text");

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, map);

    //then
    assertEquals("stringField", rootAttribute.getName());
    assertEquals("dummy-text", rootAttribute.getAttributeValue().s());
  }

  @Test
  public void testRootAttributeReader_UnknownFieldIsGetsIgnored() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("unknownField", S);
    SampleTestClass sampleInstance = new SampleTestClass();

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, sampleInstance);

    //then
    assertNull(rootAttribute);
  }

  @Test
  public void testRootAttributeReader_NullValueOfKnownFieldsGetsIgnored() {
    //given
    RootAttributeConfig rootAttributeConfig = new RootAttributeConfig("stringField", S);
    SampleTestClass sampleInstance = new SampleTestClass();
    sampleInstance.setStringField(null);

    //when
    RootAttribute rootAttribute = rootAttributeReader.readRootAttribute(rootAttributeConfig, sampleInstance);

    //then
    assertNull(rootAttribute);
  }
}
