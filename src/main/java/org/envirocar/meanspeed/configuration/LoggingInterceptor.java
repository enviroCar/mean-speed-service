/**
 * Copyright (C) 2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package org.envirocar.meanspeed.configuration;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class LoggingInterceptor implements Interceptor {
    private final Logger log;

    public LoggingInterceptor(Logger log) {
        this.log = Objects.requireNonNull(log);
    }

    public LoggingInterceptor() {
        this(LoggerFactory.getLogger("okhttp3"));
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Instant before = Instant.now();
        Request request = chain.request();
        Response response = chain.proceed(request);
        Instant after = Instant.now();

        if (response.isSuccessful()) {
            log.debug("{} {}: {} {}",
                      request.method(),
                      request.url(),
                      response.code(),
                      Duration.between(before, after));
        } else {
            log.warn("{} {}: {} {}",
                     request.method(),
                     request.url(),
                     response.code(),
                     Duration.between(before, after));
        }
        return response;
    }
}
