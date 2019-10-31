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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import org.envirocar.meanspeed.JsonConstants;
import org.envirocar.meanspeed.model.Feature;

public class MatchedPoint {
    private Long osmId;
    private String measurementId;
    private String streetName;
    private Feature unmatchedPoint;
    private Feature pointOnRoad;

    @JsonGetter(JsonConstants.OSM_ID)
    public Long getOsmId() {
        return osmId;
    }

    @JsonSetter(JsonConstants.OSM_ID)
    public void setOsmId(Long osmId) {
        this.osmId = osmId;
    }

    @JsonGetter(JsonConstants.MEASUREMENT_ID)
    public String getMeasurementId() {
        return measurementId;
    }

    @JsonSetter(JsonConstants.MEASUREMENT_ID)
    public void setMeasurementId(String measurementId) {
        this.measurementId = measurementId;
    }

    @JsonGetter(JsonConstants.STREET_NAME)
    public String getStreetName() {
        return streetName;
    }

    @JsonSetter(JsonConstants.STREET_NAME)
    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    @JsonGetter(JsonConstants.UNMATCHED_POINT)
    public Feature getUnmatchedPoint() {
        return unmatchedPoint;
    }

    @JsonSetter(JsonConstants.UNMATCHED_POINT)
    public void setUnmatchedPoint(Feature unmatchedPoint) {
        this.unmatchedPoint = unmatchedPoint;
    }

    @JsonGetter(JsonConstants.POINT_ON_ROAD)
    public Feature getPointOnRoad() {
        return pointOnRoad;
    }

    @JsonSetter(JsonConstants.POINT_ON_ROAD)
    public void setPointOnRoad(Feature pointOnRoad) {
        this.pointOnRoad = pointOnRoad;
    }
}
