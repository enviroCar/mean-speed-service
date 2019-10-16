package org.envirocar.qad;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.envirocar.trackcount.mapmatching.MapMatchingResult;
import org.envirocar.trackcount.service.TrackCountService;
import org.junit.Test;
import org.n52.jackson.datatype.jts.JtsModule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TrackCountServiceTest {
	
	@Test
	public void testTrackOcuntService() {
		
		TrackCountService trackCountService = new TrackCountService();
		
		MapMatchingResult matchedTrack = null;
		try {			
			JtsModule jtsModule =  new JtsModule();
						
			ObjectMapper objectMapper = new ObjectMapper()
	                .findAndRegisterModules()
	                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
	                .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
	                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
	                .registerModule(jtsModule);
			
			matchedTrack = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("mapMatchingResult.json"), MapMatchingResult.class);
		} catch (IOException e) {
			fail(e.getMessage());
		}
				
		trackCountService.insertNewTrack(matchedTrack);
		
	}
	
}
