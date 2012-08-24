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
package org.esa.nest.datamodel;

import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.dataop.maptransf.Datum;
import org.esa.nest.gpf.ReaderUtils;
import org.esa.nest.util.XMLSupport;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: lveci
 * Date: Aug 12, 2008
 * To change this template use File | Settings | File Templates.
 */
public final class AbstractMetadataIO {

     public static void Save(final Product product, final MetadataElement metadataElem, final File metadataFile) {

         final Element root = new Element("Metadata");
         final Document doc = new Document(root);

         if(metadataElem != null) {
             final Element AbstractedMetadataElem = new Element("AbstractedMetadata");
             root.addContent(AbstractedMetadataElem);
             XMLSupport.metadataElementToDOMElement(metadataElem, AbstractedMetadataElem);
         }
         if(product.getTiePointGrids().length > 0) {
             final Element tiePointGridsElem = new Element("tie-point-grids");
             root.addContent(tiePointGridsElem);
             writeTiePointGrids(product, tiePointGridsElem);
         }

         XMLSupport.SaveXML(doc, metadataFile.getAbsoluteFile().toString());
    }

    public static boolean Load(final Product product, final MetadataElement metadataElem, final File metadataFile)
        throws IOException {

        Document doc;
        try {
            doc = XMLSupport.LoadXML(metadataFile.getAbsolutePath());
        } catch(IOException e) {
            System.out.println(e.getMessage());
            return false;
        }

        final Element root = doc.getRootElement();
        final List elements = root.getContent();
        for(Object o : elements) {
            if(o instanceof Element) {
                final Element elem = (Element) o;
                if(elem.getName().equals("AbstractedMetadata"))
                    findAbstractedMetadata(elem.getContent(), metadataElem);
                else if(elem.getName().equals("tie-point-grids"))
                    parseTiePointGrids(product, elem);
            }
        }
        return true;
    }

    private static void findAbstractedMetadata(final List domChildren, final MetadataElement metadataElem) {
        for (Object aChild : domChildren) {
            if (aChild instanceof Element) {
                final Element child = (Element) aChild;
                final String childName = child.getName();
                if(child.getContentSize() > 0) {
                    MetadataElement subElem = metadataElem.getElement(childName);
                    if(subElem == null) {
                        subElem = new  MetadataElement(childName);
                        metadataElem.addElement(subElem);
                    }
                    findAbstractedMetadata(child.getContent(), subElem);
                } else if(childName.equals("attrib")) {
                    loadAttribute(child, metadataElem);
                }
            }
        }
    }

    private static void loadAttribute(final Element domElem, final MetadataElement rootElem) {

        final Attribute nameAttrib = domElem.getAttribute("name");
        if(nameAttrib == null) return;

        final String name = nameAttrib.getValue();
        if(name == null) return;

        final Attribute valueAttrib = domElem.getAttribute("value");
        if(valueAttrib == null) return;

        final Attribute typeAttrib = domElem.getAttribute("type");
        Integer typeFromFile = null;
        if(typeAttrib != null) {
            typeFromFile = Integer.parseInt(typeAttrib.getValue());
        }

        MetadataAttribute metaAttrib = rootElem.getAttribute(name);
        if(metaAttrib == null) {
            if(typeFromFile != null) {
                metaAttrib = new MetadataAttribute(name, typeFromFile);
                rootElem.addAttribute(metaAttrib);
            } else {
                return;                
            }
        }

        final int type = metaAttrib.getDataType();
        if(type == ProductData.TYPE_ASCII) {
            String valStr = valueAttrib.getValue();
            if(valStr == null || valStr.isEmpty())
                valStr = " ";
            metaAttrib.getData().setElems(valStr);
        } else if(type == ProductData.TYPE_UTC) {
                metaAttrib.getData().setElems(AbstractMetadata.parseUTC(valueAttrib.getValue()).getArray());
        } else if(type == ProductData.TYPE_FLOAT64 || type == ProductData.TYPE_FLOAT32) {
            metaAttrib.getData().setElemDouble(Double.parseDouble(valueAttrib.getValue()));
        } else if(type == ProductData.TYPE_INT8 && typeFromFile != null && typeFromFile == ProductData.TYPE_ASCII) {
            metaAttrib.getData().setElems(valueAttrib.getValue());  
        } else {
            metaAttrib.getData().setElemInt(Integer.parseInt(valueAttrib.getValue()));
        }

        final Attribute unitAttrib = domElem.getAttribute("unit");
        final Attribute descAttrib = domElem.getAttribute("desc");

        if(descAttrib != null)
            metaAttrib.setDescription(descAttrib.getValue());
        if(unitAttrib != null)
            metaAttrib.setUnit(unitAttrib.getValue());
    }

    private static void parseTiePointGrids(final Product product, final Element tpgElem) throws IOException {
        final List tpgElements = tpgElem.getContent();
        for(Object o : tpgElements) {
            if(!(o instanceof Element)) continue;

            final Element elem = (Element) o;
            final String name = elem.getName();
            final List content = elem.getContent();
            final List<Float> valueList = new ArrayList<Float>();
            int columnCount = 0;
            int rowCount = 0;
            for(Object row : content) {
                if(!(row instanceof Element)) continue;
                final Element rowElem = (Element) row;
                final Attribute value = rowElem.getAttribute("value");

                int columns = parseTiePointGirdRow(value.getValue(), valueList);
                if(columnCount == 0)
                    columnCount = columns;
                else if(columnCount != columns)
                    throw new IOException("Metadata for tie-point-grid "+name+" has incorrect number of columns");
                ++rowCount;
            }

            addTiePointGrid(product, name, valueList, columnCount, rowCount);
        }

        // update GeoCoding
        setGeoCoding(product);
    }

