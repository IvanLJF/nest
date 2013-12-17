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
package org.esa.nest.util;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.runtime.RuntimeConfig;
import com.bc.ceres.core.runtime.RuntimeConfigException;
import com.bc.ceres.core.runtime.internal.DefaultRuntimeConfig;
import junit.framework.TestCase;
import org.esa.beam.dataio.dimap.DimapProductConstants;
import org.esa.beam.framework.dataio.*;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.dataop.maptransf.Datum;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.gpf.operators.standard.WriteOp;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.util.PropertyMap;
import org.esa.beam.util.jai.JAIUtils;
import org.esa.beam.util.logging.BeamLogManager;
import org.esa.nest.datamodel.AbstractMetadata;
import org.esa.nest.gpf.ReaderUtils;
import org.esa.nest.gpf.RecursiveProcessor;

import javax.media.jai.JAI;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Utilities for Operator unit tests
 * In order to test the datasets at Array, set the following to true in the nest.config
 * nest.test.ReadersOnAllProducts=true nest.test.ProcessingOnAllProducts=true
 */
public class TestUtils {

    public static final Logger log = BeamLogManager.getSystemLogger();

    private static final PropertyMap testPreferences = Config.getAutomatedTestConfigPropertyMap("nest.tests");

    private final static String contextID = ResourceUtils.getContextID();
    public final static String rootPathExpectedProducts = testPreferences.getPropertyString(contextID+".test.rootPathExpectedProducts");
    public final static String rootPathTerraSarX = testPreferences.getPropertyString(contextID+".test.rootPathTerraSarX");
    public final static String rootPathASAR= testPreferences.getPropertyString(contextID+".test.rootPathASAR");
    public final static String rootPathRadarsat2 = testPreferences.getPropertyString(contextID+".test.rootPathRadarsat2");
    public final static String rootPathRadarsat1 = testPreferences.getPropertyString(contextID+".test.rootPathRadarsat1");
    public final static String rootPathERS = testPreferences.getPropertyString(contextID+".test.rootPathERS");
    public final static String rootPathJERS = testPreferences.getPropertyString(contextID+".test.rootPathJERS");
    public final static String rootPathALOS = testPreferences.getPropertyString(contextID+".test.rootPathALOS");
    public final static String rootPathCosmoSkymed = testPreferences.getPropertyString(contextID+".test.rootPathCosmoSkymed");
    public final static String rootPathMixProducts = testPreferences.getPropertyString(contextID+".test.rootPathMixProducts");

    private final static int subsetX = Integer.parseInt(testPreferences.getPropertyString(contextID+".test.subsetX"));
    private final static int subsetY = Integer.parseInt(testPreferences.getPropertyString(contextID+".test.subsetY"));
    private final static int subsetWidth = Integer.parseInt(testPreferences.getPropertyString(contextID+".test.subsetWidth"));
    private final static int subsetHeight = Integer.parseInt(testPreferences.getPropertyString(contextID+".test.subsetHeight"));

    private static final int maxIteration = Integer.parseInt(testPreferences.getPropertyString(contextID+".test.maxProductsPerRootFolder"));
    private static final String testReadersOnAllProducts = testPreferences.getPropertyString(contextID+".test.ReadersOnAllProducts");
    private static final String testProcessingOnAllProducts = testPreferences.getPropertyString(contextID+".test.ProcessingOnAllProducts");

    public static final boolean canTestReadersOnAllProducts = testReadersOnAllProducts != null && testReadersOnAllProducts.equalsIgnoreCase("true");
    public static final boolean canTestProcessingOnAllProducts = testProcessingOnAllProducts != null && testProcessingOnAllProducts.equalsIgnoreCase("true");

    private static final boolean DEBUG = false;
    private static final boolean FailOnSkip = false;
    private static boolean testEnviromentInitialized = false;

    public static void initTestEnvironment() throws RuntimeConfigException {
        if(testEnviromentInitialized)
            return;

        final RuntimeConfig runtimeConfig = new DefaultRuntimeConfig();

        JAI.getDefaultInstance().getTileScheduler().setParallelism(Runtime.getRuntime().availableProcessors());
        MemUtils.configureJaiTileCache();

        //disable JAI media library
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");
        testEnviromentInitialized = true;
    }

    public static int getMaxIterations() {
        return maxIteration;
    }

