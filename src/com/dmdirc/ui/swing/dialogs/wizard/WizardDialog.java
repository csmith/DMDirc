/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.ui.swing.dialogs.wizard;

import com.dmdirc.ui.swing.components.StandardDialog;
import com.dmdirc.util.ListenerList;

import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

/**
 * Basic wizard container.
 */
public final class WizardDialog extends StandardDialog implements ActionListener,
        Serializable {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Step panel list. */
    private final StepLayout steps;
    /** Wizard title. */
    private final String title;
    /** Wizard. */
    private final transient WizardListener wizard;
    /** Parent component. */
    private final Component parent;
    /** Step panel. */
    private JPanel stepsPanel;
    /** Title panel. */
    private JLabel titleLabel;
    /** Current step. */
    private int currentStep;
    /** Prevous step button. */
    private JButton prev;
    /** Next step button. */
    private JButton next;
    /** Progress label. */
    private JLabel progressLabel;
    /** Step Listeners. */
    private ListenerList stepListeners;

    /**
     * Creates a new instance of WizardFrame that requires a mainframe.
     *
     * @param title Title for the wizard
     * @param steps Steps for the wizard
     * @param wizard Wizard to inform of changes
     * @param modal Whether the wizard should be modal
     * @param parent Parent component
     */
    public WizardDialog(final String title,
            final List<Step> steps,
            final WizardListener wizard, final boolean modal,
            final Component parent) {
        super(null, modal);

        stepListeners = new ListenerList();

        this.title = title;
        this.steps = new StepLayout();
        this.wizard = wizard;
        this.parent = parent;

        initComponents();
        layoutComponents();

        for (Step step : steps) {
            addStep(step);
        }
    }

    /** Initialises the components. */
    private void initComponents() {
        titleLabel = new JLabel(title);
        stepsPanel = new JPanel(steps);

        titleLabel.setFont(titleLabel.getFont().
                deriveFont((float) (titleLabel.getFont().getSize() * 1.5)));
        
        progressLabel = new JLabel();

        orderButtons(new JButton(), new JButton());
        next = new JButton();
        setOkButton(next);

        prev = new JButton("<< Previous");
        next.setText("Next >>");
        
        next.addActionListener(this);
        prev.addActionListener(this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        final JPanel titlePanel = new JPanel(new MigLayout("fill"));
        titlePanel.add(titleLabel, "growx, wrap");
        titlePanel.add(new JSeparator(), "growx, pad 0");
        titlePanel.setBackground(Color.WHITE);
        
        final JPanel progressPanel = new JPanel(new MigLayout("fill"));
        progressPanel.add(new JSeparator(), "span 3, growx, pad 0, wrap");
        progressPanel.add(progressLabel, "growx");
        progressPanel.add(prev, "sg button");
        progressPanel.add(next, "sg button");
        progressPanel.setBackground(Color.WHITE);
        
        setLayout(new MigLayout("fill, wrap 1, ins 0"));
        add(titlePanel, "growx");
        add(stepsPanel, "grow");
        add(progressPanel, "growx");        
    }

    /** Displays the wizard. */
    public void display() {
        if (!steps.isEmpty()) {
            steps.first(stepsPanel);
            currentStep = 0;

            prev.setEnabled(false);
            if (steps.size() == 1) {
                next.setText("Finish");
            }

            updateProgressLabel();

            setTitle(title);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            pack();
            if (parent != null) {
                setLocationRelativeTo(parent);
            } else {
                // Position wizard center-screen on the correct monitor of a
                // multi-monitor system. (See MainFrame constructor for more info)
                final PointerInfo myPointerInfo =
                        MouseInfo.getPointerInfo();
                final GraphicsDevice myDevice = myPointerInfo.getDevice();
                final GraphicsConfiguration myGraphicsConfig =
                        myDevice.getDefaultConfiguration();
                final Rectangle gcBounds = myGraphicsConfig.getBounds();
                final int xPos =
                        gcBounds.x + ((gcBounds.width - getWidth()) / 2);
                final int yPos =
                        gcBounds.y + ((gcBounds.height - getHeight()) / 2);
                setLocation(xPos, yPos);
            }
            setResizable(false);
            setVisible(true);
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == next) {
            nextStep();
        } else if (e.getSource() == prev) {
            prevStep();
        } else if (e.getSource() == getCancelButton()) {
            fireWizardCancelled();
        }
    }

    /**
     * Adds a step to the wizard.
     *
     * @param step Step to add
     */
    public void addStep(final Step step) {
        stepsPanel.add(step, step.toString());
    }

    /**
     * Enables or disables the "next step" button.
     *
     * @param newValue boolean true to make "next" button enabled, else false
     */
    public void enableNextStep(final boolean newValue) {
        next.setEnabled(newValue);
    }

    /**
     * Enables or disables the "previous step" button.
     *
     * @param newValue boolean true to make "previous" button enabled, else false
     */
    public void enablePreviousStep(final boolean newValue) {
        prev.setEnabled(newValue);
    }

    /** Moves to the next step. */
    private void nextStep() {
        if ("Next >>".equals(next.getText())) {
            fireStepAboutToBeDisplayed(steps.getStep(currentStep + 1));
            steps.next(stepsPanel);
            fireStepHidden(steps.getStep(currentStep));
            currentStep++;
            prev.setEnabled(true);
            if (currentStep == steps.size() - 1) {
                next.setText("Finish");
            }
            updateProgressLabel();
        } else if ("Finish".equals(next.getText())) {
            dispose();
            fireWizardFinished();
        }
    }

    /** Moves to the previous step. */
    private void prevStep() {
        fireStepAboutToBeDisplayed(steps.getStep(currentStep - 1));
        steps.previous(stepsPanel);
        fireStepHidden(steps.getStep(currentStep));
        currentStep--;
        if (currentStep == 0) {
            prev.setEnabled(false);
        }
        next.setText("Next >>");
        updateProgressLabel();
    }

    /**
     * Returns the step at the specified index.
     *
     * @param stepNumber step number
     *
     * @return Specified step.
     */
    public Step getStep(final int stepNumber) {
        return steps.getStep(stepNumber);
    }

    /**
     * Returns the current step.
     *
     * @return Current step number
     */
    public int getCurrentStep() {
        return currentStep;
    }

    /** Updates the progress label. */
    private void updateProgressLabel() {
        progressLabel.setText("Step " + (currentStep + 1) + " of " + steps.size());
    }

    /**
     * Adds a step listener to the list.
     *
     * @param listener
     */
    public void addStepListener(final StepListener listener) {
        stepListeners.add(StepListener.class, listener);
    }

    /**
     * Removes a step listener from the list.
     *
     * @param listener
     */
    public void removeStepListener(final StepListener listener) {
        stepListeners.remove(StepListener.class, listener);
    }
    
    /**
     * Adds a wizard listener to the list.
     *
     * @param listener
     */
    public void addWizardListener(final WizardListener listener) {
        stepListeners.add(WizardListener.class, listener);
    }

    /**
     * Removes a wizard listener from the list.
     *
     * @param listener
     */
    public void removeWizardListener(final WizardListener listener) {
        stepListeners.remove(WizardListener.class, listener);
    }

    /**
     * Fires step about to be displayed events.
     *
     * @param step Step to be displayed
     */
    private void fireStepAboutToBeDisplayed(final Step step) {
        List<StepListener> listeners =
                stepListeners.get(StepListener.class);
        for (StepListener listener : listeners) {
            listener.stepAboutToDisplay(step);
        }
    }

    /**
     * Fires step hidden events.
     *
     * @param step step thats been hidden
     */
    private void fireStepHidden(final Step step) {
        List<StepListener> listeners =
                stepListeners.get(StepListener.class);
        for (StepListener listener : listeners) {
            listener.stepHidden(step);
        }
    }
    
    /**
     * Fires wizard finished events.
     */
    private void fireWizardFinished() {
        List<WizardListener> listeners =
                stepListeners.get(WizardListener.class);
        for (WizardListener listener : listeners) {
            listener.wizardFinished();
        }
    }
    
    /**
     * Fires wizard cancelled events.
     */
    private void fireWizardCancelled() {
        List<WizardListener> listeners =
                stepListeners.get(WizardListener.class);
        for (WizardListener listener : listeners) {
            listener.wizardCancelled();
        }
    }
}