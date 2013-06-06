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
package org.esa.nest.gpf;

import org.esa.beam.framework.gpf.ui.BaseOperatorUI;
import org.esa.beam.framework.gpf.ui.UIValidation;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.visat.VisatApp;
import org.esa.nest.dataio.dem.DEMFactory;
import org.esa.nest.util.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Map;

/**
 * User interface for SARSimulationOp
 */
public class SARSimulationOpUI extends BaseOperatorUI {

    private final JList bandList = new JList();
    private final JComboBox<String> demName = new JComboBox<String>(DEMFactory.getDEMNameList());
    private static final String externalDEMStr = "External DEM";

    private final JComboBox demResamplingMethod = new JComboBox<String>(DEMFactory.getDEMResamplingMethods());

    private final JTextField externalDEMFile = new JTextField("");
    private final JTextField externalDEMNoDataValue = new JTextField("");
    private final JButton externalDEMBrowseButton = new JButton("...");
    private final JLabel externalDEMFileLabel = new JLabel("External DEM:");
    private final JLabel externalDEMNoDataValueLabel = new JLabel("DEM No Data Value:");
    private final JCheckBox reGridMethodCheckBox = new JCheckBox("Re-grid method (slower)");
    private final JCheckBox orbitMethodCheckBox = new JCheckBox("Orbit method");
    private final JCheckBox saveDEMCheckBox = new JCheckBox("Save DEM band");
    private final JCheckBox saveZeroHeightSimulationCheckBox = new JCheckBox("Save Zero Height Simulation");
    private final JCheckBox saveLocalIncidenceAngleCheckBox = new JCheckBox("Save Simulated Local Incidence Angle");
    private final JCheckBox saveLayoverShadowMaskCheckBox = new JCheckBox("Save Layover-Shadow Mask");

    private boolean isSARSimTC = false;
    private boolean reGridMethod = false;
    private boolean orbitMethod = false;
    private boolean saveDEM = false;
    private boolean saveZeroHeightSimulation = false;
    private boolean saveLocalIncidenceAngle = false;
    private boolean saveLayoverShadowMask = false;
    private Double extNoDataValue = 0.0;

    private final DialogUtils.TextAreaKeyListener textAreaKeyListener = new DialogUtils.TextAreaKeyListener();

