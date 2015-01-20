/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.validation.ArgsCheck;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An implementation of a property injector that is based on a parameter
 * provider.
 * <p>
 * This injector will try to inject values to any declared property of a given
 * bean, that is annotated with a {@link Property} annotation. The values
 * injected are resolved using an instance of {@link ParameterProvider}, thus
 * the algorithm of resolving the values should be implemented in a parameter
 * provider. This implementation will not resolve inherited properties. It is
 * advisable that inherited properties (private or not) are resolved by the
 * super constructor or when creating the instance of the bean.
 * 
 * @author Elvis Ligu
 */
@Property(domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN, 
name = ModuleConstants.DEFAUL_PROPERTY_INJECTOR_PROPERTY)
public class DefaultPropertyInjector implements PropertyInjector {

   /**
    * A provider of properties that are discovered when analyzing the fields of
    * a given instance.
    */
   private ParameterProvider provider;

   /**
    * Create a new injector given the property provider.
    * <p>
    * 
    * @param provider
    *           used by this injector to retrieve annotated values
    */
   public DefaultPropertyInjector(
         @Property(
               domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN, 
               name = ModuleConstants.DEFAULT_PARAMETER_PROVIDER_PROPERTY)
         ParameterProvider provider) {
      ArgsCheck.notNull("provider", provider);
      this.provider = provider;
   }

   /**
    * Inject properties to the given instance.
    * <p>
    * Only declared properties of the class of this instance will be inserted
    * and only properties with {@link Property} annotation.
    * 
    * @param bean
    *           the instance to where properties should be inserted
    */
   public void injectProperties(Object bean) {
      ArgsCheck.notNull("bean", bean);

      Collection<Field> fields = getInstanceFields(bean.getClass());
      for (Field f : fields) {

         // Set up first the access to this field
         // and ensure it will be accessible
         boolean accessible = f.isAccessible();

         if (!accessible) {
            f.setAccessible(true);
         }

         // Use the provider to get the value
         Object val = provider.getParameter(f.getType(), f.getAnnotations(),
               null);
         try {
            f.set(bean, val);
         } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
         } finally {
            f.setAccessible(accessible);
         }
      }
   }

   /**
    * Return all instance fields that are annotated with {@link Property}
    * annotation.
    * <p>
    * 
    * @param clazz
    * @return
    */
   static Collection<Field> getInstanceFields(Class<?> clazz) {

      List<Field> fields = new ArrayList<>();
      Field[] declared = clazz.getDeclaredFields();

      for (Field f : declared) {

         int mod = f.getModifiers();

         boolean accept = !Modifier.isStatic(mod)
               && !Modifier.isFinal(mod)
               && DefaultParameterProvider.getPropertyAnnotation(f
                     .getAnnotations()) != null;

         if (accept) {
            fields.add(f);
         }
      }
      return fields;
   }
}
