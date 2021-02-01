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
package com.hotels.beeju.core;

import java.security.Permission;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;

public class HiveMetaStoreCore {

  private HiveMetaStoreClient client;
  private final BeejuCore beejuCore;

  public HiveMetaStoreCore(BeejuCore beejuCore) {
    this.beejuCore = beejuCore;
  }

  public void initialise() throws InterruptedException, ExecutionException {
    HiveConf hiveConf = new HiveConf(beejuCore.conf(), HiveMetaStoreClient.class);
    ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    try {
      client = singleThreadExecutor.submit(new CallableHiveClient(hiveConf)).get();
    } finally {
      singleThreadExecutor.shutdown();
    }
  }

  class NoExitSecurityManager extends SecurityManager {

    private boolean isExitAllowedFlag;

    public NoExitSecurityManager() {
      super();
      isExitAllowedFlag = false;
    }

    public boolean isExitAllowed() {
      return isExitAllowedFlag;
    }

    @Override
    public void checkExit(int status) {
      if (!isExitAllowed()) {
        throw new SecurityException();
      }
      super.checkExit(status);
    }

    public void setExitAllowed(boolean f) {
      isExitAllowedFlag = f;
    }
  }

  public void shutdown() {
    Policy.getPolicy();

    Policy allPermissionPolicy = new Policy() {
      @Override
      public boolean implies(ProtectionDomain domain, Permission permission) {
        return true;
      }
    };

    Policy.setPolicy(allPermissionPolicy);
    NoExitSecurityManager securityManager = new NoExitSecurityManager();
    System.setSecurityManager(securityManager);

    client.close();

    securityManager.setExitAllowed(true);
  }

  /**
   * @return the {@link HiveMetaStoreClient} backed by an HSQLDB in-memory database.
   */
  public HiveMetaStoreClient client() {
    return client;
  }
  public static class CallableHiveClient implements Callable<HiveMetaStoreClient> {

    private final HiveConf hiveConf;

    CallableHiveClient(HiveConf hiveConf) {
      this.hiveConf = hiveConf;
    }

    @Override
    public HiveMetaStoreClient call() throws Exception {
      return new HiveMetaStoreClient(hiveConf);
    }
  }
}
