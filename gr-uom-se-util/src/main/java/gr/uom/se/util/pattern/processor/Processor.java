/**
 * 
 */
package gr.uom.se.util.pattern.processor;

/**
 * A general interface used in situations when a producer produce the data and
 * then pass them to {@code process()} method (the consumer).
 * <p>
 * 
 * This interface can be utilized in situation when for a single source of data
 * there are two many independent processing that has to be done. For example
 * consider the following code:
 * 
 * <pre>
 * List&ltString&gt names = ...
 * List&ltPocessor&ltString&gt&gt processors = ...
 * 
 * for(String name : names) {
 *    for(Processor&ltString&gt p : process) {
 *       p.process(name);
 *    }
 * }
 * </pre>
 * 
 * In this package we provide some generic implementations of this interface
 * that can be used in situations such as the above code.
 * <ul>
 * <li>{@link SerialQueue} - works the same as the above code. Each
 * data is passed to the list of processors in a serial way (no threads)</li>
 * <li>{@link ThreadQueue} - works the same as the above code, but
 * you can specify a number of threads so the incoming data (from
 * {@code process} method) will be passed to each processor in the queue who
 * will run in parallel, depending on the number of threads.</li>
 * <li>{@link BlockingQueue} - works the same as the parallel
 * queue processor but is very efficient when a new item is passed to the queue
 * because it will block the current thread for sending more data if all the
 * worker threads (the threads that runs the processors) are busy, until one of
 * them finishes processing.</li>
 * </ul>
 * 
 * Before starting any processing of data, it is required that {@link start()}
 * method should be invoked. This will inform this processor that the data
 * should be coming soon so he may need to initialize its state.
 * <p>
 * After all data are consumed a call to {@link #stop()} is required, in order
 * to clean its internal state.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface Processor<T> {

   /**
    * Process the next entity.
    * <p>
    * 
    * @param entity
    *           to be processed. must not be null.
    * @return if false then the processor should not get any more entries. If
    *         true then the processor would be able to process the next entry.
    */
   public boolean process(T entity);

   /**
    * When all entities have been processed by this processor this method should
    * be called, in order to inform the processor that there are no more
    * entries.
    * 
    * @throws InterruptedException
    *            when a work is in the middle of processing and can not be
    *            stopped.
    *            <p>
    */
   public void stop() throws InterruptedException;

   /**
    * Before any entity is processed this method should be called in order to
    * inform the processor that it will start to get entities.
    * <p>
    */
   public void start();

   /**
    * Get the id of the processor.
    * <p>
    * Very useful when debugging code or building complex processors.
    * 
    * @return the id of this processor
    */
   public String getId();
   
   /**
    * @return true if this processor is started, and yet not stopped
    */
   public boolean isStarted();
}
