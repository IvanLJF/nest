/*
 * Copyright (C) 2012 by Array Systems Computing Inc. http://www.array.ca
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
package org.esa.nest.dataio.dem;

import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.FileUtils;
import org.esa.beam.visat.VisatApp;
import org.esa.nest.gpf.StatusProgressMonitor;
import org.esa.nest.util.ResourceUtils;
import org.esa.nest.util.ftpUtils;

import java.io.*;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Holds information about a dem file.
 */
public abstract class ElevationFile {

    protected File localFile;
    private final File localZipFile;
    private final ProductReader productReader;
    protected boolean localFileExists = false;
    protected boolean remoteFileExists = true;
    private boolean errorInLocalFile = false;
    private ElevationTile tile = null;
    private ftpUtils ftp = null;
    private Map<String, Long> fileSizeMap = null;
    private boolean unrecoverableError = false;

    public ElevationFile(final File localFile, final ProductReader reader) {
        this.localFile = localFile;
        this.localZipFile = new File(localFile.getParentFile(),
                    FileUtils.getFilenameWithoutExtension(localFile)+".zip");
        this.productReader = reader;
    }

    public void dispose() {
        try {
            if(ftp != null)
                ftp.disconnect();
            ftp = null;
            tile.dispose();
            tile = null;
        } catch(Exception e) {
            //
        }
    }

    public String getFileName() {
        return localFile.getName();
    }

    public ElevationTile getTile() throws IOException {
        if(tile == null) {
            if(!remoteFileExists && !localFileExists)
                return null;
            getFile();
        }
        return tile;
    }

    protected ElevationTile createTile(final Product product) {
        return null;
    }

    protected boolean findLocalFile() {
        if ((localFile.exists() && localFile.isFile()) || (localZipFile.exists() && localZipFile.isFile())) {
            return true;
        }
        return false;
    }

