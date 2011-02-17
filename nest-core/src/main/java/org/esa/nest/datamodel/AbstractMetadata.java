/*
 * Copyright (C) 2010 Array Systems Computing Inc. http://www.array.ca
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
package org.esa.nest.datamodel;

import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.OperatorException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * Created by IntelliJ IDEA.
 * User: lveci
 * Date: Aug 12, 2008
 * To change this template use File | Settings | File Templates.
 */
public final class AbstractMetadata {

    public static final int NO_METADATA = 99999;
    //public static final short NO_METADATA_BYTE = 99;
    private static final short NO_METADATA_BYTE = 0;
    public static final String NO_METADATA_STRING = " ";

    public static final String ABSTRACT_METADATA_ROOT = "Abstracted_Metadata";
    @Deprecated
    private static final String ABSTRACT_METADATA_ROOT_OLD = "Abstracted Metadata";

    public static final String SLAVE_METADATA_ROOT = "Slave Metadata";
    public static final String MASTER_BANDS = "Master_bands";

    public static final String PRODUCT = "PRODUCT";
    public static final String PRODUCT_TYPE = "PRODUCT_TYPE";
    public static final String SPH_DESCRIPTOR = "SPH_DESCRIPTOR";
    public static final String PATH = "PATH";
    public static final String MISSION = "MISSION";
    public static final String ACQUISITION_MODE = "ACQUISITION_MODE";
    public static final String BEAMS = "BEAMS";
    public static final String SWATH = "SWATH";
    public static final String PROC_TIME = "PROC_TIME";
    public static final String ProcessingSystemIdentifier = "Processing_system_identifier";
    public static final String CYCLE = "CYCLE";
    public static final String REL_ORBIT = "REL_ORBIT";
    public static final String ABS_ORBIT = "ABS_ORBIT";
    public static final String STATE_VECTOR_TIME = "STATE_VECTOR_TIME";
    private static final String VECTOR_SOURCE = "VECTOR_SOURCE";

    // SPH
    private static final String NUM_SLICES = "NUM_SLICES";
    public static final String first_line_time = "first_line_time";
    public static final String last_line_time = "last_line_time";
    public static final String first_near_lat = "first_near_lat";
    public static final String first_near_long = "first_near_long";
    public static final String first_far_lat = "first_far_lat";
    public static final String first_far_long = "first_far_long";
    public static final String last_near_lat = "last_near_lat";
    public static final String last_near_long = "last_near_long";
    public static final String last_far_lat = "last_far_lat";
    public static final String last_far_long = "last_far_long";

    public static final String PASS = "PASS";
    public static final String SAMPLE_TYPE = "SAMPLE_TYPE";
    public static final String mds1_tx_rx_polar = "mds1_tx_rx_polar";
    public static final String mds2_tx_rx_polar = "mds2_tx_rx_polar";
    public static final String mds3_tx_rx_polar = "mds3_tx_rx_polar";
    public static final String mds4_tx_rx_polar = "mds4_tx_rx_polar";
    public static final String polsarData = "polsar_data";
    public static final String[] polarTags = { AbstractMetadata.mds1_tx_rx_polar,AbstractMetadata.mds2_tx_rx_polar,
                                               AbstractMetadata.mds3_tx_rx_polar,AbstractMetadata.mds4_tx_rx_polar };
    public static final String algorithm = "algorithm";
    public static final String azimuth_looks = "azimuth_looks";
    public static final String range_looks = "range_looks";
    public static final String range_spacing = "range_spacing";
    public static final String azimuth_spacing = "azimuth_spacing";
    public static final String pulse_repetition_frequency = "pulse_repetition_frequency";
    public static final String radar_frequency = "radar_frequency";
    public static final String line_time_interval = "line_time_interval";

    public static final String TOT_SIZE = "total_size";
    public static final String num_output_lines = "num_output_lines";
    public static final String num_samples_per_line = "num_samples_per_line";

    public static final String subset_offset_x = "subset_offset_x";
    public static final String subset_offset_y = "subset_offset_y";

