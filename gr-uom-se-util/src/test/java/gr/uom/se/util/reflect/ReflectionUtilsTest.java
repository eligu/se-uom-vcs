/**
 * 
 */
package gr.uom.se.util.reflect;

import gr.uom.se.filter.Filter;
import gr.uom.se.filter.FilterUtils;
import gr.uom.se.util.reflect.MemberModifierFilter;
import gr.uom.se.util.reflect.MemberOfFilter;
import gr.uom.se.util.reflect.ReflectionUtils;
import gr.uom.se.util.validation.ContainmentCheck;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author Elvis Ligu
 *
 */
public class ReflectionUtilsTest extends TestCase {

   @SuppressWarnings("unchecked")
   public void testGetConstructors() {

      assertNotNull(
            ReflectionUtils.getDefaultConstructor(ClassA.class));
      
      Set<Constructor<AbstractClassA>> cons = 
            ReflectionUtils.getConstructors(AbstractClassA.class, null);
      assertNotNull(cons);
      assertEquals(cons.size(), 2);
      
      cons = ReflectionUtils.getConstructors(
            AbstractClassA.class, 
            (Filter<Constructor<?>>)MemberModifierFilter.PUBLIC_FILTER);
      assertNotNull(cons);
      assertEquals(cons.size(), 1);
      
      int mod = cons.iterator().next().getModifiers();
      assertTrue(Modifier.isPublic(mod));
   }
   
   public void testGeAccessibleMethods() {
      // Create a filter to not include Object methods
      @SuppressWarnings("unchecked")
      Filter<Method> filter = FilterUtils.not(
            (Filter<Method>) MemberOfFilter.OBJECT_MEMBERS);
      
      Set<Method> methods = 
            ReflectionUtils.getAccessibleMethods(ClassA.class, Accessor.class, filter);
      
      assertNotNull(methods);
      assertEquals(methods.size(), 3);
      
      String[] methodNames = new String[]{"packageMethod", "methodIA", "name"};
      String[] names = new String[3];
      int i = 0;
      for(Method m : methods) {
         names[i++] = m.getName();
      }
      assertTrue(ContainmentCheck.containsOnly(methodNames, names));
      assertTrue(ContainmentCheck.containsOnly(names, methodNames));
   }
}
