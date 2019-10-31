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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Track {

    private final String id;
    private final LineString geometry;
    private final List<Measurement> measurements;

    public Track(String id, List<Measurement> measurements) {
        this.id = Objects.requireNonNull(id);
        this.measurements = new ArrayList<>(measurements);
        if (measurements.isEmpty()) {
            throw new IllegalArgumentException("no measurements");
        }
        this.geometry = calculateLineString();

    }

    private LineString calculateLineString() {
        GeometryFactory factory = this.measurements.iterator().next().getGeometry().getFactory();
        return factory.createLineString(this.measurements.stream().map(Measurement::getGeometry).map(Point.class::cast)
                                                         .map(Point::getCoordinate).toArray(Coordinate[]::new));
    }

    public List<Measurement> getMeasurements() {
        return Collections.unmodifiableList(measurements);
    }

    public Measurement getMeasurement(int idx) {
        return this.measurements.get(idx);
    }

    public int size() {
        return getMeasurements().size();
    }

    public String getId() {
        return id;
    }

    public LineString getGeometry() {
        return geometry;
    }

    public Envelope getEnvelope() {
        return geometry.getEnvelopeInternal();
    }

}