    // SRGR
    public static final String srgr_flag = "srgr_flag";
    public static final String map_projection = "map_projection";

    // calibration and flags
    public static final String ant_elev_corr_flag = "ant_elev_corr_flag";
    public static final String range_spread_comp_flag = "range_spread_comp_flag";
    public static final String inc_angle_comp_flag = "inc_angle_comp_flag";
    public static final String abs_calibration_flag = "abs_calibration_flag";
    public static final String calibration_factor = "calibration_factor";
    public static final String replica_power_corr_flag = "replica_power_corr_flag";
    public static final String range_sampling_rate = "range_sampling_rate";
    public static final String avg_scene_height = "avg_scene_height";
    public static final String multilook_flag = "multilook_flag";
    public static final String ref_inc_angle = "ref_inc_angle";
    public static final String ref_slant_range = "ref_slant_range";
    public static final String ref_slant_range_exp = "ref_slant_range_exp";
    public static final String rescaling_factor = "rescaling_factor";
    public static final String coregistered_stack = "coregistered_stack";

    public static final String external_calibration_file = "external_calibration_file";
    public static final String orbit_state_vector_file = "orbit_state_vector_file";
    public static final String target_report_file = "target_report_file";
    public static final String wind_field_report_file = "wind_field_report_file";

    // orbit state vectors
    public static final String orbit_state_vectors = "Orbit_State_Vectors";
    public static final String orbit_vector = "orbit_vector";
    public static final String orbit_vector_time = "time";
    public static final String orbit_vector_x_pos = "x_pos";
    public static final String orbit_vector_y_pos = "y_pos";
    public static final String orbit_vector_z_pos = "z_pos";
    public static final String orbit_vector_x_vel = "x_vel";
    public static final String orbit_vector_y_vel = "y_vel";
    public static final String orbit_vector_z_vel = "z_vel";

    // SRGR Coefficients
    public static final String srgr_coefficients = "SRGR_Coefficients";
    public static final String srgr_coef_list = "srgr_coef_list";
    public static final String srgr_coef_time = "zero_doppler_time";
    public static final String ground_range_origin = "ground_range_origin";
    public static final String coefficient = "coefficient";
    public static final String srgr_coef = "srgr_coef";

    // Doppler Centroid Coefficients
    public static final String dop_coefficients = "Doppler_Centroid_Coefficients";
    public static final String dop_coef_list = "dop_coef_list";
    public static final String dop_coef_time = "zero_doppler_time";
    public static final String slant_range_time = "slant_range_time";
    public static final String dop_coef = "dop_coef";

    // orthorectification
    public static final String is_terrain_corrected = "is_terrain_corrected";
    public static final String DEM = "DEM";
    public static final String geo_ref_system = "geo_ref_system";
    public static final String lat_pixel_res = "lat_pixel_res";
    public static final String lon_pixel_res = "lon_pixel_res";
    public static final String slant_range_to_first_pixel = "slant_range_to_first_pixel";

