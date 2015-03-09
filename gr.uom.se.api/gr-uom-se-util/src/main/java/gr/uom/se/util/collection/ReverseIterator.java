package gr.uom.se.util.collection;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * An iterator implementation that delegates it calls to a list iterator, in
 * reverse.
 * <p>
 * Given a list or a list iterator this each call to {@link #next()} will
 * delegate to {@link ListIterator#previous()} and calling {@link #hasNext()}
 * will delegate to {@link ListIterator#hasPrevious()}. If a list is supplied
 * while constructing the instance it will set the iterator to the last position
 * of this list so it can traverse all elements in reverse order.
 * 
 * @author Elvis Ligu
 * @param <T>
 */
public class ReverseIterator<T> implements Iterator<T> {
   private final ListIterator<T> iterator;

   /**
    * Create a reverse iterator based on the given iterator.
    * <p>
    */
   public ReverseIterator(ListIterator<T> it) {
      iterator = it;
   }

   /**
    * Create a reverse iterator based on the given list.
    * <p>
    */
   public ReverseIterator(List<T> list) {
      this(list.listIterator(list.size()));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean hasNext() {
      return iterator.hasPrevious();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public T next() {
      return iterator.previous();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void remove() {
      iterator.remove();
   }

}