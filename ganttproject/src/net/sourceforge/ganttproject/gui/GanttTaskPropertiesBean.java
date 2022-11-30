/*
GanttProject is an opensource project management tool.
Copyright (C) 2011 GanttProject team

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.sourceforge.ganttproject.gui;

import biz.ganttproject.core.chart.render.ShapeConstants;
import biz.ganttproject.core.chart.render.ShapePaint;
import biz.ganttproject.core.option.ColorOption;
import biz.ganttproject.core.option.DefaultColorOption;
import biz.ganttproject.core.time.CalendarFactory;
import biz.ganttproject.core.time.GanttCalendar;
import com.google.common.base.Objects;
import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.GanttTask;
import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.action.GPAction;
import net.sourceforge.ganttproject.gui.UIUtil.DateValidator;
import net.sourceforge.ganttproject.gui.options.OptionsPageBuilder;
import net.sourceforge.ganttproject.gui.options.SpringUtilities;
import net.sourceforge.ganttproject.gui.taskproperties.CustomColumnsPanel;
import net.sourceforge.ganttproject.gui.taskproperties.TaskAllocationsPanel;
import net.sourceforge.ganttproject.gui.taskproperties.TaskDependenciesPanel;
import net.sourceforge.ganttproject.gui.taskproperties.TaskScheduleDatesPanel;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.shape.JPaintCombo;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskContainmentHierarchyFacade;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.TaskMutator;
import net.sourceforge.ganttproject.util.BrowserControl;
import net.sourceforge.ganttproject.util.collect.Pair;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXHyperlink;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Real panel for editing task properties
 */
public class GanttTaskPropertiesBean extends JPanel {

    private ColorOption myTaskColorOption = new DefaultColorOption("");
    private final GPAction mySetDefaultColorAction = new GPAction("defaultColor") {
        @Override
        public void actionPerformed(ActionEvent e) {
            myTaskColorOption.setValue(myUIfacade.getGanttChart().getTaskDefaultColorOption().getValue());
        }
    };
    private JXDatePicker myEarliestBeginDatePicker;

    private GanttTask[] selectedTasks;

    private static final GanttLanguage language = GanttLanguage.getInstance();

    private GanttCalendar myThird;

    private JTabbedPane tabbedPane; // TabbedPane that includes the following four
    // items

    private JPanel generalPanel;

    private JComponent predecessorsPanel;

    private JPanel resourcesPanel;

    private JPanel notesPanel;

    private JTextField nameField1;

    private JTextField tfWebLink;

    private JButton bWebLink;

    private JSpinner percentCompleteSlider;

    private JComboBox priorityComboBox;

    private JCheckBox myEarliestBeginEnabled;

    private JCheckBox mileStoneCheckBox1;

    private JCheckBox projectTaskCheckBox1;

    /**
     * Shape chooser combo Box
     */
    private JPaintCombo shapeComboBox;

    private JScrollPane scrollPaneNotes;

    private JTextArea noteAreaNotes;

    private JPanel secondRowPanelNotes;

    private String originalName;

    private String originalWebLink;

    private boolean originalIsMilestone;

    private GanttCalendar originalStartDate;

    private GanttCalendar originalEndDate;

    private GanttCalendar originalEarliestBeginDate;

    private int originalEarliestBeginEnabled;

    private boolean originalIsProjectTask;

    private String originalNotes;

    private int originalCompletionPercentage;

    private Task.Priority originalPriority;

    private ShapePaint originalShape;

    private final TaskScheduleDatesPanel myTaskScheduleDates;

    private CustomColumnsPanel myCustomColumnPanel = null;

    private TaskDependenciesPanel myDependenciesPanel;

    private TaskAllocationsPanel myAllocationsPanel;

    private final HumanResourceManager myHumanResourceManager;

    private final RoleManager myRoleManager;

    private Task myUnpluggedClone;
    private final TaskManager myTaskManager;
    private final IGanttProject myProject;
    private final UIFacade myUIfacade;

    private JCheckBox myShowInTimeline;
    private AbstractAction myOnEarliestBeginToggle;

    // file selected on the open/remove files menu/combobox
    private File fileSelected;

