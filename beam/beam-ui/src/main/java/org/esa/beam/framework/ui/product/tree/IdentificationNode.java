package org.esa.beam.framework.ui.product.tree;

import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.MetadataAttribute;

class IdentificationNode extends ProductTreeNode {
    private MetadataElement metadataElement;
    private MetadataElement rootElement;

    IdentificationNode(String name, MetadataElement element, ProductTreeNode parent) {
        super(name, element, parent);

        rootElement = element;
        final MetadataElement absRoot = element.getElement("Abstracted_Metadata");
        if(absRoot != null)
            metadataElement = createIdentificationNodes(element, absRoot);
        else
            metadataElement = rootElement;
        metadataElement.setOwner(element.getProduct());
        super.setContent(metadataElement);
    }

    public MetadataElement getMetadataElement() {
        return metadataElement;
    }

    @Override
    public ProductTreeNode getChildAt(int index) {
        final MetadataElement elem = metadataElement.getElementAt(index);
        return new IdentificationNode(elem.getName(), elem, this);
    }

    @Override
    public int getChildCount() {
        return metadataElement.getNumElements();
    }

    @Override
    protected int getIndex(ProductTreeNode child) {
        IdentificationNode metadataNode = (IdentificationNode) child;
        MetadataElement[] metadataElements = metadataElement.getElements();
        for (int i = 0, metadataElementsLength = metadataElements.length; i < metadataElementsLength; i++) {
            MetadataElement element = metadataElements[i];
            if(element == metadataNode.getMetadataElement()) {
                return i;
            }
        }
        return -1;
    }

    private MetadataElement createIdentificationNodes(final MetadataElement rootElement, final MetadataElement absRoot) {
        final MetadataElement identNode = new MetadataElement("Identification");

        addIDNode(absRoot, identNode, "Mission", "MISSION");
        addIDNode(absRoot, identNode, "Type", "PRODUCT_TYPE");
        addIDNode(absRoot, identNode, "Acquisition", "first_line_time");
        addIDNode(absRoot, identNode, "Pass", "PASS");
        addIDNode(absRoot, identNode, "Track", "REL_ORBIT");
        addIDNode(absRoot, identNode, "Orbit", "ABS_ORBIT");

        final MetadataElement slaveRoot = rootElement.getElement("Slave Metadata");
        if(slaveRoot != null) {
            for(MetadataElement slvElem : slaveRoot.getElements()) {
                final MetadataElement slvNode = new MetadataElement(slvElem.getName());
                addIDNode(slvElem, slvNode, "Mission", "MISSION");
                addIDNode(slvElem, slvNode, "Type", "PRODUCT_TYPE");
                addIDNode(slvElem, slvNode, "Acquisition", "first_line_time");
                addIDNode(slvElem, slvNode, "Pass", "PASS");
                addIDNode(slvElem, slvNode, "Track", "REL_ORBIT");
                addIDNode(slvElem, slvNode, "Orbit", "ABS_ORBIT");
                identNode.addElement(slvNode);
            }
        }
        return identNode;
    }

    private static void addIDNode(final MetadataElement absRoot, final MetadataElement identNode,
                                  final String title, final String tag) {
        final MetadataAttribute attrib = absRoot.getAttribute(tag);
        if(attrib == null) return;
        final String value = title + ": " + attrib.getData().getElemString();
        final MetadataElement newAttrib = new MetadataElement(value);
        identNode.addElement(newAttrib);
    }

}