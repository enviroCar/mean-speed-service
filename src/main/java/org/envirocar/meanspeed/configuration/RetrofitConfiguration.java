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

import com.fasterxml.jackson.databind.ObjectMapper;

//import java.time.Duration;
import okhttp3.OkHttpClient;

import org.envirocar.meanspeed.mapmatching.MapMatchingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.net.URL;
import java.time.Duration;

@Configuration
public class RetrofitConfiguration {

    @Bean
    public Retrofit retrofit(JacksonConverterFactory factory, OkHttpClient client, @Value("${trackcount.mapMatching.url}") URL url) {
        return new Retrofit.Builder().addConverterFactory(factory).client(client).baseUrl(url).build();
    }

    @Bean
    public JacksonConverterFactory jacksonConverterFactory(ObjectMapper mapper) {
        return JacksonConverterFactory.create(mapper);
    }

    @Bean
    public MapMatchingService mapMatchingService(Retrofit retrofit, @Value("${trackcount.mapMatching.url}") URL url) {
        return retrofit.newBuilder().baseUrl(url).build().create(MapMatchingService.class);
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                       .followRedirects(true)
                       .followSslRedirects(true).callTimeout(Duration.ofMillis(30000))
                       .connectTimeout(Duration.ofMillis(30000)).readTimeout(Duration.ofMillis(30000))
//                     .addInterceptor(new LoggingInterceptor())
                       .build();
    }

}
