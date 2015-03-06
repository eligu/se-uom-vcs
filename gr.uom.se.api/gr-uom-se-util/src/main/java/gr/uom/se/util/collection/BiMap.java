package gr.uom.se.util.collection;

import gr.uom.se.util.validation.ArgsCheck;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread safe bi-directional map backed by two maps, one for keys and one for
 * values.
 * <p>
 * This map implementation doesn't support null values or keys. Any attempt to
 * use a null with this map will throw an exception.
 * <p>
 * When constructing instances of this map by specifying the baking maps do not
 * use concurrent maps as they should add an overhead. Instead if a non-thread
 * safe implementation exist use that, because all methods of this map are
 * thread safe.
 * <p>
 * <b>WARNING:</b> method {@link #entrySet()} is not supported by this map and
 * will throw an exception if it is called.
 * 
 * @author Theodore Chaikalis
 * @author Elvis Ligu
 */
public class BiMap<K, V> implements Map<K, V>, Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 2753278274240643943L;

   /**
    * Mapping of keys to values.
    */
   private final Map<K, V> keys;
   /**
    * Mapping of values to keys.
    */
   private final Map<V, K> values;

   /**
    * Map lock.
    */
   private ReadWriteLock lock = new ReentrantReadWriteLock();

   /**
    * Creates an instance of this map with default configurations.
    */
   public BiMap() {
      this(new HashMap<K, V>(), new HashMap<V, K>());
   }

   /**
    * Create an instance of this map provided the given keys and values.
    * <p>
    * This will drop all mapping and will only use the two maps as the baking
    * maps fo keys and values.
    * 
    * @param keys
    *           the mapping of keys to values.
    * @param values
    *           the mapping of values to keys.
    */
   public BiMap(Map<K, V> keys, Map<V, K> values) {
      this.keys = keys;
      this.values = values;
   }

   /**
    * Create an instance of this map with the given initial capacity.
    * <p>
    * 
    * @param capacity
    *           the initial capacity of this map.
    */
   public BiMap(int capacity) {
      this(new HashMap<K, V>(capacity), new HashMap<V, K>(capacity));
   }

   /**
    * Get a view of these keys.
    * <p>
    * NOTE: when using this view do not alter the map, otherwise the results
    * will be unpredictable. Also do not alter this set directly. Instead use
    * this map in order to alter the mapped pairs.
    * 
    * @return keys
    */
   public Set<K> keys() {
      lock.readLock().lock();
      try {
         return Collections.unmodifiableSet(keys.keySet());
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * Get a copy of the keys of this map.
    * <p>
    * You can freely modify this copy. Although the returned collection is not a
    * set it will contains only the mapped keys.
    * 
    * @return a copy of the keys set.
    */
   public Collection<K> keysCopy() {
      lock.readLock().lock();
      try {
         Set<K> set = keys.keySet();

         Collection<K> result = new ArrayList<>();
         result.addAll(set);
         return result;
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * Get a copy view of the mappings from keys to values.
    * <p>
    * The copy will not preserve the characteristics of the bakings maps.
    * 
    * @return a copy of the mappings from keys to values.
    */
   public Map<K, V> keyMappingCopy() {
      lock.readLock().lock();
      try {
         Map<K, V> map = new HashMap<>(keys);
         return map;
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * Get an unmodifiable view of the mappings from keys to values.
    * <p>
    * If this map is modified while clients are iterating through the returned
    * map, the result will be unspecified.
    * 
    * @return mappings from keys to values.
    */
   public Map<K, V> keyMapping() {
      lock.readLock().lock();
      try {
         return Collections.unmodifiableMap(keys);
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * Get an unmodifiable view of the mappings from values to keys.
    * <p>
    * 
    * If this map is modified while clients are iterating through the returned
    * map, the result will be unspecified.
    * 
    * @return mappings from keys to values.
    */
   public Map<V, K> valueMapping() {
      lock.readLock().lock();
      try {
         return Collections.unmodifiableMap(values);
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * Get a copy view of the mappings from values to keys.
    * <p>
    * The copy will not preserve the characteristics of the baking maps.
    * 
    * @return a copy of mappings from values to keys.
    */
   public Map<V, K> valueMappingCopy() {
      lock.readLock().lock();
      try {
         Map<V, K> map = new HashMap<>(values);
         return map;
      } finally {
         lock.readLock().unlock();
      }
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
      lock.readLock().lock();
      try {
         return Collections.unmodifiableSet(values.keySet());
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * Get a copy of the values of this map.
    * <p>
    * You can freely modify this copy. Although the returned collection is not a
    * set, it will contains only the mapped values.
    * 
    * @return a copy of the keys set.
    */
   public Collection<V> valuesCopy() {
      lock.readLock().lock();
      try {
         Set<V> set = values.keySet();

         Collection<V> result = new ArrayList<>();
         result.addAll(set);
         return result;
      } finally {
         lock.readLock().unlock();
      }
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
      lock.writeLock().lock();
      try {
         return put0(k, v);
      } finally {
         lock.writeLock().unlock();
      }
   }

   private V put0(K k, V v) {
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
      lock.readLock().lock();
      try {
         return keys.get(key);
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * Get the mapping of the given value.
    * 
    * <p>
    * 
    * @param value
    *           to find the key for.
    * @return the mapping of this value (the key).
    */
   public K getKey(V value) {
      ArgsCheck.notNull("value", value);
      lock.readLock().lock();
      try {
         return values.get(value);
      } finally {
         lock.readLock().unlock();
      }
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
      lock.writeLock().lock();
      try {
         V old = keys.remove(key);
         if (old != null) {
            values.remove(old);
         }
         return old;
      } finally {
         lock.writeLock().unlock();
      }
   }

   /**
    * Remove the given mapping of key value pairs, by using the value.
    * <p>
    * 
    * @param value
    *           to be removed
    * @return the old mapped key.
    */
   public K removeKey(V value) {
      ArgsCheck.notNull("value", value);
      lock.writeLock().lock();
      try {
         K old = values.remove(value);
         if (old != null) {
            keys.remove(old);
         }
         return old;
      } finally {
         lock.writeLock().unlock();
      }
   }

   @Override
   public void clear() {
      lock.writeLock().lock();
      try {
         keys.clear();
         values.clear();
      } finally {
         lock.writeLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int size() {
      lock.readLock().lock();
      try {
         return keys.size();
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isEmpty() {
      lock.readLock().lock();
      try {
         return keys.isEmpty();
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean containsKey(Object key) {
      ArgsCheck.notNull("key", key);
      lock.readLock().lock();
      try {
         return keys.containsKey(key);
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean containsValue(Object value) {
      ArgsCheck.notNull("value", value);
      lock.readLock().lock();
      try {
         return values.containsKey(value);
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void putAll(Map<? extends K, ? extends V> m) {
      ArgsCheck.notNull("map", m);
      lock.writeLock().lock();
      try {
         for (K key : m.keySet()) {
            if (key != null) {
               V value = m.get(key);
               if (value != null) {
                  put0(key, value);
               }
            }
         }
      } finally {
         lock.writeLock().unlock();
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
    * The same as {@linkplain #equals(Object) equals} but it will use the
    * reversed {value:key} mapping to check for equality.
    * 
    * @param obj
    *           map object
    * @return true if values mapping are equals to the specified object.
    */
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
}
