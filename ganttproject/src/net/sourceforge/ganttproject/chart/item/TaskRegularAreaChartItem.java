/*
 * Created on 21.11.2004
 */
package net.sourceforge.ganttproject.chart.item;

import net.sourceforge.ganttproject.task.Task;

/**
 * @author bard
 */
public class TaskRegularAreaChartItem extends ChartItem {
    public TaskRegularAreaChartItem(Task task) {
        super(task);
    }

    // used to get information about task on mouse over task "rectangle"
    public String getTaskInfo() {
        Task task = getTask();
        final String newLine = "\n";
        return "Task Name - " + task.getName() + newLine
                + "Task ID - " + task.getTaskID() + newLine
                + "Task Duration - " + task.getDuration() + newLine
                + "Task Priority - " + task.getPriority() + newLine
                + "Number of Docs Associated - " + task.getTaskFiles().size() + newLine
                + "Number of Resources Associated - " + task.getAssignments().length + newLine
                + "Task Completion Percentage - " + task.getCompletionPercentage() + " %";
    }
}
