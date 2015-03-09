/**
 * 
 */
package gr.uom.se.util.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * @author Elvis Ligu
 */
public class BiMapTest {

   /**
    * Test method for {@link gr.uom.se.util.collection.BiHashMap#keys()}.
    */
   @Test
   public void testKeys() {
      BiMap<String, String> map = Maps.newBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      Set<String> keys = map.keys();
      assertEquals(1, keys.size());
      assertEquals(name, keys.iterator().next());
   }
   
   /**
    * Test method for {@link gr.uom.se.util.collection.OrderedBiNavigableMap#keys()}.
    */
   @Test
   public void testKeysOdrdered() {
      BiMap<String, String> map = Maps.newOrderedBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      Set<String> keys = map.keys();
      assertEquals(1, keys.size());
      assertEquals(name, keys.iterator().next());
   }

   /**
    * Test method for {@link gr.uom.se.util.collection.BiHashMap#keysCopy()}.
    */
   @Test
   public void testKeysCopy() {
      BiMap<String, String> map = Maps.newBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Collection<String> keys = map.keysCopy();
      assertEquals(1, keys.size());
      assertEquals(name, keys.iterator().next());
      keys.clear();
      assertEquals(surname, map.get(name));
   }
   
   /**
    * Test method for {@link gr.uom.se.util.collection.OrderedBiNavigableMap#keysCopy()}.
    */
   @Test
   public void testKeysCopyOrdered() {
      BiMap<String, String> map = Maps.newOrderedBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Collection<String> keys = map.keysCopy();
      assertEquals(1, keys.size());
      assertEquals(name, keys.iterator().next());
      keys.clear();
      assertEquals(surname, map.get(name));
   }

   /**
    * Test method for {@link gr.uom.se.util.collection.BiHashMap#keyMappingCopy()}.
    */
   @Test
   public void testKeyMappingCopy() {
      BiMap<String, String> map = Maps.newBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Map<String, String> keyMap = map.keyMappingCopy();
      assertEquals(map, keyMap);
      keyMap.put("key", "value");
      assertNotEquals(map, keyMap);
      keyMap.remove("key");
      assertEquals(map, keyMap);
      keyMap.clear();
      assertEquals(surname, map.get(name));
   }
   
   /**
    * Test method for {@link gr.uom.se.util.collection.OrderedBiNavigableMap#keyMappingCopy()}.
    */
   @Test
   public void testKeyMappingCopyOrdered() {
      BiMap<String, String> map = Maps.newOrderedBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Map<String, String> keyMap = map.keyMappingCopy();
      assertEquals(map, keyMap);
      keyMap.put("key", "value");
      assertNotEquals(map, keyMap);
      keyMap.remove("key");
      assertEquals(map, keyMap);
      keyMap.clear();
      assertEquals(surname, map.get(name));
   }

   /**
    * Test method for {@link gr.uom.se.util.collection.BiHashMap#keyMapping()}.
    */
   @Test
   public void testKeyMapping() {
      BiMap<String, String> map = Maps.newBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Map<String, String> keyMap = map.keyMapping();
      assertEquals(map, keyMap);
      assertEquals(surname, keyMap.get(name));
   }
   
   /**
    * Test method for {@link gr.uom.se.util.collection.OrderedBiNavigableMap#keyMapping()}.
    */
   @Test
   public void testKeyMappingOrdered() {
      BiMap<String, String> map = Maps.newOrderedBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Map<String, String> keyMap = map.keyMapping();
      assertEquals(map, keyMap);
      assertEquals(surname, keyMap.get(name));
   }

   /**
    * Test method for {@link gr.uom.se.util.collection.BiHashMap#valueMapping()}.
    */
   @Test
   public void testValueMapping() {
      BiMap<String, String> map = Maps.newBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Map<String, String> valueMap = map.valueMapping();
      assertTrue(map.equalValues(valueMap));
      assertEquals(name, valueMap.get(surname));
   }
   
   /**
    * Test method for {@link gr.uom.se.util.collection.OrderedBiNavigableMap#valueMapping()}.
    */
   @Test
   public void testValueMappingOrdered() {
      BiMap<String, String> map = Maps.newOrderedBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Map<String, String> valueMap = map.valueMapping();
      assertTrue(map.equalValues(valueMap));
      assertEquals(name, valueMap.get(surname));
   }

   /**
    * Test method for {@link gr.uom.se.util.collection.BiHashMap#valueMappingCopy()}
    * .
    */
   @Test
   public void testValueMappingCopy() {
      BiMap<String, String> map = Maps.newBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Map<String, String> valueMap = map.valueMappingCopy();
      assertTrue(map.equalValues(valueMap));
      valueMap.put("key", "value");
      assertFalse(map.equalValues(valueMap));
      valueMap.remove("key");
      assertTrue(map.equalValues(valueMap));
      valueMap.clear();
      assertEquals(surname, map.get(name));
   }
   
