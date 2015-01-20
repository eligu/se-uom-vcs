/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.manager.ConfigManager;
import gr.uom.se.util.module.annotations.NULLVal;
import gr.uom.se.util.module.annotations.Property;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;

/**
 * An implementation of parameter provider which is based on a configuration
 * manager, from where it can retrieve the values of required parameters.
 * <p>
 * Each parameter is resolved using the following algorithm:
 * <ol>
 * <li>If the parameter has a {@link Property} annotation:</li>
 * <ol>
 * <li>The configuration domain and the property name will be read from
 * annotation.</li>
 * <li>Based on these coordinates (domain, name) the configuration manager will
 * be queried for the parameter value. If found it will be returned.</li>
 * <li>If the value was not found on the previous step, the map provided when
 * the method {@link #getParameter(Class, Annotation[], Map)} is called will be
 * queried for the value. If found and the value is the same as type as the
 * parameter then it will be returned. If found and the value is not the same
 * type as the parameter, then its toString() method will be called and the
 * string representation of it will be converted to a value which is compatible
 * with the parameter.</li>
 * <li>If the value was not found in the previous step then the
 * {@code stringVal} value of the annotation will be analyzed. If the string
 * value is equals to {@link NULLVal#NULL_STR} a null will be returned. If it is
 * equals to {@link NULLVal#LOAD_STR} the parameter will be considered a module
 * and it will be loaded by an instance of a {@link ModuleLoader}. If the string
 * value is none of the above it will be considered a string representation of
 * the parameter and will be converted to a value that is compatible with the
 * parameter.</li>
 * </ol>
 * <li>If the parameter is not annotated it will be considered a module and it
 * will be loaded using an instance of {@link ModuleLoader}.</li> </ol>
 * 
 * The strategy to use a module loader to load a parameter that is not annotated
 * or can not be resolved is as follows:
 * <ol>
 * <li>Look for the loader under domain
 * {@link ModuleConstants#getDefaultConfigFor(Class)} and property name
 * {@link ModuleConstants#LOADER_PROPERTY}, where Class is the type of
 * parameter. If the loader was found use it.</li>
 * <li>If the loader was not found in previous step, then look for it under
 * domain {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN} and property name
 * {@link ModuleConstants#DEFAULT_MODULE_LOADER_PROPERTY}. If found then use it.
 * </li>
 * <li>If the loader was not found in previous step, then check if a loader was
 * provided when the instance of this class was created. If not, then create an
 * instance of {@link DefaultModuleLoader} and use it. If yes, then use the
 * provided loader.</li>
 * </ol>
 * Each time this provider looks for a property it will look first in
 * configuration manager (provided when created, null or not) if the value is
 * not found there it will look under the provided map (if the map is not null).
 * 
 * @see DefaultModuleLoader
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
@Property(
      domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN, 
      name = ModuleConstants.DEFAULT_PARAMETER_PROVIDER_PROPERTY)
public class DefaultParameterProvider implements ParameterProvider {

   /**
    * Used to check for properties if we can load them from here.
    * <p>
    */
   private ConfigManager config;

   /**
    * Used in case a parameter has no annotation, so we can not find it within
    * config, and we need to load it.
    * <p>
    * This loader is a cached loader for the default loader.
    */
   protected ModuleLoader loader;

   /**
    * Create a parameter provider with the given config, and loader.
    * <p>
    * Both parameters can be null.
    */
   public DefaultParameterProvider(
         @Property(
               domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN, 
               name = ModuleConstants.CONFIG_MANAGER_PROPERTY)
         ConfigManager config,
         @Property(
               domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN, 
               name = ModuleConstants.DEFAULT_MODULE_LOADER_PROPERTY)
         ModuleLoader loader) {
      this.config = config;
      this.loader = loader;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getParameter(Class<T> parameterType, Annotation[] annotations,
         Map<String, Map<String, Object>> properties) {

      // If there are not annotations the loader will try to parse the
      // parameter metadata and to find info if it can be loaded
      // by this loader
      if (annotations == null || annotations.length == 0) {
         return getLoader(parameterType, properties).load(parameterType);
      }

      // We should check the number of @Property annotations
      // and will be skipping other non related annotations
      Property propertyAnnotation = getPropertyAnnotation(annotations);

      // In case this parameter has other annotations
      // rather than known ones
      if (propertyAnnotation == null) {
         return getLoader(parameterType, properties).load(parameterType);
      }

      // We have a @Property annotation
      // and we should extract info from there
      return extractValue(parameterType, propertyAnnotation, properties);
   }

   /**
    * Return a loader to load any parameter that is not annotated and its value
    * can not be obtained by a configuration.
    * <p>
    * 
    * @param type
    *           of parameter to load
    * @param properties
    * @return
    */
   protected ModuleLoader getLoader(Class<?> type,
         Map<String, Map<String, Object>> properties) {
      // Check first under the default domain of the given class
      // if there is any loader available
      String loaderProperty = ModuleConstants.LOADER_PROPERTY;
      String loaderDomain = ModuleConstants.getDefaultConfigFor(type);

      ModuleLoader loader = getCompatibleProperty(loaderDomain, loaderProperty,
            ModuleLoader.class, properties, config);

      // If a loader is not available then check under the default
      // module's domain if a loader is available
      if (loader == null) {
         loaderProperty = ModuleConstants.DEFAULT_MODULE_LOADER_PROPERTY;
         loaderDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
         loader = getCompatibleProperty(loaderDomain, loaderProperty,
               ModuleLoader.class, properties, config);
      }

      // If no loader was found then create a default module loader
      // if it is not created
      if (loader == null) {
         if (this.loader == null) {
            this.loader = new DefaultModuleLoader(config, this);
         }
         loader = this.loader;
      }
      return loader;
   }

   @SuppressWarnings("unchecked")
   static <T> T getCompatibleProperty(String domain, String name,
         Class<T> type, Map<String, Map<String, Object>> properties,
         ConfigManager config) {

      T val = null;
      if (config != null) {
         val = config.getProperty(domain, name);
      }

      if (val == null && properties != null) {
         // Check first for a value within default config
         Map<String, Object> propDomain = properties.get(domain);
         if (propDomain != null) {
            Object pVal = propDomain.get(name);

            // If the value is within the default config,
            // check if it is a compatible type with T, if so
            // return the value
            if (pVal != null) {
               if (type.isAssignableFrom(pVal.getClass())) {
                  return (T) pVal;
               }
            }
         }
      }
      return val;
   }

   /**
    * Return a property annotation, or null if annotations is null or empty.
    * <p>
    * This method will throw an exception if there are more than one property
    * annotations.
    * 
    * @param annotations
    *           to check for the property annotation
    * @return a property annotation or null if there is not any
    */
   static Property getPropertyAnnotation(Annotation... annotations) {
      if (annotations == null || annotations.length == 0) {
         return null;
      }
      // We should check the number of @Property annotations
      // and will be skipping other non related annotations
      Property propertyAnnotation = null;
      int count = 0;
      for (Annotation an : annotations) {
         if (an.annotationType().equals(Property.class)) {
            count++;
            propertyAnnotation = (Property) an;
         }
      }

      // Check for more than one property annotation
      if (count > 1) {
         throw new IllegalArgumentException(
               "found more than 1 annotation @Property for parameter");
      }
      return propertyAnnotation;
   }

   /**
    * Extract a value for the given parameter based on its annotation, and the
    * configuration.
    * <p>
    * If the value can not be extracted from the configuration then the default
    * stringval value will be converted to a value. The parameter should be a
    * primitive type.
    * 
    * @param parameterType
    * @param annotation
    * @param properties
    * @return
    */
   @SuppressWarnings("unchecked")
   protected <T> T extractValue(Class<T> parameterType, Property annotation,
         Map<String, Map<String, Object>> properties) {
      String domain = annotation.domain();
      String name = annotation.name();
      String strval = null;
      T val = null;

      // Check first if there is a value defined in config manager
      if (config != null) {
         val = config.getProperty(domain, name);
         if (val != null) {
            return val;
         }
      }

      // If there is not a value defined then check for a
      // specific value within the metadata provided by the class
      // itself
      if (properties != null) {
         // Check first for a value within default config
         Map<String, Object> propDomain = properties.get(domain);
         if (propDomain != null) {
            Object pVal = propDomain.get(name);

            // If the value is within the default config,
            // check if it is a compatible type with T, if so
            // return the value, otherwise consider the value as
            // a string which should be parsed and converted to
            // a value
            if (pVal != null) {
               if (parameterType.isAssignableFrom(pVal.getClass())) {
                  return (T) pVal;
               } else {
                  strval = pVal.toString();
               }
            }
         }
      }

      // If the strval has not a value that means the default
      // config didn't have one, so we check for the annotation.
      // The strval should definitely get a value at this point
      if (strval == null) {
         strval = annotation.stringVal();
      }

      // In this case the string val should have a value
      // If the value is same as NULL_STR that means we
      // should return a null value, if it is LOAD_STR then
      // we should consider this property as a module that
      // should be loaded by a loader
      if (strval != null) {
         if (strval.equals(NULLVal.NULL_STR)) {
            return null;
         }
         if (strval.equals(NULLVal.LOAD_STR)) {
            return getParameter(parameterType, null, properties);
         }
      }

      if (strval != null && !strval.isEmpty()) {
         return getPrimitiveValue(parameterType, strval);
      }

      // The property could not be initialized so an exception is thrown
      throw new IllegalArgumentException("Can not extract a value of "
            + parameterType.getName() + " " + " at " + domain + ":" + name
            + " with value " + strval);
   }

   /**
    * Use json-smart library to extract a primitive type from a string value.
    * <p>
    * 
    * @param type
    * @param strval
    * @return
    */
   @SuppressWarnings("unchecked")
   static <T> T getPrimitiveValue(Class<T> type, String strval) {
      try {
         return (T) JSONValue.parseWithException(strval, wrapType(type));
      } catch (ParseException ex) {
         throw new IllegalArgumentException(ex);
      }
   }

   /**
    * If the given type is a primitive type, wrap it to its corresponding Java
    * type.
    * <p>
    * 
    * @param type
    *           the type to wrap
    * @return the wrapped primitive
    */
   static Class<?> wrapType(Class<?> type) {
      Class<?> wrapper = primitives.get(type);
      if (wrapper != null) {
         return wrapper;
      }
      return type;
   }

   /**
    * Mapped primitives to their Java objects.
    * <p>
    */
   private final static Map<Class<?>, Class<?>> primitives = new HashMap<>();
   static {
      primitives.put(int.class, Integer.class);
      primitives.put(double.class, Double.class);
      primitives.put(float.class, Float.class);
      primitives.put(long.class, Long.class);
      primitives.put(short.class, Short.class);
      primitives.put(char.class, Character.class);
      primitives.put(boolean.class, Boolean.class);
      primitives.put(byte.class, Byte.class);
   }
}
