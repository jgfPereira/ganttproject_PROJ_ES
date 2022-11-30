package net.sourceforge.ganttproject.test.task;

import biz.ganttproject.core.calendar.WeekendCalendarImpl;
import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.chart.TaskChartModelFacade;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;
import org.easymock.EasyMock;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the association of files to tasks
 * - namely adding and removing files from a certain task
 */
public class ManageFilesOfTaskTest {

    private static Task createTask(TaskManager taskManager, String taskName, int complPercentage) {
        Task result = taskManager.createTask();
        result.move(taskManager.getRootTask());
        result.setName(taskName);
        result.setCompletionPercentage(complPercentage);
        return result;
    }

    private static Task setupNecessaryTaskDependecies(String taskName, int complPercentage) {
        // setup necessary dependencies to create task
        TaskChartModelFacade mockChartModel = EasyMock.createMock(TaskChartModelFacade.class);
        TaskManager taskManager = TestSetupHelper.newTaskManagerBuilder()
                .withCalendar(new WeekendCalendarImpl()).build();

        Task t = createTask(taskManager, taskName, complPercentage);
        t.setStart(TestSetupHelper.newTuesday());
        t.setDuration(taskManager.createLength(2));
        return t;
    }

    @Test
    public void addFilesToTaskTest() throws IOException {
        Task t = setupNecessaryTaskDependecies("task1", 24);
        String gpTestDirPath = System.getProperty("user.dir");

        File file1 = new File(gpTestDirPath + File.separator + "file1.txt");
        PrintWriter pw = new PrintWriter(file1);
        this.populateFile(pw, file1, 10);

        t.addFile(file1);
        assertTrue(t.getTaskFiles().contains(file1));

        // delete file after test - so dont populate tester explorer
        file1.delete();


    }

    @Test
    public void removeFilesFromTaskTest() throws IOException {
        Task t = setupNecessaryTaskDependecies("task2", 25);
        String gpTestDirPath = System.getProperty("user.dir");

        File file1 = new File(gpTestDirPath + File.separator + "file2.txt");
        PrintWriter pw = new PrintWriter(file1);
        this.populateFile(pw, file1, 10);

        t.addFile(file1);
        assertTrue(t.getTaskFiles().contains(file1));

        t.removeFile(file1);
        assertFalse(t.getTaskFiles().contains(file1));

        // delete file after test - so dont populate tester explorer
        file1.delete();


    }

    @Test
    public void addRemoveFileToSeveralTasksTest() throws IOException {
        Task t1 = setupNecessaryTaskDependecies("task3", 26);
        Task t2 = setupNecessaryTaskDependecies("task4", 27);
        String gpTestDirPath = System.getProperty("user.dir");

        File file1 = new File(gpTestDirPath + File.separator + "file3.txt");
        PrintWriter pw = new PrintWriter(file1);
        this.populateFile(pw, file1, 10);

        t1.addFile(file1);
        assertTrue(t1.getTaskFiles().contains(file1));
        t2.addFile(file1);
        assertTrue(t2.getTaskFiles().contains(file1));

        t1.removeFile(file1);
        assertFalse(t1.getTaskFiles().contains(file1));
        t2.removeFile(file1);
        assertFalse(t2.getTaskFiles().contains(file1));

        // delete file after test - so dont populate tester explorer
        file1.delete();

    }

    @Test
    public void addRemoveSeveralFilesOnTaskTest() throws IOException {
        Task t = setupNecessaryTaskDependecies("task5", 28);
        String gpTestDirPath = System.getProperty("user.dir");

        File file1 = new File(gpTestDirPath + File.separator + "file4.txt");
        PrintWriter pw1 = new PrintWriter(file1);
        this.populateFile(pw1, file1, 15);

        File file2 = new File(gpTestDirPath + File.separator + "file5.txt");
        PrintWriter pw2 = new PrintWriter(file2);
        this.populateFile(pw2, file2, 16);

        File file3 = new File(gpTestDirPath + File.separator + "file6.txt");
        PrintWriter pw3 = new PrintWriter(file3);
        this.populateFile(pw3, file3, 17);

        t.addFile(file1);
        t.addFile(file2);
        t.addFile(file3);
        assertTrue(t.getTaskFiles().contains(file1));
        assertTrue(t.getTaskFiles().contains(file2));
        assertTrue(t.getTaskFiles().contains(file3));

        t.removeFile(file3);
        t.removeFile(file2);
        t.removeFile(file1);
        assertFalse(t.getTaskFiles().contains(file3));
        assertFalse(t.getTaskFiles().contains(file2));
        assertFalse(t.getTaskFiles().contains(file1));

        // delete file after test - so dont populate tester explorer
        file1.delete();
        file2.delete();
        file3.delete();
    }

    private void populateFile(PrintWriter pw, File f, int numLines) {
        for (int i = 0; i < numLines; i++) {
            String s = "Im on line " + i + " of file " + f.getName() + "\n";
            pw.append(s);
        }
        pw.flush();
        pw.close();
    }
}
