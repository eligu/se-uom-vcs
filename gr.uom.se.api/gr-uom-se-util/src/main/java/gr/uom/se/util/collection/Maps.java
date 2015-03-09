/**
 * 
 */
package gr.uom.se.util.collection;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.Comparator;
import java.util.Map;

/**
 * @author Elvis Ligu
 */
public final class Maps {

   /**
    * Given a map revert its key value pairs and put them to the reverted map.
    * <p>
    * 
    * @param map
    *           the source of key value pairs
    * @param reverted
    *           the destination
    * @return a the same map reverted key value pairs from source
    */
   public static <K, V, T extends Map<V, K>> T getReverted(
         Map<? extends K, ? extends V> map, T reverted) {
      ArgsCheck.notNull("map", map);
      ArgsCheck.notNull("reverted", reverted);
      for (K key : map.keySet()) {
         reverted.put(map.get(key), key);
      }
      return reverted;
   }

   /**
    * Add all mappings of keys to values from {@code source} to {@code keys} and
    * {@code values} by keeping bidirectional mappings.
    * <p>
    * 
    * @param source
    *           the source of values
    * @param keys
    *           the keys
    * @param values
    *           the values
    */
   public static <K, V> void addAllBidirectional(
         Map<? extends K, ? extends V> source, Map<K, V> keys, Map<V, K> values) {
      ArgsCheck.notNull("source", source);
      ArgsCheck.notNull("keys", keys);
      ArgsCheck.notNull("values", values);
      for (K key : source.keySet()) {
         if (key != null) {
            V value = source.get(key);
            if (value != null) {
               keys.put(key, value);
               values.put(value, key);
            }
         }
      }
   }

   /**
    * Get a new instance of an ordered map.
    * <p>
    * 
    * @return a new instance of an ordered map.
    */
   public static <K, V> OrderedMap<K, V> newOrderedMap() {
      return newOrderedNavigableMap();
   }

   /**
    * Get a new instance of an ordered map.
    * <p>
    * 
    * @param map
    *           which contains the initial elements to be inserted into map.
    * @return a new instance of an ordered map.
    */
   public static <K, V> OrderedMap<K, V> newOrderedMap(
         Map<? extends K, ? extends V> map) {
      return newOrderedNavigableMap(map);
   }

   /**
    * Get a new instance of an ordered map.
    * <p>
    * 
    * @param comp
    *           key comparator.
    * @return a new instance of an ordered navigable map.
    */
   public static <K, V> OrderedNavigableMap<K, V> newOrderedMap(
         Comparator<? super K> comp) {
      return newOrderedNavigableMap(comp);
   }

   /**
    * Create a new ordered bi directional map.
    * 
    * @return a new ordered bi directional map.
    */
   public static <K, V> OrderedBiMap<K, V> newOrderedBiMap() {
      return new OrderedBiNavigableMap<>();
   }

   /**
    * Get a new instance of an ordered bi directional map.
    * <p>
    * 
    * @param map
    *           which contains the initial elements to be inserted into map.
    * @return a new instance of an ordered navigable map.
    */
   public static <K, V> OrderedBiMap<K, V> newOrderedBiMap(
         Map<? extends K, ? extends V> map) {

      return new OrderedBiNavigableMap<>(map);
   }

   /**
    * Get a new instance of an ordered bi directional map.
    * <p>
    * 
    * @param keyComp
    *           key comparator.
    * @return a new instance of an ordered bi map.
    */
   public static <K, V> OrderedBiMap<K, V> newOrderedBiMap(
         Comparator<? super K> keyCom, Comparator<? super V> valueComp) {
      return new OrderedBiNavigableMap<>(keyCom, valueComp);
   }

   /**
    * Get a new instance of an ordered navigable map.
    * <p>
    * 
    * @return a new instance of an ordered navigable map.
    */
   public static <K, V> OrderedNavigableMap<K, V> newOrderedNavigableMap() {
      return new OrderedNavigableMap<>();
   }

   /**
    * Get a new instance of an ordered navigable map.
    * <p>
    * 
    * @param map
    *           which contains the initial elements to be inserted into map.
    * @return a new instance of an ordered navigable map.
    */
   public static <K, V> OrderedNavigableMap<K, V> newOrderedNavigableMap(
         Map<? extends K, ? extends V> map) {
      return new OrderedNavigableMap<>(map);
   }

   /**
    * Get a new instance of an ordered navigable map.
    * <p>
    * 
    * @param comp
    *           key comparator.
    * @return a new instance of an ordered navigable map.
    */
   public static <K, V> OrderedNavigableMap<K, V> newOrderedNavigableMap(
         Comparator<? super K> comp) {
      return new OrderedNavigableMap<>(comp);
   }

   /**
    * Create a new bi directional map.
    * 
    * @return a new bi directional map.
    */
   public static <K, V> BiMap<K, V> newBiMap() {
      return new BiHashMap<>();
   }

   /**
    * Get a new instance of a bi directional map.
    * <p>
    * 
    * @param map
    *           which contains the initial elements to be inserted into map.
    * @return a new instance of a bi map.
    */
   public static <K, V> BiMap<K, V> newBiMap(Map<? extends K, ? extends V> map) {

      return new BiHashMap<>(map);
   }

   /**
    * Create a new bi directional map.
    * 
    * @param capacity
    *           initial capacity
    * @return a new bi directional map.
    */
   public static <K, V> BiMap<K, V> newBiMap(int capacity) {
      return new BiHashMap<>(capacity);
   }
}
