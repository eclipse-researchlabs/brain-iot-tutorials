/*-
 * #%L
 * com.paremus.ui.rest.app2
 * %%
 * Copyright (C) 2018 - 2019 Paremus Ltd
 * %%
 * Licensed under the Fair Source License, Version 0.9 (the "License");
 *
 * See the NOTICE.txt file distributed with this work for additional
 * information regarding copyright ownership. You may not use this file
 * except in compliance with the License. For usage restrictions see the
 * LICENSE.txt file distributed with this work
 * #L%
 */
package com.paremus.brain.iot.ui.rest.app;

import com.paremus.ui.rest.dto.IdentityDTO;

import java.util.Map;

public class HostDTO extends IdentityDTO {

    public String fwId;

    public Map<String, String> behaviours;

    /**
     * system info:
     * <pre>
     *     cpu_id: Intel64 Family 6 Model 70 Stepping 1
     *     cpu_name: Intel(R) Core(TM) i7-4980HQ CPU @ 2.80GHz
     *     disk: 59.6 GiB // largest disk
     *     disk0: /dev/sda 59.6 GiB
     *     disk1: etc
     *     memory: 7.8 GiB
     *     model: Apple Inc.[MacBookPro11,3]
     *     os: GNU/Linux Ubuntu 18.04.3 LTS (Bionic Beaver) build 4.9.184-linuxkit
     * </pre>
     */
    public Map<String, String> info;

    /**
     * uninstall is required on PUT. It should be a key from the behaviours map, or "ALL" to reset host.
     */
    public String uninstall;

}
