package org.esa.beam.visat.toolviews.layermanager;

import org.esa.beam.framework.ui.AppContext;

import javax.swing.JComponent;

public abstract class AbstractLayerForm {
    private final AppContext appContext;

    protected AbstractLayerForm(AppContext appContext) {
        this.appContext = appContext;
    }

    public AppContext getAppContext() {
        return appContext;
    }

    public abstract JComponent getFormControl();

    public abstract void updateFormControl();
}