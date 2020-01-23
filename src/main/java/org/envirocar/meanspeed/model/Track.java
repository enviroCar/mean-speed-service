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
package org.envirocar.meanspeed.model;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.stream.IntStream;

import org.envirocar.meanspeed.configuration.JtsConfiguration;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public class Track {

    private final String id;
    private final LineString geometry;
    private final List<Measurement> measurements;
    private final String fuelType;

    public Track(String id, String fuelType, List<Measurement> measurements) {
        this.id = Objects.requireNonNull(id);
        this.fuelType = fuelType;
        if (measurements instanceof RandomAccess) {
            this.measurements = Objects.requireNonNull(measurements);
        } else {
            this.measurements = new ArrayList<>(measurements);
        }
        this.geometry = calculateLineString();
    }

    public String getFuelType() {
        return fuelType;
    }

    private LineString calculateLineString() {
        return getGeometry(0, size() - 1);
    }

    public Track subset(int begin, int end) {
        return new Track(getId(), getFuelType(), measurements.subList(begin, end + 1));
    }

    public List<Measurement> getMeasurements() {
        return Collections.unmodifiableList(measurements);
    }

    public Measurement getMeasurement(int idx) {
        return this.measurements.get(idx);
    }

    public Point getGeometry(int idx) {
        return this.measurements.get(idx).getGeometry();
    }

    public Instant getTime(int idx) {
        return getMeasurement(idx).getTime();
    }

    public double getSpeed(int idx) {
        return getMeasurement(idx).getValues().getSpeed();
    }

    public Values getValues(int idx) {
        return getMeasurement(idx).getValues();
    }

    public double getLength(int start, int end) {
        return getGeometry(start, end).getLength();
    }

    public LineString getGeometry(int start, int end) {
        return JtsConfiguration.geometryFactory().createLineString(IntStream.rangeClosed(start, end)
                                                                            .mapToObj(this::getMeasurement)
                                                                            .map(Measurement::getGeometry)
                                                                            .map(Point::getCoordinate)
                                                                            .toArray(Coordinate[]::new));
    }

    public Duration getExtendedDuration(int start, int end) {
        Temporal startTime = getTime(start);
        Temporal endTime = getTime(end);
        if (start > 0) {
            startTime = Duration.between(getTime(start - 1), startTime).dividedBy(2).subtractFrom(startTime);
        }
        if (end < size()) {
            endTime = Duration.between(endTime, getTime(end + 1)).dividedBy(2).addTo(endTime);
        }
        return Duration.between(startTime, endTime);
    }

    public Duration getDuration(int start, int end) {
        return Duration.between(getTime(start), getTime(end));
    }

    public int size() {
        return measurements.size();
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return getId();
    }

    public LineString getGeometry() {
        return geometry;
    }

    public Envelope getEnvelope() {
        return geometry.getEnvelopeInternal();
    }

}
