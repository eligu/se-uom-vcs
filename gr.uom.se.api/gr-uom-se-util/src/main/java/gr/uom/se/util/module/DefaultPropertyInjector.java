/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.validation.ArgsCheck;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
@Property(domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN, name = ModuleConstants.PROPERTY_INJECTOR_PROPERTY)
public class DefaultPropertyInjector implements PropertyInjector {

   /**
    * A provider of properties that are discovered when analyzing the fields of
    * a given instance.
    */
   private volatile ParameterProvider provider;

   /**
    * A config manager to look for parameter provider each time a property
    * should be injected.
    * <p>
    */
   private ConfigManager config;

   private final ModulePropertyLocator locator;

   /**
    * Create a new injector given the property provider.
    * <p>
    * 
    * @param provider
    *           used by this injector to retrieve annotated values
    */
   public DefaultPropertyInjector(
         ConfigManager config,
         @Property(domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN, name = ModuleConstants.PARAMETER_PROVIDER_PROPERTY) ParameterProvider provider) {
      this.config = config;
      this.provider = provider;
      this.locator = new DefaultModulePropertyLocator();
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
      injectProperties(bean, locator);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void injectProperties(Object bean,
         Map<String, Map<String, Object>> properties) {
      ArgsCheck.notNull("bean", bean);
      ModulePropertyLocator thisLocator;
      if(properties == null || properties.isEmpty()) {
         thisLocator = locator;
      } else {
         thisLocator = new DynamicModulePropertyLocator(properties);
      }
      injectProperties(bean, thisLocator);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void injectProperties(Object bean,
         ModulePropertyLocator propertyLocator) {
      ArgsCheck.notNull("bean", bean);

      if (propertyLocator == null) {
         propertyLocator = locator;
      }

      Class<?> beanClass = bean.getClass();
      // Get the injectable fields
      Collection<Field> fields = getInstanceFields(beanClass);
      // If there is no injectable field
      // return
      if (fields.isEmpty()) {
         return;
      }
      // Create the default properties from bean
      Map<String, Map<String, Object>> properties = ModuleUtils
            .resolveModuleConfig(beanClass);
      // Get a parameter provider for the given bean
      ParameterProvider provider = resolveParameterProvider(beanClass,
            properties, propertyLocator);
      for (Field f : fields) {

         // Set up first the access to this field
         // and ensure it will be accessible
         boolean accessible = f.isAccessible();

         if (!accessible) {
            f.setAccessible(true);
         }

         // Use the provider to get the value
         Object val = provider.getParameter(f.getType(), f.getAnnotations(),
               properties, propertyLocator);
         try {
            f.set(bean, val);
         } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
         } finally {
            if (!accessible) {
               f.setAccessible(accessible);
            }
         }
      }
   }

   /**
    * Resolve a parameter provider for the given type.
    * <p>
    * The parameter will be searched at the default domains using
    * {@link ModuleUtils#getParameterProvider(Class, ConfigManager, Map)} if it
    * was not found there, it will be created using a default parameter
    * provider.
    * 
    * @param type
    * @param properties
    * @return
    */
   protected ParameterProvider resolveParameterProvider(Class<?> type,
         Map<String, Map<String, Object>> properties,
         ModulePropertyLocator propertyLocator) {
      ParameterProvider provider = propertyLocator.getParameterProvider(type,
            config, properties);
      // If no provider was found then create a default provider
      if (provider == null) {
         if (this.provider == null) {
            this.provider = new DefaultParameterProvider(config, null);
         }
         provider = this.provider;
      }
      return provider;
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

         boolean accept = !Modifier.isStatic(mod) && !Modifier.isFinal(mod)
               && ModuleUtils.getPropertyAnnotation(f.getAnnotations()) != null;

         if (accept) {
            fields.add(f);
         }
      }
      return fields;
   }
}
