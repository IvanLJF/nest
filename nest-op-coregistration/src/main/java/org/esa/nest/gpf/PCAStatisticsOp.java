/*
 * Copyright (C) 2002-2007 by ?
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.nest.gpf;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.dataio.dimap.DimapProductConstants;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.util.ProductUtils;
import org.esa.nest.datamodel.AbstractMetadata;

import javax.media.jai.JAI;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The operator evaluates the following local statistics for the user selected area of the image, and produces
 * an image file of the result:
 *
 * 1. Mean
 */

/**
 * The sample operator implementation for an algorithm
 * that can compute bands independently of each other.
 */
@OperatorMetadata(alias="PCA-Statistic", description="Computes statistics for PCA", internal=true)
public class PCAStatisticsOp extends Operator {

    @SourceProduct(alias="source")
    private Product sourceProduct;
    @TargetProduct
    private Product targetProduct;

    @Parameter(description = "The list of source bands.", alias = "masterBand", itemAlias = "band",
            sourceProductId="source", label="Master Band")
    private String masterBandName;

    @Parameter(description = "The list of source bands.", alias = "sourceBands", itemAlias = "band",
            sourceProductId="source", label="Slave Bands")
    private String[] sourceBandNames;

    private boolean statsCalculated = false;
    private boolean sampleTypeIsComplex;
    private int numOfBands;
    private int numOfPixels; // total number of pixel values
    private double[] min;    // min of all pixel values for each band
    private double[] max;    // max of all pixel values for each band
    private double[] sum;    // summation of all pixel values for each band
    private double[] sum2;   // summation of all pixel value squares for each band
    private double[] sum4;   // summation of all pixel value to the power of 4 for each band
    private double[] mean;   // mean for each band
    private double[] coefVar;// coefficient of variation for each band
    private double[] std;    // standard deviation for each band
    private double[] enl;    // equivalent number of looks for each band
    private HashMap<String, Integer> statisticsBandIndex;

    private MetadataElement abs;
    private HashMap<String, String[]> targetBandNameToSourceBandName;

    /**
     * Default constructor. The graph processing framework
     * requires that an operator has a default constructor.
     */
    public PCAStatisticsOp() {
    }

    /**
     * Initializes this operator and sets the one and only target product.
     * <p>The target product can be either defined by a field of type {@link org.esa.beam.framework.datamodel.Product} annotated with the
     * {@link org.esa.beam.framework.gpf.annotations.TargetProduct TargetProduct} annotation or
     * by calling {@link #setTargetProduct} method.</p>
     * <p>The framework calls this method after it has created this operator.
     * Any client code that must be performed before computation of tile data
     * should be placed here.</p>
     *
     * @throws org.esa.beam.framework.gpf.OperatorException
     *          If an error occurs during operator initialisation.
     * @see #getTargetProduct()
     */
    @Override
    public void initialize() throws OperatorException {
        try {
        abs = OperatorUtils.getAbstractedMetadata(sourceProduct);
        sampleTypeIsComplex = abs.getAttributeString("sample_type").contains("COMPLEX");

        getNumOfBandsForStatistics();

        setInitialValues();

        createTargetProduct();

        } catch(Exception e) {
            throw new OperatorException(e.getMessage());
        }
    }

    /**
     * Get the number of bands for which statistics are computed.
     */
    void getNumOfBandsForStatistics() {

        numOfBands = 0;
        statisticsBandIndex = new HashMap<String, Integer>();
        for(Band band : sourceProduct.getBands()) {
            statisticsBandIndex.put(band.getName(), numOfBands);
            numOfBands++;
        }
    }

    /**
     * Set initial values to some internal variables.
     */
    void setInitialValues() {

        min = new double[numOfBands];
        max = new double[numOfBands];
        mean = new double[numOfBands];
        coefVar = new double[numOfBands];
        std = new double[numOfBands];
        enl = new double[numOfBands];
        sum = new double[numOfBands];
        sum2 = new double[numOfBands];
        sum4 = new double[numOfBands];
        for (int i = 0; i < numOfBands; i++) {
            min[i] = Double.MAX_VALUE;
            max[i] = 0.0;
            sum[i] = 0.0;
            sum2[i] = 0.0;
            sum4[i] = 0.0;
        }

        numOfPixels = sourceProduct.getSceneRasterWidth() * sourceProduct.getSceneRasterHeight();
    }

