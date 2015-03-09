/**
 * 
 */
package gr.uom.se.util.collection;

import java.util.Iterator;
import java.util.Map;

/**
 * An ordered map that can handle the retrieval of getting the next key of a
 * given key, or its next value. As well, as getting the previous key or value.
 * <p>
 * 
 * @author Elvis Ligu
 */
public interface OrderedMap<K, V> extends Map<K, V>, Iterable<K> {

   /**
    * Given a key return its next key if one exist or null if there is no next
    * key.
    * 
    * @param key
    *           to look for the next key
    * @return the next key of the given key or null if there is no next.
    */
   K getNext(K key);

   /**
    * Given a key return its previous key if one exist or null if there is no
    * previous key.
    * 
    * @param key
    *           to look for the previous key
    * @return the previous key of the given key or null if there is no previous.
    */
   K getPrevious(K key);

   /**
    * Get a reverse key iterator for this ordered map.
    * <p>
    * 
    * @return a reverse key iterator.
    */
   Iterator<K> reverseIterator();
}
