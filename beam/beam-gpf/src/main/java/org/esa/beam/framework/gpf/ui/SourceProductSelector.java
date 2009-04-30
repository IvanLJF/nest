package org.esa.beam.framework.gpf.ui;

import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductIOPlugInManager;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductManager;
import org.esa.beam.framework.datamodel.ProductFilter;
import org.esa.beam.framework.ui.BasicApp;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.application.SelectionChangeEvent;
import org.esa.beam.framework.ui.application.SelectionChangeListener;
import org.esa.beam.framework.ui.application.support.DefaultSelection;
import org.esa.beam.util.SystemUtils;
import org.esa.beam.util.io.BeamFileChooser;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bc.ceres.swing.TableLayout;

/**
 * WARNING: This class belongs to a preliminary API and may change in future releases.
 * todo - add capability to select/add/remove multiple soures (nf - 27.11.2007)
 * todo - add capability to specify optional sources
 *
 * @author Ralf Quast
 * @version $Revision: 1.1 $ $Date: 2009-04-28 14:37:14 $
 */
public class SourceProductSelector {

    private AppContext appContext;
    private ProductFilter productFilter;
    private Product extraProduct;
    private File currentDirectory;
    private DefaultComboBoxModel productListModel;
    private JLabel productNameLabel;
    private JButton productFileChooserButton;
    private JComboBox productNameComboBox;
    private List<SelectionChangeListener> selectionListeners;
    private final ProductManager.Listener productManagerListener;

    public SourceProductSelector(AppContext appContext) {
        this(appContext, "Name:");
    }

