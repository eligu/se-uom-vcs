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
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * @author Elvis Ligu
 */
public class BiMapTest {

   /**
    * Test method for {@link gr.uom.se.util.collection.BiMap#keys()}.
    */
   @Test
   public void testKeys() {
      BiMap<String, String> map = new BiMap<>();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      Set<String> keys = map.keys();
      assertEquals(1, keys.size());
      assertEquals(name, keys.iterator().next());
   }

   /**
    * Test method for {@link gr.uom.se.util.collection.BiMap#keysCopy()}.
    */
   @Test
   public void testKeysCopy() {
      BiMap<String, String> map = new BiMap<>();
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
    * Test method for {@link gr.uom.se.util.collection.BiMap#keyMappingCopy()}.
    */
   @Test
   public void testKeyMappingCopy() {
      BiMap<String, String> map = new BiMap<>();
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
    * Test method for {@link gr.uom.se.util.collection.BiMap#keyMapping()}.
    */
   @Test
   public void testKeyMapping() {
      BiMap<String, String> map = new BiMap<>();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Map<String, String> keyMap = map.keyMapping();
      assertEquals(map, keyMap);
      assertEquals(surname, keyMap.get(name));
   }

   /**
    * Test method for {@link gr.uom.se.util.collection.BiMap#valueMapping()}.
    */
   @Test
   public void testValueMapping() {
      BiMap<String, String> map = new BiMap<>();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Map<String, String> valueMap = map.valueMapping();
      assertTrue(map.equalValues(valueMap));
      assertEquals(name, valueMap.get(surname));
   }

   /**
    * Test method for {@link gr.uom.se.util.collection.BiMap#valueMappingCopy()}
    * .
    */
   @Test
   public void testValueMappingCopy() {
      BiMap<String, String> map = new BiMap<>();
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
    * Test method for {@link gr.uom.se.util.collection.BiMap#values()}.
    */
   @Test
   public void testValues() {
      BiMap<String, String> map = new BiMap<>();
      String name = "Elvis";
      String surname = "Ligu";
      map.put(name, surname);
      map.put(name, surname);
      Set<String> values = map.values();
      assertEquals(1, values.size());
      assertEquals(surname, values.iterator().next());
   }

   /**
    * Test method for {@link gr.uom.se.util.collection.BiMap#valuesCopy()}.
    */
   @Test
   public void testValuesCopy() {
      BiMap<String, String> map = new BiMap<>();
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
    * <li>{@link BiMap#put(Object, Object)}</li>
    * <li>{@link BiMap#get(Object)}</li>
    * <li>{@link BiMap#getKey(Object)}</li>
    * <li>{@link BiMap#containsKey(Object)}</li>
    * <li>{@link BiMap#containsValue(Object)}</li>
    * </ul>
    */
   @Test
   public void testPut() {
      BiMap<String, String> map = new BiMap<>();
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
    * <li>{@link BiMap#remove(Object)}</li>
    * <li>{@link BiMap#removeKey(Object)}</li>
    * </ul>
    */
   @Test
   public void testRemove() {
      BiMap<String, String> map = new BiMap<>();
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
    * <li>{@link BiMap#clear()}</li>
    * <li>{@link BiMap#size()}</li>
    * <li>{@link BiMap#isEmpty()}</li>
    * </ul>
    */
   @Test
   public void testClear() {
      BiMap<String, String> map = new BiMap<>();
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
    * {@link gr.uom.se.util.collection.BiMap#putAll(java.util.Map)}.
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
      
      BiMap<String, String> biMap = new BiMap<>() ;
      biMap.putAll(map);
      assertEquals(biMap, map);
   }

}