    private static void setGeoCoding(final Product product) {
        final TiePointGrid[] grids = product.getTiePointGrids();
        TiePointGrid latGrid = null;
        TiePointGrid lonGrid = null;
        for(TiePointGrid g : grids) {
            if(g.getName().toLowerCase().contains("lat"))
                latGrid = g;
            else if(g.getName().toLowerCase().contains("lon"))
                lonGrid = g;
        }
        if(latGrid != null && lonGrid != null) {
            final TiePointGeoCoding tpGeoCoding = new TiePointGeoCoding(latGrid, lonGrid, Datum.WGS_84);

            product.setGeoCoding(tpGeoCoding);
        }
    }

    private static int parseTiePointGirdRow(final String line, final List<Float> valueList) {
        final StringTokenizer tokenizer = new StringTokenizer(line, ",");
        int tokenCount = 0;
        while(tokenizer.hasMoreTokens()) {
            valueList.add(Float.parseFloat(tokenizer.nextToken()));
            ++tokenCount;
        }
        return tokenCount;
    }

    private static void addTiePointGrid(final Product product, final String name, final List<Float> valueList,
                                        final int inputWidth, final int inputHeight) {
        int gridWidth = inputWidth;
        int gridHeight = inputHeight;
        if(gridWidth < 5)
            gridWidth *= 5;
        if(gridHeight < 5)
            gridHeight *= 5;

        final float subSamplingX = (float)product.getSceneRasterWidth() / (float)(gridWidth - 1);
        final float subSamplingY = (float)product.getSceneRasterHeight() / (float)(gridHeight - 1);

        final float[] inPoints = new float[valueList.size()];
        final float[] outPoints = new float[gridWidth*gridHeight];
        int i = 0;
        for(Float val : valueList) {
            inPoints[i++] = val;
        }

        ReaderUtils.createFineTiePointGrid(inputWidth, inputHeight, gridWidth, gridHeight, inPoints, outPoints);

        final TiePointGrid newTPG = new TiePointGrid(name, gridWidth, gridHeight, 0, 0,
                subSamplingX, subSamplingY, outPoints);

        product.addTiePointGrid(newTPG);
    }

    private static void writeTiePointGrids(final Product product, final Element root) {
        final TiePointGrid[] grids = product.getTiePointGrids();

        for(TiePointGrid g : grids) {
            final Element gridElem = new Element(g.getName());
            root.addContent(gridElem);

            final String unit = g.getUnit();
            final String desc = g.getDescription();
            gridElem.setAttribute("unit", unit == null ? "" : unit);
            gridElem.setAttribute("desc", desc == null ? "" : desc);

            final int width = g.getRasterWidth();
            final int height = g.getRasterHeight();
            final float[] tiePoints = g.getTiePoints();
            int index = 0;
            for(int r = 0; r < height; ++r) {
                final Element rowElem = new Element("row");
                gridElem.addContent(rowElem);

                final StringBuilder valueStrBld = new StringBuilder();
                for(int c = 0; c < width; ++c) {
                    valueStrBld.append(tiePoints[index++]);
                    if(c < width-1)
                        valueStrBld.append(',');
                }

                rowElem.setAttribute("value", valueStrBld.toString());
            }
        }
    }

    /**
     * Add metadata from an XML file into the Metadata of the product
     * @param xmlRoot root element of xml file
     * @param metadataRoot MetadataElement to place it into
     */
    public static void AddXMLMetadata(final Element xmlRoot, final MetadataElement metadataRoot) {

        final String rootName = xmlRoot.getName();
        final boolean rootChildrenEmpty = xmlRoot.getChildren().isEmpty();
        if(rootChildrenEmpty && xmlRoot.getAttributes().isEmpty()) {
            if(!xmlRoot.getValue().isEmpty()) {
                addAttribute(metadataRoot, rootName, xmlRoot.getValue());
            }
        } else if(rootChildrenEmpty) {
            final MetadataElement metaElem = new MetadataElement(rootName);

            if(!xmlRoot.getValue().isEmpty())
                addAttribute(metaElem, rootName, xmlRoot.getValue());

            final List<Attribute> xmlAttribs = xmlRoot.getAttributes();
            for (Attribute aChild : xmlAttribs) {
                addAttribute(metaElem, aChild.getName(), aChild.getValue());
            }

            metadataRoot.addElement(metaElem);
        } else {
            final MetadataElement metaElem = new MetadataElement(rootName);

            final List children = xmlRoot.getContent();
            for (Object aChild : children) {
                if (aChild instanceof Element) {
                    AddXMLMetadata((Element) aChild, metaElem);
                } else if(aChild instanceof Attribute) {
                    final Attribute childAtrrib = (Attribute) aChild;
                    addAttribute(metaElem, childAtrrib.getName(), childAtrrib.getValue());
                }
            }

            final List<Attribute> xmlAttribs = xmlRoot.getAttributes();
            for (Attribute aChild : xmlAttribs) {
                addAttribute(metaElem, aChild.getName(), aChild.getValue());
            }

            metadataRoot.addElement(metaElem);
        }
    }

    private static void addAttribute(final MetadataElement meta, final String name, String value) {
        try {
            final MetadataAttribute attribute = new MetadataAttribute(name, ProductData.TYPE_ASCII, 1);
            if(value.isEmpty())
                value = " ";
            attribute.getData().setElems(value);
            meta.addAttribute(attribute);
        } catch(Exception e) {
            System.out.println(e.getMessage()+" "+name +" "+ value);
        }
    }
}