    /**
     * Create target product.
     */
    void createTargetProduct() {

        targetProduct = new Product(sourceProduct.getName(),
                                    sourceProduct.getProductType(),
                                    sourceProduct.getSceneRasterWidth(),
                                    sourceProduct.getSceneRasterHeight());

        targetProduct.setPreferredTileSize(JAI.getDefaultTileSize());

        ProductUtils.copyMetadata(sourceProduct, targetProduct);
        ProductUtils.copyTiePointGrids(sourceProduct, targetProduct);
        ProductUtils.copyFlagCodings(sourceProduct, targetProduct);
        ProductUtils.copyGeoCoding(sourceProduct, targetProduct);
        targetProduct.setStartTime(sourceProduct.getStartTime());
        targetProduct.setEndTime(sourceProduct.getEndTime());

        for(Band band : sourceProduct.getBands()) {
            ProductUtils.copyBand(band.getName(), sourceProduct, targetProduct);
        }
    }

    private void addSelectedBands() {

        if (sourceBandNames == null || sourceBandNames.length == 0) {
            final Band[] bands = sourceProduct.getBands();
            final ArrayList<String> bandNameList = new ArrayList<String>(sourceProduct.getNumBands());
            for (Band band : bands) {
                bandNameList.add(band.getName());
            }
            sourceBandNames = bandNameList.toArray(new String[bandNameList.size()]);
        }

        final Band[] sourceBands = new Band[sourceBandNames.length];
        for (int i = 0; i < sourceBandNames.length; i++) {
            final String sourceBandName = sourceBandNames[i];
            final Band sourceBand = sourceProduct.getBand(sourceBandName);
            if (sourceBand == null) {
                throw new OperatorException("Source band not found: " + sourceBandName);
            }
            sourceBands[i] = sourceBand;
        }

        String targetBandName;
        targetBandNameToSourceBandName = new HashMap<String, String[]>();
        for (int i = 0; i < sourceBands.length; i++) {

            final Band srcBand = sourceBands[i];
            final String unit = srcBand.getUnit();
            if(unit == null) {
                throw new OperatorException("band "+srcBand.getName()+" requires a unit");
            }

            String targetUnit = "";

            if (unit.contains("phase")) {

                continue;

            } else if (unit.contains("imaginary")) {

                throw new OperatorException("Real and imaginary bands should be selected in pairs");

            } else if (unit.contains("real")) {

                if (i == sourceBands.length - 1) {
                    throw new OperatorException("Real and imaginary bands should be selected in pairs");
                }
                final String nextUnit = sourceBands[i+1].getUnit();
                if (nextUnit == null || !nextUnit.contains("imaginary")) {
                    throw new OperatorException("Real and imaginary bands should be selected in pairs");
                }
                final String[] srcBandNames = new String[2];
                srcBandNames[0] = srcBand.getName();
                srcBandNames[1] = sourceBands[i+1].getName();
                final String pol = OperatorUtils.getPolarizationFromBandName(srcBandNames[0]);
                if (pol != null) {
                    targetBandName = "Intensity_" + pol.toUpperCase();
                } else {
                    targetBandName = "Intensity";
                }
                ++i;
                if(targetProduct.getBand(targetBandName) == null) {
                    targetBandNameToSourceBandName.put(targetBandName, srcBandNames);
                    targetUnit = "intensity";
                }

            } else {

                final String[] srcBandNames = {srcBand.getName()};
                targetBandName = srcBand.getName();
                if(targetProduct.getBand(targetBandName) == null) {
                    targetBandNameToSourceBandName.put(targetBandName, srcBandNames);
                    targetUnit = unit;
                }
            }

            if(targetProduct.getBand(targetBandName) == null) {

                final Band targetBand = new Band(targetBandName,
                                           srcBand.getDataType(),
                                           srcBand.getRasterWidth(),
                                           srcBand.getRasterHeight());

                targetBand.setUnit(targetUnit);
                targetProduct.addBand(targetBand);
            }
        }
    }

