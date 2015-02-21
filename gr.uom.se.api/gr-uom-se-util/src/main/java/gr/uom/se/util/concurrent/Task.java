package gr.uom.se.util.concurrent;

/**
 * An interface to define a runnable type that can be scheduled for running to a
 * scheduler.
 * <p>
 * Clients of {@link TaskScheduler} are not required to use this interface if in
 * order to schedule a task. However it can be helpful using this interface when
 * when we have a group of tasks that belongs to the same type, by providing an
 * abstract type.
 *
 * @author Elvis Ligu
 */
public interface Task extends Runnable {

   /**
    * Return the type of this task.
    * <p>
    * The returned type should never be null.
    *
    * @return the type of this task, never null.
    */
   TaskType getType();
}
