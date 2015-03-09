/**
 * 
 */
package gr.uom.se.util.collection;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * An abstract implementation of a BiMap that is based on two maps.
 * <p>
 * 
 * @author Elvis Ligu
 * @param <K>
 * @param <V>
 */
public abstract class AbstractBiMap<K, V> implements BiMap<K, V> {
   /**
    * Mapping of keys to values.
    */
   protected final Map<K, V> keys;
   /**
    * Mapping of values to keys.
    */
   protected final Map<V, K> values;

   /**
    * 
    */
   protected AbstractBiMap(Map<K, V> keys, Map<V, K> values) {
      this.keys = keys;
      this.values = values;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<K> keys() {
      return Collections.unmodifiableSet(keys.keySet());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<K> keysCopy() {
      Set<K> set = keys.keySet();
      Set<K> result = new LinkedHashSet<>();
      result.addAll(set);
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Map<K, V> keyMappingCopy() {
      Map<K, V> map = new HashMap<>(keys);
      return map;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Map<K, V> keyMapping() {
      return Collections.unmodifiableMap(keys);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Map<V, K> valueMapping() {
      return Collections.unmodifiableMap(values);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Map<V, K> valueMappingCopy() {
      Map<V, K> map = new HashMap<>(values);
      return map;
   }

   /**
    * Get a view of these values.
    * <p>
    * NOTE: when using this view do not alter the map, otherwise the results
    * will be unpredictable. Also do not alter this set directly. Instead use
    * this map in order to alter the mapped pairs.
    * 
    * @return values
    */
   @Override
   public Set<V> values() {
      return Collections.unmodifiableSet(values.keySet());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<V> valuesCopy() {
      Set<V> set = values.keySet();
      Set<V> result = new LinkedHashSet<>();
      result.addAll(set);
      return result;
   }

   /**
    * Put a mapping from keys to values, and vice versa.
    * <p>
    * This will be a bi directional mapping of key-value pair.
    * 
    * @param k
    *           the key
    * @param v
    *           the value
    * @return the old mapped value
    */
   @Override
   public V put(K k, V v) {
      ArgsCheck.notNull("key", k);
      ArgsCheck.notNull("value", v);
      V old = keys.put(k, v);
      if (old != null) {
         values.remove(old);
      }
      values.put(v, k);
      return old;
   }

   /**
    * Get the value for the given key.
    * <p>
    * 
    * @param key
    *           to find the mapping
    * @return the mapped value of the given key
    */
   @Override
   public V get(Object key) {
      ArgsCheck.notNull("key", key);
      return keys.get(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public K getKey(V value) {
      ArgsCheck.notNull("value", value);
      return values.get(value);
   }

   /**
    * Remove the given mapping of the key.
    * <p>
    * 
    * @param key
    *           to be removed
    * @return the old value mapped to this key
    */
   @Override
   public V remove(Object key) {
      ArgsCheck.notNull("key", key);
      V old = keys.remove(key);
      if (old != null) {
         values.remove(old);
      }
      return old;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public K removeKey(V value) {
      ArgsCheck.notNull("value", value);
      K old = values.remove(value);
      if (old != null) {
         keys.remove(old);
      }
      return old;
   }

   @Override
   public void clear() {
      keys.clear();
      values.clear();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int size() {
      return keys.size();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isEmpty() {
      return keys.isEmpty();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean containsKey(Object key) {
      ArgsCheck.notNull("key", key);
      return keys.containsKey(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean containsValue(Object value) {
      ArgsCheck.notNull("value", value);
      return values.containsKey(value);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void putAll(Map<? extends K, ? extends V> m) {
      ArgsCheck.notNull("map", m);
      for (K key : m.keySet()) {
         if (key != null) {
            V value = m.get(key);
            if (value != null) {
               put(key, value);
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<K> keySet() {
      return keys();
   }

   /**
    * {@inheritDoc}
    * <p>
    * <b>This operation is unsupported.
    */
   @Override
   public Set<Map.Entry<K, V>> entrySet() {
      throw new UnsupportedOperationException();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object obj) {
      return keys.equals(obj);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equalValues(Object obj) {
      return values.equals(obj);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
      return keys.toString();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Iterator<K> iterator() {
      return keys.keySet().iterator();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Iterator<V> valuesIterator() {
      return values.keySet().iterator();
   }
}