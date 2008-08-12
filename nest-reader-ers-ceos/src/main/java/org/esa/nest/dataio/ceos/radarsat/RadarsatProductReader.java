package org.esa.nest.dataio.ceos.radarsat;

import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.nest.dataio.ceos.CEOSProductDirectory;
import org.esa.nest.dataio.ceos.CEOSProductReader;
import org.esa.nest.dataio.ceos.IllegalCeosFormatException;

import java.io.File;
import java.io.IOException;

/**
 * The product reader for Radarsat products.
 *
 */
public class RadarsatProductReader extends CEOSProductReader {

    /**
     * Constructs a new abstract product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be <code>null</code> for internal reader
     *                     implementations
     */
    public RadarsatProductReader(final ProductReaderPlugIn readerPlugIn) {
       super(readerPlugIn);
    }

    protected CEOSProductDirectory createProductDirectory(File inputFile) throws IOException, IllegalCeosFormatException {
        return new RadarsatProductDirectory(inputFile.getParentFile());
    }

    DecodeQualification checkProductQualification(File file) {

        try {
            _dataDir = createProductDirectory(file);

            RadarsatProductDirectory dataDir = (RadarsatProductDirectory)_dataDir;
            if(dataDir.isRadarsat())
                return DecodeQualification.INTENDED;
            return DecodeQualification.SUITABLE;

        } catch (Exception e) {
            return DecodeQualification.UNABLE;
        }
    }
}