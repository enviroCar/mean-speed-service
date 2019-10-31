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

import java.math.BigDecimal;
import java.time.Instant;

public class Interpolate {

    public static Instant linear(Instant v0, Instant v1, BigDecimal fraction) {
        return BigDecimals
                       .toInstant(linear(BigDecimals.create(v0), BigDecimals
                                                                                            .create(v1), fraction));
    }

    public static BigDecimal linear(BigDecimal v0, BigDecimal v1, BigDecimal fraction) {
        if (v0 == null) {
            return v1;
        }
        if (v1 == null) {
            return v0;
        }
        return v0.add(fraction.multiply(v1.subtract(v0)));
    }
}
