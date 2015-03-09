package gr.uom.se.util.collection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A bi-directional map backed by two maps, one for keys and one for
 * values.
 * <p>
 * This map implementation doesn't support null values or keys. Any attempt to
 * use a null with this map will throw an exception.
 * <p>
 * <b>WARNING:</b> method {@link #entrySet()} is not supported by this map and
 * will throw an exception if it is called.
 * 
 * @author Theodore Chaikalis
 * @author Elvis Ligu
 */
public class BiHashMap<K, V> extends AbstractBiMap<K, V> implements
      Serializable, BiMap<K, V> {

   /**
    * 
    */
   private static final long serialVersionUID = 1508355269052784425L;

   /**
    * Creates an instance of this map with default configurations.
    */
   public BiHashMap() {
      this(new HashMap<K, V>());
   }

   /**
    * Create an instance of this map provided the given keys and values.
    * <p>
    * This will drop all mapping and will only use the two maps as the baking
    * maps for keys and values.
    * 
    * @param keys
    */
   public BiHashMap(Map<? extends K, ? extends V> keys) {
      super(new HashMap<K, V>(keys.size()), new HashMap<V, K>(keys.size()));
      Maps.addAllBidirectional(keys, this.keys, this.values);
   }

   /**
    * Create an instance of this map with the given initial capacity.
    * <p>
    * 
    * @param capacity
    *           the initial capacity of this map.
    */
   public BiHashMap(int capacity) {
      super(new HashMap<K, V>(capacity), new HashMap<V, K>(capacity));
   }
}
