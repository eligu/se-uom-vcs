/**
 * 
 */
package gr.uom.se.util.concurrent;

/**
 * @author Elvis Ligu
 */
public interface TaskListener {

   void started(Runnable task);
   void submitted(Runnable task);
   void completed(Runnable task);
   void abborted(Runnable task);
}

class TaskListenerAdapter implements TaskListener {

   /**
    * {@inheritDoc}
    */
   @Override
   public void started(Runnable task) { }

   /**
    * {@inheritDoc}
    */
   @Override
   public void submitted(Runnable task) { }

   /**
    * {@inheritDoc}
    */
   @Override
   public void completed(Runnable task) { }

   /**
    * {@inheritDoc}
    */
   @Override
   public void abborted(Runnable task) { }  
}
