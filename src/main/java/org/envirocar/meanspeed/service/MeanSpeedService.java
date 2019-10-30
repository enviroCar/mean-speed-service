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
	
	public MeanSpeedService() {
		postgreSQLDatabase = new PostgreSQLDatabase();
	}
	
	public void insertNewTrack(MapMatchingResult matchedTrack, FeatureCollection track) {
		
		List<MatchedPoint> matchedPoints = matchedTrack.getMatchedPoints();
		
		List<Long> osmIDList = new ArrayList<Long>();
		
		for (MatchedPoint matchedPoint : matchedPoints) {
			Long osmID = matchedPoint.getOsmId();
			if(osmIDList.contains(osmID)) {
				continue;
			}
			String measurementID = matchedPoint.getMeasurementId();
			double speed = getSpeed(measurementID, track);
			postgreSQLDatabase.updateSegmentMetadata(osmID, speed);
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
		
		LOG.info("Measurement id:" + measurementID);		
		
		return result;		
	}

	private JsonNode getProperty(String name, ObjectNode objectNode) {		
		return objectNode.get(name);
	}
	
}
