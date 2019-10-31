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