    /**
     * Abstract common metadata from products to be used uniformly by all operators
     * @param root the product metadata root
     * @return abstracted metadata root
     */
    public static MetadataElement addAbstractedMetadataHeader(MetadataElement root) {
        MetadataElement absRoot;
        if(root == null) {
            absRoot = new MetadataElement(ABSTRACT_METADATA_ROOT);    
        } else {
            absRoot = root.getElement(ABSTRACT_METADATA_ROOT);
            if(absRoot == null) {
                absRoot = new MetadataElement(ABSTRACT_METADATA_ROOT);
                root.addElementAt(absRoot, 0);
            }
        }

        // MPH
        addAbstractedAttribute(absRoot, PRODUCT, ProductData.TYPE_ASCII, "", "Product name");
        addAbstractedAttribute(absRoot, PRODUCT_TYPE, ProductData.TYPE_ASCII, "", "Product type");
        addAbstractedAttribute(absRoot, SPH_DESCRIPTOR, ProductData.TYPE_ASCII, "", "Description");
        addAbstractedAttribute(absRoot, MISSION, ProductData.TYPE_ASCII, "", "Satellite mission");
        addAbstractedAttribute(absRoot, ACQUISITION_MODE, ProductData.TYPE_ASCII, "", "Acquisition mode");
        addAbstractedAttribute(absRoot, BEAMS, ProductData.TYPE_ASCII, "", "Beams used");
        addAbstractedAttribute(absRoot, SWATH, ProductData.TYPE_ASCII, "", "Swath name");
        addAbstractedAttribute(absRoot, PROC_TIME, ProductData.TYPE_UTC, "utc", "Processed time");
        addAbstractedAttribute(absRoot, ProcessingSystemIdentifier, ProductData.TYPE_ASCII, "", "Processing system identifier");
        addAbstractedAttribute(absRoot, CYCLE, ProductData.TYPE_INT32, "", "Cycle");
        addAbstractedAttribute(absRoot, REL_ORBIT, ProductData.TYPE_INT32, "", "Track");
        addAbstractedAttribute(absRoot, ABS_ORBIT, ProductData.TYPE_INT32, "", "Orbit");
        addAbstractedAttribute(absRoot, STATE_VECTOR_TIME, ProductData.TYPE_UTC, "utc", "Time of orbit state vector");
        addAbstractedAttribute(absRoot, VECTOR_SOURCE, ProductData.TYPE_ASCII, "", "State vector source");

        // SPH
        addAbstractedAttribute(absRoot, NUM_SLICES, ProductData.TYPE_INT32, "", "Number of slices");
        addAbstractedAttribute(absRoot, first_line_time, ProductData.TYPE_UTC, "utc", "First zero doppler azimuth time");
        addAbstractedAttribute(absRoot, last_line_time, ProductData.TYPE_UTC, "utc", "Last zero doppler azimuth time");
        addAbstractedAttribute(absRoot, first_near_lat, ProductData.TYPE_FLOAT64, "deg", "");
        addAbstractedAttribute(absRoot, first_near_long, ProductData.TYPE_FLOAT64, "deg", "");
        addAbstractedAttribute(absRoot, first_far_lat, ProductData.TYPE_FLOAT64, "deg", "");
        addAbstractedAttribute(absRoot, first_far_long, ProductData.TYPE_FLOAT64, "deg", "");
        addAbstractedAttribute(absRoot, last_near_lat, ProductData.TYPE_FLOAT64, "deg", "");
        addAbstractedAttribute(absRoot, last_near_long, ProductData.TYPE_FLOAT64, "deg", "");
        addAbstractedAttribute(absRoot, last_far_lat, ProductData.TYPE_FLOAT64, "deg", "");
        addAbstractedAttribute(absRoot, last_far_long, ProductData.TYPE_FLOAT64, "deg", "");

        addAbstractedAttribute(absRoot, PASS, ProductData.TYPE_ASCII, "", "ASCENDING or DESCENDING");
        addAbstractedAttribute(absRoot, SAMPLE_TYPE, ProductData.TYPE_ASCII, "", "DETECTED or COMPLEX");
        addAbstractedAttribute(absRoot, mds1_tx_rx_polar, ProductData.TYPE_ASCII, "", "Polarization");
        addAbstractedAttribute(absRoot, mds2_tx_rx_polar, ProductData.TYPE_ASCII, "", "Polarization");
        addAbstractedAttribute(absRoot, mds3_tx_rx_polar, ProductData.TYPE_ASCII, "", "Polarization");
        addAbstractedAttribute(absRoot, mds4_tx_rx_polar, ProductData.TYPE_ASCII, "", "Polarization");
        addAbstractedAttribute(absRoot, polsarData, ProductData.TYPE_UINT8, "flag", "Polarimetric Matrix");
        addAbstractedAttribute(absRoot, algorithm, ProductData.TYPE_ASCII, "", "Processing algorithm");
        addAbstractedAttribute(absRoot, azimuth_looks, ProductData.TYPE_FLOAT64, "", "");
        addAbstractedAttribute(absRoot, range_looks, ProductData.TYPE_FLOAT64, "", "");
        addAbstractedAttribute(absRoot, range_spacing, ProductData.TYPE_FLOAT64, "m", "Range sample spacing");
        addAbstractedAttribute(absRoot, azimuth_spacing, ProductData.TYPE_FLOAT64, "m", "Azimuth sample spacing");
        addAbstractedAttribute(absRoot, pulse_repetition_frequency, ProductData.TYPE_FLOAT64, "Hz", "PRF");
        addAbstractedAttribute(absRoot, radar_frequency, ProductData.TYPE_FLOAT64, "MHz", "Radar frequency");
        addAbstractedAttribute(absRoot, line_time_interval, ProductData.TYPE_FLOAT64, "s", "");

        addAbstractedAttribute(absRoot, TOT_SIZE, ProductData.TYPE_UINT32, "Mb", "Total product size");
        addAbstractedAttribute(absRoot, num_output_lines, ProductData.TYPE_UINT32, "lines", "Raster height");
        addAbstractedAttribute(absRoot, num_samples_per_line, ProductData.TYPE_UINT32, "samples", "Raster width");

        addAbstractedAttribute(absRoot, subset_offset_x, ProductData.TYPE_UINT32, "samples", "X coordinate of UL corner of subset in original image");
        addAbstractedAttribute(absRoot, subset_offset_y, ProductData.TYPE_UINT32, "samples", "Y coordinate of UL corner of subset in original image");

        // SRGR
        addAbstractedAttribute(absRoot, srgr_flag, ProductData.TYPE_UINT8, "flag", "SRGR applied");
        MetadataAttribute att = addAbstractedAttribute(absRoot, avg_scene_height, ProductData.TYPE_FLOAT64, "m", "Average scene height ellipsoid");
        att.getData().setElemInt(0);
        addAbstractedAttribute(absRoot, map_projection, ProductData.TYPE_ASCII, "", "Map projection applied");

        // orthorectification
        addAbstractedAttribute(absRoot, is_terrain_corrected, ProductData.TYPE_UINT8, "flag", "orthorectification applied");
        addAbstractedAttribute(absRoot, DEM, ProductData.TYPE_ASCII, "", "Digital Elevation Model used");
        addAbstractedAttribute(absRoot, geo_ref_system, ProductData.TYPE_ASCII, "", "geographic reference system");
        addAbstractedAttribute(absRoot, lat_pixel_res, ProductData.TYPE_FLOAT64, "deg", "pixel resolution in geocoded image");
        addAbstractedAttribute(absRoot, lon_pixel_res, ProductData.TYPE_FLOAT64, "deg", "pixel resolution in geocoded image");
        addAbstractedAttribute(absRoot, slant_range_to_first_pixel, ProductData.TYPE_FLOAT64, "m", "Slant range to 1st data sample");

        // calibration
        addAbstractedAttribute(absRoot, ant_elev_corr_flag, ProductData.TYPE_UINT8, "flag", "Antenna elevation applied");
        addAbstractedAttribute(absRoot, range_spread_comp_flag, ProductData.TYPE_UINT8, "flag", "range spread compensation applied");
        addAbstractedAttribute(absRoot, replica_power_corr_flag, ProductData.TYPE_UINT8, "flag", "Replica pulse power correction applied");
        addAbstractedAttribute(absRoot, abs_calibration_flag, ProductData.TYPE_UINT8, "flag", "Product calibrated");
        addAbstractedAttribute(absRoot, calibration_factor, ProductData.TYPE_FLOAT64, "", "Calibration constant");
        addAbstractedAttribute(absRoot, inc_angle_comp_flag, ProductData.TYPE_UINT8, "flag", "incidence angle compensation applied");
        addAbstractedAttribute(absRoot, ref_inc_angle, ProductData.TYPE_FLOAT64, "", "Reference incidence angle");
        addAbstractedAttribute(absRoot, ref_slant_range, ProductData.TYPE_FLOAT64, "", "Reference slant range");
        addAbstractedAttribute(absRoot, ref_slant_range_exp, ProductData.TYPE_FLOAT64, "", "Reference slant range exponent");
        addAbstractedAttribute(absRoot, rescaling_factor, ProductData.TYPE_FLOAT64, "", "Rescaling factor");
         
        addAbstractedAttribute(absRoot, range_sampling_rate, ProductData.TYPE_FLOAT64, "MHz", "Range Sampling Rate");

        // Multilook
        addAbstractedAttribute(absRoot, multilook_flag, ProductData.TYPE_UINT8, "flag", "Multilook applied");

        // coregistration
        addAbstractedAttribute(absRoot, coregistered_stack, ProductData.TYPE_UINT8, "flag", "Coregistration applied");

        addAbstractedAttribute(absRoot, external_calibration_file, ProductData.TYPE_ASCII, "", "External calibration file used");
        addAbstractedAttribute(absRoot, orbit_state_vector_file, ProductData.TYPE_ASCII, "", "Orbit file used");

        absRoot.addElement(new MetadataElement(orbit_state_vectors));
        absRoot.addElement(new MetadataElement(srgr_coefficients));
        absRoot.addElement(new MetadataElement(dop_coefficients));

        return absRoot;
    }