    public SourceProductSelector(AppContext appContext, String labelText) {
        this.appContext = appContext;
        productListModel = new DefaultComboBoxModel();

        productNameLabel = new JLabel(labelText);
        productFileChooserButton = new JButton(new ProductFileChooserAction());
        final Dimension size = new Dimension(26, 16);
        productFileChooserButton.setPreferredSize(size);
        productFileChooserButton.setMinimumSize(size);

        productNameComboBox = new JComboBox(productListModel);
        productNameComboBox.setPrototypeDisplayValue("[1] 123456789 123456789 12345");
        productNameComboBox.setRenderer(new ProductListCellRenderer());
        productNameComboBox.addPopupMenuListener(new ProductPopupMenuListener());
        productNameComboBox.setTransferHandler(new ComboBoxTransferHandler());
        
        productNameComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Product product = (Product) productNameComboBox.getSelectedItem();
                if (product != null) {
                    fireSelectionChanged();
                    if (product.getFileLocation() != null) {
                        productNameComboBox.setToolTipText(product.getFileLocation().getPath());
                    } else {
                        productNameComboBox.setToolTipText(product.getDisplayName());
                    }
                } else {
                    productNameComboBox.setToolTipText("Select a source product.");
                }
            }
        });

        productFilter = new AllProductFilter();
        selectionListeners = new ArrayList<SelectionChangeListener>(7);

        productManagerListener = new ProductManager.Listener() {
            public void productAdded(ProductManager.Event event) {
                 addProduct(event.getProduct());
            }

            public void productRemoved(ProductManager.Event event) {
                Product product = event.getProduct();
                if (productListModel.getSelectedItem() == product) {
                    productListModel.setSelectedItem(null);
                    productListModel.removeElement(product);
                }
            }
        };
    }

    /**
     * @return the product filter, default is a filter which accepts all products
     */
    public ProductFilter getProductFilter() {
        return productFilter;
    }

    /**
     * @param productFilter the product filter
     */
    public void setProductFilter(ProductFilter productFilter) {
        this.productFilter = productFilter;
    }

    public synchronized void initProducts() {
        productListModel.removeAllElements();
        for (Product product : appContext.getProductManager().getProducts()) {
            addProduct(product);
        }
        productListModel.setSelectedItem(appContext.getSelectedProduct());
        appContext.getProductManager().addListener(productManagerListener)       ;
    }

    public int getProductCount() {
        return productListModel.getSize();
    }

    public void setSelectedIndex(int index) {
        productListModel.setSelectedItem(productListModel.getElementAt(index));
    }

    public Product getSelectedProduct() {
        return (Product) productListModel.getSelectedItem();
    }

    public void setCurrentDirectory(File directory) {
        if (directory != null && directory.isDirectory()) {
            currentDirectory = directory;
        }
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public void setSelectedProduct(Product product) {
        if (productListModelContains(product)) {
            productListModel.setSelectedItem(product);
        } else {
            if (productFilter.accept(product)) {
                if (extraProduct != null) {
                    productListModel.removeElement(extraProduct);
                    extraProduct.dispose();
                }
                productListModel.addElement(product);
                productListModel.setSelectedItem(product);
                extraProduct = product;
            }
        }
    }

    public synchronized void releaseProducts() {
        appContext.getProductManager().removeListener(productManagerListener)       ;
        if (extraProduct != null && getSelectedProduct() != extraProduct) {
            extraProduct.dispose();
        }
        extraProduct = null;
        productListModel.removeAllElements();
    }

    public void addSelectionChangeListener(SelectionChangeListener listener) {
        selectionListeners.add(listener);
    }

    public void removeSelectionChangeListener(SelectionChangeListener listener) {
        selectionListeners.remove(listener);
    }

    private void fireSelectionChanged() {
        for (SelectionChangeListener changeListener : selectionListeners) {
            final DefaultSelection selection = new DefaultSelection(productNameComboBox.getSelectedItem());
            changeListener.selectionChanged(new SelectionChangeEvent(productNameComboBox, selection));
        }
    }

    private void addProduct(Product product) {
        if (productFilter.accept(product)) {
            productListModel.addElement(product);
        }
    }

    private void removeProduct() {
        productListModel.removeElement(extraProduct);
    }

    // UI Components

    /////////////////////////////////////

    public JComboBox getProductNameComboBox() {
        return productNameComboBox;
    }

    public JLabel getProductNameLabel() {
        return productNameLabel;
    }

    public JButton getProductFileChooserButton() {
        return productFileChooserButton;
    }

    private boolean productListModelContains(Product product) {
        for (int i = 0; i < productListModel.getSize(); i++) {
            if (productListModel.getElementAt(i).equals(product)) {
                return true;
            }
        }
        return false;
    }

    public JPanel createDefaultPanel() {
        final JPanel subPanel = new JPanel(new BorderLayout(3, 3));
        subPanel.add(getProductNameComboBox(), BorderLayout.CENTER);
        subPanel.add(getProductFileChooserButton(), BorderLayout.EAST);

        final TableLayout tableLayout = new TableLayout(1);
        tableLayout.setTableAnchor(TableLayout.Anchor.WEST);
        tableLayout.setTableWeightX(1.0);
        tableLayout.setRowFill(0, TableLayout.Fill.HORIZONTAL);
        tableLayout.setRowFill(1, TableLayout.Fill.HORIZONTAL);
        tableLayout.setTablePadding(3, 3);
        JPanel panel = new JPanel(tableLayout);
        panel.setBorder(BorderFactory.createTitledBorder("Source Product"));
        panel.add(getProductNameLabel());
        panel.add(subPanel);
        panel.add(tableLayout.createVerticalSpacer());
        return panel;
    }

    private class ProductFileChooserAction extends AbstractAction {

        private String APPROVE_BUTTON_TEXT = "Select";
        private JFileChooser chooser;

        public ProductFileChooserAction() {
            super("...");
            chooser = new BeamFileChooser();
            chooser.setDialogTitle("Select Source Product");
            final Iterator<ProductReaderPlugIn> iterator = ProductIOPlugInManager.getInstance().getAllReaderPlugIns();
            while (iterator.hasNext()) {
                // todo - (mp, 2008/04/22)check if product file filter is applicable
                chooser.addChoosableFileFilter(iterator.next().getProductFileFilter());
            }
            // todo - (mp, 2008/04/22)check if product file filter is applicable
            chooser.setAcceptAllFileFilterUsed(true);
            chooser.setFileFilter(chooser.getAcceptAllFileFilter());
        }

        public void actionPerformed(ActionEvent event) {
            final Window window = SwingUtilities.getWindowAncestor((JComponent) event.getSource());

            String homeDirPath = SystemUtils.getUserHomeDir().getPath();
            String openDir = appContext.getPreferences().getPropertyString(BasicApp.PROPERTY_KEY_APP_LAST_OPEN_DIR, homeDirPath);
            currentDirectory = new File(openDir);
            chooser.setCurrentDirectory(currentDirectory);

            if (chooser.showDialog(window, APPROVE_BUTTON_TEXT) == JFileChooser.APPROVE_OPTION) {
                final File file = chooser.getSelectedFile();

                Product product = null;
                try {
                    product = ProductIO.readProduct(file, null);
                    if (product == null) {
                        throw new IOException(MessageFormat.format("File ''{0}'' could not be read.", file.getPath()));
                    }

                    if (productFilter.accept(product)) {
                        setSelectedProduct(product);
                    } else {
                        final String message = String.format("Product [%s] is not a valid source.",
                                                             product.getFileLocation().getCanonicalPath());
                        handleError(window, message);
                        product.dispose();
                    }
                } catch (IOException e) {
                    handleError(window, e.getMessage());
                } catch (Exception e) {
                    if (product != null) {
                        product.dispose();
                    }
                    handleError(window, e.getMessage());
                    e.printStackTrace();
                }
                currentDirectory = chooser.getCurrentDirectory();
                appContext.getPreferences().setPropertyString(BasicApp.PROPERTY_KEY_APP_LAST_OPEN_DIR, currentDirectory.getAbsolutePath());
            }
        }

        private void handleError(final Component component, final String message) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(component, message, "Error",
                                                  JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    private static class ProductListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            final Component cellRendererComponent =
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (cellRendererComponent instanceof JLabel && value instanceof Product) {
                final JLabel label = (JLabel) cellRendererComponent;
                final Product product = (Product) value;
                label.setText(product.getDisplayName());
            }

            return cellRendererComponent;
        }
    }

    /**
     * To let the popup menu be wider than the closed combobox.
     * Adapted an idea from http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6257236
     */
    private static class ProductPopupMenuListener implements PopupMenuListener {

        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            JComboBox box = (JComboBox) e.getSource();
            Object comp = box.getUI().getAccessibleChild(box, 0);
            if (!(comp instanceof JPopupMenu)) {
                return;
            }
            JComponent scrollPane = (JComponent) ((JPopupMenu) comp)
                    .getComponent(0);
            Dimension size = new Dimension();
            size.width = scrollPane.getPreferredSize().width;
            final int boxItemCount = box.getModel().getSize();
            for (int i = 0; i < boxItemCount; i++) {
                Product product = (Product) box.getModel().getElementAt(i);
                final JLabel label = new JLabel();
                label.setText(product.getDisplayName());
                size.width = Math.max(label.getPreferredSize().width, size.width);
            }
            size.height = scrollPane.getPreferredSize().height;
            scrollPane.setPreferredSize(size);
            scrollPane.setMaximumSize(size);
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }
    }

    private static class AllProductFilter implements ProductFilter {

        public boolean accept(Product product) {
            return true;
        }
    }

    public static class ComboBoxTransferHandler extends TransferHandler {

        public boolean canImport(TransferHandler.TransferSupport info) {
            return info.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY;
        }

        /**
         * Perform the actual import.  This demo only supports drag and drop.
         */
        public boolean importData(TransferHandler.TransferSupport info) {
            if (!info.isDrop()) {
                return false;
            }

            JComboBox comboBox = (JComboBox)info.getComponent();
            DefaultComboBoxModel comboModel = (DefaultComboBoxModel)comboBox.getModel();

            // Get the string that is being dropped.
            Transferable t = info.getTransferable();
            String data;
            try {
                data = (String)t.getTransferData(DataFlavor.stringFlavor);
            }
            catch (Exception e) { return false; }

            // Wherever there is a newline in the incoming data,
            // break it into a separate item in the list.
            String[] values = data.split("\n");

            // Perform the actual import.
            for (String value : values) {
                try {
                    File selectedFile = new File(value);
                    if(selectedFile.exists()) {
                        ProductReader reader = ProductIO.getProductReaderForFile(selectedFile);
                        if(reader != null) {
                            Product product = reader.readProductNodes(selectedFile, null);
                            if(product != null) 
                                comboModel.addElement(product);
                        }
                    }
                } catch(Exception e) {
                    return false;
                }
            }
            return true;
        }
    }

}