    private synchronized void getFile() throws IOException {
        try {
            if(tile != null) return;
            if(!localFileExists && !errorInLocalFile) {
                localFileExists = findLocalFile();
            }
            if(localFileExists) {
                getLocalFile();
            } else if(remoteFileExists) {
                if(getRemoteFile()) {
                    getLocalFile();
                }
            }
            if(tile != null) {
                errorInLocalFile = false;
            } else {
                if(!remoteFileExists && localFileExists) {
                    System.out.println("Unable to reader product "+localFile.getAbsolutePath());
                }
                localFileExists = false;
                errorInLocalFile = true;
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
            tile = null;
            localFileExists = false;
            errorInLocalFile = true;
            if(unrecoverableError) {
                throw new IOException(e);
            }
        }
    }

    private void getLocalFile() throws IOException {
        File dataFile = localFile;
        if(!dataFile.exists())
            dataFile = getFileFromZip(localZipFile);
        if(dataFile != null) {
            final Product product = productReader.readProductNodes(dataFile, null);
            if(product != null) {
                tile = createTile(product);
            }
        }
    }

    protected abstract String getRemoteFTP();

    protected abstract String getRemotePath();

    protected abstract boolean getRemoteFile() throws IOException;

    protected boolean getRemoteHttpFile(final String baseUrl) throws IOException {
        final VisatApp visatApp = VisatApp.getApp();
        final String remotePath = baseUrl+localZipFile.getName();
        System.out.println("http retrieving "+remotePath);
        try {
            if(visatApp != null)
                visatApp.setStatusBarMessage("Downloading "+localZipFile.getName());

            downloadFile(new URL(remotePath), localZipFile);

            if(visatApp != null)
                    visatApp.setStatusBarMessage("");
            return true;
        } catch(Exception e) {
            System.out.println("http error:"+e.getMessage() + " on " + remotePath);
            remoteFileExists = false;
        }
        return false;
    }

    /**
     * Downloads a file from the specified URL to the specified local target directory.
     * The method uses a Swing progress monitor to visualize the download process.
     * @param fileUrl the URL of the file to be downloaded
     * @param localZipFile the target file
     * @throws IOException if an I/O error occurs
     * @return File the downloaded file
     */
    private static File downloadFile(final URL fileUrl, final File localZipFile) throws IOException {
        final File outputFile = new File(localZipFile.getParentFile(), new File(fileUrl.getFile()).getName());
        final URLConnection urlConnection = fileUrl.openConnection();
        final int contentLength = urlConnection.getContentLength();
        final InputStream is = new BufferedInputStream(urlConnection.getInputStream(), contentLength);
        final OutputStream os;
        try {
            os = new BufferedOutputStream(new FileOutputStream(outputFile));
        } catch (IOException e) {
            is.close();
            throw e;
        }

        try {
            final StatusProgressMonitor status = new StatusProgressMonitor(contentLength,
                    "Downloading "+localZipFile.getName()+"... ");
            status.setAllowStdOut(false);

            final int size = 32768;
            final byte[] buf = new byte[size];
            int n;
            int total = 0;
            while ((n = is.read(buf, 0, size)) > -1)  {
                os.write(buf, 0, n);
                total += n;
                status.worked(total);
            }
            status.done();

            while (true) {
                final int b = is.read();
                if (b == -1) {
                    break;
                }
                os.write(b);
            }
        } catch (IOException e) {
            outputFile.delete();
            throw e;
        } finally {
            try {
                os.close();
            } finally {
                is.close();
            }
        }
        return outputFile;
    }

    protected boolean getRemoteFTPFile() throws IOException {
        try {
            if(ftp == null) {
                ftp = new ftpUtils(getRemoteFTP());
                fileSizeMap = ftpUtils.readRemoteFileList(ftp, getRemoteFTP(), getRemotePath());
            }

            final String remoteFileName = localZipFile.getName();
            final Long fileSize = fileSizeMap.get(remoteFileName);

            final ftpUtils.FTPError result = ftp.retrieveFile(getRemotePath() + remoteFileName, localZipFile, fileSize);
            if(result == ftpUtils.FTPError.OK) {
                return true;
            } else {
                if(result == ftpUtils.FTPError.FILE_NOT_FOUND) {
                    remoteFileExists = false;
                } else {
                    dispose();
                }
                localZipFile.delete();
            }
            return false;
        } catch(SocketException e) {
            unrecoverableError = true;
            throw e;
        } catch(Exception e) {
            System.out.println(e.getMessage());
            if(ftp == null) {
                unrecoverableError = false;      // allow to continue
                remoteFileExists = false;
                throw new IOException("Failed to connect to FTP "+ getRemoteFTP()+
                                      "\n"+e.getMessage());
            }
            dispose();
        }
        return false;
    }

    protected File getFileFromZip(final File dataFile) throws IOException {
        final String ext = FileUtils.getExtension(dataFile.getName());
        if (ext.equalsIgnoreCase(".zip")) {
            final String baseName = localFile.getName();
            final File newFile = new File(ResourceUtils.getApplicationUserTempDataDir(), baseName);
            if(newFile.exists())
                return newFile;

            ZipFile zipFile = null;
            BufferedOutputStream fileoutputstream = null;
            try {
                zipFile = new ZipFile(dataFile);
                fileoutputstream = new BufferedOutputStream(new FileOutputStream(newFile));

                ZipEntry zipEntry = zipFile.getEntry(baseName);
                if (zipEntry == null) {
                    zipEntry = zipFile.getEntry(baseName.toLowerCase());
                    if (zipEntry == null) {
                        final String folderName = FileUtils.getFilenameWithoutExtension(dataFile.getName());
                        zipEntry = zipFile.getEntry(folderName +'/'+ localFile.getName());
                        if (zipEntry == null) {
                            localFileExists = false;
                            throw new IOException("Entry '" + baseName + "' not found in zip file.");
                        }
                    }
                }

                final int size = 8192;
                final byte[] buf = new byte[size];
                InputStream zipinputstream = zipFile.getInputStream(zipEntry);

                int n;
                while ((n = zipinputstream.read(buf, 0, size)) > -1)
                    fileoutputstream.write(buf, 0, n);

                return newFile;
            } catch(Exception e) {
                System.out.println(e.getMessage());
                dataFile.delete();
                return null;
            } finally {
                if(zipFile != null)
                    zipFile.close();
                if(fileoutputstream != null)
                    fileoutputstream.close();
            }
        }
        return dataFile;
    }
}