    /**
     * Adds an attribute into dest
     * @param dest the destination element
     * @param tag the name of the attribute
     * @param dataType the ProductData type
     * @param unit The unit
     * @param desc The description
     * @return the newly created attribute
     */
    public static MetadataAttribute addAbstractedAttribute(final MetadataElement dest, final String tag, final int dataType,
                                               final String unit, final String desc) {
        final MetadataAttribute attribute = new MetadataAttribute(tag, dataType, 1);
        if(dataType == ProductData.TYPE_ASCII) {
            attribute.getData().setElems(NO_METADATA_STRING);
        } else if(dataType == ProductData.TYPE_INT8 || dataType == ProductData.TYPE_UINT8) {
            attribute.getData().setElems( new String[] {String.valueOf(NO_METADATA_BYTE)} );
        } else if(dataType != ProductData.TYPE_UTC) {
            attribute.getData().setElems( new String[] {String.valueOf(NO_METADATA)} );
        }
        attribute.setUnit(unit);
        attribute.setDescription(desc);
        attribute.setReadOnly(false);
        dest.addAttribute(attribute);
        return attribute;
    }

    /**
     * Sets an attribute as a string
     * @param dest the destination element
     * @param tag the name of the attribute
     * @param value the string value
     */
    public static void setAttribute(final MetadataElement dest, final String tag, final String value) {
        final MetadataAttribute attrib = dest.getAttribute(tag);
        if(attrib != null && value != null) {
            if(value.isEmpty())
                attrib.getData().setElems(NO_METADATA_STRING);
            else
                attrib.getData().setElems(value);
        } else {
            if(attrib == null)
                System.out.println(tag + " not found in metadata");
            if(value == null)
                System.out.println(tag + " metadata value is null");
        }
    }

