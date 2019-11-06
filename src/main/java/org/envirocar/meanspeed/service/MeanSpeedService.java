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
package org.envirocar.meanspeed.service;

import java.util.ArrayList;
import java.util.List;

import org.envirocar.meanspeed.database.PostgreSQLDatabase;
import org.envirocar.meanspeed.mapmatching.MapMatchingResult;
import org.envirocar.meanspeed.mapmatching.MatchedPoint;
import org.envirocar.meanspeed.model.Feature;
import org.envirocar.meanspeed.model.FeatureCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class MeanSpeedService {
	
	private static final Logger LOG = LoggerFactory.getLogger(MeanSpeedService.class);
	private static final String ID_NAME = "id";
	private static final String GPS_SPEED_NAME = "GPS Speed";
	private static final String PHENOMENONS_NAME = "phenomenons";
	private static final String VALUE_NAME = "value";
	PostgreSQLDatabase postgreSQLDatabase;
	
	public MeanSpeedService(PostgreSQLDatabase postgreSQLDatabase) {
		this.postgreSQLDatabase = postgreSQLDatabase;
	}
	
	public void insertNewTrack(MapMatchingResult matchedTrack, FeatureCollection track) {
		
		List<MatchedPoint> matchedPoints = matchedTrack.getMatchedPoints();
		
		List<Long> osmIDList = new ArrayList<Long>();
		
		for (MatchedPoint matchedPoint : matchedPoints) {
			Long osmID = matchedPoint.getOsmId();
			String measurementID = null;
			double speed = Double.NaN;
			try {
				measurementID = matchedPoint.getMeasurementId();
				speed = getSpeed(measurementID, track);
				postgreSQLDatabase.updateSegmentSpeed(osmID, speed);				
			} catch (Exception e) {
				LOG.error("Could not add measurment with id {} and speed {}.", measurementID, speed);
			}
			
			if(osmIDList.contains(osmID)) {
				continue;
			}
			postgreSQLDatabase.updateSegmentTrackCount(osmID);
			osmIDList.add(osmID);
		}
		
	}
	
	private double getSpeed(String measurementID, FeatureCollection track) {
		
		double result = -1d;
		
		List<Feature> featureList = track.getFeatures();
		
		for (Feature feature : featureList) {
			ObjectNode properties = feature.getProperties();
			String id = getProperty(ID_NAME, properties).asText().trim();
			if(id.equals(measurementID.trim())){
				JsonNode phenomenons = getProperty(PHENOMENONS_NAME, properties);
				JsonNode gpsSpeedNode = getProperty(GPS_SPEED_NAME, (ObjectNode) phenomenons);
				result = getProperty(VALUE_NAME, (ObjectNode) gpsSpeedNode).asDouble();
				return result;
			}
		}
		
		LOG.trace("Measurement id:" + measurementID);		
		
		return result;		
	}

	private JsonNode getProperty(String name, ObjectNode objectNode) {		
		return objectNode.get(name);
	}

	public PostgreSQLDatabase getPostgreSQLDatabase() {
		return postgreSQLDatabase;
	}
	
}
