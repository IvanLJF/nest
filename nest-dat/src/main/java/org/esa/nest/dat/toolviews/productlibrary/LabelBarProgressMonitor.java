package org.esa.nest.dat.toolviews.productlibrary;

import com.bc.ceres.core.Assert;
import com.bc.ceres.core.ProgressMonitor;

import javax.swing.*;
import java.util.ArrayList;

/**
 * A {@link com.bc.ceres.core.ProgressMonitor} which uses a
 * Swing's {@link javax.swing.ProgressMonitor} to display progress.
 */
public class LabelBarProgressMonitor implements ProgressMonitor {

    public static final String stopCommand = "stop";
    public static final String updateCommand = "update";

    private final JProgressBar progressBar;
    private final JLabel messageLabel;

    private double currentWork;
    private double totalWork;

    private int totalWorkUI;
    private int currentWorkUI;
    private int lastWorkUI;
    private boolean cancelRequested;

    private final ArrayList<ProgressBarListener> listenerList = new ArrayList<ProgressBarListener>(1);

    public LabelBarProgressMonitor(JProgressBar progressBar, JLabel messageLabel) {
        this.progressBar = progressBar;
        this.messageLabel = messageLabel;
    }

    /**
     * Notifies that the main task is beginning.  This must only be called once
     * on a given progress monitor instance.
     *
     * @param name      the name (or description) of the main task
     * @param totalWork the total number of work units into which
     *                  the main task is been subdivided. If the value is <code>UNKNOWN</code>
     *                  the implementation is free to indicate progress in a way which
     *                  doesn't require the total number of work units in advance.
     */
    public void beginTask(String name, int totalWork) {
        Assert.notNull(name, "name");
        currentWork = 0.0;
        this.totalWork = totalWork;
        currentWorkUI = 0;
        lastWorkUI = 0;
        totalWorkUI = totalWork;
        if (messageLabel != null) {
            messageLabel.setText(name);
        }
        cancelRequested = false;
        setDescription(name);
        progressBar.setMaximum(totalWork);
    }

    /**
     * Notifies that the work is done; that is, either the main task is completed
     * or the user canceled it. This method may be called more than once
     * (implementations should be prepared to handle this case).
     */
    public void done() {
        runInUI(new Runnable() {
            public void run() {
                if (progressBar != null) {
                    progressBar.setValue(progressBar.getMaximum());

                    for (final ProgressBarListener listener : listenerList) {
                        listener.notifyDone();
                    }
                }
            }
        });
    }

    /**
     * Internal method to handle scaling correctly. This method
     * must not be called by a client. Clients should
     * always use the method </code>worked(int)</code>.
     *
     * @param work the amount of work done
     */
    public void internalWorked(double work) {
        currentWork += work;
        currentWorkUI = (int) (totalWorkUI * currentWork / totalWork);
        if (currentWorkUI > lastWorkUI) {
            runInUI(new Runnable() {
                public void run() {
                    if (progressBar != null) {
                        int progress = progressBar.getMinimum() + currentWorkUI;
                        progressBar.setValue(progress);

                        for (final ProgressBarListener listener : listenerList) {
                            listener.notifyStart();
                        }
                    }
                    lastWorkUI = currentWorkUI;
                }
            });
        }
    }

    /**
     * Returns whether cancelation of current operation has been requested.
     * Long-running operations should poll to see if cancelation
     * has been requested.
     *
     * @return <code>true</code> if cancellation has been requested,
     *         and <code>false</code> otherwise
     *
     * @see #setCanceled(boolean)
     */
    public boolean isCanceled() {
        return cancelRequested;
    }

    /**
     * Sets the cancel state to the given value.
     *
     * @param canceled <code>true</code> indicates that cancelation has
     *                 been requested (but not necessarily acknowledged);
     *                 <code>false</code> clears this flag
     *
     * @see #isCanceled()
     */
    public void setCanceled(boolean canceled) {
        cancelRequested = canceled;
        if (canceled) {
            done();
        }
    }

    /**
     * Sets the task name to the given value. This method is used to
     * restore the task label after a nested operation was executed.
     * Normally there is no need for clients to call this method.
     *
     * @param name the name (or description) of the main task
     *
     * @see #beginTask(String, int)
     */
    public void setTaskName(final String name) {
        runInUI(new Runnable() {
            public void run() {
                if (messageLabel != null) {
                    messageLabel.setText(name);
                }
            }
        });
    }

    /**
     * Notifies that a subtask of the main task is beginning.
     * Subtasks are optional; the main task might not have subtasks.
     *
     * @param name the name (or description) of the subtask
     */
    public void setSubTaskName(final String name) {
        messageLabel.setText(name);
    }

    /**
     * Notifies that a given number of work unit of the main task
     * has been completed. Note that this amount represents an
     * installment, as opposed to a cumulative amount of work done
     * to date.
     *
     * @param work the number of work units just completed
     */
    public void worked(int work) {
        internalWorked(work);
    }

    ////////////////////////////////////////////////////////////////////////
    // Stuff to be performed in Swing's event-dispatching thread

    private void runInUI(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    private void setDescription(final String description) {
        messageLabel.setText(description);
    }

    public void addListener(final ProgressBarListener listener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }

    public void removeListener(final ProgressBarListener listener) {
        listenerList.remove(listener);
    }

    public interface ProgressBarListener {

        public void notifyStart();

        public void notifyDone();
    }
}