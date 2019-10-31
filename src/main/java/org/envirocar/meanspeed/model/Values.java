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

import java.math.BigDecimal;

import org.envirocar.meanspeed.Interpolate;

public class Values {
    private BigDecimal speed;
    private BigDecimal consumption;
    private BigDecimal carbonDioxide;

    public Values(BigDecimal speed, BigDecimal consumption, BigDecimal carbonDioxide) {
        this.speed = speed;
        this.consumption = consumption;
        this.carbonDioxide = carbonDioxide;
    }

    public BigDecimal getSpeed() {
        return speed;
    }

    public BigDecimal getConsumption() {
        return consumption;
    }

    public BigDecimal getCarbonDioxide() {
        return carbonDioxide;
    }

    public static Values interpolate(Values v1, Values v2, BigDecimal fraction) {
        BigDecimal speed = Interpolate.linear(v1.getSpeed(), v2.getSpeed(), fraction);
        BigDecimal consumption = Interpolate.linear(v1.getConsumption(), v2.getConsumption(), fraction);
        BigDecimal carbonDioxide = Interpolate.linear(v1.getCarbonDioxide(), v2.getCarbonDioxide(), fraction);
        return new Values(speed, consumption, carbonDioxide);
    }

}
