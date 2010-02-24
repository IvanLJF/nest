package org.esa.nest.dataio.ceos.alos;

import junit.framework.TestCase;
import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.nest.dataio.ReaderUtils;
import org.esa.nest.util.TestUtils;

import java.io.File;

/**
 * Test ALOS PALSAR CEOS Product Reader.
 *
 * @author lveci
 */
public class TestAlosPalsarProductReader extends TestCase {

    private AlosPalsarProductReaderPlugIn readerPlugin;
    private ProductReader reader;

    public TestAlosPalsarProductReader(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        readerPlugin = new AlosPalsarProductReaderPlugIn();
        reader = readerPlugin.createReaderInstance();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        reader = null;
        readerPlugin = null;
    }

    /**
     * Open all files in a folder recursively
     * @throws Exception anything
     */
    public void testOpenAll() throws Exception
    {
        final File folder = new File(TestUtils.rootPathALOS);
        if(!folder.exists()) return;

        if(TestUtils.canTestReadersOnAllProducts())
            TestUtils.recurseReadFolder(folder, readerPlugin, reader, null, null);
    }

    /**
     * Open all files in a folder recursively
     * @throws Exception anything
     */
    public void testOpenAll2() throws Exception
    {
        final File folder = new File(TestUtils.rootPathALOS);
        if(!folder.exists()) return;

        if(TestUtils.canTestReadersOnAllProducts())
            recurseFolder(folder);
    }

    private void recurseFolder(File folder) throws Exception {
        for(File file : folder.listFiles()) {
            if(file.isDirectory()) {
                recurseFolder(file);
            } else if(readerPlugin.getDecodeQualification(file) == DecodeQualification.INTENDED) {

                try {
                    final Product product = reader.readProductNodes(file, null);
                    verifyProduct(product);
                } catch(Exception e) {
                    System.out.println("Failed to read "+ file.toString());
                    throw e;
                }
            }
        }
    }

    public static void verifyProduct(Product product) throws Exception {
        if(product == null)
            throw new Exception("product is null");
        if(product.getMetadataRoot() == null)
            throw new Exception("metadataroot is null");
        if(product.getNumBands() == 0)
            throw new Exception("numbands is zero");
        if(product.getProductType() == null || product.getProductType().isEmpty())
            throw new Exception("productType is null");
        if(product.getStartTime() == null)
            throw new Exception("startTime is null");

        for(Band b : product.getBands()) {
            if(b.getUnit() == null || b.getUnit().isEmpty())
                throw new Exception("band " + b.getName() + " has null unit");
        }
    }
}