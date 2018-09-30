/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.routon.smartcampus.studentcard;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
//    public static String TST_VCOM = "0000fff1-0000-1000-8000-00805f9b34fb";
//    public static String TST_VCOMService = "0000fff0-0000-1000-8000-00805f9b34fb";
  public static String TST_VCOMService = "f000ffd0-0451-4000-b000-000000000000";
  public static String TST_VCOM = "f000ffd1-0451-4000-b000-000000000000";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access Profile");
        attributes.put("0000fff0-0000-1000-8000-00805f9b34fb", "TST Serial Service");
        // Sample Characteristics.
        attributes.put(TST_VCOM, "TST SerialPort");
        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Primary Service");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
