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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class RemoteFabricClient {
    static final int DEFAULT_TIMEOUT = 60 * 3; // 3 mins
    private final File trustStoreFile;
    private final String trustStorePassword;
    private int timeoutSecs = DEFAULT_TIMEOUT;

    public RemoteFabricClient(File trustStoreFile, String trustStorePassword) {
        this.trustStoreFile = trustStoreFile;
        this.trustStorePassword = trustStorePassword;
    }

    public List<Map<String, Object>> JSONBodyAsMap(URI uri, String username, String password) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setConnectTimeout(timeoutSecs * 1000);
        conn.setReadTimeout(timeoutSecs * 1000);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("Authorization",
                "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
        conn.setRequestProperty("Accept", "application/json");
        try {
            if (conn instanceof HttpsURLConnection) {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                if (trustStoreFile == null) {
                    System.err.println("Trustore file not configured, accepting all certificates");
                    TrustManager tm = new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    };
                    sslContext.init(null, new TrustManager[]{tm}, null);
                } else {
                    try (FileInputStream keyStoreIn = new FileInputStream(trustStoreFile)) {
                        TrustManagerFactory trustMgrFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                        keyStore.load(keyStoreIn, trustStorePassword.toCharArray());
                        trustMgrFactory.init(keyStore);
                        sslContext.init(null, trustMgrFactory.getTrustManagers(), null);
                    }
                }
                HttpsURLConnection tlsConn = (HttpsURLConnection) conn;
                tlsConn.setSSLSocketFactory(sslContext.getSocketFactory());
                tlsConn.setHostnameVerifier((hostname, session) -> true);
            }
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Security error while unlocking truststore: " + e, e);
        }
        ObjectMapper mapper = new ObjectMapper();
        try (BufferedReader rd = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            return mapper.readValue(rd, new TypeReference<List<Map<String, Object>>>() {
            });
        }
    }
}