    @Override
    public JComponent CreateOpTab(String operatorName, Map<String, Object> parameterMap, AppContext appContext) {

        demName.addItem(externalDEMStr);

        initializeOperatorUI(operatorName, parameterMap);

        final JComponent panel = createPanel();
        initParameters();

        demName.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if(((String)demName.getSelectedItem()).startsWith(externalDEMStr)) {
                    enableExternalDEM(true);
                } else {
                    externalDEMFile.setText("");
                    enableExternalDEM(false);
                }
            }
        });
        externalDEMFile.setColumns(30);
        enableExternalDEM(((String)demName.getSelectedItem()).startsWith(externalDEMStr));

        externalDEMBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final File file = VisatApp.getApp().showFileOpenDialog("External DEM File", false, null);
                externalDEMFile.setText(file.getAbsolutePath());
                extNoDataValue = OperatorUIUtils.getNoDataValue(file);
                externalDEMNoDataValue.setText(String.valueOf(extNoDataValue));
            }
        });

        externalDEMNoDataValue.addKeyListener(textAreaKeyListener);

        reGridMethodCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                reGridMethod = (e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        orbitMethodCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                orbitMethod = (e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        saveDEMCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                saveDEM = (e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        saveZeroHeightSimulationCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                saveZeroHeightSimulation = (e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        saveLocalIncidenceAngleCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                saveLocalIncidenceAngle = (e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        saveLayoverShadowMaskCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    saveLayoverShadowMask = (e.getStateChange() == ItemEvent.SELECTED);
                }
        });

        return new JScrollPane(panel);
    }

    @Override
    public void initParameters() {

        OperatorUIUtils.initParamList(bandList, getBandNames());

        final String demNameParam = (String)paramMap.get("demName");
        if(demNameParam != null)
            demName.setSelectedItem(DEMFactory.appendAutoDEM(demNameParam));
        demResamplingMethod.setSelectedItem(paramMap.get("demResamplingMethod"));

        final File extFile = (File)paramMap.get("externalDEMFile");
        if(extFile != null) {
            externalDEMFile.setText(extFile.getAbsolutePath());
            extNoDataValue =  (Double)paramMap.get("externalDEMNoDataValue");
            if(extNoDataValue != null && !textAreaKeyListener.isChangedByUser()) {
                externalDEMNoDataValue.setText(String.valueOf(extNoDataValue));
            }
        }

        isSARSimTC = (Boolean)paramMap.get("isSARSimTC");

        if(!isSARSimTC) {
            reGridMethod = (Boolean)paramMap.get("reGridMethod");
            reGridMethodCheckBox.setSelected(reGridMethod);

            orbitMethod = (Boolean)paramMap.get("orbitMethod");
            orbitMethodCheckBox.setSelected(orbitMethod);

            saveDEM = (Boolean)paramMap.get("saveDEM");
            saveDEMCheckBox.setSelected(saveDEM);

            saveZeroHeightSimulation = (Boolean)paramMap.get("saveZeroHeightSimulation");
            saveZeroHeightSimulationCheckBox.setSelected(saveZeroHeightSimulation);

            saveLocalIncidenceAngle = (Boolean)paramMap.get("saveLocalIncidenceAngle");
            saveLocalIncidenceAngleCheckBox.setSelected(saveLocalIncidenceAngle);
        }
        saveLayoverShadowMask = (Boolean)paramMap.get("saveLayoverShadowMask");
        saveLayoverShadowMaskCheckBox.setSelected(saveLayoverShadowMask);

        enableExtraOptions(!isSARSimTC);
    }

    @Override
    public UIValidation validateParameters() {

        return new UIValidation(UIValidation.State.OK, "");
    }

    @Override
    public void updateParameters() {

        OperatorUIUtils.updateParamList(bandList, paramMap, OperatorUIUtils.SOURCE_BAND_NAMES);

        paramMap.put("demName", DEMFactory.getProperDEMName((String)demName.getSelectedItem()));
        paramMap.put("demResamplingMethod", demResamplingMethod.getSelectedItem());

        final String extFileStr = externalDEMFile.getText();
        if(!extFileStr.isEmpty()) {
            paramMap.put("externalDEMFile", new File(extFileStr));
            paramMap.put("externalDEMNoDataValue", Double.parseDouble(externalDEMNoDataValue.getText()));
        }

        if(!isSARSimTC) {
            paramMap.put("reGridMethod", reGridMethod);
            paramMap.put("orbitMethod", orbitMethod);
            paramMap.put("saveDEM", saveDEM);
            paramMap.put("saveZeroHeightSimulation", saveZeroHeightSimulation);
            paramMap.put("saveLocalIncidenceAngle", saveLocalIncidenceAngle);
        }
        paramMap.put("saveLayoverShadowMask", saveLayoverShadowMask);
    }

    private JComponent createPanel() {

        final JPanel contentPane = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = DialogUtils.createGridBagConstraints();

        contentPane.add(new JLabel("Source Bands:"), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        contentPane.add(new JScrollPane(bandList), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, "Digital Elevation Model:", demName);
        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, externalDEMFileLabel, externalDEMFile);
        gbc.gridx = 2;
        contentPane.add(externalDEMBrowseButton, gbc);
        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, externalDEMNoDataValueLabel, externalDEMNoDataValue);
        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, "DEM Resampling Method:", demResamplingMethod);

        gbc.gridx = 0;
        if(!isSARSimTC) {
            gbc.gridy++;
            contentPane.add(reGridMethodCheckBox, gbc);
            gbc.gridy++;
            contentPane.add(orbitMethodCheckBox, gbc);
            gbc.gridy++;
            contentPane.add(saveDEMCheckBox, gbc);
            gbc.gridy++;
            contentPane.add(saveZeroHeightSimulationCheckBox, gbc);
            gbc.gridy++;
            contentPane.add(saveLocalIncidenceAngleCheckBox, gbc);
        }
        gbc.gridy++;
        contentPane.add(saveLayoverShadowMaskCheckBox, gbc);
        DialogUtils.fillPanel(contentPane, gbc);

        return contentPane;
    }

    private void enableExtraOptions(boolean flag) {
        reGridMethodCheckBox.setVisible(flag);
        orbitMethodCheckBox.setVisible(flag);
        saveDEMCheckBox.setVisible(flag);
        saveZeroHeightSimulationCheckBox.setVisible(flag);
        saveLocalIncidenceAngleCheckBox.setVisible(flag);
    }

    private void enableExternalDEM(boolean flag) {
        DialogUtils.enableComponents(externalDEMFileLabel, externalDEMFile, flag);
        DialogUtils.enableComponents(externalDEMNoDataValueLabel, externalDEMNoDataValue, flag);
        externalDEMBrowseButton.setVisible(flag);
    }
}