/**
 * Copyright (C) 2015-2021 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hotels.beeju;

import static org.apache.hadoop.hive.metastore.conf.MetastoreConf.ConfVars.CONNECT_URL_KEY;

import java.util.Map;

import org.junit.runner.Description;

import com.hotels.beeju.core.ThriftHiveMetaStoreCore;

/**
 * A JUnit Rule that creates a Hive Metastore Thrift service backed by a Hive Metastore using an HSQLDB in-memory
 * database.
 * <p>
 * A fresh database instance will be created for each test method.
 * </p>
 */
public class ThriftHiveMetaStoreJUnitRule extends HiveMetaStoreJUnitRule {

  private ThriftHiveMetaStoreCore thriftHiveMetaStoreCore = new ThriftHiveMetaStoreCore(core);

  /**
   * Create a Thrift Hive Metastore service with a pre-created database "test_database".
   */
  public ThriftHiveMetaStoreJUnitRule() {
    this("test_database");
  }

  /**
   * Create a Thrift Hive Metastore service with a pre-created database using the provided name.
   *
   * @param databaseName Database name.
   */
  public ThriftHiveMetaStoreJUnitRule(String databaseName) {
    this(databaseName, null);
  }

  /**
   * Create a Thrift Hive Metastore service with a pre-created database using the provided name and configuration.
   *
   * @param databaseName Database name.
   * @param preConfiguration Hive configuration properties that will be set prior to BeeJU potentially overriding these
   *          with its defaults.
   */
  public ThriftHiveMetaStoreJUnitRule(String databaseName, Map<String, String> preConfiguration) {
    super(databaseName, preConfiguration);
  }

  /**
   * Create a Thrift Hive Metastore service with a pre-created database using the provided name and configuration.
   *
   * @param databaseName Database name.
   * @param preConfiguration Hive configuration properties that will be set prior to BeeJU potentially overriding these
   *          with its defaults.
   * @param postConfiguration Hive configuration properties that will be set to override BeeJU's defaults.
   */
  public ThriftHiveMetaStoreJUnitRule(
      String databaseName,
      Map<String, String> preConfiguration,
      Map<String, String> postConfiguration) {
    super(databaseName, preConfiguration, postConfiguration);
  }

  @Override
  public void starting(Description description) {
    System.clearProperty(CONNECT_URL_KEY.getVarname());
    try {
      thriftHiveMetaStoreCore.initialise();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    super.starting(description);
  }

  @Override
  public void finished(Description description) {
    try {
      thriftHiveMetaStoreCore.shutdown();
    } finally {
      super.finished(description);
    }
  }

  /**
   * @return {@link com.hotels.beeju.core.ThriftHiveMetaStoreCore#getThriftConnectionUri()}.
   */
  public String getThriftConnectionUri() {
    return thriftHiveMetaStoreCore.getThriftConnectionUri();
  }

  /**
   * @return {@link com.hotels.beeju.core.ThriftHiveMetaStoreCore#getThriftPort()}
   */
  public int getThriftPort() {
    return thriftHiveMetaStoreCore.getThriftPort();
  }

  /**
   * @param thriftPort The Port to use for the Thrift Hive metastore, if not set then a port number will automatically
   *          be allocated.
   */
  public void setThriftPort(int thriftPort) {
    thriftHiveMetaStoreCore.setThriftPort(thriftPort);
  }

}
