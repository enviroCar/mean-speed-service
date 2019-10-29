package org.envirocar.meanspeed.service;

import java.util.ArrayList;
import java.util.List;

import org.envirocar.meanspeed.database.PostgreSQLDatabase;
import org.envirocar.meanspeed.mapmatching.MapMatchingResult;
import org.envirocar.meanspeed.mapmatching.MatchedPoint;

public class MeanSpeedService {

	PostgreSQLDatabase postgreSQLDatabase;
	
	public MeanSpeedService() {
		postgreSQLDatabase = new PostgreSQLDatabase();
	}
	
	public void insertNewTrack(MapMatchingResult matchedTrack) {
		
		List<MatchedPoint> matchedPoints = matchedTrack.getMatchedPoints();
		
		List<Long> osmIDList = new ArrayList<Long>();
		
		for (MatchedPoint matchedPoint : matchedPoints) {
			Long osmID = matchedPoint.getOsmId();
			if(osmIDList.contains(osmID)) {
				continue;
			}
			postgreSQLDatabase.increaseTrackCount(osmID);
			osmIDList.add(osmID);
		}
		
	}
	
}
