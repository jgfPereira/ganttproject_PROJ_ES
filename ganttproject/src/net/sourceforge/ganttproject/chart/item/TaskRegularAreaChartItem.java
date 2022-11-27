/*
 * Created on 21.11.2004
 */
package net.sourceforge.ganttproject.chart.item;

import net.sourceforge.ganttproject.chart.gantt.GanttChartController;
import net.sourceforge.ganttproject.task.Task;

import java.awt.*;

/**
 * @author bard
 */
public class TaskRegularAreaChartItem extends ChartItem {

    private int mouseX;
    private int mouseY;

    public TaskRegularAreaChartItem(Task task) {
        super(task);
    }

    public String getTaskInfo() {
        Task task = getTask();
        final String newLine = System.getProperty("line.separator");
        return"Task Name - " + task.getName() + " | "
                + "Task Duration - " + task.getDuration() + " | "
                + "Task Priority - " + task.getPriority() + " | "
                + "Task Completion Percentage - " + task.getCompletionPercentage();
    }

    public void setMousePos(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;

    }

    public Point createPointOnMousePos() {
        return new Point(mouseX, mouseY);
    }


}
