package org.envirocar.segmentmetadata.stopcalculation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.envirocar.meanspeed.mapmatching.MapMatchingResult;
import org.envirocar.meanspeed.mapmatching.MatchedPoint;
import org.envirocar.meanspeed.model.Feature;
import org.envirocar.meanspeed.model.FeatureCollection;
import org.envirocar.meanspeed.model.Measurement;
import org.envirocar.meanspeed.model.OSMSegment;
import org.envirocar.meanspeed.model.Values;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OSMSegmentCreator {
	
	private static final Logger LOG = LoggerFactory.getLogger(OSMSegmentCreator.class);
	private static final String ID_NAME = "id";
	private static final String GPS_SPEED_NAME = "GPS Speed";
	private static final String CO2_NAME = "CO2";
	private static final String CONSUMPTION_NAME = "Consumption";
	private static final String PHENOMENONS_NAME = "phenomenons";
	private static final String VALUE_NAME = "value";
	private static final String TIME_NAME = "time";
	private static final String GEOMETRY_NAME = "geometry";
	private List<OSMSegment> osmSegments;
	
	public Collection<OSMSegment> createOSMSSegments(MapMatchingResult matchedTrack, FeatureCollection track){
		
		Map<String, OSMSegment> result = new HashMap<>();
		
		List<MatchedPoint> matchedPoints = matchedTrack.getMatchedPoints();
		
		List<String> osmIDList = new ArrayList<String>();
		
		OSMSegment osmSegment;
		
		for (MatchedPoint matchedPoint : matchedPoints) {
			Long osmID = matchedPoint.getOsmId();
			Long gId = matchedPoint.getGId();
			String full_Id = osmID + "_" + gId;
			String measurementID = null;
			double speed = -1d;
			double co2 = -1d;
			double consumption = -1d;
			Instant time = null;
			measurementID = matchedPoint.getMeasurementId();
			
			List<Feature> featureList = track.getFeatures();
			
			for (Feature feature : featureList) {
				ObjectNode properties = feature.getProperties();
				String id = getProperty(ID_NAME, properties).asText().trim();
				if(id.equals(measurementID.trim())){
					try {
						time = Instant.parse(getProperty(TIME_NAME, properties).asText());
					} catch (Exception e) {
						LOG.error("Could not parse time for measurement: " + measurementID);
					}
					JsonNode phenomenons = getProperty(PHENOMENONS_NAME, properties);
					try {
						speed = getProperty(VALUE_NAME, (ObjectNode) getProperty(GPS_SPEED_NAME, (ObjectNode) phenomenons)).asDouble();
					} catch (Exception e) {
						LOG.error("Could not parse speed for measurement: " + measurementID);
					}
					try {
						co2 = getProperty(VALUE_NAME, (ObjectNode) getProperty(CO2_NAME, (ObjectNode) phenomenons)).asDouble();
					} catch (Exception e) {
						LOG.error("Could not parse co2 for measurement: " + measurementID);
					}
					try {
						consumption = getProperty(VALUE_NAME, (ObjectNode) getProperty(CONSUMPTION_NAME, (ObjectNode) phenomenons)).asDouble();						
					} catch (Exception e) {
						LOG.error("Could not parse consumption for measurement: " + measurementID);
					}
				}
			}
			
			if(osmIDList.contains(full_Id)) {
				osmSegment = result.get(full_Id);
			} else {
				osmSegment = new OSMSegment(osmID, gId);
				osmIDList.add(full_Id);
			}
			osmSegment.addMeasurement(new Measurement(measurementID, (Point) matchedPoint.getUnmatchedPoint().getGeometry(), time, new Values(speed, consumption, co2)));
			result.put(full_Id, osmSegment);
		}
		
		return result.values();
	}
		
	private double getDoubleValue(String measurementID, FeatureCollection track, String valueName) {
		
		double result = -1d;
		
		List<Feature> featureList = track.getFeatures();
		
		for (Feature feature : featureList) {
			ObjectNode properties = feature.getProperties();
			String id = getProperty(ID_NAME, properties).asText().trim();
			if(id.equals(measurementID.trim())){
				JsonNode phenomenons = getProperty(PHENOMENONS_NAME, properties);
				JsonNode valueNode = getProperty(valueName, (ObjectNode) phenomenons);
				result = getProperty(VALUE_NAME, (ObjectNode) valueNode).asDouble();
				return result;
			}
		}
		
		LOG.trace("Measurement id:" + measurementID);
		
		return result;
	}

	private JsonNode getProperty(String name, ObjectNode objectNode) {		
		return objectNode.get(name);
	}
	
	private Instant getTime(String measurementID, FeatureCollection track) {
		
		Instant result = null;
		
		String time = "";
		
		List<Feature> featureList = track.getFeatures();
		
		for (Feature feature : featureList) {
			ObjectNode properties = feature.getProperties();
			String id = getProperty(ID_NAME, properties).asText().trim();
			if(id.equals(measurementID.trim())){
				time = getProperty(TIME_NAME, properties).asText();
			}
		}
		
		result = Instant.parse(time);
		
		return result;		
	}
	
}
