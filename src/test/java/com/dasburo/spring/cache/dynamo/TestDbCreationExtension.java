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

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestDbCreationExtension implements AfterAllCallback, BeforeAllCallback {
  private DynamoDBProxyServer server;

  public TestDbCreationExtension() {
    System.setProperty("sqlite4java.library.path", "native-libs");
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) throws Exception {
    this.stopUnchecked(server);
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    server = ServerRunner.createServerFromCommandLineArgs(
      new String[]{"-inMemory", "-port", "8090"});
    server.start();
  }

  private void stopUnchecked(DynamoDBProxyServer dynamoDbServer) {
    try {
      dynamoDbServer.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
