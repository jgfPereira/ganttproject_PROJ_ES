package net.sourceforge.ganttproject.chart.mouse;

import biz.ganttproject.core.calendar.WeekendCalendarImpl;
import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.chart.TaskChartModelFacade;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;

import org.easymock.EasyMock;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * Test if a task is created correctly,
 * if that happens then the popup on mouse over
 * will also be correct since it uses the task internal state,
 * to add information to the popup - which in turn facilitates
 * the user experience (specially when there are too much tasks).
 */
public class MouseOverTaskRectPopupTest {

    private static Task createTask(TaskManager taskManager, String taskName, int complPercentage) {
        Task result = taskManager.createTask();
        result.move(taskManager.getRootTask());
        result.setName(taskName);
        result.setCompletionPercentage(complPercentage);
        return result;
    }

    @Test
    public void mouseOverTaskRectTest() {
        // setup necessary dependencies to create task
        TaskChartModelFacade mockChartModel = EasyMock.createMock(TaskChartModelFacade.class);
        TaskManager taskManager = TestSetupHelper.newTaskManagerBuilder()
                .withCalendar(new WeekendCalendarImpl()).build();

        Task t = createTask(taskManager, "firstTask", 72);
        t.setStart(TestSetupHelper.newTuesday());
        t.setDuration(taskManager.createLength(2));

        // check if task is created with corrected info
        assertEquals("firstTask", t.getName());
        assertEquals(72, t.getCompletionPercentage());
        assertEquals(2, t.getDuration().getLength());
    }

    @Test
    public void mouseOverSeveralTaskRectTest() {
        // setup necessary dependencies to create tasks
        TaskChartModelFacade mockChartModel = EasyMock.createMock(TaskChartModelFacade.class);
        TaskManager taskManager = TestSetupHelper.newTaskManagerBuilder()
                .withCalendar(new WeekendCalendarImpl()).build();

        Task t1 = createTask(taskManager, "fase2Proj", 88);
        t1.setStart(TestSetupHelper.newMonday());
        t1.setDuration(taskManager.createLength(6));

        // check if first task is created with corrected info
        assertEquals("fase2Proj", t1.getName());
        assertEquals(88, t1.getCompletionPercentage());
        assertEquals(6, t1.getDuration().getLength());

        Task t2 = createTask(taskManager, "unitTests", 32);
        t2.setStart(TestSetupHelper.newThursday());
        t2.setDuration(taskManager.createLength(1));

        // check if second task is created with corrected info
        assertEquals("unitTests", t2.getName());
        assertEquals(32, t2.getCompletionPercentage());
        assertEquals(1, t2.getDuration().getLength());

        Task t3 = createTask(taskManager, "useCases", 12);
        t3.setStart(TestSetupHelper.newSunday());
        t3.setDuration(taskManager.createLength(2));

        // check if third task is created with corrected info
        assertEquals("useCases", t3.getName());
        assertEquals(12, t3.getCompletionPercentage());
        assertEquals(2, t3.getDuration().getLength());
    }
}