    // file selected on the first merge files menu/combobox
    private File fileSelectedMerge1;

    // file selected on the second merge files menu/combobox
    private File fileSelectedMerge2;

    // used to keep track of task files - necessary to update the menus/gui "instantly"
    // (without having to close and reopen task properties dialog on every change).
    private File[] taskFiles;

    // first file to merge
    private File firstMerge;

    // second file to merge;
    private File secondMerge;


    public GanttTaskPropertiesBean(GanttTask[] selectedTasks, IGanttProject project, UIFacade uifacade) {
        myTaskScheduleDates = new TaskScheduleDatesPanel(uifacade);
        this.selectedTasks = selectedTasks;
        storeOriginalValues(selectedTasks[0]);
        myHumanResourceManager = project.getHumanResourceManager();
        myRoleManager = project.getRoleManager();
        myTaskManager = project.getTaskManager();
        myProject = project;
        myUIfacade = uifacade;
        init();
        setSelectedTaskProperties();
    }

    private static void addEmptyRow(JPanel form) {
        form.add(Box.createRigidArea(new Dimension(1, 10)));
        form.add(Box.createRigidArea(new Dimension(1, 10)));
    }

    /**
     * Construct the general panel
     */
    private void constructGeneralPanel() {
        final JPanel propertiesPanel = new JPanel(new SpringLayout());

        propertiesPanel.add(new JLabel(language.getText("name")));
        nameField1 = new JTextField(20);
        nameField1.setName("name_of_task");
        propertiesPanel.add(nameField1);
        Pair<String, JCheckBox> checkBox = constructCheckBox();
        if (checkBox != null) {
            propertiesPanel.add(new JLabel(checkBox.first()));
            propertiesPanel.add(checkBox.second());
        }
        addEmptyRow(propertiesPanel);

        myTaskScheduleDates.insertInto(propertiesPanel);

        constructEarliestBegin(propertiesPanel);
        addEmptyRow(propertiesPanel);

        propertiesPanel.add(new JLabel(language.getText("priority")));
        priorityComboBox = new JComboBox();
        for (Task.Priority p : Task.Priority.values()) {
            priorityComboBox.addItem(language.getText(p.getI18nKey()));
        }
        priorityComboBox.setEditable(false);
        propertiesPanel.add(priorityComboBox);

        propertiesPanel.add(new JLabel(language.getText("advancement")));
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, 100, 1);
        percentCompleteSlider = new JSpinner(spinnerModel);
        propertiesPanel.add(percentCompleteSlider);

        addEmptyRow(propertiesPanel);

        propertiesPanel.add(new JLabel(language.getText("option.taskProperties.main.showInTimeline.label")));
        myShowInTimeline = new JCheckBox();
        propertiesPanel.add(myShowInTimeline);

        propertiesPanel.add(new JLabel(language.getText("shape")));
        shapeComboBox = new JPaintCombo(ShapeConstants.PATTERN_LIST);
        propertiesPanel.add(shapeComboBox);

        OptionsPageBuilder builder = new OptionsPageBuilder(GanttTaskPropertiesBean.this, OptionsPageBuilder.TWO_COLUMN_LAYOUT);
        builder.setUiFacade(myUIfacade);
        JPanel colorBox = new JPanel(new BorderLayout(5, 0));
        colorBox.add(builder.createColorComponent(myTaskColorOption).getJComponent(), BorderLayout.WEST);
        //colorBox.add(Box.createHorizontalStrut(5));
        colorBox.add(new JXHyperlink(mySetDefaultColorAction), BorderLayout.CENTER);

        propertiesPanel.add(new JLabel(language.getText("colors")));
        propertiesPanel.add(colorBox);

        Box weblinkBox = Box.createHorizontalBox();
        tfWebLink = new JTextField(20);
        weblinkBox.add(tfWebLink);
        weblinkBox.add(Box.createHorizontalStrut(2));
        bWebLink = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/web_16.gif")));
        bWebLink.setToolTipText(GanttProject.getToolTip(language.getText("openWebLink")));
        weblinkBox.add(bWebLink);

        bWebLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // link to open the web link
                if (!BrowserControl.displayURL(tfWebLink.getText())) {
                    GanttDialogInfo gdi = new GanttDialogInfo(null, GanttDialogInfo.ERROR, GanttDialogInfo.YES_OPTION,
                            language.getText("msg4"), language.getText("error"));
                    gdi.setVisible(true);
                }
            }
        });
        propertiesPanel.add(new JLabel(language.getText("webLink")));
        propertiesPanel.add(weblinkBox);

        // create label and button to add files to a task
        propertiesPanel.add(new JLabel("Add File"));
        JPanel fileBtnBox = new JPanel(new BorderLayout(5, 0));
        final JButton addFilesButton = new JButton(UIManager.getIcon("FileChooser.upFolderIcon"));
        addFilesButton.setToolTipText("Associate file with task");
        fileBtnBox.add(addFilesButton, BorderLayout.WEST);
        propertiesPanel.add(fileBtnBox);

        // create open and remove files buttons
        propertiesPanel.add(new JLabel("Open/Rmv File"));
        JPanel openRemoveFileBtnBox = new JPanel(new BorderLayout(5, 0));
        final JButton openFilesButton = new JButton(UIManager.getIcon("FileView.fileIcon"));
        openFilesButton.setToolTipText("Open file in explorer");
        final JButton removeFilesButton = new JButton((UIManager.getIcon("OptionPane.errorIcon")));
        removeFilesButton.setToolTipText("Remove file from task");

        // get files of the task to auxiliary array
        taskFiles = selectedTasks[0].getTaskFiles().toArray(new File[0]);
        // create menu/combobox to select files for opening/removal
        final DefaultComboBoxModel openRemoveFilesMenuModel = new DefaultComboBoxModel(taskFiles);
        JComboBox openRemoveFilesMenu = new JComboBox(openRemoveFilesMenuModel);

        // add elements to open/remove box/container and then to panel
        openRemoveFileBtnBox.add(openFilesButton, BorderLayout.WEST);
        openRemoveFileBtnBox.add(openRemoveFilesMenu, BorderLayout.CENTER);
        openRemoveFileBtnBox.add(removeFilesButton, BorderLayout.EAST);
        propertiesPanel.add(openRemoveFileBtnBox);

        // create merge files button
        propertiesPanel.add(new JLabel("Merge Txt Files"));
        JPanel mergeFileBtnBox = new JPanel(new BorderLayout(5, 0));
        final JButton mergeFilesButton = new JButton(UIManager.getIcon("FileChooser.detailsViewIcon"));
        mergeFilesButton.setToolTipText("Merge two text files into a new file");

        // create first combo box to select first file to merge
        final DefaultComboBoxModel mergeBoxModel = new DefaultComboBoxModel(taskFiles);
        JComboBox mergeFilesMenu = new JComboBox(mergeBoxModel);
        mergeFileBtnBox.add(mergeFilesButton, BorderLayout.WEST);
        mergeFileBtnBox.add(mergeFilesMenu, BorderLayout.CENTER);
        propertiesPanel.add(mergeFileBtnBox);

        // create second combo box to select second file to merge
        propertiesPanel.add(new JLabel("Select second file:"));
        JPanel mergeMenu2Box = new JPanel(new BorderLayout(5, 0));
        final DefaultComboBoxModel mergeBoxModel2 = new DefaultComboBoxModel(taskFiles);
        JComboBox mergeFilesMenu2 = new JComboBox(mergeBoxModel2);
        mergeMenu2Box.add(mergeFilesMenu2, BorderLayout.CENTER);
        propertiesPanel.add(mergeMenu2Box);

        // create checkbox to know if should delete the individual/merged files
        propertiesPanel.add(new JLabel("Delete the merged files"));
        final JCheckBox delete_files = new JCheckBox();
        propertiesPanel.add(delete_files);

        // create checkbox to know if should keep the individual/merged files
        propertiesPanel.add(new JLabel("Keep the merged files"));
        final JCheckBox keep_files = new JCheckBox();
        propertiesPanel.add(keep_files);
        keep_files.doClick();

        // Action Listeners of all buttons, checkboxs and JComboBoxs

        delete_files.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (delete_files.isSelected()) {
                    if (keep_files.isSelected()) {
                        keep_files.doClick();
                    }
                } else {
                    if (!keep_files.isSelected()) {
                        delete_files.doClick();
                    }
                }
            }
        });

        keep_files.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (keep_files.isSelected()) {
                    if (delete_files.isSelected()) {
                        delete_files.doClick();
                    }
                } else {
                    if (!delete_files.isSelected()) {
                        keep_files.doClick();
                    }
                }
            }
        });

        addFilesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == addFilesButton) {
                    final JFileChooser fc = new JFileChooser();
                    int returnVal = fc.showOpenDialog(addFilesButton);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        TaskMutator mutator = selectedTasks[0].createMutator();
                        mutator.addFile(file);
                        mutator.commit();
                        myDependenciesPanel.commit();
                        myAllocationsPanel.commit();
                        myCustomColumnPanel.commit();

                        // update comboboxs in dynamic way - otherwhise would have to close and reopen task properties dialog
                        openRemoveFilesMenuModel.addElement(file);
                        mergeBoxModel.addElement(file);
                        mergeBoxModel2.addElement(file);

                        // add file to auxiliary task files array
                        // handle as list because its easier and less error prone
                        List<File> tmpFiles = new ArrayList<>(Arrays.asList(taskFiles));
                        tmpFiles.add(file);
                        taskFiles = tmpFiles.toArray(new File[tmpFiles.size()]);


                    }
                }
            }
        });

        openFilesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (taskFiles.length > 0) {
                    if (e.getSource() == openFilesButton) {
                        //open file with adequate program
                        if (fileSelected == null) {
                            try {
                                java.awt.Desktop.getDesktop().open(taskFiles[0]);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            try {
                                java.awt.Desktop.getDesktop().open(fileSelected);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

        removeFilesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == removeFilesButton && taskFiles.length > 0) {

                    File removedFile;
                    // if no file is selected delete the first one on the JComboBox
                    if (fileSelected == null) {
                        removedFile = taskFiles[0];
                    } else {
                        removedFile = fileSelected;
                    }

                    // remove file from auxiliary task files array
                    // handle as list because its easier and less error prone
                    List<File> tmpFiles = new ArrayList<>(Arrays.asList(taskFiles));
                    tmpFiles.remove(removedFile);
                    taskFiles = tmpFiles.toArray(new File[tmpFiles.size()]);

                    // update combobox in dynamic way - otherwhise would have to close and reopen task properties dialog
                    openRemoveFilesMenuModel.removeElement(removedFile);
                    mergeBoxModel.removeElement(removedFile);
                    mergeBoxModel2.removeElement(removedFile);

                    TaskMutator mutator = selectedTasks[0].createMutator();
                    mutator.removeFile(removedFile);
                    mutator.commit();
                    myDependenciesPanel.commit();
                    myAllocationsPanel.commit();
                    myCustomColumnPanel.commit();

                }


            }
        });


        openRemoveFilesMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                fileSelected = (File) cb.getSelectedItem();
            }
        });

        mergeFilesMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                fileSelectedMerge1 = (File) cb.getSelectedItem();
            }
        });

        mergeFilesMenu2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                fileSelectedMerge2 = (File) cb.getSelectedItem();
            }
        });

        mergeFilesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (taskFiles.length > 1) {
                    if (fileSelectedMerge1 == null) {
                        firstMerge = taskFiles[0];
                    } else {
                        firstMerge = fileSelectedMerge1;
                    }
                    if (fileSelectedMerge2 == null) {
                        secondMerge = taskFiles[1];
                    } else {
                        secondMerge = fileSelectedMerge2;
                    }
                } else {
                    // only have 0 or 1 file - so cant merge
                    return;
                }

                String ext1 = getFileExtension(firstMerge);
                String ext2 = getFileExtension(secondMerge);
                String filename1 = getFileNameWithoutExtension(firstMerge);
                String filename2 = getFileNameWithoutExtension(secondMerge);

                File mergedFile = null;
                String mergedFileName = "merged_" + filename1 + "_" + filename2 + ".txt";
                if ((ext1.equals("txt")) && (ext2.equals("txt")) && (!filename1.equals(filename2)) && !areFilesAlreadyMerged(mergedFileName)) {
                    try {
                        mergedFile = new File(mergedFileName);
                        PrintWriter pw = new PrintWriter(mergedFile);
                        String file1Content = getFileContents(firstMerge);
                        String file2Content = getFileContents(secondMerge);
                        mergeFilesContents(pw, file1Content, file2Content);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    return;
                }

                // add merged file to the menus/comboxs
                openRemoveFilesMenuModel.addElement(mergedFile);
                mergeBoxModel.addElement(mergedFile);
                mergeBoxModel2.addElement(mergedFile);

                if (delete_files.isSelected()) {
                    openRemoveFilesMenuModel.removeElement(firstMerge);
                    mergeBoxModel.removeElement(firstMerge);
                    mergeBoxModel2.removeElement(firstMerge);
                    openRemoveFilesMenuModel.removeElement(secondMerge);
                    mergeBoxModel.removeElement(secondMerge);
                    mergeBoxModel2.removeElement(secondMerge);
                }

                List<File> tmpFiles = new ArrayList<>(Arrays.asList(taskFiles));
                tmpFiles.add(mergedFile);
                if (delete_files.isSelected()) {
                    tmpFiles.remove(firstMerge);
                    tmpFiles.remove(secondMerge);
                }
                taskFiles = tmpFiles.toArray(new File[tmpFiles.size()]);

                TaskMutator mutator = selectedTasks[0].createMutator();
                mutator.addFile(mergedFile);
                if (delete_files.isSelected()) {
                    mutator.removeFile(firstMerge);
                    mutator.removeFile(secondMerge);
                }

                mutator.commit();
                myDependenciesPanel.commit();
                myAllocationsPanel.commit();
                myCustomColumnPanel.commit();
            }

        });


        SpringUtilities.makeCompactGrid(propertiesPanel, (propertiesPanel.getComponentCount() / 2), 2, 1, 1, 5, 5);
        JPanel propertiesWrapper = new JPanel(new BorderLayout());
        propertiesWrapper.add(propertiesPanel, BorderLayout.NORTH);
        generalPanel = new JPanel(new SpringLayout());
        generalPanel.add(propertiesWrapper);
        generalPanel.add(notesPanel);
        SpringUtilities.makeCompactGrid(generalPanel, 1, 2, 1, 1, 10, 5);
    }

    // used to check if two files had already been merged
    private boolean areFilesAlreadyMerged(String mergedFileName) {
        for (int i = 0; i < this.taskFiles.length; i++) {
            if (this.taskFiles[i].getName().equals(mergedFileName)) {
                return true;
            }
        }

        return false;
    }

    private String getFileExtension(File f) {
        return f.getName().substring(f.getName().lastIndexOf('.') + 1);
    }

    private String getFileNameWithoutExtension(File f) {
        return f.getName().substring(0, f.getName().lastIndexOf('.'));
    }

    private String getFileContents(File f) throws IOException {
        return new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())), StandardCharsets.UTF_8);
    }

    private void mergeFilesContents(PrintWriter pw, String cont1, String cont2) {
        pw.append(cont1);
        pw.append("\n");
        pw.append(cont2);
        pw.flush();
        pw.close();
    }

    private void constructEarliestBegin(Container propertiesPanel) {
        final JXHyperlink copyFromBeginDate = new JXHyperlink(new GPAction("option.taskProperties.main.earliestBegin.copyBeginDate") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setThird(myTaskScheduleDates.getStart());
            }

            @Override
            protected String getLocalizedName() {
                String fallbackLabel = String.format("%s %s", language.getText("copy"), language.getText("generic.startDate.label"));
                return Objects.firstNonNull(super.getLocalizedName(), fallbackLabel);
            }

        });
        myEarliestBeginDatePicker = UIUtil.createDatePicker();
        Box valueBox = Box.createHorizontalBox();
        myOnEarliestBeginToggle = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                myEarliestBeginDatePicker.setEnabled(myEarliestBeginEnabled.isSelected());
                if (getThird() == null) {
                    setThird(myTaskScheduleDates.getStart());
                }
                copyFromBeginDate.setEnabled(myEarliestBeginEnabled.isSelected());
            }
        };
        myEarliestBeginEnabled = new JCheckBox(myOnEarliestBeginToggle);
        valueBox.add(myEarliestBeginEnabled);
        valueBox.add(Box.createHorizontalStrut(10));
        valueBox.add(myEarliestBeginDatePicker);
        valueBox.add(Box.createHorizontalStrut(5));
        valueBox.add(copyFromBeginDate);
        propertiesPanel.add(new JLabel(language.getText("earliestBegin")));
        propertiesPanel.add(valueBox);
    }

    private void constructCustomColumnPanel() {
        myCustomColumnPanel = new CustomColumnsPanel(myProject.getTaskCustomColumnManager(), myUIfacade,
                selectedTasks[0].getCustomValues(), myUIfacade.getTaskTree().getVisibleFields());
    }

    /**
     * Construct the predecessors tabbed pane
     */
    private void constructPredecessorsPanel() {
        myDependenciesPanel = new TaskDependenciesPanel();
        myDependenciesPanel.init(selectedTasks[0]);
        predecessorsPanel = myDependenciesPanel.getComponent();
    }

    /**
     * Construct the resources panel
     */
    private void constructResourcesPanel() {
        myAllocationsPanel = new TaskAllocationsPanel(selectedTasks[0], myHumanResourceManager, myRoleManager);
        resourcesPanel = myAllocationsPanel.getComponent();
    }

    /**
     * Construct the notes panel
     */
    private void constructNotesPanel() {
        secondRowPanelNotes = new JPanel(new BorderLayout());
        UIUtil.createTitle(secondRowPanelNotes, language.getText("notesTask"));

        noteAreaNotes = new JTextArea(8, 40);
        noteAreaNotes.setLineWrap(true);
        noteAreaNotes.setWrapStyleWord(true);
        noteAreaNotes.setBackground(new Color(1.0f, 1.0f, 1.0f));

        scrollPaneNotes = new JScrollPane(noteAreaNotes);
        secondRowPanelNotes.add(scrollPaneNotes, BorderLayout.CENTER);
        notesPanel = secondRowPanelNotes;
    }

    /**
     * Initialize the widgets
     */
    private void init() {
        constructNotesPanel();

        tabbedPane = new JTabbedPane() {
            @Override
            public void addTab(String title, Icon icon, Component component) {
                super.addTab(title, icon, UIUtil.contentPaneBorder((JComponent) component));
            }
        };
        constructGeneralPanel();

        tabbedPane.addTab(language.getText("general"), new ImageIcon(getClass().getResource("/icons/properties_16.gif")),
                generalPanel);

        constructPredecessorsPanel();
        tabbedPane.addTab(language.getText("predecessors"), new ImageIcon(getClass().getResource("/icons/relashion.gif")),
                predecessorsPanel);

        constructResourcesPanel();

        tabbedPane.addTab(language.getCorrectedLabel("human"), new ImageIcon(getClass().getResource("/icons/res_16.gif")),
                resourcesPanel);

        setLayout(new BorderLayout());

        add(tabbedPane, BorderLayout.CENTER);

        constructCustomColumnPanel();
        tabbedPane.addTab(language.getText("customColumns"), new ImageIcon(getClass().getResource("/icons/custom.gif")),
                myCustomColumnPanel.getComponent());
        tabbedPane.addFocusListener(new FocusAdapter() {
            private boolean isFirstFocusGain = true;

            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if (isFirstFocusGain) {
                    nameField1.requestFocus();
                    isFirstFocusGain = false;
                }
            }
        });
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(2, 0, 5, 0));
    }

    /**
     * Apply the modified properties to the selected Tasks
     */
    public void applySettings() {
        for (int i = 0; i < selectedTasks.length; i++) {
            // TODO The originalXXX values should not be used,
            // but the original values should be read from each processed task to
            // determine whether the value has been changed
            TaskMutator mutator = selectedTasks[i].createMutator();
            if (originalName == null || !originalName.equals(getTaskName())) {
                mutator.setName(getTaskName());
            }
            if (originalWebLink == null || !originalWebLink.equals(getWebLink())) {
                mutator.setWebLink(getWebLink());
            }
            if (mileStoneCheckBox1 != null) {
                if (originalIsMilestone != isMilestone()) {
                    mutator.setMilestone(isMilestone());
                }
            } else if (projectTaskCheckBox1 != null) {
                if (originalIsProjectTask != isProjectTask()) {
                    mutator.setProjectTask(isProjectTask());
                }
            }
            if (!originalStartDate.equals(getStart())) {
                mutator.setStart(getStart());
            }
            if (!originalEndDate.equals(getEnd())) {
                mutator.setEnd(getEnd());
            }
            if (originalEarliestBeginDate == null && getThird() != null || originalEarliestBeginDate != null && getThird() == null
                    || originalEarliestBeginDate != null && !originalEarliestBeginDate.equals(getThird())
                    || originalEarliestBeginEnabled != getThirdDateConstraint()) {
                mutator.setThird(getThird(), getThirdDateConstraint());
            }

            if (getLength() > 0) {
                mutator.setDuration(selectedTasks[i].getManager().createLength(getLength()));
            }
            if (!Objects.equal(originalNotes, getNotes())) {
                mutator.setNotes(getNotes());
            }
            if (originalCompletionPercentage != getPercentComplete()) {
                mutator.setCompletionPercentage(getPercentComplete());
            }
            if (this.originalPriority != getPriority()) {
                mutator.setPriority(getPriority());
            }
            mutator.setColor(myTaskColorOption.getValue());
            if (this.originalShape == null && shapeComboBox.getSelectedIndex() != 0 || originalShape != null
                    && !this.originalShape.equals(shapeComboBox.getSelectedPaint())) {
                mutator.setShape(new ShapePaint((ShapePaint) shapeComboBox.getSelectedPaint(), Color.white,
                        myTaskColorOption.getValue()));
            }

            mutator.commit();
            myDependenciesPanel.commit();
            myAllocationsPanel.commit();
            myCustomColumnPanel.commit();

            if (!myShowInTimeline.isSelected()) {
                myUIfacade.getCurrentTaskView().getTimelineTasks().remove(selectedTasks[i]);
            } else {
                myUIfacade.getCurrentTaskView().getTimelineTasks().add(selectedTasks[i]);
            }
        }
    }

    private void setSelectedTaskProperties() {
        myUnpluggedClone = selectedTasks[0].unpluggedClone();
        nameField1.setText(originalName);

        setName(selectedTasks[0].toString());

        percentCompleteSlider.setValue(new Integer(originalCompletionPercentage));
        priorityComboBox.setSelectedIndex(originalPriority.ordinal());

        myTaskScheduleDates.setUnpluggedClone(myUnpluggedClone);
        DateValidator validator = UIUtil.DateValidator.Default.aroundProjectStart(myProject.getTaskManager().getProjectStart());
        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setThird(CalendarFactory.createGanttCalendar(((JXDatePicker) e.getSource()).getDate()));
            }
        };
        UIUtil.setupDatePicker(myEarliestBeginDatePicker, originalEarliestBeginDate == null ? null : originalEarliestBeginDate.getTime(), validator, listener);
        myThird = originalEarliestBeginDate;
        myEarliestBeginEnabled.setSelected(originalEarliestBeginEnabled == 1);
        myOnEarliestBeginToggle.actionPerformed(null);


        if (mileStoneCheckBox1 != null) {
            mileStoneCheckBox1.setSelected(originalIsMilestone);
        } else if (projectTaskCheckBox1 != null) {
            projectTaskCheckBox1.setSelected(originalIsProjectTask);
        }
        myTaskScheduleDates.setupFields(isMilestone(), isSupertask());

        tfWebLink.setText(originalWebLink);

        if (selectedTasks[0].shapeDefined()) {
            for (int j = 0; j < ShapeConstants.PATTERN_LIST.length; j++) {
                if (originalShape.equals(ShapeConstants.PATTERN_LIST[j])) {
                    shapeComboBox.setSelectedIndex(j);
                    break;
                }
            }
        }

        noteAreaNotes.setText(originalNotes);
        myTaskColorOption.setValue(selectedTasks[0].getColor());
        myShowInTimeline.setSelected(myUIfacade.getCurrentTaskView().getTimelineTasks().contains(selectedTasks[0]));
    }

    private boolean isSupertask() {
        return myUnpluggedClone.getManager().getTaskHierarchy().hasNestedTasks(selectedTasks[0]);
    }


    private boolean isMilestone() {
        if (mileStoneCheckBox1 == null) {
            return false;
        }
        return mileStoneCheckBox1.isSelected();
    }


    private boolean isProjectTask() {
        return projectTaskCheckBox1.isSelected();
    }

    private int getThirdDateConstraint() {
        return myEarliestBeginEnabled.isSelected() ? 1 : 0;
    }

    private String getNotes() {
        return noteAreaNotes.getText();
    }

    private String getTaskName() {
        String text = nameField1.getText();
        return text == null ? "" : text.trim();
    }

    private String getWebLink() {
        String text = tfWebLink.getText();
        return text == null ? "" : text.trim();
    }

    private int getPercentComplete() {
        return ((Integer) percentCompleteSlider.getValue()).hashCode();
    }

    private Task.Priority getPriority() {
        return Task.Priority.getPriority(priorityComboBox.getSelectedIndex());
    }

    private GanttCalendar getStart() {
        return myTaskScheduleDates.getStart();
    }

    private GanttCalendar getEnd() {
        return myTaskScheduleDates.getEnd();
    }

    private int getLength() {
        return myTaskScheduleDates.getLength();
    }

    private GanttCalendar getThird() {
        return myThird;
    }


    private void setThird(GanttCalendar third) {
        myThird = third;
        myEarliestBeginDatePicker.setDate(myThird.getTime());
    }

    private void storeOriginalValues(GanttTask task) {
        originalName = task.getName();
        originalWebLink = task.getWebLink();
        originalIsMilestone = task.isLegacyMilestone();
        originalStartDate = task.getStart();
        originalEndDate = task.getEnd();
        originalNotes = task.getNotes();
        originalCompletionPercentage = task.getCompletionPercentage();
        originalPriority = task.getPriority();
        originalShape = task.getShape();
        originalEarliestBeginDate = task.getThird();
        originalEarliestBeginEnabled = task.getThirdDateConstraint();
        originalIsProjectTask = task.isProjectTask();
    }

    private boolean canBeProjectTask(Task testedTask, TaskContainmentHierarchyFacade taskHierarchy) {
        Task[] nestedTasks = taskHierarchy.getNestedTasks(testedTask);
        if (nestedTasks.length == 0) {
            return false;
        }
        for (Task parent = taskHierarchy.getContainer(testedTask); parent != null; parent = taskHierarchy.getContainer(parent)) {
            if (parent.isProjectTask()) {
                return false;
            }
        }
        for (Task nestedTask : nestedTasks) {
            if (isProjectTaskOrContainsProjectTask(nestedTask)) {
                return false;
            }
        }
        return true;
    }

    private boolean isProjectTaskOrContainsProjectTask(Task task) {
        if (task.isProjectTask()) {
            return true;
        }
        for (Task nestedTask : task.getNestedTasks()) {
            if (isProjectTaskOrContainsProjectTask(nestedTask)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a milestone, a project task or no checkbox depending on the
     * selected task
     *
     * @return the created checkbox or null
     */
    private Pair<String, JCheckBox> constructCheckBox() {
        boolean canBeProjectTask = true;
        boolean canBeMilestone = true;
        TaskContainmentHierarchyFacade taskHierarchy = myTaskManager.getTaskHierarchy();
        for (Task task : selectedTasks) {
            canBeMilestone &= !taskHierarchy.hasNestedTasks(task);
            canBeProjectTask &= canBeProjectTask(task, taskHierarchy);
        }
        assert false == (canBeProjectTask && canBeMilestone);

        final Pair<String, JCheckBox> result;
        if (canBeProjectTask) {
            projectTaskCheckBox1 = new JCheckBox();
            result = Pair.create(language.getText("projectTask"), projectTaskCheckBox1);
        } else if (canBeMilestone) {
            mileStoneCheckBox1 = new JCheckBox(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    myTaskScheduleDates.setupFields(isMilestone(), isSupertask());
                }
            });
            result = Pair.create(language.getText("meetingPoint"), mileStoneCheckBox1);
        } else {
            result = null;
        }
        return result;
    }
}
