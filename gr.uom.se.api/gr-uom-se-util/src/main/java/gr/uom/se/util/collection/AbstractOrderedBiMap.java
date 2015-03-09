/**
 * 
 */
package gr.uom.se.util.collection;

import java.util.Iterator;

/**
 * An abstract implementation of ordered bi map that uses two ordered maps.
 * <p>
 * 
 * @author Elvis Ligu
 */
public abstract class AbstractOrderedBiMap<K, V> extends AbstractBiMap<K, V> implements
      OrderedBiMap<K, V> {

   protected final OrderedMap<K, V> myKeys;
   protected final OrderedMap<V, K> myValues;

   /**
    * @param keys
    * @param values
    */
   protected AbstractOrderedBiMap(OrderedMap<K, V> keys, OrderedMap<V, K> values) {
      super(keys, values);
      myKeys = keys;
      myValues = values;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public K getNext(K key) {
      return myKeys.getNext(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public K getPrevious(K key) {
      return myKeys.getPrevious(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Iterator<K> reverseIterator() {
      return myKeys.reverseIterator();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public V getNextValue(V value) {
      return myValues.getNext(value);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public V getPreviousValue(V value) {
      return myValues.getPrevious(value);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Iterator<V> reverseValuesIterator() {
      return myValues.reverseIterator();
   }

}
