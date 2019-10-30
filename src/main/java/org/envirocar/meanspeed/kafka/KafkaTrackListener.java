package org.envirocar.meanspeed.kafka;

import org.envirocar.meanspeed.JsonConstants;
import org.envirocar.meanspeed.mapmatching.MapMatchingResult;
import org.envirocar.meanspeed.mapmatching.MapMatchingService;
import org.envirocar.meanspeed.model.FeatureCollection;
import org.envirocar.meanspeed.service.MeanSpeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import retrofit2.Call;
import retrofit2.Response;

@Service
public class KafkaTrackListener {

	private static final Logger LOG = LoggerFactory.getLogger(KafkaTrackListener.class);

	private MeanSpeedService trackCountService;
	
	private MapMatchingService mapMatcher;
	
    @Autowired
    public KafkaTrackListener(MeanSpeedService service, MapMatchingService mapMatcher) {
    	this.trackCountService = service;
    	this.mapMatcher = mapMatcher;
    }

    @KafkaListener(topics = "tracks")
    public void onNewTrack(FeatureCollection track) {
    	
        String id = track.getProperties().path(JsonConstants.ID).textValue();
        
        LOG.info("Received track {}", id);
        
		MapMatchingResult matchedTrack = null;
		try {
			Call<MapMatchingResult> result = mapMatcher.mapMatch(track);
			Response<MapMatchingResult> response = result.execute();
			matchedTrack = response.body();
		} catch (Exception e) {
			LOG.error("Could not match track to map.", e);
			return;
		}
        
		trackCountService.insertNewTrack(matchedTrack, track);
		
        LOG.info("Inserted track {}", id);
    }
}