    /**
     * Sets an attribute as a UTC
     * @param dest the destination element
     * @param tag the name of the attribute
     * @param value the UTC value
     */
    public static void setAttribute(final MetadataElement dest, final String tag, final ProductData.UTC value) {
        final MetadataAttribute attrib = dest.getAttribute(tag);
        if(attrib != null && value != null) {
            attrib.getData().setElems(value.getArray());
        } else {
            if(attrib == null)
                System.out.println(tag + " not found in metadata");
            if(value == null)
                System.out.println(tag + " metadata value is null");
        }
    }

    /**
     * Sets an attribute as an int
     * @param dest the destination element
     * @param tag the name of the attribute
     * @param value the string value
     */
    public static void setAttribute(final MetadataElement dest, final String tag, final int value) {
        final MetadataAttribute attrib = dest.getAttribute(tag);
        if(attrib == null)
            System.out.println(tag + " not found in metadata");
        else
            attrib.getData().setElemInt(value);
    }

    /**
     * Sets an attribute as a double
     * @param dest the destination element
     * @param tag the name of the attribute
     * @param value the string value
     */
    public static void setAttribute(final MetadataElement dest, final String tag, final double value) {
        final MetadataAttribute attrib = dest.getAttribute(tag);
        if(attrib == null)
            System.out.println(tag + " not found in metadata");
        else
            attrib.getData().setElemDouble(value);
    }