    public static Product readSourceProduct(final String path) throws IOException {
        final File inputFile = new File(path);
        if(!inputFile.exists()) {
            throw new IOException(path + " not found");
        }

        final ProductReader reader = ProductIO.getProductReaderForFile(inputFile);
        if(reader == null)
            throw new IOException("No reader found for "+inputFile);
        return reader.readProductNodes(inputFile, null);
    }

    public static Product createProduct(final String type, final int w, final int h) {
        final Product product = new Product("name", type, w, h);

        product.setStartTime(AbstractMetadata.parseUTC("10-MAY-2008 20:30:46.890683"));
        product.setEndTime(AbstractMetadata.parseUTC("10-MAY-2008 20:35:46.890683"));
        product.setDescription("description");

        addGeoCoding(product);

        AbstractMetadata.addAbstractedMetadataHeader(product.getMetadataRoot());

        return product;
    }

    private static void addGeoCoding(final Product product) {

        final TiePointGrid latGrid = new TiePointGrid("lat", 2, 2, 0.5f, 0.5f,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                      new float[]{10.0f, 10.0f, 5.0f, 5.0f});
        final TiePointGrid lonGrid = new TiePointGrid("lon", 2, 2, 0.5f, 0.5f,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                      new float[]{10.0f, 10.0f, 5.0f, 5.0f},
                      TiePointGrid.DISCONT_AT_360);
        final TiePointGeoCoding tpGeoCoding = new TiePointGeoCoding(latGrid, lonGrid, Datum.WGS_84);

        product.addTiePointGrid(latGrid);
        product.addTiePointGrid(lonGrid);
        product.setGeoCoding(tpGeoCoding);
    }

    public static void verifyProduct(final Product product, final boolean verifyTimes,
                                     final boolean verifyGeoCoding) throws Exception {
        ReaderUtils.verifyProduct(product, verifyTimes, verifyGeoCoding);

        // readPixels: execute computeTiles()
        final float[] floatValues = new float[100];
        product.getBandAt(0).readPixels(0, 0, 10, 10, floatValues, ProgressMonitor.NULL);
    }

    public static void attributeEquals(final MetadataElement elem, final String name,
                                       final double trueValue) throws Exception {
        final double val = elem.getAttributeDouble(name, 0);
        if(Double.compare(val, trueValue) != 0) {
            if(Float.compare((float)val, (float)trueValue) != 0)
                throwErr(name + " is " + val + ", expecting " + trueValue);
        }
    }

    public static void attributeEquals(final MetadataElement elem, String name,
                                       final String trueValue) throws Exception {
        final String val = elem.getAttributeString(name, "");
        if(!val.equals(trueValue))
            throwErr(name + " is " + val + ", expecting " + trueValue);
    }

    private static void compareMetadata(final Product testProduct, final Product expectedProduct,
                                        final String[] excemptionList) throws Exception {
        final MetadataElement testAbsRoot = AbstractMetadata.getAbstractedMetadata(testProduct);
        if(testAbsRoot == null)
            throwErr("Metadata is null");
        final MetadataElement expectedAbsRoot = AbstractMetadata.getAbstractedMetadata(expectedProduct);
        if(expectedAbsRoot == null)
            throwErr("Metadata is null");

        if(excemptionList != null) {
            Arrays.sort(excemptionList);
        }

        final MetadataAttribute[] attribList = expectedAbsRoot.getAttributes();
        for(MetadataAttribute expectedAttrib : attribList) {
            if(excemptionList != null && Arrays.binarySearch(excemptionList, expectedAttrib.getName()) >= 0)
                continue;

            final MetadataAttribute result = testAbsRoot.getAttribute(expectedAttrib.getName());
            if(result == null) {
                throwErr("Metadata attribute "+expectedAttrib.getName()+" is missing");
            }
            final ProductData expectedData = result.getData();
            if(!expectedData.equalElems(expectedAttrib.getData())) {
                if((expectedData.getType() == ProductData.TYPE_FLOAT64 ||
                    expectedData.getType() == ProductData.TYPE_FLOAT64) &&
                      Double.compare(expectedData.getElemDouble(), result.getData().getElemDouble()) == 0) {

                } else if(expectedData.toString().trim().equalsIgnoreCase(result.getData().toString().trim())) {

                } else {
                    throwErr("Metadata attribute "+expectedAttrib.getName()+" expecting "+expectedAttrib.getData().toString()
                        +" got "+ result.getData().toString());
                }
            }
        }
    }

