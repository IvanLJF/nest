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
package org.esa.nest.dataio.ceos.jers;

import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.nest.dataio.ceos.CEOSProductReaderPlugIn;

import java.io.File;

/**
 * The ReaderPlugIn for JERS products.
 *
 */
public class JERSProductReaderPlugIn extends CEOSProductReaderPlugIn {

    public JERSProductReaderPlugIn() {
        constants = new JERSConstants();
    }

    /**
     * Creates an instance of the actual product reader class. This method should never return <code>null</code>.
     *
     * @return a new reader instance, never <code>null</code>
     */
    @Override
    public ProductReader createReaderInstance() {
        return new JERSProductReader(this);
    }

    @Override
    protected DecodeQualification checkProductQualification(File file) {
        final String name = file.getName().toUpperCase();
        for(String prefix : constants.getVolumeFilePrefix()) {
            if(name.startsWith(prefix)) {
                final JERSProductReader reader = new JERSProductReader(this);
                return reader.checkProductQualification(file);
            }
        }
        return DecodeQualification.UNABLE;
    }
}