    public static void setAttribute(final MetadataElement dest, final String tag, final Double value) {
        final MetadataAttribute attrib = dest.getAttribute(tag);
        if(attrib == null)
            System.out.println(tag + " not found in metadata");
        else if(value != null)
            attrib.getData().setElemDouble(value);
    }

    public static ProductData.UTC parseUTC(final String timeStr) {
        try {
            if(timeStr == null)
                return new ProductData.UTC(0);
            return ProductData.UTC.parse(timeStr);
        } catch(ParseException e) {
            return new ProductData.UTC(0);
        }
    }

    public static ProductData.UTC parseUTC(final String timeStr, final String format) {
        try {
            final int dotPos = timeStr.lastIndexOf(".");
            if (dotPos > 0) {
                final String newTimeStr = timeStr.substring(0, Math.min(dotPos+6, timeStr.length()));
                return ProductData.UTC.parse(newTimeStr, format);
            }
            return ProductData.UTC.parse(timeStr, format);
        } catch(ParseException e) {
            System.out.println("UTC parse error:"+ e.toString());
            return new ProductData.UTC(0);
        }
    }

    public static boolean getAttributeBoolean(final MetadataElement elem, final String tag) throws Exception {
        final int val = elem.getAttributeInt(tag);
        if(val == NO_METADATA)
            throw new Exception("Metadata "+tag+" has not been set");
        return val != 0;
    }

    public static double getAttributeDouble(final MetadataElement elem, final String tag) throws Exception {       
        final double val = elem.getAttributeDouble(tag);
        if(val == NO_METADATA)
            throw new Exception("Metadata "+tag+" has not been set");
        return val;
    }

    public static int getAttributeInt(final MetadataElement elem, final String tag) throws Exception {
        final int val = elem.getAttributeInt(tag);
        if(val == NO_METADATA)
            throw new Exception("Metadata "+tag+" has not been set");
        return val;
    }

    public static boolean loadExternalMetadata(final Product product, final MetadataElement absRoot,
                                               final File productFile) throws IOException {
        // load metadata xml file if found
        final String inputStr = productFile.getAbsolutePath();
        final String metadataStr = inputStr.substring(0, inputStr.lastIndexOf('.')) + ".xml";
        File metadataFile = new File(metadataStr);
        if(metadataFile.exists() && AbstractMetadataIO.Load(product, absRoot, metadataFile)) {
            return true;
        } else {
            metadataFile = new File(productFile.getParentFile(), "metadata.xml");
            return metadataFile.exists() && AbstractMetadataIO.Load(product, absRoot, metadataFile);
        }
    }

    public static void saveExternalMetadata(final Product product, final MetadataElement absRoot, final File productFile) {
         // load metadata xml file if found
        final String inputStr = productFile.getAbsolutePath();
        final String metadataStr = inputStr.substring(0, inputStr.lastIndexOf('.')) + ".xml";
        final File metadataFile = new File(metadataStr);
        AbstractMetadataIO.Save(product, absRoot, metadataFile);
    }

    /**
     * Get abstracted metadata.
     * @param sourceProduct the product
     * @return MetadataElement
     * @throws OperatorException if abs metadata not found
     */
    public static MetadataElement getAbstractedMetadata(final Product sourceProduct) throws OperatorException {

        final MetadataElement root = sourceProduct.getMetadataRoot();
        if (root == null) {
            throw new OperatorException("Root Metadata not found");
        }
        MetadataElement abstractedMetadata = root.getElement(AbstractMetadata.ABSTRACT_METADATA_ROOT);
        if (abstractedMetadata == null) {
            abstractedMetadata = root.getElement(AbstractMetadata.ABSTRACT_METADATA_ROOT_OLD);
            if (abstractedMetadata != null) {
                abstractedMetadata.setName(AbstractMetadata.ABSTRACT_METADATA_ROOT);
            }
        }
        if(abstractedMetadata == null) {
            abstractedMetadata = addAbstractedMetadataHeader(root);
        } else {
            patchMissingMetadata(abstractedMetadata);
        }
        return abstractedMetadata;
    }

