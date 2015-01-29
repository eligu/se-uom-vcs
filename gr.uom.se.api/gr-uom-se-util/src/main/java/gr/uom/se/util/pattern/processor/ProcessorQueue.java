/**
 * 
 */
package gr.uom.se.util.pattern.processor;

/**
 * An extension interface to a processor that will simplify the use of the
 * processors.
 * <p>
 * 
 * Having a data set (of the same type) and a family of processors we can
 * iterate on the data set and pass each piece of it to all processors. A queue
 * processor does exactly that. It maintains a family of processors that process
 * the same types of data, and manages the processing of its data piece, by
 * delivering each data to his processors.
 * <p>
 * Except from adding and removing processors from the queue, the queue itself
 * is a processor, however when the method {@link #process(Object)} is called it
 * will delegate the call to all processors it contains. Seeing a queue as a
 * processor we can build complex processors. For example we can have a queue
 * that runs all its processors in serial (see {@link SerialQueue} and a queue
 * that runs in parallel {@link ThreadQueueImp}), and putting both of them to a
 * another serial queue we can build a mixed queue, where some processors run in
 * serial and some in parallel. On the other hand when an object need to obtain
 * two or more processors, to process some data of him, instead of having a list
 * and manage the processors he can just use a single processor, and that could
 * be a queue of processors, constructed outside of the object itself, so we can
 * decouple the logic of processor executors from the object itself.
 * <p>
 * A general usage pattern is the following:
 * 
 * <pre>
 * Processor&ltT&gt p1 = ...
 * Processor&ltT&gt p2 = ...
 * Processor&ltT&gt p3 = ...
 * 
 * ProcessorQueue&ltT&gt queue = new SerialQueue&ltT&gt();
 * queue.add(p1);
 * queue.add(p2);
 * queue.add(p3);
 * 
 * queue.start();
 * for(T t : data) {
 *    queue.process(t);
 * }
 * queue.stop();
 * </pre>
 * 
 * The above code will add all three processors to a serial queue, and then will
 * start the processing. Each entity will be passed to the queue, which in turn
 * will pass each entity to every processor in the queue, preserving the the
 * turn of processors.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface ProcessorQueue<T> extends Processor<T> {

   /**
    * Add the given processor to this queue.
    * <p>
    * 
    * The queue may contains two equal processors and is not required to check
    * if a given processor is already queued. However, depending on
    * implementations a queue may allow only distinct processors, and a good
    * candidate to distinct two processors is their ID.
    * 
    * @param processor
    *           to be added to this queue. Must not be null.
    */
   public void add(Processor<T> processor);

   /**
    * Remove the given processor from this queue.
    * <p>
    * 
    * @param processor
    *           to be removed. Must not be null.
    */
   public void remove(Processor<T> processor);

   /**
    * Will remove all processors from this queue.
    * <p>
    */
   public void removeAll();

   /**
    * {@inheritDoc}
    * <p>
    * Each time an entity is retrieved for processing, it will be delivered to
    * all registered processors this queue contains (their
    * {@code process(Object)} method will be called).
    */
   @Override
   public boolean process(T entity);

   /**
    * Given a processor id will return the processor with the same id.
    * <p>
    * Note that each processor must have a unique id. If there are two
    * processors with the same id it will return the first found.
    * 
    * @param pid
    *           processor id, must not be null
    * @return a processor with the specified id
    */
   public Processor<T> getProcessor(String pid);

   /**
    * @return the number of processors this queue contains.
    */
   public int getProcessorsCount();
}
