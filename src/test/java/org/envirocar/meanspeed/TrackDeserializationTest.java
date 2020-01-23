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
package org.envirocar.meanspeed;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.envirocar.meanspeed.kafka.KafkaJsonDeserializer;
import org.envirocar.meanspeed.model.FeatureCollection;
import org.envirocar.meanspeed.model.Track;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = {KafkaConfiguration.class})
public class TrackDeserializationTest {
	
	@Test
	public void testDeserialization() throws IOException {
		
		JtsModule jtsModule =  new JtsModule();
		
		JsonFactory jf = new JsonFactory();
		
		ObjectMapper objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .registerModule(jtsModule);
		
        StringWriter writer = new StringWriter();
        String encoding = StandardCharsets.UTF_8.name();
        IOUtils.copy(getClass().getClassLoader().getResourceAsStream("track.json"), writer, encoding);
        
		byte[] bytes = writer.toString().getBytes();
		
		FeatureCollection featureCollection = new KafkaJsonDeserializer<>(FeatureCollection.class, objectMapper).deserialize("", bytes);
		
		assertTrue(featureCollection != null);
		
		Track track = new TrackParserImpl().createTrack(featureCollection);
		
		assertTrue(track.getFuelType() != null);
	
	}
	
}
