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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.envirocar.meanspeed.database.PostgreSQLDatabase;
import org.envirocar.meanspeed.mapmatching.MapMatchingResult;
import org.envirocar.meanspeed.mapmatching.MatchedPoint;
import org.envirocar.meanspeed.model.Feature;
import org.envirocar.meanspeed.model.FeatureCollection;
import org.envirocar.meanspeed.model.Measurement;
import org.envirocar.meanspeed.model.OSMSegment;
import org.envirocar.meanspeed.model.Values;
import org.envirocar.segmentmetadata.stopcalculation.OSMSegmentCreator;
import org.envirocar.segmentmetadata.stopcalculation.OSMSegmentStopCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class SegmentMetadataService {
	
	private static final Logger LOG = LoggerFactory.getLogger(SegmentMetadataService.class);
	private static final String ID_NAME = "id";
	private static final String GPS_SPEED_NAME = "GPS Speed";
	private static final String CO2_NAME = "CO2";
	private static final String CONSUMPTION_NAME = "Consumption";
	private static final String PHENOMENONS_NAME = "phenomenons";
	private static final String VALUE_NAME = "value";
	PostgreSQLDatabase postgreSQLDatabase;
	
	public SegmentMetadataService(PostgreSQLDatabase postgreSQLDatabase) {
		this.postgreSQLDatabase = postgreSQLDatabase;
	}
	
	public void insertNewTrack(MapMatchingResult matchedTrack, FeatureCollection track) {
		
		List<MatchedPoint> matchedPoints = matchedTrack.getMatchedPoints();
		
		List<Long> osmIDList = new ArrayList<Long>();
				
		for (MatchedPoint matchedPoint : matchedPoints) {
			Long osmID = matchedPoint.getOsmId();
			Long gId = matchedPoint.getGId();
			String measurementID = null;
			double speed = Double.NaN;
			double co2 = Double.NaN;
			double consumption = Double.NaN;
			measurementID = matchedPoint.getMeasurementId();
			try {
				speed = getValue(measurementID, track, GPS_SPEED_NAME);
				postgreSQLDatabase.updateSegmentSpeed(osmID, gId, speed);
			} catch (Exception e) {
				LOG.error("Could not add measurement with id {} and speed {}.", measurementID, speed);
			}
			try {
				co2 = getValue(measurementID, track, CO2_NAME);
				postgreSQLDatabase.updateSegmentCo2(osmID, gId, co2);
			} catch (Exception e) {
				LOG.error("Could not add measurement with id {} and co2 {}.", measurementID, co2);
			}
			try {
				consumption = getValue(measurementID, track, CONSUMPTION_NAME);
				postgreSQLDatabase.updateSegmentConsumption(osmID, gId, consumption);
			} catch (Exception e) {
				LOG.error("Could not add measurement with id {} and consumption {}.", measurementID, consumption);
			}
			
			if(osmIDList.contains(osmID)) {
				continue;
			}
			postgreSQLDatabase.updateSegmentTrackCount(osmID, gId);
			osmIDList.add(osmID);
		}
		
	}
	
	public void insertNewTrack2(MapMatchingResult matchedTrack, FeatureCollection track) {
		
		List<String> osmIDList = new ArrayList<String>();
		
		Collection<OSMSegment> osmSegments = new OSMSegmentCreator().createOSMSSegments(matchedTrack, track);
		
		Iterator<OSMSegment> osmSegmentIterator = osmSegments.iterator();
		
		OSMSegmentStopCalculator osmSegmentStopCalculator = new OSMSegmentStopCalculator();
		
		while (osmSegmentIterator.hasNext()) {
			OSMSegment osmSegment = (OSMSegment) osmSegmentIterator.next();

			Long osmID = osmSegment.getId();
			Long gId = osmSegment.getGId();
			String full_Id = osmID + "_" + gId;
			String measurementID = null;
			double speed = -1d;
			double co2 = -1d;
			double consumption = -1d;
			int stops = -1;
			
			List<Measurement> measurements = osmSegment.getMeasurements();
			
		    for (Measurement measurement : measurements) {
				
				measurementID = measurement.getId();
				
				Values values = measurement.getValues();
				
				try {
					speed = values.getSpeed();
					if(speed != -1d) {
					    postgreSQLDatabase.updateSegmentSpeed(osmID, gId, speed);
					}
				} catch (Exception e) {
					LOG.error("Could not add measurement with id {} and speed {}.", measurementID, speed);
				}
				try {
					co2 = values.getCarbonDioxide();
					if(co2 != -1d) {
					    postgreSQLDatabase.updateSegmentCo2(osmID, gId, co2);
					}
				} catch (Exception e) {
					LOG.error("Could not add measurement with id {} and co2 {}.", measurementID, co2);
				}
				try {
				    consumption = values.getConsumption();
					if(consumption != -1d) {
						postgreSQLDatabase.updateSegmentConsumption(osmID, gId, consumption);
					}
				} catch (Exception e) {
					LOG.error("Could not add measurement with id {} and consumption {}.", measurementID, consumption);
				}
			}
			
		    try {
				stops = osmSegmentStopCalculator.calculateStops(measurements);
				if(stops > 0) {
				    postgreSQLDatabase.updateSegmentStops(osmID, gId, stops);
				}
			} catch (Exception e) {
				LOG.error("Could not calculate stops add measurement with id {} and consumption {}.", measurementID, consumption);
			}
		    
			if(osmIDList.contains(full_Id)) {
				continue;
			}
			postgreSQLDatabase.updateSegmentTrackCount(osmID, gId);
			osmIDList.add(full_Id);
			
		}
		
	}
	
	private double getValue(String measurementID, FeatureCollection track, String valueName) {
		
		double result = -1d;
		
		List<Feature> featureList = track.getFeatures();
		
		for (Feature feature : featureList) {
			ObjectNode properties = feature.getProperties();
			String id = getProperty(ID_NAME, properties).asText().trim();
			if(id.equals(measurementID.trim())){
				JsonNode phenomenons = getProperty(PHENOMENONS_NAME, properties);
				JsonNode gpsSpeedNode = getProperty(valueName, (ObjectNode) phenomenons);
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