   /**
    * Test method for {@link gr.uom.se.util.collection.OrderedBiNavigableMap#valueMappingCopy()}
    * .
    */
   @Test
   public void testValueMappingCopyOrdered() {
      BiMap<String, String> map = Maps.newOrderedBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Map<String, String> valueMap = map.valueMappingCopy();
      assertTrue(map.equalValues(valueMap));
      valueMap.put("key", "value");
      assertFalse(map.equalValues(valueMap));
      valueMap.remove("key");
      assertTrue(map.equalValues(valueMap));
      valueMap.clear();
      assertEquals(surname, map.get(name));
   }

   /**
    * Test method for {@link gr.uom.se.util.collection.BiHashMap#values()}.
    */
   @Test
   public void testValues() {
      BiMap<String, String> map = Maps.newBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Set<String> values = map.values();
      assertEquals(1, values.size());
      assertEquals(surname, values.iterator().next());
   }
   
   /**
    * Test method for {@link gr.uom.se.util.collection.OrderedBiNavigableMap#values()}.
    */
   @Test
   public void testValuesOrdered() {
      BiMap<String, String> map = Maps.newOrderedBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Set<String> values = map.values();
      assertEquals(1, values.size());
      assertEquals(surname, values.iterator().next());
   }

   /**
    * Test method for {@link gr.uom.se.util.collection.BiHashMap#valuesCopy()}.
    */
   @Test
   public void testValuesCopy() {
      BiMap<String, String> map = Maps.newBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Collection<String> values = map.valuesCopy();
      assertEquals(1, values.size());
      assertEquals(surname, values.iterator().next());
      values.clear();
      assertEquals(surname, map.get(name));
   }
   
   /**
    * Test method for {@link gr.uom.se.util.collection.OrderedBiNavigableMap#valuesCopy()}.
    */
   @Test
   public void testValuesCopyOrdered() {
      BiMap<String, String> map = Maps.newOrderedBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Collection<String> values = map.valuesCopy();
      assertEquals(1, values.size());
      assertEquals(surname, values.iterator().next());
      values.clear();
      assertEquals(surname, map.get(name));
   }

   /**
    * Test method for:
    * <ul>
    * <li>{@link BiHashMap#put(Object, Object)}</li>
    * <li>{@link BiHashMap#get(Object)}</li>
    * <li>{@link BiHashMap#getKey(Object)}</li>
    * <li>{@link BiHashMap#containsKey(Object)}</li>
    * <li>{@link BiHashMap#containsValue(Object)}</li>
    * </ul>
    */
   @Test
   public void testPut() {
      BiMap<String, String> map = Maps.newBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      assertTrue(map.containsKey(name));
      assertTrue(map.containsValue(surname));
      assertEquals(surname, map.get(name));
      assertEquals(name, map.getKey(surname));
   }
   
   /**
    * Test method for:
    * <ul>
    * <li>{@link OrderedBiNavigableMap#put(Object, Object)}</li>
    * <li>{@link OrderedBiNavigableMap#get(Object)}</li>
    * <li>{@link OrderedBiNavigableMap#getKey(Object)}</li>
    * <li>{@link OrderedBiNavigableMap#containsKey(Object)}</li>
    * <li>{@link OrderedBiNavigableMap#containsValue(Object)}</li>
    * </ul>
    */
   @Test
   public void testPutOrdered() {
      BiMap<String, String> map = Maps.newOrderedBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      assertTrue(map.containsKey(name));
      assertTrue(map.containsValue(surname));
      assertEquals(surname, map.get(name));
      assertEquals(name, map.getKey(surname));
   }

   /**
    * Test method for:
    * <ul>
    * <li>{@link BiHashMap#remove(Object)}</li>
    * <li>{@link BiHashMap#removeKey(Object)}</li>
    * </ul>
    */
   @Test
   public void testRemove() {
      BiMap<String, String> map = Maps.newBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      assertEquals(surname, map.get(name));
      assertEquals(name, map.getKey(surname));
      map.remove(name);
      assertEquals(null, map.get(name));
      assertEquals(null, map.getKey(surname));
   }
   
   /**
    * Test method for:
    * <ul>
    * <li>{@link OrderedBiNavigableMap#remove(Object)}</li>
    * <li>{@link OrderedBiNavigableMap#removeKey(Object)}</li>
    * </ul>
    */
   @Test
   public void testRemoveOrdered() {
      BiMap<String, String> map = Maps.newOrderedBiMap();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      assertEquals(surname, map.get(name));
      assertEquals(name, map.getKey(surname));
      map.remove(name);
      assertEquals(null, map.get(name));
      assertEquals(null, map.getKey(surname));
   }

