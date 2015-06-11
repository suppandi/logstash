package org.apache.mesos.logstash.executor;

import org.apache.log4j.Logger;
import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.MesosExecutorDriver;
import org.apache.mesos.Protos;

import java.util.Arrays;

/**
 * Executor for Logstash.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class LogstashExecutor implements Executor {

    public static final Logger LOGGER = Logger.getLogger(LogstashExecutor.class.toString());

    public static void main(String[] args) {
                   LOGGER.info("Started LogstashExecutor");

            MesosExecutorDriver driver = new MesosExecutorDriver(new LogstashExecutor());
            Protos.Status status = driver.run();
            if (status.equals(Protos.Status.DRIVER_STOPPED)) {
                System.exit(0);
            } else {
                System.exit(1);
            }
    }

    @Override
    public void registered(ExecutorDriver driver, Protos.ExecutorInfo executorInfo, Protos.FrameworkInfo frameworkInfo, Protos.SlaveInfo slaveInfo) {
        LOGGER.info("Executor Logstash registered on slave " + slaveInfo.getHostname());
    }

    @Override
    public void reregistered(ExecutorDriver driver, Protos.SlaveInfo slaveInfo) {
        LOGGER.info("Executor Logstash re-registered on slave " + slaveInfo.getHostname());
    }

    @Override
    public void disconnected(ExecutorDriver driver) {
        LOGGER.info("Executor Logstash disconnected");
    }

    @Override
    public void launchTask(final ExecutorDriver driver, final Protos.TaskInfo task) {
        Protos.TaskStatus status = Protos.TaskStatus.newBuilder()
                .setTaskId(task.getTaskId())
                .setState(Protos.TaskState.TASK_RUNNING).build();
        driver.sendStatusUpdate(status);

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    Protos.TaskStatus taskStatus = Protos.TaskStatus.newBuilder()
                            .setTaskId(task.getTaskId())
                            .setState(Protos.TaskState.TASK_FINISHED).build();
                    driver.sendStatusUpdate(taskStatus);
                }
            })  {});
        } catch (Exception e) {
            status = Protos.TaskStatus.newBuilder()
                    .setTaskId(task.getTaskId())
                    .setState(Protos.TaskState.TASK_FAILED).build();
            driver.sendStatusUpdate(status);
        }
    }

    @Override
    public void killTask(ExecutorDriver driver, Protos.TaskID taskId) {
        LOGGER.info("Kill task: " + taskId.getValue());
        Protos.TaskStatus status = Protos.TaskStatus.newBuilder()
                .setTaskId(taskId)
                .setState(Protos.TaskState.TASK_FAILED).build();
        driver.sendStatusUpdate(status);
    }

    @Override
    public void frameworkMessage(ExecutorDriver driver, byte[] data) {
        LOGGER.info("Framework message: " + Arrays.toString(data));
    }

    @Override
    public void shutdown(ExecutorDriver driver) {
        LOGGER.info("Shutting down framework...");
    }

    @Override
    public void error(ExecutorDriver driver, String message) {
        LOGGER.info("Error in executor: " + message);
    }
}
