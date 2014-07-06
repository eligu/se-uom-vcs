/**
 * 
 */
package gr.uom.se.util.pattern.processor;

/**
 * A special case of a {@link ProcessorQueue} to create thread queues.
 * <p>
 * 
 * This interface extends a plain processor queue by adding a special method
 * {@link #shutdown()} which is used to shut down the threads of this queue.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface ThreadQueue<T> extends ProcessorQueue<T> {

   /**
    * Shut down this queue, by shutting down any remaining threads.
    * <p>
    * 
    * The queue may not be used after the shut down. If this queue uses a shared
    * thread queue this may cause other tasks to not be scheduled for execute if
    * you shut down this queue.
    * 
    * @throws InterruptedException
    */
   public void shutdown() throws InterruptedException;
}
