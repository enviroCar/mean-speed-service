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

import static org.junit.Assert.fail;

import java.io.IOException;

import org.envirocar.meanspeed.database.PostgreSQLDatabase;
import org.envirocar.meanspeed.mapmatching.MapMatchingResult;
import org.envirocar.meanspeed.model.FeatureCollection;
import org.envirocar.meanspeed.service.SegmentMetadataService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class TrackCountServiceTest {
    
//	@Autowired	
//	SegmentMetadataService trackCountService;

	@Autowired
	private PostgreSQLDatabase database;
	
	@Test
	public void testTrackCountService() {
				
		MapMatchingResult matchedTrack = null;		
        FeatureCollection track = null;	
		try {			
			JtsModule jtsModule =  new JtsModule();
						
			ObjectMapper objectMapper = new ObjectMapper()
	                .findAndRegisterModules()
	                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
	                .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
	                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
	                .registerModule(jtsModule);
			
			String trackName = "track.json";
			String matchedTrackName = "mapMatchingResult.json";
//			String trackName = "track_with_missing_speed_values.json";
//			String matched = "track_with_missing_speed_values_matched.json";
			
			matchedTrack = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream(matchedTrackName), MapMatchingResult.class);
			
			track = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream(trackName), FeatureCollection.class);
						
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		SegmentMetadataService segmentMetadataService = new SegmentMetadataService(database);
		
		segmentMetadataService.insertNewTrack2(matchedTrack, track);
		
		
//		Collection<OSMSegment> osmSegments = new OSMSegmentCreator().createOSMSSegments(matchedTrack, track);
//				
//		StopCalculator stopCalculator = new StopCalculator(new TrackParserImpl().createTrack(track));
//		
//		stopCalculator.calculateStops();
//		
//		System.out.println(stopCalculator.getStopCount());
//		
//		assertTrue(osmSegments != null);
//		assertTrue(osmSegments.size() > 0);
//				
//		OSMSegmentStopCalculator osmSegmentStopCalculator = new OSMSegmentStopCalculator(osmSegments);
//		
//		osmSegmentStopCalculator.calculateStops();
//		
////		System.out.println(osmSegmentStopCalculator.getStopCount());
//		
//		
////		trackCountService.insertNewTrack(matchedTrack, features);
		
	}
	
}