    private static void patchMissingMetadata(final MetadataElement abstractedMetadata) {
        final MetadataElement tmpElem = new MetadataElement("tmp");
        final MetadataElement completeMetadata = addAbstractedMetadataHeader(tmpElem);

        final MetadataAttribute[] attribs = completeMetadata.getAttributes();
        for(MetadataAttribute at : attribs) {
            if(abstractedMetadata.getAttribute(at.getName()) == null) {
                abstractedMetadata.addAttribute(at);
                abstractedMetadata.getProduct().setModified(false);
            }
        }
    }

    /**
     * Create sub-metadata element.
     * @param root The root metadata element.
     * @param tag The sub-metadata element name.
     * @return The sub-metadata element.
     */
    public static MetadataElement addElement(final MetadataElement root, final String tag) {

        MetadataElement subElemRoot = root.getElement(tag);
        if(subElemRoot == null) {
            subElemRoot = new MetadataElement(tag);
            root.addElement(subElemRoot);
        }
        return subElemRoot;
    }

    /**
     * Get orbit state vectors.
     * @param absRoot Abstracted metadata root.
     * @return orbitStateVectors Array of orbit state vectors.
     * @throws OperatorException The exceptions.
     */
    public static OrbitStateVector[] getOrbitStateVectors(final MetadataElement absRoot) throws OperatorException {

        final MetadataElement elemRoot = absRoot.getElement(orbit_state_vectors);
        if(elemRoot == null) {
            throw new OperatorException("This product has no orbit state vectors");
        }
        final int numElems = elemRoot.getNumElements();
        final OrbitStateVector[] orbitStateVectors = new OrbitStateVector[numElems];
        for (int i = 0; i < numElems; i++) {

            final MetadataElement subElemRoot = elemRoot.getElement(orbit_vector + (i+1));
            final OrbitStateVector vector = new OrbitStateVector(
                        subElemRoot.getAttributeUTC(orbit_vector_time),
                        subElemRoot.getAttributeDouble(orbit_vector_x_pos),
                        subElemRoot.getAttributeDouble(orbit_vector_y_pos),
                        subElemRoot.getAttributeDouble(orbit_vector_z_pos),
                        subElemRoot.getAttributeDouble(orbit_vector_x_vel),
                        subElemRoot.getAttributeDouble(orbit_vector_y_vel),
                        subElemRoot.getAttributeDouble(orbit_vector_z_vel));
            orbitStateVectors[i] = vector;
        }
        return orbitStateVectors;
    }

    /**
     * Set orbit state vectors.
     * @param absRoot Abstracted metadata root.
     * @param orbitStateVectors The orbit state vectors.
     */
    public static void setOrbitStateVectors(final MetadataElement absRoot, OrbitStateVector[] orbitStateVectors) {

        final MetadataElement elemRoot = absRoot.getElement(orbit_state_vectors);
        final int numElems = elemRoot.getNumElements();
        if (numElems != orbitStateVectors.length) {
            throw new OperatorException("Length of orbit state vector array is not correct");
        }

        for (int i = 0; i < numElems; i++) {
            final OrbitStateVector vector = orbitStateVectors[i];
            final MetadataElement subElemRoot = elemRoot.getElement(orbit_vector + (i+1));
            subElemRoot.setAttributeUTC(orbit_vector_time, vector.time);
            subElemRoot.setAttributeDouble(orbit_vector_x_pos, vector.x_pos);
            subElemRoot.setAttributeDouble(orbit_vector_y_pos, vector.y_pos);
            subElemRoot.setAttributeDouble(orbit_vector_z_pos, vector.z_pos);
            subElemRoot.setAttributeDouble(orbit_vector_x_vel, vector.x_vel);
            subElemRoot.setAttributeDouble(orbit_vector_y_vel, vector.y_vel);
            subElemRoot.setAttributeDouble(orbit_vector_z_vel, vector.z_vel);
        }
    }

