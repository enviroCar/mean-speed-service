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
package org.envirocar.meanspeed;

import com.fasterxml.jackson.databind.JsonNode;

import org.envirocar.meanspeed.model.Feature;
import org.envirocar.meanspeed.model.FeatureCollection;
import org.envirocar.meanspeed.model.Measurement;
import org.envirocar.meanspeed.model.Track;
import org.envirocar.meanspeed.model.Values;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class TrackParserImpl implements TrackParser {
    private static final String PHENOMENON_SPEED = "Speed";
    private static final String PHENOMENON_CONSUMPTION = "Consumption";
    private static final String PHENOMENON_CARBON_DIOXIDE = "CO2";

    @Override
    public Track createTrack(FeatureCollection collection) {

        String id = collection.getProperties().path(JsonConstants.ID).textValue();
        List<Measurement> measurements = collection.getFeatures().stream()
                                                   .map(this::createMeasurement)
                                                   .collect(toList());
        return new Track(id, measurements);
    }

    private Measurement createMeasurement(Feature feature) {
        Point geometry = (Point) feature.getGeometry();
        String id = feature.getProperties().path(JsonConstants.ID).textValue();
        Instant time = OffsetDateTime.parse(feature.getProperties().path(JsonConstants.TIME).textValue(),
                                            DateTimeFormatter.ISO_DATE_TIME).toInstant();
        JsonNode phenomenons = feature.getProperties().path(JsonConstants.PHENOMENONS);
        BigDecimal speed = phenomenons.get(PHENOMENON_SPEED).path(JsonConstants.VALUE).decimalValue();
        BigDecimal consumption = phenomenons.get(PHENOMENON_CONSUMPTION).path(JsonConstants.VALUE).decimalValue();
        BigDecimal carbonDioxide = phenomenons.get(PHENOMENON_CARBON_DIOXIDE).path(JsonConstants.VALUE).decimalValue();
        return new Measurement(id, geometry, time, new Values(speed, consumption, carbonDioxide));
    }

}
