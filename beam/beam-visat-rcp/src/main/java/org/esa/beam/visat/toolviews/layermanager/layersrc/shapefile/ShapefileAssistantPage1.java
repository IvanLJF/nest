package org.esa.beam.visat.toolviews.layermanager.layersrc.shapefile;

import org.esa.beam.framework.ui.FileHistory;
import org.esa.beam.util.PropertyMap;
import org.esa.beam.visat.toolviews.layermanager.layersrc.AbstractLayerSourceAssistantPage;
import org.esa.beam.visat.toolviews.layermanager.layersrc.FilePathListCellRenderer;
import org.esa.beam.visat.toolviews.layermanager.layersrc.HistoryComboBoxModel;
import org.esa.beam.visat.toolviews.layermanager.layersrc.LayerSourcePageContext;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

class ShapefileAssistantPage1 extends AbstractLayerSourceAssistantPage {

    private static final String PROPERTY_LAST_FILE_PREFIX = "ShapefileAssistant.Shapefile.history";
    private static final String PROPERTY_LAST_DIR = "ShapefileAssistant.Shapefile.lastDir";

    private HistoryComboBoxModel fileHistoryModel;


    ShapefileAssistantPage1() {
        super("Select ESRI Shapefile");
    }

    @Override
    public Component createPageComponent() {
        GridBagConstraints gbc = new GridBagConstraints();
        final JPanel panel = new JPanel(new GridBagLayout());

        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Path to ESRI Shapefile (*.shp):"), gbc);

        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        final LayerSourcePageContext context = getContext();
        final PropertyMap preferences = context.getAppContext().getPreferences();
        final FileHistory fileHistory = new FileHistory(5, PROPERTY_LAST_FILE_PREFIX);
        fileHistory.initBy(preferences);
        fileHistoryModel = new HistoryComboBoxModel(fileHistory);
        JComboBox shapefileBox = new JComboBox(fileHistoryModel);
        shapefileBox.setRenderer(new FilePathListCellRenderer(80));
        shapefileBox.setEditable(true);
        shapefileBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                context.updateState();
            }
        });
        panel.add(shapefileBox, gbc);

        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        JButton button = new JButton("...");
        button.addActionListener(new ShpaeFilechooserActionListener());
        panel.add(button, gbc);

        return panel;
    }

    @Override
    public boolean validatePage() {
        if (fileHistoryModel != null) {
            String path = (String) fileHistoryModel.getSelectedItem();
            return path != null && !path.trim().isEmpty();
        }
        return false;
    }

    @Override
    public boolean hasNextPage() {
        return true;
    }

    @Override
    public AbstractLayerSourceAssistantPage getNextPage() {
        final LayerSourcePageContext context = getContext();
        fileHistoryModel.getHistory().copyInto(context.getAppContext().getPreferences());
        String path = (String) fileHistoryModel.getSelectedItem();
        if (path != null && !path.trim().isEmpty()) {
            try {
                final String oldPath = (String) context.getPropertyValue(ShapefileLayerSource.PROPERTY_FILE_PATH);
                if (!path.equals(oldPath)) {
                    context.setPropertyValue(ShapefileLayerSource.PROPERTY_FILE_PATH, path);
                    // clear other properties they are not valid anymore
                    context.setPropertyValue(ShapefileLayerSource.PROPERTY_FEATURE_COLLECTION, null);
                    context.setPropertyValue(ShapefileLayerSource.PROPERTY_SELECTED_STYLE, null);
                    context.setPropertyValue(ShapefileLayerSource.PROPERTY_STYLES, null);
                }
                return new ShapefileAssistantPage2();
            } catch (Exception e) {
                e.printStackTrace();
                context.showErrorDialog("Failed to load ESRI shapefile:\n" + e.getMessage());
            }
        }

        return null;
    }

    @Override
    public boolean canFinish() {
        return false;
    }

    private class ShpaeFilechooserActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setAcceptAllFileFilterUsed(false);
            final FileNameExtensionFilter shapefileFilter = new FileNameExtensionFilter("ESRI Shapefile", "shp");
            fileChooser.addChoosableFileFilter(shapefileFilter);
            fileChooser.setFileFilter(shapefileFilter);
            File lastDir = getLastDirectory();
            fileChooser.setCurrentDirectory(lastDir);
            LayerSourcePageContext pageContext = getContext();
            fileChooser.showOpenDialog(pageContext.getWindow());
            if (fileChooser.getSelectedFile() != null) {
                String filePath = fileChooser.getSelectedFile().getPath();
                fileHistoryModel.setSelectedItem(filePath);
                PropertyMap preferences = pageContext.getAppContext().getPreferences();
                preferences.setPropertyString(PROPERTY_LAST_DIR, fileChooser.getCurrentDirectory().getAbsolutePath());
                pageContext.updateState();
            }
        }

        private File getLastDirectory() {
            PropertyMap preferences = getContext().getAppContext().getPreferences();
            String dirPath = preferences.getPropertyString(PROPERTY_LAST_DIR, System.getProperty("user.home"));
            File lastDir = new File(dirPath);
            if (!lastDir.isDirectory()) {
                lastDir = new File(System.getProperty("user.home"));
            }
            return lastDir;
        }
    }

}