    /**
     * Get SRGR Coefficients.
     * @param absRoot Abstracted metadata root.
     * @return Array of SRGR coefficient data sets.
     */
    public static SRGRCoefficientList[] getSRGRCoefficients(final MetadataElement absRoot) {

        final MetadataElement elemRoot = absRoot.getElement(srgr_coefficients);
        final MetadataElement[] srgr_coef_listElem = elemRoot.getElements();
        final SRGRCoefficientList[] srgrCoefficientList = new SRGRCoefficientList[srgr_coef_listElem.length];
        int k = 0;
        for (MetadataElement listElem : srgr_coef_listElem) {
            final SRGRCoefficientList srgrList = new SRGRCoefficientList();
            srgrList.time  = listElem.getAttributeUTC(srgr_coef_time);
            srgrList.timeMJD = srgrList.time.getMJD();
            srgrList.ground_range_origin = listElem.getAttributeDouble(ground_range_origin);

            final int numSubElems = listElem.getNumElements();
            srgrList.coefficients = new double[numSubElems];
            for (int i=0; i < numSubElems; ++i) {
                final MetadataElement coefElem = listElem.getElementAt(i);
                srgrList.coefficients[i] = coefElem.getAttributeDouble(srgr_coef, 0.0);
            }
            srgrCoefficientList[k++] = srgrList;
        }
        return srgrCoefficientList;
    }

    /**
     * Get Doppler Centroid Coefficients.
     * @param absRoot Abstracted metadata root.
     * @return Array of Doppler centroid coefficient data sets.
     */
    public static DopplerCentroidCoefficientList[] getDopplerCentroidCoefficients(final MetadataElement absRoot) {

        final MetadataElement elemRoot = absRoot.getElement(dop_coefficients);
        final MetadataElement[] dop_coef_listElem = elemRoot.getElements();
        final DopplerCentroidCoefficientList[] dopCoefficientList = new DopplerCentroidCoefficientList[dop_coef_listElem.length];
        int k = 0;
        for (MetadataElement listElem : dop_coef_listElem) {
            final DopplerCentroidCoefficientList dopList = new DopplerCentroidCoefficientList();
            dopList.time  = listElem.getAttributeUTC(srgr_coef_time);
            dopList.timeMJD = dopList.time.getMJD();
            dopList.slant_range_time = listElem.getAttributeDouble(slant_range_time);

            final int numSubElems = listElem.getNumElements();
            dopList.coefficients = new double[numSubElems];
            for (int i=0; i < numSubElems; ++i) {
                final MetadataElement coefElem = listElem.getElementAt(i);
                dopList.coefficients[i] = coefElem.getAttributeDouble(dop_coef, 0.0);
            }
            dopCoefficientList[k++] = dopList;
        }
        return dopCoefficientList;
    }

    public static class OrbitStateVector {

        public final ProductData.UTC time;
        public final double time_mjd;
        public double x_pos, y_pos, z_pos;
        public double x_vel, y_vel, z_vel;
        public OrbitStateVector(final ProductData.UTC t,
                                final double xpos, final double ypos, final double zpos,
                                final double xvel, final double yvel, final double zvel) {
            this.time = t;
            time_mjd = t.getMJD();
            this.x_pos = xpos;
            this.y_pos = ypos;
            this.z_pos = zpos;
            this.x_vel = xvel;
            this.y_vel = yvel;
            this.z_vel = zvel;
        }
    }

    public static class SRGRCoefficientList {
        public ProductData.UTC time = null;
        public double timeMJD = 0;
        public double ground_range_origin = 0.0;
        public double[] coefficients = null;
    }

    public static class DopplerCentroidCoefficientList {
        public ProductData.UTC time = null;
        public double timeMJD = 0;
        public double slant_range_time = 0.0;
        public double[] coefficients = null;
    }
}
