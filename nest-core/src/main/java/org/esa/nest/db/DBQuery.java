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
package org.esa.nest.db;

import org.esa.beam.framework.datamodel.GeoPos;
import org.esa.beam.util.Debug;
import org.esa.beam.util.StringUtils;
import org.esa.nest.datamodel.AbstractMetadata;
import org.esa.nest.util.SQLUtils;
import org.esa.nest.util.XMLSupport;
import org.jdom.Attribute;
import org.jdom.Element;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

/**

 */
public class DBQuery {

    public static final String ALL_MISSIONS = "All_Missions";
    public static final String ALL_PRODUCT_TYPES = "All_Types";
    public static final String ALL_PASSES = "All_Passes";
    public static final String ALL_MODES = "All_Modes";
    public static final String ASCENDING_PASS = "ASCENDING";
    public static final String DESCENDING_PASS = "DESCENDING";
    public static final String ALL_FOLDERS = "All_Folders";
    public static final String ANY = "Any";
    public static final String DUALPOL = "Dual-Pol";
    public static final String QUADPOL = "Quad-Pol";
    public static final String CALIBRATED = "Calibrated";
    public static final String NOT_CALIBRATED = "Not_Calibrated";
    public static final String ORBIT_PRELIMINARY = "Preliminary";
    public static final String ORBIT_PRECISE = "Precise";
    public static final String ORBIT_VERIFIED = "Verified";
    public static final String DB_QUERY = "dbQuery";

    private String selectedMissions[] = {};
    private String selectedProductTypes[] = {};
    private String selectedAcquisitionMode = "";
    private String selectedPass = "";
    private String selectedTrack = "";
    private String selectedSampleType = "";
    private String selectedPolarization = ANY;
    private String selectedCalibration = ANY;
    private String selectedOrbitCorrection = ANY;
    private Rectangle.Float selectionRectangle = null;
    private File baseDir = null;
    private File excludeDir = null;
    private Calendar startDate = null;
    private Calendar endDate = null;
    private String freeQuery = "";

    private final Map<String, String> metadataQueryMap = new HashMap<String, String>();

    public DBQuery() {
    }

    public void setSelectedMissions(final String[] missions) {
        selectedMissions = missions;
    }

    public String[] getSelectedMissions() {
        return selectedMissions;
    }

    public void setSelectedProductTypes(final String[] productTypes) {
        selectedProductTypes = productTypes;
    }

    public String[] getSelectedProductTypes() {
        return selectedProductTypes;
    }

    public void setSelectedAcquisitionMode(final String mode) {
        if(mode != null)
            selectedAcquisitionMode = mode;
    }

    public String getSelectedAcquisitionMode() {
        return selectedAcquisitionMode;
    }

    public void setSelectedPass(final String pass) {
        if(pass != null)
            selectedPass = pass;
    }

    public String getSelectedPass() {
        return selectedPass;
    }

    public void setSelectedTrack(final String track) {
        if(track != null)
            selectedTrack = track;
    }

    public String getSelectedTrack() {
        return selectedTrack;
    }

    public void setSelectedSampleType(final String sampleType) {
        if(sampleType != null)
            selectedSampleType = sampleType;
    }

    public String getSelectedSampleType() {
        return selectedSampleType;
    }

    public void setSelectedPolarization(final String pol) {
        if(pol != null)
            selectedPolarization = pol;
    }

    public String getSelectedPolarization() {
        return selectedPolarization;
    }

    public void setSelectedCalibration(final String calib) {
        if(calib != null)
            selectedCalibration = calib;
    }

    public String getSelectedCalibration() {
        return selectedCalibration;
    }

    public void setSelectedOrbitCorrection(final String orbitCor) {
        if(orbitCor != null)
            selectedOrbitCorrection = orbitCor;
    }

    public String getSelectedOrbitCorrection() {
        return selectedOrbitCorrection;
    }

    public void setBaseDir(final File dir) {
        baseDir = dir;
    }

    public void setExcludeDir(final File dir) {
        excludeDir = dir;
    }

