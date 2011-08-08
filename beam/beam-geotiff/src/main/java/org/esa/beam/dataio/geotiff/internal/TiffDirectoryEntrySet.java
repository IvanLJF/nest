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

package org.esa.beam.dataio.geotiff.internal;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A directroy entry set implementation for the GeoTIFF format.
 *
 * @author Marco Peters
 * @author Sabine Embacher
 * @author Norman Fomferra
 * @version $Revision: 1.3 $ $Date: 2010-12-23 14:35:09 $
 */
class TiffDirectoryEntrySet {

    private final SortedMap entryMap;

    public TiffDirectoryEntrySet() {
        entryMap = new TreeMap();
    }

    public void set(final TiffDirectoryEntry entry) {
        final Integer key = getKey(entry);
        entryMap.put(key, entry);
    }

    public TiffDirectoryEntry[] getEntries() {
        return (TiffDirectoryEntry[]) entryMap.values().toArray(new TiffDirectoryEntry[entryMap.size()]);
    }

    public TiffDirectoryEntry getEntry(final TiffShort tag) {
        final Integer key = getKey(tag);
        return (TiffDirectoryEntry) entryMap.get(key);
    }

    private static Integer getKey(final TiffDirectoryEntry entry) {
        return getKey(entry.getTag());
    }

    private static Integer getKey(final TiffShort tag) {
        return tag.getValue();
    }
}
