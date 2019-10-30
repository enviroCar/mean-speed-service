package org.envirocar.qad;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.envirocar.meanspeed.mapmatching.MapMatchingResult;
import org.envirocar.meanspeed.model.FeatureCollection;
import org.envirocar.meanspeed.service.MeanSpeedService;
import org.junit.Test;
import org.n52.jackson.datatype.jts.JtsModule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TrackCountServiceTest {
	
	@Test
	public void testTrackOcuntService() {
		
		MeanSpeedService trackCountService = new MeanSpeedService();
		
		MapMatchingResult matchedTrack = null;		
        FeatureCollection features = null;	
		try {			
			JtsModule jtsModule =  new JtsModule();
						
			ObjectMapper objectMapper = new ObjectMapper()
	                .findAndRegisterModules()
	                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
	                .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
	                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
	                .registerModule(jtsModule);
			
			matchedTrack = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("mapMatchingResult.json"), MapMatchingResult.class);
			
			features = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("track.json"), FeatureCollection.class);
			
		} catch (IOException e) {
			fail(e.getMessage());
		}
		trackCountService.insertNewTrack(matchedTrack, features);
		
	}
	
}