    public void setStartEndDate(final Calendar start, final Calendar end) {
        startDate = start;
        endDate = end;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void clearMetadataQuery() {
        metadataQueryMap.clear();
    }

    public void addMetadataQuery(final String name, final String value) {
        metadataQueryMap.put(name, value);
    }

    public void setFreeQuery(final String queryStr) {
        freeQuery = queryStr;
    }

    public String getFreeQuery() {
        return freeQuery;
    }

    public ProductEntry[] queryDatabase(final ProductDB db) throws SQLException {

        if(StringUtils.contains(selectedMissions, ALL_MISSIONS))
            selectedMissions = new String[] {};
        if(StringUtils.contains(selectedProductTypes, ALL_PRODUCT_TYPES))
            selectedProductTypes = new String[] {};

        final StringBuilder queryStr = new StringBuilder(1000);
        if(selectedMissions.length > 0) {
            queryStr.append(SQLUtils.getOrList(ProductTable.TABLE+'.'+AbstractMetadata.MISSION, selectedMissions));
        }
        if(selectedProductTypes.length > 0) {
            SQLUtils.addAND(queryStr);
            queryStr.append(SQLUtils.getOrList(ProductTable.TABLE+'.'+AbstractMetadata.PRODUCT_TYPE, selectedProductTypes));
        }
        if(!selectedAcquisitionMode.isEmpty() && !selectedAcquisitionMode.equals(ALL_MODES)) {
            SQLUtils.addAND(queryStr);
            queryStr.append(ProductTable.TABLE+'.'+AbstractMetadata.ACQUISITION_MODE+"='"+selectedAcquisitionMode+ '\'');
        }
        if(!selectedPass.isEmpty() && !selectedPass.equals(ALL_PASSES)) {
            SQLUtils.addAND(queryStr);
            queryStr.append(ProductTable.TABLE+'.'+AbstractMetadata.PASS+"='"+selectedPass+ '\'');
        }
        if(!selectedTrack.isEmpty()) {
            SQLUtils.addAND(queryStr);
            queryStr.append("( "+ MetadataTable.TABLE+'.'+AbstractMetadata.REL_ORBIT+'='+selectedTrack+" )");
        }
        if(!selectedSampleType.isEmpty() && !selectedSampleType.equals(ANY)) {
            SQLUtils.addAND(queryStr);
            queryStr.append("( "+ MetadataTable.TABLE+'.'+AbstractMetadata.SAMPLE_TYPE+"='"+selectedSampleType+"' )");
        }
        if(!selectedPolarization.isEmpty() && !selectedPolarization.equals(ANY)) {
            SQLUtils.addAND(queryStr);
            if(selectedPolarization.equals(DUALPOL)) {
               queryStr.append( "( "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.mds1_tx_rx_polar+"!='' AND "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.mds2_tx_rx_polar+"!='' AND "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.mds3_tx_rx_polar+"='' AND "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.mds4_tx_rx_polar+"='' )");
            } else if(selectedPolarization.equals(QUADPOL)) {
                queryStr.append( "( "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.mds1_tx_rx_polar+"!='' AND "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.mds2_tx_rx_polar+"!='' AND "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.mds3_tx_rx_polar+"!='' AND "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.mds4_tx_rx_polar+"!='' )");
            } else {
                queryStr.append("( "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.mds1_tx_rx_polar+"='"+selectedPolarization+ '\'' + " OR "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.mds2_tx_rx_polar+"='"+selectedPolarization+ '\'' + " OR "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.mds3_tx_rx_polar+"='"+selectedPolarization+ '\'' + " OR "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.mds4_tx_rx_polar+"='"+selectedPolarization+ '\'' + " )");
            }
        }
        if(!selectedCalibration.isEmpty() && !selectedCalibration.equals(ANY)) {
            SQLUtils.addAND(queryStr);
            if(selectedCalibration.equals(CALIBRATED))
                queryStr.append(MetadataTable.TABLE+'.'+AbstractMetadata.abs_calibration_flag+"=1");
            else if(selectedCalibration.equals(NOT_CALIBRATED))
                queryStr.append(MetadataTable.TABLE+'.'+AbstractMetadata.abs_calibration_flag+"=0");
        }
        if(!selectedOrbitCorrection.isEmpty() && !selectedOrbitCorrection.equals(ANY)) {
            formOrbitCorrectionQuery(queryStr);
        }

        if(startDate != null) {
            SQLUtils.addAND(queryStr);
            final Date start = SQLUtils.toSQLDate(startDate);
            if(endDate != null) {
                final Date end = SQLUtils.toSQLDate(endDate);
                queryStr.append("( "+ProductTable.TABLE+'.'+AbstractMetadata.first_line_time
                        +" BETWEEN '"+ start.toString() +"' AND '"+ end.toString() + "' )");
            } else {
                queryStr.append(ProductTable.TABLE+'.'+AbstractMetadata.first_line_time +">='"+ start.toString()+ '\'');
            }
        } else if(endDate != null) {
            SQLUtils.addAND(queryStr);
            final Date end = SQLUtils.toSQLDate(endDate);
            queryStr.append(ProductTable.TABLE+'.'+AbstractMetadata.first_line_time +"<='"+ end.toString()+ '\'');
        }

        final Set<String> metadataNames = metadataQueryMap.keySet();
        for(String name : metadataNames) {
            final String value = metadataQueryMap.get(name);
            if(value != null && !value.isEmpty()) {
                SQLUtils.addAND(queryStr);
                queryStr.append(MetadataTable.TABLE+'.'+name+"='"+value+ '\'');
            }
        }

        if(!freeQuery.isEmpty()) {
            SQLUtils.addAND(queryStr);
            final String metadataFreeQuery = SQLUtils.insertTableName(db.getMetadataNames(), MetadataTable.TABLE, freeQuery);
            queryStr.append("( "+metadataFreeQuery+" )");
        }

        if(baseDir != null) {
            SQLUtils.addAND(queryStr);
            queryStr.append(ProductTable.TABLE+'.'+AbstractMetadata.PATH+" LIKE '"+baseDir.getAbsolutePath()+"%'");
        }
        if(excludeDir != null) {
            SQLUtils.addAND(queryStr);
            queryStr.append(ProductTable.TABLE+'.'+AbstractMetadata.PATH+" NOT LIKE '"+excludeDir.getAbsolutePath()+"%'");
        }

        if(queryStr.length() > 0) {
            Debug.trace("Query="+queryStr);
            return instersectMapSelection(db.queryProduct(queryStr.toString()));
        } else {
            return instersectMapSelection(db.getProductEntryList(true));
        }
    }

    private void formOrbitCorrectionQuery(final StringBuilder queryStr) {
        SQLUtils.addAND(queryStr);
        if(selectedOrbitCorrection.equals(ORBIT_VERIFIED)) {
            queryStr.append(MetadataTable.TABLE+'.'+AbstractMetadata.orbit_state_vector_file+" LIKE 'DORIS Verified%'");
        } else if(selectedOrbitCorrection.equals(ORBIT_PRECISE)) {
            queryStr.append("( "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.orbit_state_vector_file+" LIKE 'DORIS Precise%' OR "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.orbit_state_vector_file+" LIKE 'DELFT Precise%' OR "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.orbit_state_vector_file+" LIKE 'PRARE Precise%'"+ " )");
        } else if(selectedOrbitCorrection.equals(ORBIT_PRELIMINARY)) {
            queryStr.append("( "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.orbit_state_vector_file+" NOT LIKE 'DORIS%' AND "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.orbit_state_vector_file+" NOT LIKE 'DELFT%' AND "+
                    MetadataTable.TABLE+'.'+AbstractMetadata.orbit_state_vector_file+" NOT LIKE 'PRARE%'"+ " )");
        }
    }

    public void setSelectionRect(final GeoPos[] selectionBox) {
        selectionRectangle = getBoundingRect(selectionBox);
    }

    private ProductEntry[] instersectMapSelection(final ProductEntry[] resultsList) {

        if(selectionRectangle != null && selectionRectangle.getWidth() != 0 && selectionRectangle.getHeight() != 0) {
            final List<ProductEntry> intersectList = new ArrayList<ProductEntry>(resultsList.length);

            //System.out.println("selBox x="+selectionRectangle.getX()+" y="+selectionRectangle.getY()+
            //                         " w="+selectionRectangle.getWidth()+" h="+selectionRectangle.getHeight());
            for(ProductEntry entry : resultsList) {
                final GeoPos start = entry.getFirstNearGeoPos();
                final GeoPos end = entry.getLastFarGeoPos();
                if(selectionRectangle.contains(new Point2D.Float(start.getLat(), start.getLon())) ||
                        selectionRectangle.contains(new Point2D.Float(end.getLat(), end.getLon()))) {
                    intersectList.add(entry);
                } else {
                    final GeoPos[] geoBoundary = entry.getGeoBoundary();
                    boolean found = false;
                    // for all points
                    boolean allPoints = true;
                    for(GeoPos pos : geoBoundary) {
                        if(!selectionRectangle.contains(new Point2D.Float(pos.getLat(), pos.getLon()))) {
                            allPoints = false;
                            break;
                        }
                    }
                    if(allPoints) {
                        intersectList.add(entry);
                        found = true;
                    }

                    // for any points
                   /* for(GeoPos pos : geoBoundary) {
                        if(selectionRectangle.contains(new Point2D.Float(pos.getLat(), pos.getLon()))) {
                            intersectList.add(entry);
                            found = true;
                            break;
                        }
                    }  */
                 /*   if(!found) {
                        // check if path intersect selection box
                        final List<GeneralPath> pathList = ProductUtils.assemblePathList(geoBoundary);

                        for(GeneralPath path : pathList) {
                            if(path.contains(selectionRectangle.getCenterX(), selectionRectangle.getCenterY())) {
                                intersectList.add(entry);
                                break;
                            }
                        }
                    }     */
                }
            }
            return intersectList.toArray(new ProductEntry[intersectList.size()]);
        }
        return resultsList;
    }

    public static Rectangle.Float getBoundingRect(final GeoPos[] geoPositions) {
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (final GeoPos pos : geoPositions) {
            final float x = pos.getLat();
            final float y = pos.getLon();

            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
        }
        if (minX >= maxX || minY >= maxY) {
            return new Rectangle.Float(minX, minY, 0, 0);
        }

        return new Rectangle.Float(minX, minY, maxX - minX, maxY - minY);
    }

    public Element toXML() {
        final Element elem = new Element(DB_QUERY);
        final Element missionsElem = new Element("selectedMissions");
        elem.addContent(missionsElem);
        for(String m : selectedMissions) {
            missionsElem.addContent(new Element(m));
        }
        final Element productTypesElem = new Element("selectedProductTypes");
        elem.addContent(productTypesElem);
        for(String p : selectedProductTypes) {
            productTypesElem.addContent(new Element(p));
        }
        if(selectionRectangle != null) {
            final Element rectElem = new Element("selectionRectangle");
            elem.addContent(rectElem);
            rectElem.setAttribute("x", String.valueOf(selectionRectangle.getX()));
            rectElem.setAttribute("y", String.valueOf(selectionRectangle.getY()));
            rectElem.setAttribute("w", String.valueOf(selectionRectangle.getWidth()));
            rectElem.setAttribute("h", String.valueOf(selectionRectangle.getHeight()));
        }

        elem.setAttribute("selectedAcquisitionMode", selectedAcquisitionMode);
        elem.setAttribute("selectedPass", selectedPass);
        if(baseDir != null)
            elem.setAttribute("baseDir", baseDir.getAbsolutePath());
        if(startDate != null) {
            final Element startDateElem = new Element("startDate");
            elem.addContent(startDateElem);
            startDateElem.setAttribute("year", String.valueOf(startDate.get(Calendar.YEAR)));
            startDateElem.setAttribute("month", String.valueOf(startDate.get(Calendar.MONTH)));
            startDateElem.setAttribute("day", String.valueOf(startDate.get(Calendar.DAY_OF_MONTH)));
        }
        if(endDate != null) {
            final Element endDateElem = new Element("endDate");
            elem.addContent(endDateElem);
            endDateElem.setAttribute("year", String.valueOf(endDate.get(Calendar.YEAR)));
            endDateElem.setAttribute("month", String.valueOf(endDate.get(Calendar.MONTH)));
            endDateElem.setAttribute("day", String.valueOf(endDate.get(Calendar.DAY_OF_MONTH)));
        }
        elem.setAttribute("freeQuery", freeQuery);
        return elem;
    }

    public void fromXML(final Element dbQueryElem) {

        final Element missionsElem = dbQueryElem.getChild("selectedMissions");
        if(missionsElem != null) {
            selectedMissions = XMLSupport.getStringList(missionsElem);
        }
        final Element productTypesElem = dbQueryElem.getChild("selectedProductTypes");
        if(productTypesElem != null) {
            selectedProductTypes = XMLSupport.getStringList(productTypesElem);
        }
        final Element rectElem = dbQueryElem.getChild("selectionRectangle");
        if(rectElem != null) {
            final Attribute x = rectElem.getAttribute("x");
            final Attribute y = rectElem.getAttribute("y");
            final Attribute w = rectElem.getAttribute("w");
            final Attribute h = rectElem.getAttribute("h");
            if(x != null && y != null && w != null && h != null) {
                selectionRectangle = new Rectangle.Float(
                        Float.parseFloat(x.getValue()),
                        Float.parseFloat(y.getValue()),
                        Float.parseFloat(w.getValue()),
                        Float.parseFloat(h.getValue()));
            }
        }

        selectedAcquisitionMode = XMLSupport.getAttrib(dbQueryElem, "selectedAcquisitionMode");
        selectedPass = XMLSupport.getAttrib(dbQueryElem, "selectedPass");
        final String baseDirStr = XMLSupport.getAttrib(dbQueryElem, "baseDir");
        if(!baseDirStr.isEmpty())
            baseDir = new File(baseDirStr);
        final Element startDateElem = dbQueryElem.getChild("startDate");
        if(startDateElem != null) {
            startDate = getCalendarDate(startDateElem);
        }
        final Element endDateElem = dbQueryElem.getChild("endDate");
        if(endDateElem != null) {
            endDate = getCalendarDate(endDateElem);
        }
        freeQuery = XMLSupport.getAttrib(dbQueryElem, "freeQuery");
    }

    private static GregorianCalendar getCalendarDate(final Element elem) {
        final Attribute y = elem.getAttribute("year");
        final Attribute m = elem.getAttribute("month");
        final Attribute d = elem.getAttribute("day");
        if(y != null && m != null && d != null) {
            return new GregorianCalendar(Integer.parseInt(y.getValue()),
                    Integer.parseInt(m.getValue()),
                    Integer.parseInt(d.getValue()));
        }
        return null;
    }
}
