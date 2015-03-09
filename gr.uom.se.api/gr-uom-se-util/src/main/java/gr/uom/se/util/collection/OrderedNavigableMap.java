/**
 * 
 */
package gr.uom.se.util.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Elvis Ligu
 */
public class OrderedNavigableMap<K, V> implements NavigableMap<K, V>,
      OrderedMap<K, V>, Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = -8933979883625453702L;

   private final NavigableMap<K, V> map;

   /**
    * Constructs a new, empty tree map, using the natural ordering of its keys.
    * All keys inserted into the map must implement the Comparable interface.
    * Furthermore, all such keys must be mutually comparable: k1.compareTo(k2)
    * must not throw a ClassCastException for any keys k1 and k2 in the map. If
    * the user attempts to put a key into the map that violates this constraint
    * (for example, the user attempts to put a string key into a map whose keys
    * are integers), the put(Object key, Object value) call will throw a
    * ClassCastException.
    */
   public OrderedNavigableMap() {
      this.map = new TreeMap<K, V>();
   }

   /**
    * Constructs a new, empty tree map, ordered according to the given
    * comparator. All keys inserted into the map must be mutually comparable by
    * the given comparator: comparator.compare(k1, k2) must not throw a
    * ClassCastException for any keys k1 and k2 in the map. If the user attempts
    * to put a key into the map that violates this constraint, the put(Object
    * key, Object value) call will throw a ClassCastException.
    * 
    * @param comparator
    *           the comparator that will be used to order this map. If null, the
    *           natural ordering of the keys will be used.
    */
   public OrderedNavigableMap(Comparator<? super K> comparator) {
      this.map = new TreeMap<K, V>(comparator);
   }

   /**
    * Constructs a new tree map containing the same mappings as the given map,
    * ordered according to the natural ordering of its keys. All keys inserted
    * into the new map must implement the Comparable interface. Furthermore, all
    * such keys must be mutually comparable: k1.compareTo(k2) must not throw a
    * ClassCastException for any keys k1 and k2 in the map. This method runs in
    * n*log(n) time.
    * 
    * @param map
    *           the map whose mappings are to be placed in this map
    */
   public OrderedNavigableMap(Map<? extends K, ? extends V> map) {
      this.map = new TreeMap<K, V>(map);
   }

   /**
    * Create a map, that will be baked by the given map.
    * <p>
    * Should be used by its subclasses.
    * 
    * @param map
    */
   protected OrderedNavigableMap(NavigableMap<K, V> map) {
      this.map = map;
   }

   /**
    * Constructs a new tree map containing the same mappings and using the same
    * ordering as the specified sorted map. This method runs in linear time.
    * 
    * @param map
    *           the sorted map whose mappings are to be placed in this map, and
    *           whose comparator is to be used to sort this map
    */
   public OrderedNavigableMap(SortedMap<K, ? extends V> map) {
      this.map = new TreeMap<K, V>(map);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Comparator<? super K> comparator() {
      return this.map.comparator();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public K firstKey() {
      return this.map.firstKey();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public K lastKey() {
      return this.map.lastKey();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<K> keySet() {
      return this.map.keySet();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Collection<V> values() {
      return this.map.values();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<java.util.Map.Entry<K, V>> entrySet() {
      return this.map.entrySet();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int size() {
      return this.map.size();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isEmpty() {
      return this.map.isEmpty();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean containsKey(Object key) {
      return this.map.containsKey(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean containsValue(Object value) {
      return this.map.containsValue(value);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public V get(Object key) {
      return this.map.get(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public V put(K key, V value) {
      return this.map.put(key, value);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public V remove(Object key) {
      return this.map.remove(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void putAll(Map<? extends K, ? extends V> m) {
      this.map.putAll(m);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void clear() {
      this.map.clear();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public java.util.Map.Entry<K, V> lowerEntry(K key) {
      return this.map.lowerEntry(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public K lowerKey(K key) {
      return this.map.lowerKey(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public java.util.Map.Entry<K, V> floorEntry(K key) {
      return this.map.floorEntry(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public K floorKey(K key) {
      return this.map.floorKey(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public java.util.Map.Entry<K, V> ceilingEntry(K key) {
      return this.map.ceilingEntry(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public K ceilingKey(K key) {
      return this.map.ceilingKey(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public java.util.Map.Entry<K, V> higherEntry(K key) {
      return this.map.higherEntry(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public K higherKey(K key) {
      return this.map.higherKey(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public java.util.Map.Entry<K, V> firstEntry() {
      return this.map.firstEntry();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public java.util.Map.Entry<K, V> lastEntry() {
      return this.map.lastEntry();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public java.util.Map.Entry<K, V> pollFirstEntry() {
      return this.map.pollFirstEntry();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public java.util.Map.Entry<K, V> pollLastEntry() {
      return this.map.pollLastEntry();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public NavigableMap<K, V> descendingMap() {
      return this.map.descendingMap();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public NavigableSet<K> navigableKeySet() {
      return this.map.navigableKeySet();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public NavigableSet<K> descendingKeySet() {
      return this.map.descendingKeySet();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey,
         boolean toInclusive) {
      return this.map.subMap(fromKey, fromInclusive, toKey, toInclusive);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
      return this.map.headMap(toKey, inclusive);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
      return this.map.tailMap(fromKey, inclusive);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SortedMap<K, V> subMap(K fromKey, K toKey) {
      return this.map.subMap(fromKey, toKey);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SortedMap<K, V> headMap(K toKey) {
      return this.map.headMap(toKey);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SortedMap<K, V> tailMap(K fromKey) {
      return this.map.tailMap(fromKey);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
      return this.map.toString();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode() {
      return this.map.hashCode();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object obj) {
      return this.map.equals(obj);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Iterator<K> iterator() {
      return this.map.keySet().iterator();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public K getNext(K key) {
      return this.map.navigableKeySet().higher(key);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public K getPrevious(K key) {
      return this.map.navigableKeySet().lower(key);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Iterator<K> reverseIterator() {
      return this.map.descendingKeySet().iterator();
   }
}