    public static void compareProducts(final Product targetProduct,
                                       final Product expectedProduct) throws Exception {
        // compare updated metadata
        compareMetadata(targetProduct, expectedProduct, null);

        if(targetProduct.getNumBands() != expectedProduct.getNumBands())
            throwErr("Different number of bands");

        if(!targetProduct.isCompatibleProduct(expectedProduct, 0))
            throwErr("Geocoding is different");

        for(TiePointGrid expectedTPG : expectedProduct.getTiePointGrids()) {
            final TiePointGrid trgTPG = targetProduct.getTiePointGrid(expectedTPG.getName());
            if(trgTPG == null)
                throwErr("TPG "+expectedTPG.getName()+" not found");

            final float[] expectedTiePoints = expectedTPG.getTiePoints();
            final float[] trgTiePoints = trgTPG.getTiePoints();

            if(!Arrays.equals(trgTiePoints, expectedTiePoints)) {
                throwErr("TPGs are different in file "+expectedProduct.getFileLocation().getAbsolutePath());
            }
        }

        for(Band expectedBand : expectedProduct.getBands()) {

            final Band trgBand = targetProduct.getBand(expectedBand.getName());
            if(trgBand == null)
                throwErr("Band "+expectedBand.getName()+" not found");

            final float[] floatValues = new float[2500];
            trgBand.readPixels(40, 40, 50, 50, floatValues, ProgressMonitor.NULL);

            final float[] expectedValues = new float[2500];
            expectedBand.readPixels(40, 40, 50, 50, expectedValues, ProgressMonitor.NULL);

            if(!Arrays.equals(floatValues, expectedValues)) {
                throwErr("Pixels are different in file "+expectedProduct.getFileLocation().getAbsolutePath());
            }
        }
    }


    public static void compareProducts(final Product targetProduct,
                                       final String expectedPath, final String[] excemptionList) throws Exception {

        final Band targetBand = targetProduct.getBandAt(0);
        if(targetBand == null)
            throwErr("targetBand at 0 is null");

        // readPixels: execute computeTiles()
        final float[] floatValues = new float[2500];
        targetBand.readPixels(40, 40, 50, 50, floatValues, ProgressMonitor.NULL);

        // compare with expected outputs:
        final File expectedFile = new File(expectedPath);
        if(!expectedFile.exists()) {
            throwErr("Expected file not found "+expectedFile.toString());
        }

        final ProductReader reader2 = ProductIO.getProductReaderForFile(expectedFile);

        final Product expectedProduct = reader2.readProductNodes(expectedFile, null);
        final Band expectedBand = expectedProduct.getBandAt(0);

        final float[] expectedValues = new float[2500];
        expectedBand.readPixels(40, 40, 50, 50, expectedValues, ProgressMonitor.NULL);
        if(!Arrays.equals(floatValues, expectedValues)) {
                throwErr("Pixels are different in file "+expectedPath);
        }

        // compare updated metadata
        compareMetadata(targetProduct, expectedProduct, excemptionList);
    }

    public static void executeOperator(final Operator op) throws Exception {
        // get targetProduct: execute initialize()
        final Product targetProduct = op.getTargetProduct();
        TestUtils.verifyProduct(targetProduct, false, false);

        final Band targetBand = targetProduct.getBandAt(0);
        if(targetBand == null)
            throwErr("targetBand at 0 is null");

        final int bandWidth = targetBand.getSceneRasterWidth();
        final int bandHeight = targetBand.getSceneRasterHeight();

        // readPixels: execute computeTiles()
        final float[] floatValues = new float[10000];
        targetBand.readPixels(within(subsetX, bandWidth),
                              within(subsetY, bandHeight),
                              within(subsetWidth, bandWidth),
                              within(subsetHeight, bandHeight), 
                              floatValues, ProgressMonitor.NULL);
    }

