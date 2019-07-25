/**
 * Copyright (C) 2015-2019 Expedia, Inc.
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
package com.hotels.beeju.core;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.thrift.TException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class BeejuCore {

  // "user" conflicts with USER db and the metastore_db can't be created.
  public static final String METASTORE_DB_USER = "db_user";
  public static final String METASTORE_DB_PASSWORD = "db_password";

  protected final HiveConf conf = new HiveConf();
  private final String databaseName;
  private final String connectionURL;
  private final String driverClassName;

  public BeejuCore(String databaseName, Map<String, String> configuration){
    checkNotNull(databaseName, "databaseName is required");
    this.databaseName = databaseName;

    if (configuration != null && !configuration.isEmpty()) {
      for (Map.Entry<String, String> entry : configuration.entrySet()) {
        conf.set(entry.getKey(), entry.getValue());
      }
    }

    driverClassName = EmbeddedDriver.class.getName();
    conf.setBoolean("hcatalog.hive.client.cache.disabled", true);
    connectionURL = "jdbc:derby:memory:" + UUID.randomUUID() + ";create=true";
    conf.setVar(HiveConf.ConfVars.METASTORECONNECTURLKEY, connectionURL);
    conf.setVar(HiveConf.ConfVars.METASTORE_CONNECTION_DRIVER, driverClassName);
    conf.setVar(HiveConf.ConfVars.METASTORE_CONNECTION_USER_NAME, METASTORE_DB_USER);
    conf.setVar(HiveConf.ConfVars.METASTOREPWD, METASTORE_DB_PASSWORD);
    conf.setBoolVar(HiveConf.ConfVars.HMSHANDLERFORCERELOADCONF, true);
    // Hive 2.x compatibility
    conf.setBoolean("datanucleus.schema.autoCreateAll", true);
    conf.setBoolean("hive.metastore.schema.verification", false);
    // override default port as some of our test environments claim it is in use.
    conf.setInt("hive.server2.webui.port", 0); // ConfVars.HIVE_SERVER2_WEBUI_PORT
    try {
      // overriding default derby log path to go to tmp
      String derbyLog = File.createTempFile("derby", ".log").getCanonicalPath();
      System.setProperty("derby.stream.error.file", derbyLog);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setHiveVar(HiveConf.ConfVars variable, String value){
    conf.setVar(variable, value);
  }

  public void setHiveIntVar(HiveConf.ConfVars variable, int value){
    conf.setIntVar(variable, value);
  }

  /**
   * Create a new database with the specified name.
   *
   * @param databaseName Database name.
   * @throws TException If an error occurs creating the database.
   */
  public void createDatabase(String databaseName, File tempFile, HiveConf conf) throws TException {
    HiveMetaStoreClient client = new HiveMetaStoreClient(new HiveConf(conf));
    String databaseFolder = new File(tempFile, databaseName).toURI().toString();
    try {
      client.createDatabase(new Database(databaseName, null, databaseFolder, null));
    } finally {
      client.close();
    }
  }

  /**
   * @return a copy of the {@link HiveConf} used to create the Hive Metastore database. This {@link HiveConf} should be
   *         used by tests wishing to connect to the database.
   */
  public HiveConf conf() {
    return new HiveConf(conf);
  }

  /**
   * @return the name of the pre-created database.
   */
  public String databaseName() {
    return databaseName;
  }

  /**
   * @return the name of the JDBC driver class used to access the database.
   */
  public String driverClassName() {
    return driverClassName;
  }

  /**
   * @return the JDBC connection URL to the HSQLDB in-memory database.
   */
  public String connectionURL() {
    return connectionURL;
  }

}
