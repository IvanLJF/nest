package org.esa.beam.framework.ui;

import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.binding.swing.BindingContext;
import com.bc.ceres.binding.swing.PropertyEditor;
import com.bc.ceres.binding.swing.PropertyEditorRegistry;
import com.bc.ceres.swing.TableLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

// todo - Class has no BEAM dependencies, move into Ceres (com.bc.ceres.binding.swing) (nf)

/**
 * A utility class used to create a {@link JPanel} containing default Swing components and their corresponding bindings for the
 * {@link com.bc.ceres.binding.PropertyContainer} given by the {@link com.bc.ceres.binding.swing.BindingContext}.
 * <p/>
 * <p>If the {@code displayName} property of the binding's {@link com.bc.ceres.binding.PropertySet PropertySet}
 * is set, it will be used as label, otherwise a label is derived from the {@code name} property.</p>
 */
public class PropertyPane {

    private final BindingContext bindingContext;

    public PropertyPane(PropertySet propertySet) {
        this(new BindingContext(propertySet));
    }

    public PropertyPane(BindingContext bindingContext) {
        this.bindingContext = bindingContext;
    }

    public BindingContext getBindingContext() {
        return bindingContext;
    }

    public JPanel createPanel() {
        PropertySet propertyContainer = bindingContext.getPropertySet();
        Property[] models = propertyContainer.getProperties();

        boolean displayUnitColumn = wantDisplayUnitColumn(models);
        TableLayout layout = new TableLayout(displayUnitColumn ? 3 : 2);
        layout.setTableAnchor(TableLayout.Anchor.WEST);
        layout.setTableFill(TableLayout.Fill.HORIZONTAL);
        layout.setTablePadding(3, 3);
        final JPanel panel = new JPanel(layout);

        int rowIndex = 0;
        final PropertyEditorRegistry registry = PropertyEditorRegistry.getInstance();
        for (Property model : models) {
            PropertyDescriptor descriptor = model.getDescriptor();
            PropertyEditor propertyEditor = registry.findValueEditor(descriptor);
            JComponent[] components = propertyEditor.createComponents(descriptor, bindingContext);
            if (components.length == 2) {
                layout.setCellWeightX(rowIndex, 0, 0.0);
                panel.add(components[1], new TableLayout.Cell(rowIndex, 0));
                layout.setCellWeightX(rowIndex, 1, 1.0);
                panel.add(components[0], new TableLayout.Cell(rowIndex, 1));
            } else {
                layout.setCellColspan(rowIndex, 0, 2);
                layout.setCellWeightX(rowIndex, 0, 1.0);
                panel.add(components[0], new TableLayout.Cell(rowIndex, 0));
            }
            if(displayUnitColumn) {
                final JLabel label = new JLabel("");
                if (descriptor.getUnit() != null) {
                    label.setText(descriptor.getUnit());
                }
                layout.setCellWeightX(rowIndex, 2, 0.0);
                panel.add(label, new TableLayout.Cell(rowIndex, 2));
            }
            rowIndex++;
        }
        layout.setCellColspan(rowIndex, 0, 2);
        layout.setCellWeightX(rowIndex, 0, 1.0);
        layout.setCellWeightY(rowIndex, 0, 1.0);
        panel.add(new JPanel());
        return panel;
    }

    private boolean wantDisplayUnitColumn(Property[] models) {
        boolean showUnitColumn = false;
        for (Property model : models) {
            PropertyDescriptor descriptor = model.getDescriptor();
            String unit = descriptor.getUnit();
            if (!(unit == null || unit.length() == 0)) {
                showUnitColumn = true;
                break;
            }
        }
        return showUnitColumn;
    }
}
