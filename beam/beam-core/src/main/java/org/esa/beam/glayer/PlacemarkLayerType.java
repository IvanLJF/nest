/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package org.esa.beam.glayer;

import com.bc.ceres.binding.ConversionException;
import com.bc.ceres.binding.Converter;
import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerContext;
import com.bc.ceres.glayer.LayerType;
import org.esa.beam.framework.datamodel.GcpDescriptor;
import org.esa.beam.framework.datamodel.PinDescriptor;
import org.esa.beam.framework.datamodel.PlacemarkDescriptor;
import org.esa.beam.framework.datamodel.Product;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;

/**
 * A layer used to display placemarks.
 *
 * @deprecated since BEAM 4.7, replaced by VectorDataLayer
 */
@Deprecated
public class PlacemarkLayerType extends LayerType {

    public static final String PROPERTY_PRODUCT = "product";
    public static final String PROPERTY_PLACEMARK_DESCRIPTOR = "placemarkDescriptor";
    public static final String PROPERTY_IMAGE_TO_MODEL_TRANSFORM = "imageToModelTransform";

    private static final String TYPE_NAME = "PlacemarkLayerType";
    private static final String[] ALIASES = {"org.esa.beam.glayer.PlacemarkLayerType"};

    @Override
    public String getName() {
        return TYPE_NAME;
    }
    
    @Override
    public String[] getAliases() {
        return ALIASES;
    }
    
    @Override
    public boolean isValidFor(LayerContext ctx) {
        return true;
    }

    @Override
    public Layer createLayer(LayerContext ctx, PropertySet configuration) {
        final Product product = (Product) configuration.getValue(PlacemarkLayerType.PROPERTY_PRODUCT);
        final String descriptorName = PlacemarkLayerType.PROPERTY_PLACEMARK_DESCRIPTOR;
        PlacemarkDescriptor placemarkDescriptor = (PlacemarkDescriptor) configuration.getValue(descriptorName);
        final String transformName = PlacemarkLayerType.PROPERTY_IMAGE_TO_MODEL_TRANSFORM;
        AffineTransform imageToModelTransform = (AffineTransform) configuration.getValue(transformName);

        return new PlacemarkLayer(this, product, placemarkDescriptor, imageToModelTransform, configuration);
    }

    @Override
    public PropertySet createLayerConfig(LayerContext ctx) {
        final PropertyContainer propertyContainer = new PropertyContainer();

        final Property textBgColorModel = Property.create(PlacemarkLayer.PROPERTY_NAME_TEXT_BG_COLOR, Color.class,
                                                          PlacemarkLayer.DEFAULT_TEXT_BG_COLOR, true);
        propertyContainer.addProperty(textBgColorModel);

        final Property textFgColorModel = Property.create(PlacemarkLayer.PROPERTY_NAME_TEXT_FG_COLOR, Color.class,
                                                          PlacemarkLayer.DEFAULT_TEXT_FG_COLOR, true);
        propertyContainer.addProperty(textFgColorModel);

        final Property textEnabledModel = Property.create(PlacemarkLayer.PROPERTY_NAME_TEXT_ENABLED, Boolean.class,
                                                          PlacemarkLayer.DEFAULT_TEXT_ENABLED, true);
        propertyContainer.addProperty(textEnabledModel);

        final Property textFontModel = Property.create(PlacemarkLayer.PROPERTY_NAME_TEXT_FONT, Font.class,
                                                       PlacemarkLayer.DEFAULT_TEXT_FONT, true);
        propertyContainer.addProperty(textFontModel);

        final Property productModel = Property.create(PROPERTY_PRODUCT, Product.class);
        productModel.getDescriptor().setNotNull(true);
        propertyContainer.addProperty(productModel);

        final Property placemarkModel = Property.create(PROPERTY_PLACEMARK_DESCRIPTOR, PlacemarkDescriptor.class);
        placemarkModel.getDescriptor().setConverter(new Converter<Object>() {
            @Override
            public Class<?> getValueType() {
                return PlacemarkDescriptor.class;
            }

            @Override
            public Object parse(String text) throws ConversionException {
                if (GcpDescriptor.INSTANCE.getRoleName().equals(text)) {
                    return GcpDescriptor.INSTANCE;
                } else if (PinDescriptor.INSTANCE.getRoleName().equals(text)) {
                    return PinDescriptor.INSTANCE;
                } else {
                    final String message = String.format("No PlacemarkDescriptor known for role: %s", text);
                    throw new ConversionException(message);
                }
            }

            @Override
            public String format(Object value) {
                final PlacemarkDescriptor placemarkDescriptor = (PlacemarkDescriptor) value;
                return placemarkDescriptor.getRoleName();
            }
        });
        placemarkModel.getDescriptor().setNotNull(true);
        propertyContainer.addProperty(placemarkModel);

        final Property transformModel = Property.create(PROPERTY_IMAGE_TO_MODEL_TRANSFORM, AffineTransform.class);
        placemarkModel.getDescriptor().setNotNull(true);
        propertyContainer.addProperty(transformModel);

        return propertyContainer;
    }
}
