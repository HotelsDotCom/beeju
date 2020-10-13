/**
 * Copyright (C) 2015-2020 Expedia, Inc.
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

import java.util.concurrent.CountDownLatch;

public class ThriftHiveMetaStoreApp {

  public static void main(String[] args) throws Throwable {
    ThriftHiveMetaStoreJUnitRule rule = new ThriftHiveMetaStoreJUnitRule();
    rule.setThriftPort(22334);
    rule.before();
    System.out.println("BeeJU Thrift Hive Metastore listening on: " + rule.getThriftConnectionUri());
    CountDownLatch latch = new CountDownLatch(1);
    latch.await();
  }

}