   /**
    * Test method for:
    * <ul>
    * <li>{@link BiHashMap#clear()}</li>
    * <li>{@link BiHashMap#size()}</li>
    * <li>{@link BiHashMap#isEmpty()}</li>
    * </ul>
    */
   @Test
   public void testClear() {
      BiMap<String, String> map = Maps.newBiMap();
      String k1 = "Elvis";
      String v1 = "Ligu";
      String k2 = "key";
      String v2 = "value";
      map.put(k1, v1);
      map.put(k2, v2);
      assertEquals(v1, map.get(k1));
      assertEquals(k1, map.getKey(v1));
      assertEquals(v2, map.get(k2));
      assertEquals(k2, map.getKey(v2));
      assertEquals(2, map.size());
      map.clear();
      assertTrue(map.isEmpty());
      assertNull(map.get(k1));
      assertNull(map.get(k2));
      assertNull(map.getKey(v1));
      assertNull(map.getKey(v2));
   }
   
   /**
    * Test method for:
    * <ul>
    * <li>{@link OrderedBiNavigableMap#clear()}</li>
    * <li>{@link OrderedBiNavigableMap#size()}</li>
    * <li>{@link OrderedBiNavigableMap#isEmpty()}</li>
    * </ul>
    */
   @Test
   public void testClearOrdered() {
      BiMap<String, String> map = Maps.newOrderedBiMap();
      String k1 = "Elvis";
      String v1 = "Ligu";
      String k2 = "key";
      String v2 = "value";
      map.put(k1, v1);
      map.put(k2, v2);
      assertEquals(v1, map.get(k1));
      assertEquals(k1, map.getKey(v1));
      assertEquals(v2, map.get(k2));
      assertEquals(k2, map.getKey(v2));
      assertEquals(2, map.size());
      map.clear();
      assertTrue(map.isEmpty());
      assertNull(map.get(k1));
      assertNull(map.get(k2));
      assertNull(map.getKey(v1));
      assertNull(map.getKey(v2));
   }
   
   /**
    * Test method for
    * {@link gr.uom.se.util.collection.BiHashMap#putAll(java.util.Map)}.
    */
   @Test
   public void testPutAll() {
      Map<String, String> map = new HashMap<>();
      String k1 = "Elvis";
      String v1 = "Ligu";
      String k2 = "key";
      String v2 = "value";
      map.put(k1, v1);
      map.put(k2, v2);
      
      BiMap<String, String> biMap = Maps.newBiMap();
      biMap.putAll(map);
      assertEquals(biMap, map);
   }

   /**
    * Test method for
    * {@link gr.uom.se.util.collection.OrderedBiNavigableMap#putAll(java.util.Map)}.
    */
   @Test
   public void testPutAllOrdered() {
      Map<String, String> map = new HashMap<>();
      String k1 = "Elvis";
      String v1 = "Ligu";
      String k2 = "key";
      String v2 = "value";
      map.put(k1, v1);
      map.put(k2, v2);
      
      BiMap<String, String> biMap = Maps.newOrderedBiMap();
      biMap.putAll(map);
      assertEquals(biMap, map);
   }
   
   @Test
   public void testGetNextPrevious() {
      String k1 = "Elvis";
      String v1 = "Ligu";
      String k2 = "key";
      String v2 = "value";
      OrderedBiMap<String, String> map = Maps.newOrderedBiMap();
      map.put(k1, v1);
      map.put(k2, v2);
      assertEquals(k2, map.getNext(k1));
      assertEquals(k1, map.getPrevious(k2));
      assertEquals(v2, map.getNextValue(v1));
      assertEquals(v1, map.getPreviousValue(v2));
   }
   
   @Test
   public void testIterators() {
      String k1 = "Elvis";
      String v1 = "Ligu";
      String k2 = "key";
      String v2 = "value";
      OrderedBiMap<String, String> map = Maps.newOrderedBiMap();
      map.put(k1, v1);
      map.put(k2, v2);
      
      Iterator<String> keys = map.iterator();
      assertEquals(k1, keys.next());
      assertEquals(k2, keys.next());
      
      keys = map.reverseIterator();
      assertEquals(k2, keys.next());
      assertEquals(k1, keys.next());
      
      keys = map.valuesIterator();
      assertEquals(v1, keys.next());
      assertEquals(v2, keys.next());
      
      keys = map.reverseValuesIterator();
      assertEquals(v2, keys.next());
      assertEquals(v1, keys.next());
   }
}
