package com.dasburo.spring.cache.dynamo.rootattribute;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public class RootAttributeReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(RootAttributeReader.class);

  @Nullable
  public RootAttribute readRootAttribute(@NonNull RootAttributeConfig rootAttributeConfig, @NonNull Object object) {
    try {
      Object value = PropertyUtils.getProperty(object, rootAttributeConfig.getName());
      AttributeValue attributeValue = mapValueToAttributeValue(value, rootAttributeConfig.getType());
      return new RootAttribute(rootAttributeConfig.getName(), attributeValue);
    }
    catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      LOGGER.trace("Unable to access attribute {} on instance of class {}", rootAttributeConfig.getName(), object.getClass());
      return null;
    }
  }

  @Nullable
  private AttributeValue mapValueToAttributeValue(@NonNull Object value, @NonNull ScalarAttributeType type) {
    switch (type) {
      case S:
        return new AttributeValue().withS(String.valueOf(value));
      case N:
        return new AttributeValue().withN(String.valueOf(value));
      case B:
        return new AttributeValue().withB((ByteBuffer) value);
    }
    return null;
  }
}
