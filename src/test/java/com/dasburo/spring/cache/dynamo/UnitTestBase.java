/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dasburo.spring.cache.dynamo;

import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit test base.
 *
 * @author Georg Zimmermann
 */
public class UnitTestBase {

  protected AnnotationConfigApplicationContext context;

  @AfterEach
  public void close() {
    if (context != null) {
      context.close();
    }
  }

  protected void assertBeanExists(Class<?> bean) {
    assertNotNull(context.containsBean(bean.getName()), "The bean does not exist in the context.");
  }

  protected AnnotationConfigApplicationContext load(Class<?>[] configs, String... environment) {
    // Creates an instance of the "AnnotationConfigApplicationContext" class that represents
    // the application context.
    AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

    // Adds environment.
    TestPropertyValues.of(environment).applyTo(applicationContext);

    // Registers the configuration class and auto-configuration classes.
    applicationContext.register(TestConfiguration.class);
    applicationContext.register(configs);
    applicationContext.refresh();

    return applicationContext;
  }

}
