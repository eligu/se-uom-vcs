/**
 * 
 */
package gr.uom.se.util.collection;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A bi directional map that keeps a mapping from keys to values, and a reverse
 * mapping from values to keys.
 * 
 * @author Elvis Ligu
 * @param <K>
 * @param <V>
 */
public interface BiMap<K, V> extends Map<K, V>, Iterable<K> {

   /**
    * Get a view of these keys.
    * <p>
    * NOTE: when using this view do not alter the map, otherwise the results
    * will be unpredictable. Also do not alter this set directly. Instead use
    * this map in order to alter the mapped pairs.
    * 
    * @return keys
    */
   Set<K> keys();

   /**
    * Get a copy of the keys of this map.
    * <p>
    * You can freely modify this copy. Although the returned collection is not a
    * set it will contains only the mapped keys.
    * 
    * @return a copy of the keys set.
    */
   Set<K> keysCopy();

   /**
    * Get a copy view of the mappings from keys to values.
    * <p>
    * The copy will not preserve the characteristics of the bakings maps.
    * 
    * @return a copy of the mappings from keys to values.
    */
   Map<K, V> keyMappingCopy();

   /**
    * Get an unmodifiable view of the mappings from keys to values.
    * <p>
    * If this map is modified while clients are iterating through the returned
    * map, the result will be unspecified.
    * 
    * @return mappings from keys to values.
    */
   Map<K, V> keyMapping();

   /**
    * Get an unmodifiable view of the mappings from values to keys.
    * <p>
    * 
    * If this map is modified while clients are iterating through the returned
    * map, the result will be unspecified.
    * 
    * @return mappings from keys to values.
    */
   Map<V, K> valueMapping();

   /**
    * Get a copy view of the mappings from values to keys.
    * <p>
    * The copy will not preserve the characteristics of the baking maps.
    * 
    * @return a copy of mappings from values to keys.
    */
   Map<V, K> valueMappingCopy();

   /**
    * Get a copy of the values of this map.
    * <p>
    * You can freely modify this copy. Although the returned collection is not a
    * set, it will contains only the mapped values.
    * 
    * @return a copy of the keys set.
    */
   Set<V> valuesCopy();

   /**
    * Get the mapping of the given value.
    * 
    * <p>
    * 
    * @param value
    *           to find the key for.
    * @return the mapping of this value (the key).
    */
   K getKey(V value);

   /**
    * Remove the given mapping of key value pairs, by using the value.
    * <p>
    * 
    * @param value
    *           to be removed
    * @return the old mapped key.
    */
   K removeKey(V value);

   /**
    * The same as {@linkplain #equals(Object) equals} but it will use the
    * reversed {value:key} mapping to check for equality.
    * 
    * @param obj
    *           map object
    * @return true if values mapping are equals to the specified object.
    */
   boolean equalValues(Object obj);

   /**
    * Get an iterator on values.
    */
   Iterator<V> valuesIterator();
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Set<V> values();
}