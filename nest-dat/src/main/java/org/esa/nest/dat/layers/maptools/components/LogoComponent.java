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

import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.nest.dat.layers.ScreenPixelConverter;
import org.esa.nest.util.ResourceUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * map tools logo component
 */
public class LogoComponent implements MapToolsComponent {

    private static final ImageIcon procNestIcon = ResourceUtils.LoadIcon("org/esa/nest/icons/proc_nest.png");
    private final BufferedImage image;
    private final int rasterWidth;
    private final int rasterHeight;
    private final static double marginPct = 0.05;
    private final int margin;

    public LogoComponent(final RasterDataNode raster) {

        image = new BufferedImage(procNestIcon.getIconWidth(), procNestIcon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g = image.createGraphics();
        g.drawImage(procNestIcon.getImage(), null, null);

        rasterWidth = raster.getRasterWidth();
        rasterHeight = raster.getRasterHeight();
        margin = (int)(Math.min(rasterWidth, rasterHeight) * marginPct);
    }

    public void render(final Graphics2D graphics, final ScreenPixelConverter screenPixel) {
        final AffineTransform transformSave = graphics.getTransform();
        try {
            final AffineTransform transform = screenPixel.getImageTransform(transformSave);

            final double scale = (marginPct*2 * rasterWidth) / (double)image.getWidth();
            final Point point = new Point((int)(rasterWidth-(image.getWidth()*scale)-margin),
                                          (int)(rasterHeight-(image.getHeight()*scale)-margin));
            transform.translate(point.x, point.y);
            transform.scale(scale, scale);

            graphics.drawRenderedImage(image, transform);
        } finally {
            graphics.setTransform(transformSave);
        }
    }
}
