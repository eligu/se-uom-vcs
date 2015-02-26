/**
 * 
 */
package gr.uom.se.util.pattern.processor;

/**
 * A general interface used in situations when a producer produce the data and
 * then pass them to {@link #process(Object)} method (the consumer).
 * <p>
 * 
 * This interface can be utilized in situation when for a single source of data
 * there are many independent processes that has to be done. For example
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
 * <li>{@link SerialProcessorQueue} - works the same as the above code. Each data is
 * passed to the list of processors in a serial way (no threads)</li>
 * <li>{@link DefaultParallelProcessorQueue} - works the same as the above code, but you can
 * specify a number of threads so the incoming data (from
 * {@link #process(Object)} method) will be passed to each processor in the
 * queue who will run in parallel, depending on the number of threads.</li>
 * <li>{@link BlockingParallelProcessorQueue} - works the same as the parallel queue processor
 * but is very efficient when a new item is passed to the queue because it will
 * block the current thread for sending more data if all the worker threads (the
 * threads that runs the processors) are busy, until one of them finishes
 * processing.</li>
 * </ul>
 * 
 * Before starting any processing of data, it is required that {@link #start()}
 * method should be invoked. This will inform this processor that the data
 * should be coming soon so he may need to initialize its state.
 * <p>
 * After all data are consumed a call to {@link #stop()} is required, in order
 * to clean its internal state.
 * <p>
 * A general usage pattern is:
 * 
 * <pre>
 * Collection&ltT&gt data = ...
 * Processor&ltT&gt processor = ...
 * 
 * // Start the processing of data here
 * processor.start();
 * // Process data
 * for(T t : data) {
 *    processor.process(t);
 * }
 * // The data processing is stopped
 * processor.stop();
 * </pre>
 * 
 * Note: Each processor must have a unique id, especially when working with
 * processor queues. In such cases a queue may allow only unique processors to
 * be added. Moreover, when an exception is thrown by the queue it may include
 * the id of the processor caused the exception in the message of the thrown
 * exception. Each processor must be thread safe if he is going to be used in a
 * parallel queue. For more info see {@link ProcessorQueue} and its
 * implementations. Finally, generally speaking a processor may be reused after
 * a {@code start/process/stop} use case, however it is recommended to read the
 * implementation's documents if this is supported. 
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
    *         true then the processor should be able to process the next entry.
    */
   public boolean process(T entity);

   /**
    * Stop the processor after all entities are consumed.
    * <p>
    * When all entities have been processed by this processor this method should
    * be called, in order to inform the processor that there are no more
    * entries. This will allow the implementation of this processor to finalize
    * the results.
    * 
    * @throws InterruptedException
    *            when a work is in the middle of processing and can not be
    *            stopped.
    */
   public void stop() throws InterruptedException;

   /**
    * Start the processor before any entity is processed.
    * <p>
    * Before any entity is processed this method should be called in order to
    * inform the processor that it will start to get entities. This will allow
    * the processor to initialize its state in order to produce the new results.
    * <p>
    */
   public void start();

   /**
    * Get the id of the processor.
    * <p>
    * Very useful when debugging code or building complex processors.
    * Implementations should take care to provide a unique id for the
    * processor, as the inability to do so will cause different problems
    * especially when this processor is used within a queue.
    * 
    * @return the id of this processor
    */
   public String getId();

   /**
    * Check if this processor is started and yet not stopped.
    * <p>
    * A processor is started when the method {@link #start()} is called
    * and the {@link #stop()} is yet to be called. However a processor
    * may follow many times the usage pattern {@code start/process/stop},
    * and this method will return true if the processor is started
    * but not stopped, even in such situations.
    * 
    * @return true if this processor is started, and yet not stopped
    */
   public boolean isStarted();
}
