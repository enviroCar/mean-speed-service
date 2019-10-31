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
package org.envirocar.meanspeed.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class KafkaJsonDeserializer<T> implements KafkaDeserializer<T> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaJsonDeserializer.class);
    private final Class<T> type;
    private final ObjectMapper objectMapper;

    public KafkaJsonDeserializer(Class<T> type, ObjectMapper objectMapper) {
        this.type = Objects.requireNonNull(type);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public T deserialize(String s, byte[] bytes) {
        try {
        	File file = File.createTempFile("tmp", ".json");
        	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        	bufferedWriter.write(new String(bytes));
        	bufferedWriter.close();
        	LOG.info(file.getAbsolutePath());
            return objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            LOG.error("Error reading " + type, e);
        }
        return null;
    }
}
