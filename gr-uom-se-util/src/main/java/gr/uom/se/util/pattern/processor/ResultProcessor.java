/**
 * 
 */
package gr.uom.se.util.pattern.processor;

/**
 * A processor that may be used to return a result.
 * <p>
 * This interface is decoupled from the plain processor interface because not
 * all type of processors need to return a specific result. For example all
 * {@link ThreadQueueImp} processors just delegate each entity to be
 * processed to their queued processors, but queues are not required to return a
 * specific kind of result because each queued processor may return a different
 * type of result. However the type of a processor passed in a queue is
 * parameterized so we can pass a result processor in the queue.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface ResultProcessor<T, V> extends Processor<T> {

   /**
    * Return the result computed by this processor.
    * <p>
    * It is highly recommended that this method should be called after the
    * {@link #stop()} is called, so the processor could finish up its work.
    * Implementations may choose to throw an exception when this method is called
    * before stopping the processor.
    * 
    * @return the result of this processors.
    */
   public V getResult();
}