    public static Product createSubsetProduct(final Product sourceProduct) throws IOException {
        final int bandWidth = sourceProduct.getSceneRasterWidth();
        final int bandHeight = sourceProduct.getSceneRasterHeight();

        final ProductSubsetBuilder subsetReader = new ProductSubsetBuilder();
        final ProductSubsetDef subsetDef = new ProductSubsetDef();

        subsetDef.addNodeNames(sourceProduct.getTiePointGridNames());

        final String bandName = ProductUtils.findSuitableQuicklookBandName(sourceProduct);
        subsetDef.addNodeNames(new String[] { bandName } );
        subsetDef.setRegion(within(subsetX, bandWidth),
                            within(subsetY, bandHeight),
                            within(subsetWidth, bandWidth),
                            within(subsetHeight, bandHeight));
        subsetDef.setIgnoreMetadata(false);
        subsetDef.setTreatVirtualBandsAsRealBands(true);

        final Product subsetProduct = subsetReader.readProductNodes(sourceProduct, subsetDef);
        final File tmpFile = new File(ResourceUtils.getApplicationUserTempDataDir(), "tmp_subset.dim");
        final WriteOp writer = new WriteOp(subsetProduct, tmpFile, DimapProductConstants.DIMAP_FORMAT_NAME);
        writer.writeProduct(ProgressMonitor.NULL);

        return ProductIO.readProduct(tmpFile);
    }

    private static int within(final int val, final int max) {
        return Math.max(0, Math.min(val, max));
    }

    public static void recurseFindReadableProducts(final File origFolder, final ArrayList<File> productList, int maxCount) throws Exception {


        final File[] folderList = origFolder.listFiles(ProductFunctions.directoryFileFilter);
        for(File folder : folderList) {
            if(!folder.getName().contains("skipTest")) {
                recurseFindReadableProducts(folder, productList, maxCount);
            }
        }

        final File[] fileList = origFolder.listFiles(new ProductFunctions.ValidProductFileFilter());
        for(File file : fileList) {
            if(maxCount > 0 && productList.size() >= maxCount)
                return;

            try {
                final ProductReader reader = ProductIO.getProductReaderForFile(file);
                if(reader != null) {
                    productList.add(file);
                } else {
                    System.out.println(file.getAbsolutePath() + " is non valid");
                }
            } catch(Exception e) {
                boolean ok = false;
               /* if(exceptionExemptions != null) {
                    for(String excemption : exceptionExemptions) {
                        if(e.getMessage().contains(excemption)) {
                            ok = true;
                            System.out.println("Excemption for "+e.getMessage());
                            break;
                        }
                    }
                }    */
                if(!ok) {
                    System.out.println("Failed to process "+ file.toString());
                    throw e;
                }
            }
        }
    }

    public static int recurseProcessFolder(final OperatorSpi spi, final File origFolder, int iterations,
                                            final String[] productTypeExemptions,
                                            final String[] exceptionExemptions) throws Exception {

        final File[] folderList = origFolder.listFiles(ProductFunctions.directoryFileFilter);
        for(File folder : folderList) {
            if(maxIteration > 0 && iterations >= maxIteration)
                break;
            if(!folder.getName().contains("skipTest")) {
                iterations = recurseProcessFolder(spi, folder, iterations, productTypeExemptions, exceptionExemptions);
            }
        }

        final File[] fileList = origFolder.listFiles(new ProductFunctions.ValidProductFileFilter());
        for(File file : fileList) {
            if(maxIteration > 0 && iterations >= maxIteration)
                break;

            try {
                final ProductReader reader = ProductIO.getProductReaderForFile(file);
                if(reader != null) {
                    final Product sourceProduct = reader.readProductNodes(file, null);
                    if(productTypeExemptions != null && containsProductType(productTypeExemptions, sourceProduct.getProductType()))
                        continue;

                    TestUtils.verifyProduct(sourceProduct, false, false);
                    
                    final Operator op = spi.createOperator();
                    op.setSourceProduct(sourceProduct);

                    TestUtils.log.info(spi.getOperatorAlias()+" Processing "+ file.toString());
                    TestUtils.executeOperator(op);

                    ++iterations;
                } else {
                    TestUtils.log.warning(file.getAbsolutePath() + " is non valid");
                }
            } catch(Exception e) {
                boolean ok = false;
                if(exceptionExemptions != null) {
                    for(String exemption : exceptionExemptions) {
                        if(e.getMessage().contains(exemption)) {
                            ok = true;
                            TestUtils.log.info("Exemption for "+e.getMessage());
                            break;
                        }
                    }
                }
                if(!ok) {
                    TestUtils.log.severe("Failed to process "+ file.toString());
                    throw e;
                }
            }
        }
        return iterations;
    }

