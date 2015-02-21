package gr.uom.se.util.concurrent;

import java.util.Objects;

/**
 * Defines a task type to be used when creating a task scheduler, or when
 * submitting a task.
 * <p>
 * Predefined types are {@link Enum#SINGLE_THREAD} for heavy running tasks that
 * should be run in only by one thread, and {@link Enum#UNLIMITED} for quick
 * tasks that has no limit on the number of threads to run those kind of tasks.
 * Use {@link Enum#get(int, Object)} to create new types of tasks that can be used with
 * the tasks scheduler. Using this, will take care of returning types that are
 * equals based on the number of threads and their id (if not null).
 *
 * @author Elvis Ligu
 */
public interface TaskType {

   /**
    * Get the number of threads that can run this type of tasks.
    * <p>
    * @return the number of maximum threads that can run this type of task in
    * parallel.
    */
   int getThreadSize();

   public static enum Enum implements TaskType {

      /**
       * All tasks of this type will be run in serial. For very heavy duty tasks
       * this is an ideal candidate.
       */
      SINGLE_THREAD(1, "HEAVY_TASK_TYPE"),
      /**
       * There is no limitation about the number of threads running this kind of
       * tasks. The only limitation is the thread pool, and its number of
       * threads. This is ideal for quick tasks, such as notifiers, that
       * requires a few resources.
       */
      UNLIMITED(Integer.MAX_VALUE, "QUICK_TASK_TYPE");

      final TaskType type;

      private Enum(int threads, String id) {
         type = get(threads, id);
      }

      @Override
      public int getThreadSize() {
         return type.getThreadSize();
      }

      /**
       * Get a task type for the given number of threads.
       * <p>
       * If size is 1, it will return {@link #SINGLE_THREAD}. If size is 0 it
       * will return {@link #UNLIMITED}.
       *
       * @param threadsSize the size of threads that can run this type of tasks.
       * @param id the id of this type, usually a string.
       * @return a task type for the given number of threads. Each type returned
       * by this method will be equal if they have the same id (even if ids are
       * null) and the same number of threads.
       */
      public static TaskType get(int threadsSize, Object id) {
         return new Type(threadsSize, id);
      }
   }

   static class Type implements TaskType {

      final int threads;
      final Object id;

      public Type(int threads, Object id) {
         if (threads < 1) {
            throw new IllegalArgumentException();
         }
         this.threads = threads;
         this.id = id;
      }

      @Override
      public int getThreadSize() {
         return threads;
      }

      @Override
      public int hashCode() {
         int hash = 7;
         hash = 17 * hash + this.threads;
         hash = 17 * hash + Objects.hashCode(this.id);
         return hash;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         }
         if (getClass() != obj.getClass()) {
            return false;
         }
         final Type other = (Type) obj;
         if (this.threads != other.threads) {
            return false;
         }
         return Objects.equals(this.id, other.id);
      }

      @Override
      public String toString() {
         String str = "[" + threads + "]";
         if(id != null) {
            str = id.toString() + str;
         }
         return str;
      }
   }
}