    /**
     * Called by the framework in order to compute a tile for the given target band.
     * <p>The default implementation throws a runtime exception with the message "not implemented".</p>
     *
     * @param targetBand The target band.
     * @param targetTile The current tile associated with the target band to be computed.
     * @param pm         A progress monitor which should be used to determine computation cancelation requests.
     * @throws org.esa.beam.framework.gpf.OperatorException
     *          If an error occurs during computation of the target raster.
     */
    @Override
    public void computeTile(Band targetBand, Tile targetTile, ProgressMonitor pm) throws OperatorException {

        computeStatistics(targetBand, targetTile, targetTile.getRectangle(), pm);
    }

    /**
     * Compute statistics for given source tile.
     */
    void computeStatistics(Band targetBand, Tile targetTile, Rectangle targetTileRectangle, ProgressMonitor pm) {

        final Band sourceBand1 = sourceProduct.getBand(targetBand.getName());
        final Tile sourceRaster1 = getSourceTile(sourceBand1, targetTileRectangle, pm);
        final ProductData rawSamples1 = sourceRaster1.getRawSamples();

        final int idx = statisticsBandIndex.get(targetBand.getName());
        final int n = rawSamples1.getNumElems();
        double v, v2;
        for (int i = 0; i < n; i++) {

            if(sampleTypeIsComplex) {
                // todo
            }
            v = rawSamples1.getElemDoubleAt(i);
            if(v > max[idx])
                max[idx] = v;
            if(v < min[idx])
                min[idx] = v;
            v2 = v*v;
            sum[idx] += v;
            sum2[idx] += v2;
            sum4[idx] += v2*v2;
        }

        // copy source data to target
        //targetTile.setRawSamples(rawSamples1);

        statsCalculated = true;
    }

    /**
     * Compute statistics for the whole image.
     */
    @Override
    public void dispose() {

        if (!statsCalculated) {
            return;
        }

        completeStatistics();

        writeStatsToMetadata();
    }

    private void completeStatistics() {
        for (String bandName : statisticsBandIndex.keySet())  {

                final int bandIdx = statisticsBandIndex.get(bandName);
                final double m = sum[bandIdx] / numOfPixels;
                final double m2 = sum2[bandIdx] / numOfPixels;
                final double m4 = sum4[bandIdx] / numOfPixels;

                mean[bandIdx] = m;
                std[bandIdx] = Math.sqrt(m2 - m*m);
                coefVar[bandIdx] = Math.sqrt(m4 - m2*m2) / m2;
                enl[bandIdx] = m2*m2 / (m4 - m2*m2);
        }
    }

    private void writeStatsToMetadata() {

        MetadataAttribute attrib = abs.getAttribute("Stat");
        if(attrib == null) {
            attrib = new MetadataAttribute("Stat", ProductData.TYPE_ASCII, 1);
            abs.addAttributeFast(attrib);
        }

        AbstractMetadata.setAttribute(abs, "Stat", "written");

        try {
            ProductIO.writeProduct(sourceProduct, sourceProduct.getFileLocation(),
                                   DimapProductConstants.DIMAP_FORMAT_NAME,
                                   true, ProgressMonitor.NULL);
        } catch(Exception e) {
            throw new OperatorException(e.getMessage());
        }
    }


    // The following functions are for unit test only.
    public int getNumOfBands() {
        return numOfBands;
    }

    public double getMin(int bandIdx) {
        return min[bandIdx];
    }

    public double getMax(int bandIdx) {
        return max[bandIdx];
    }

    public double getMean(int bandIdx) {
        return mean[bandIdx];
    }

    public double getStd(int bandIdx) {
        return std[bandIdx];
    }

    public double getVarCoef(int bandIdx) {
        return coefVar[bandIdx];
    }

    public double getENL(int bandIdx) {
        return enl[bandIdx];
    }

    /**
     * The SPI is used to register this operator in the graph processing framework
     * via the SPI configuration file
     * {@code META-INF/services/org.esa.beam.framework.gpf.OperatorSpi}.
     * This class may also serve as a factory for new operator instances.
     * @see org.esa.beam.framework.gpf.OperatorSpi#createOperator()
     * @see org.esa.beam.framework.gpf.OperatorSpi#createOperator(java.util.Map, java.util.Map)
     */
    public static class Spi extends OperatorSpi {
        public Spi() {
            super(PCAStatisticsOp.class);
        }
    }
}