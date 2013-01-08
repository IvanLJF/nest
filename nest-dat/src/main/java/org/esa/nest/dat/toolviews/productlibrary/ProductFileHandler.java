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
package org.esa.nest.dat.toolviews.productlibrary;

import org.esa.beam.dataio.dimap.DimapProductConstants;
import org.esa.beam.util.io.FileUtils;
import org.esa.nest.db.ProductEntry;
import org.esa.nest.util.FileIOUtils;

import java.io.File;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Handle product files
 */
public class ProductFileHandler {

    private static final String[] singleFileExt = {"n1","e1","e2","tif","tiff"};
    private static final String[] folderExt = {"safe"};
    private static final String[] folderMissions = {"RS2","TSX","TDX","CSKS1","CSKS2","CSKS3","CSKS4",
                                                    "ALOS","JERS1","RS1"};

    public static boolean canMove(final ProductEntry entry) {
        return isDimap(entry) || isFolderProduct(entry) || isSingleFile(entry) || isSMOS(entry);
    }

    public static void copyTo(final ProductEntry entry, final File targetFolder) throws Exception {

        if(isDimap(entry)) {
            final File newFile = new File(targetFolder, entry.getFile().getName());
            Files.copy(entry.getFile().toPath(), newFile.toPath(), REPLACE_EXISTING);
            final String dataFolderName = entry.getFile().getName().replace(
                    DimapProductConstants.DIMAP_HEADER_FILE_EXTENSION,
                    DimapProductConstants.DIMAP_DATA_DIRECTORY_EXTENSION);
            final File oldDataFolder = new File(entry.getFile().getParentFile(), dataFolderName);
            final File newDataFolder = new File(targetFolder, dataFolderName);
            FileIOUtils.copyFolder(oldDataFolder.toPath(), newDataFolder.toPath());
        } else if(isSMOS(entry)) {
            final File newFile = new File(targetFolder, entry.getFile().getName());
            Files.copy(entry.getFile().toPath(), newFile.toPath(), REPLACE_EXISTING);
            final File hdrFile = FileUtils.exchangeExtension(entry.getFile(), ".HDR");
            final File newHdrFile = new File(targetFolder, hdrFile.getName());
            Files.copy(hdrFile.toPath(), newHdrFile.toPath(), REPLACE_EXISTING);
        } else if(isFolderProduct(entry)) {
            final File newFile = new File(targetFolder, entry.getFile().getParentFile().getName());
            FileIOUtils.copyFolder(entry.getFile().getParentFile().toPath(), newFile.toPath());
        } else if(isSingleFile(entry)) {
            final File newFile = new File(targetFolder, entry.getFile().getName());
            Files.copy(entry.getFile().toPath(), newFile.toPath(), REPLACE_EXISTING);
        }
    }

    public static void moveTo(final ProductEntry entry, final File targetFolder) throws Exception {

        if(isDimap(entry)) {
            final File newFile = new File(targetFolder, entry.getFile().getName());
            Files.move(entry.getFile().toPath(), newFile.toPath(), REPLACE_EXISTING, ATOMIC_MOVE);
            final String dataFolderName = entry.getFile().getName().replace(
                    DimapProductConstants.DIMAP_HEADER_FILE_EXTENSION,
                    DimapProductConstants.DIMAP_DATA_DIRECTORY_EXTENSION);
            final File oldDataFolder = new File(entry.getFile().getParentFile(), dataFolderName);
            final File newDataFolder = new File(targetFolder, dataFolderName);
            FileIOUtils.moveFolder(oldDataFolder.toPath(), newDataFolder.toPath());
        } else if(isSMOS(entry)) {
            final File newFile = new File(targetFolder, entry.getFile().getName());
            Files.move(entry.getFile().toPath(), newFile.toPath(), REPLACE_EXISTING, ATOMIC_MOVE);
            final File hdrFile = FileUtils.exchangeExtension(entry.getFile(), ".HDR");
            final File newHdrFile = new File(targetFolder, hdrFile.getName());
            Files.move(hdrFile.toPath(), newHdrFile.toPath(), REPLACE_EXISTING, ATOMIC_MOVE);
        } else if(isFolderProduct(entry)) {
            final File newFile = new File(targetFolder, entry.getFile().getParentFile().getName());
            FileIOUtils.moveFolder(entry.getFile().getParentFile().toPath(), newFile.toPath());
        } else if(isSingleFile(entry)) {
            final File newFile = new File(targetFolder, entry.getFile().getName());
            Files.move(entry.getFile().toPath(), newFile.toPath(), REPLACE_EXISTING, ATOMIC_MOVE);
        }
    }

    public static void delete(final ProductEntry entry) throws Exception {

        if(isDimap(entry)) {
            Files.delete(entry.getFile().toPath());
            final String dataFolderName = entry.getFile().getName().replace(
                    DimapProductConstants.DIMAP_HEADER_FILE_EXTENSION,
                    DimapProductConstants.DIMAP_DATA_DIRECTORY_EXTENSION);
            final File dataFolder = new File(entry.getFile().getParentFile(), dataFolderName);
            FileIOUtils.deleteFolder(dataFolder.toPath());
        } else if(isSMOS(entry)) {
            Files.delete(entry.getFile().toPath());
            final File hdrFile = FileUtils.exchangeExtension(entry.getFile(), ".HDR");
            Files.delete(hdrFile.toPath());
        } else if(isFolderProduct(entry)) {
            FileIOUtils.deleteFolder(entry.getFile().getParentFile().toPath());
        } else if(isSingleFile(entry)) {
            Files.delete(entry.getFile().toPath());
        }
    }

    private static boolean isDimap(final ProductEntry entry) {
        return entry.getFile().getName().endsWith(DimapProductConstants.DIMAP_HEADER_FILE_EXTENSION);
    }

    private static boolean isSMOS(final ProductEntry entry) {
        return entry.getFile().getName().toUpperCase().endsWith("DBL");
    }

    private static boolean isSingleFile(final ProductEntry entry) {
        final String fileName = entry.getFile().getName().toLowerCase();
        for(String ext : singleFileExt) {
            if(fileName.endsWith(ext))
                return true;
        }
        return false;
    }

    private static boolean isFolderProduct(final ProductEntry entry) {
        final String mission = entry.getMission();
        for(String folderMission : folderMissions) {
            if(mission.equals(folderMission))
                return true;
        }
        final String fileName = entry.getFile().getName().toLowerCase();
        for(String ext : folderExt) {
            if(fileName.endsWith(ext))
                return true;
        }
        if(mission.equals("ERS1") || mission.equals("ERS2")) {
            if(!isSingleFile(entry))  // if not .e1 or .e2
                return true;
        }
        return false;
    }
}
