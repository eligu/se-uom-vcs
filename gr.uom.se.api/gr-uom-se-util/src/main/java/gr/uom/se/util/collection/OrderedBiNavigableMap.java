/**
 * 
 */
package gr.uom.se.util.collection;

import java.util.Comparator;
import java.util.Map;

/**
 * An ordered bi map implementation.
 * 
 * @author Elvis Ligu
 */
public class OrderedBiNavigableMap<K, V> extends AbstractOrderedBiMap<K, V> {

   /**
    * Create an instance base on the given maps.
    * <p>
    * This should be used internally, and keys and values should be the reverse
    * mapping.
    * 
    * @param keys
    * @param values
    */
   protected OrderedBiNavigableMap(OrderedMap<K, V> keys,
         OrderedMap<V, K> values) {
      super(keys, values);
   }

   /**
    * Create an instance based on the given map.
    * <p>
    * 
    * @param map
    */
   public OrderedBiNavigableMap(Map<? extends K, ? extends V> map) {
      super(new OrderedNavigableMap<>(map), Maps.getReverted(map,
            new OrderedNavigableMap<V, K>()));
   }

   /**
    * Create an instance based on the given comparators.
    * 
    * @param keyCom
    * @param valueComp
    */
   public OrderedBiNavigableMap(Comparator<? super K> keyCom,
         Comparator<? super V> valueComp) {
      super(new OrderedNavigableMap<K, V>(keyCom),
            new OrderedNavigableMap<V, K>(valueComp));
   }

   /**
    * Create a new ordered bi map with the defaults.
    * <p>
    */
   public OrderedBiNavigableMap() {
      super(new OrderedNavigableMap<K, V>(), new OrderedNavigableMap<V, K>());
   }
}