    public static boolean containsProductType(final String[] productTypeExemptions, final String productType) {
        if(productTypeExemptions != null) {
            for (String str : productTypeExemptions) {
                if (productType.contains(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Processes all products in a folder
     * @param spi the OperatorSpi to create the operator
     * @param folderPath the path to recurse through
     * @param productTypeExemptions product types to ignore
     * @param exceptionExemptions exceptions that are ok and can be ignored for the test
     * @throws Exception general exception
     */
    public static void testProcessAllInPath(final OperatorSpi spi, final String folderPath,
                                            final String[] productTypeExemptions,
                                            final String[] exceptionExemptions) throws Exception
    {
        final File folder = new File(folderPath);
        if(!folder.exists()) return;

        if(canTestProcessingOnAllProducts) {
            int iterations = 0;
            recurseProcessFolder(spi, folder, iterations, productTypeExemptions, exceptionExemptions);
        }
    }

    /**
     * Processes all products in a folder
     * @param processor the RecursiveProcessor to create the graph
     * @param folderPath the path to recurse through
     * @param productTypeExemptions product types to ignore
     * @param exceptionExemptions exceptions that are ok and can be ignored for the test
     * @throws Exception general exception
     */
    public static void testProcessAllInPath(final RecursiveProcessor processor, final String folderPath,
                                            final String[] productTypeExemptions,
                                            final String[] exceptionExemptions) throws Exception
    {
        final File folder = new File(folderPath);
        if(!folder.exists()) return;

        if(canTestProcessingOnAllProducts) {
            int iterations = 0;
            processor.recurseProcessFolder(folder, iterations, productTypeExemptions, exceptionExemptions);
        }
    }

    private final static ProductFunctions.ValidProductFileFilter fileFilter = new ProductFunctions.ValidProductFileFilter(false);

    public static void recurseReadFolder(final File origFolder,
                                         final ProductReaderPlugIn readerPlugin,
                                         final ProductReader reader,
                                         final String[] productTypeExemptions,
                                         final String[] exceptionExemptions) throws Exception {
        recurseReadFolder(origFolder, readerPlugin, reader, productTypeExemptions, exceptionExemptions, 0);
    }

    public static int recurseReadFolder(final File origFolder,
                                         final ProductReaderPlugIn readerPlugin,
                                         final ProductReader reader,
                                         final String[] productTypeExemptions,
                                         final String[] exceptionExemptions,
                                         int iterations) throws Exception {
        final File[] folderList = origFolder.listFiles(ProductFunctions.directoryFileFilter);
        for(File folder : folderList) {
            if(!folder.getName().contains("skipTest")) {
                iterations = recurseReadFolder(folder, readerPlugin, reader, productTypeExemptions, exceptionExemptions, iterations);
                if(maxIteration > 0 && iterations >= maxIteration)
                    return iterations;
            }
        }

        final File[] files = origFolder.listFiles(fileFilter);
        for(File file : files) {
            if(readerPlugin.getDecodeQualification(file) == DecodeQualification.INTENDED) {

                try {
                    //System.out.println("Reading "+ file.toString());

                    final Product product = reader.readProductNodes(file, null);
                    if(productTypeExemptions != null && containsProductType(productTypeExemptions, product.getProductType()))
                        continue;
                    ReaderUtils.verifyProduct(product, true);
                    ++iterations;

                    if(maxIteration > 0 && iterations >= maxIteration)
                        break;
                } catch(Exception e) {
                    boolean ok = false;
                    if(exceptionExemptions != null) {
                        for(String excemption : exceptionExemptions) {
                            if(e.getMessage() != null && e.getMessage().contains(excemption)) {
                                ok = true;
                                System.out.println("Excemption for "+e.getMessage());
                                break;
                            }
                        }
                    }
                    if(!ok) {
                        System.out.println("Failed to read "+ file.toString());
                        throw e;
                    }
                }
            }
        }
        return iterations;
    }

    public static boolean skipTest(final TestCase obj) throws Exception {
        if(DEBUG) {
            System.out.println(obj.getClass().getName()+':'+obj.getName()+" skipped");
        }
        if(FailOnSkip) {
            throw new Exception(obj.getClass().getName()+':'+obj.getName()+" skipped");
        }
        return true;
    }

    private static void throwErr(final String description) throws Exception {
        throw new Exception(description);
    }
}
