/*
 * Copyright (C) 2013 by Array Systems Computing Inc. http://www.array.ca
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.nest.eo;

public final class Constants
{
    public static final double lightSpeed = 299792458.0; //  m / s
    public static final double halfLightSpeed = lightSpeed / 2.0;
    public static final double lightSpeedInMetersPerDay = Constants.lightSpeed * 86400.0;

    public static final double semiMajorAxis = GeoUtils.WGS84.a; // in m, WGS84 semi-major axis of Earth
    public static final double semiMinorAxis = GeoUtils.WGS84.b; // in m, WGS84 semi-minor axis of Earth

    public static final double MeanEarthRadius = 6371008.7714; // in m (WGS84)

    public static final double oneMillion = 1000000.0;
    public static final double oneBillion = 1000000000.0;
    public static final double oneBillionth = 1.0 / oneBillion;

    public static final double TWO_PI = 2.0*Math.PI;

    public static final double EPS = 1e-15;

    public static final String USE_PROJECTED_INCIDENCE_ANGLE_FROM_DEM = "Use projected local incidence angle from DEM";
    public static final String USE_LOCAL_INCIDENCE_ANGLE_FROM_DEM = "Use local incidence angle from DEM";
    public static final String USE_INCIDENCE_ANGLE_FROM_ELLIPSOID = "Use incidence angle from Ellipsoid";

    private Constants()
    {
    }
}