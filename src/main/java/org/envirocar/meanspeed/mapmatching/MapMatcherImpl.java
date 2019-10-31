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
package org.envirocar.meanspeed.mapmatching;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.envirocar.meanspeed.model.Feature;
import org.envirocar.meanspeed.model.FeatureCollection;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import retrofit2.Call;
import retrofit2.Response;

@Service
public class MapMatcherImpl implements MapMatcher {
    private final MapMatchingService service;

    @Autowired
    public MapMatcherImpl(MapMatchingService service) {
        this.service = Objects.requireNonNull(service);
    }

    @Override
    public FeatureCollection mapMatch(FeatureCollection featureCollection) throws MapMatchingException {

        Call<MapMatchingResult> result = service.mapMatch(featureCollection);
        Response<MapMatchingResult> response = null;
		try {
			response = result.execute();
		} catch (IOException e) {
			return new FeatureCollection();
		}
        List<Geometry> geometries = response.body().getMatchedPoints().stream()
                                          .map(MatchedPoint::getPointOnRoad)
                                          .map(Feature::getGeometry)
                                          .collect(toList());

        List<Feature> features = featureCollection.getFeatures();
        if (geometries.size() != features.size()) {
            throw new MapMatchingException(String.format("service returned wrong number of geometries, expected %d but was %d",
                                                         features.size(), geometries.size()));
        }

        IntStream.range(0, features.size()).forEach(i -> features.get(i).setGeometry(geometries.get(i)));
        return featureCollection;
    }

}
