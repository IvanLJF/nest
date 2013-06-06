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
package org.esa.nest.dat.layers.maptools.components;

import org.apache.commons.math.util.FastMath;
import org.esa.beam.framework.datamodel.GeoCoding;
import org.esa.beam.framework.datamodel.GeoPos;
import org.esa.beam.framework.datamodel.PixelPos;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.nest.dat.layers.ArrowOverlay;
import org.esa.nest.dat.layers.ScreenPixelConverter;
import org.esa.nest.eo.GeoUtils;

import java.awt.*;

/**
 * map tools compass component
 */
public class NorthArrowComponent implements MapToolsComponent {

    private double angle;
    private ArrowOverlay arrow;

    public NorthArrowComponent(final RasterDataNode raster) {
        final int rasterWidth = raster.getRasterWidth();
        final int rasterHeight = raster.getRasterHeight();
        final int margin = (int)(0.02 * FastMath.hypot(rasterWidth, rasterHeight));
        final PixelPos point1 = new PixelPos(margin, margin);

        final GeoCoding geoCoding = raster.getGeoCoding();
        if(geoCoding == null) {
            angle = Double.NaN;
            return;
        }

        final GeoPos point1Geo = geoCoding.getGeoPos(point1, null);
        final GeoPos centrePointGeo = geoCoding.getGeoPos(new PixelPos(rasterWidth/2, rasterHeight/2), null);
        final PixelPos point2 = geoCoding.getPixelPos(new GeoPos(centrePointGeo.getLat(), point1Geo.getLon()), null);

        final double op = point1.x-point2.x;
        final double hyp = FastMath.hypot(op, point1.y-point2.y);
        angle = FastMath.asin(op / hyp);

        if(point1Geo.getLat() < centrePointGeo.getLat()) {
            angle += Math.PI;
        }

        // determine distance
        final GeoPos x5Geo = geoCoding.getGeoPos(new PixelPos((int)(margin*1.5),margin), null);
        final GeoUtils.DistanceHeading dist = GeoUtils.vincenty_inverse(point1Geo.getLon(), point1Geo.getLat(),
                                                                        x5Geo.getLon(), x5Geo.getLat());

        GeoUtils.LatLonHeading coord = GeoUtils.vincenty_direct(point1Geo.getLon(), point1Geo.getLat(), dist.distance,angle);
        final PixelPos point3 = geoCoding.getPixelPos(new GeoPos((float)coord.lat, (float)coord.lon), null);

        final PixelPos dispTail = point1;
        final PixelPos dispHead = point3;

        arrow = new ArrowOverlay((int)dispTail.getX(), (int)dispTail.getY(), (int)dispHead.getX(), (int)dispHead.getY());
        arrow.setText("N");
    }

    public void render(final Graphics2D g, final ScreenPixelConverter screenPixel) {
        if(Double.isNaN(angle))
            return;

        arrow.drawArrow(g, screenPixel);
    }
}
