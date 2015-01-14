/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.filter.Filter;
import gr.uom.se.filter.FilterUtils;
import gr.uom.se.util.manager.ConfigManager;
import gr.uom.se.util.reflect.MemberModifierFilter;
import gr.uom.se.util.reflect.MemberOfFilter;
import gr.uom.se.util.validation.ArgsCheck;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Elvis
 * 
 */
public class BeanCreator {

   private ConfigManager config;
   private DefaultModuleLoader loader;

   @SuppressWarnings({ "unchecked" })
   // TODO Left here
   private static final Filter<Field> fieldsFilter = 
         (Filter<Field>) FilterUtils.and(
               FilterUtils.not(MemberOfFilter.OBJECT_MEMBERS));
   
   public BeanCreator(ConfigManager config) {
      this.config = config;
      this.loader = new DefaultModuleLoader(config);
   }

   public <T> T create(Class<T> clazz) {
      ArgsCheck.notNull("clazz", clazz);

      T bean = loader.load(clazz);
      return null;
   }

   @SuppressWarnings("unchecked")
   static Collection<Field> getInstanceFields(Class<?> clazz, Filter<Field> filter) {
      if (filter == null) {
         filter = FilterUtils.not((Filter<Field>) MemberModifierFilter.STATIC_FILTER);
      } else {
         filter= FilterUtils.and(FilterUtils.not((Filter<Field>) MemberModifierFilter.STATIC_FILTER), filter);
      }
      List<Field> fields = new ArrayList<>();
      getFields(fields, clazz, filter);
      return fields;
   }
   
   static void getFields(Collection<Field> fields, Class<?> clazz,
         Filter<Field> filter) {
      if (filter == null) {
         filter = FilterUtils.NULL();
      }
      Field[] declared = clazz.getDeclaredFields();
      for (Field f : declared) {
         if (filter.accept(f)) {
            fields.add(f);
         }
      }
      Class<?> parent = clazz.getSuperclass();
      getFields(fields, parent, filter);
      for (Class<?> c : clazz.getInterfaces()) {
         getFields(fields, c, filter);
      }
   }
}
