package org.envirocar.trackcount.service;

import java.util.ArrayList;
import java.util.List;

import org.envirocar.trackcount.database.PostgreSQLDatabase;
import org.envirocar.trackcount.mapmatching.MapMatchingResult;
import org.envirocar.trackcount.mapmatching.MatchedPoint;

public class TrackCountService {

	PostgreSQLDatabase postgreSQLDatabase;
	
	public TrackCountService() {
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
