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
package org.esa.nest.gpf;

import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper functions for OperatorUIs
 */
public final class OperatorUIUtils {

    public final static String SOURCE_BAND_NAMES = "sourceBandNames";

    public static void initBandList(final JList bandList, final String[] bandNames) {
        final Object selectedValues[] = bandList.getSelectedValues();

        bandList.removeAll();
        bandList.setListData(bandNames);
        bandList.setFixedCellWidth(200);
        bandList.setMinimumSize(new Dimension(50, 4));
        
        final int size = bandList.getModel().getSize();
        final List<Integer> indices = new ArrayList<Integer>(size);

        for (Object selectedValue : selectedValues) {
            final String selValue = (String) selectedValue;

            for (int j = 0; j < size; ++j) {
                final String val = (String) bandList.getModel().getElementAt(j);
                if (val.equals(selValue)) {
                    indices.add(j);
                    break;
                }
            }
        }
        setSelectedListIndices(bandList, indices);
    }

    public static void setSelectedListIndices(final JList list, final List<Integer> indices) {
        final int[] selIndex = new int[indices.size()];
        for(int i=0; i < indices.size(); ++i) {
            selIndex[i] = indices.get(i);
        }
        list.setSelectedIndices(selIndex);
    }

    public static void updateBandList(final JList bandList, final Map<String, Object> paramMap, final String paramName) {
        final Object selectedValues[] = bandList.getSelectedValues();
        final String bandNames[] = new String[selectedValues.length];
        for(int i=0; i<selectedValues.length; ++i) {
            bandNames[i] = (String)selectedValues[i];
        }

        paramMap.put(paramName, bandNames);
    }

    public static double getNoDataValue(final File extFile) {
        try {
            final Product product = ProductIO.readProduct(extFile);
            if(product != null)
                return product.getBandAt(0).getNoDataValue();
        } catch(Exception e) {
            //
        }
        return 0;
    }
}