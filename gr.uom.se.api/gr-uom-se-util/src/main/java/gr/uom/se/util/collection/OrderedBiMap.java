/**
 * 
 */
package gr.uom.se.util.collection;

import java.util.Iterator;

/**
 * An order bi directional map that keeps a mapping from keys to values and
 * reverse mapping from values to keys, which allows the ordered retrieval of
 * keys (such as get next or get previous) and ordered retrieval of values.
 * 
 * @author Elvis Ligu
 */
public interface OrderedBiMap<K, V> extends OrderedMap<K, V>, BiMap<K, V> {

   /**
    * Given a value return its next value if one exist or null if there is no
    * next value.
    * 
    * @param value
    *           to look for the next value
    * @return the next value of the given value or null if there is no next.
    */
   V getNextValue(V value);

   /**
    * Given a value return its previous value if one exist or null if there is
    * no previous value.
    * 
    * @param value
    *           to look for the previous value
    * @return the previous value of the given value or null if there is no
    *         previous.
    */
   V getPreviousValue(V value);

   /**
    * Get a reverse value iterator for this ordered map.
    * <p>
    * 
    * @return a reverse value iterator.
    */
   Iterator<V> reverseValuesIterator();
}
