package gr.uom.se.util.module;

import java.util.Map;

/**
 * A general interface to define a contract between a class instance (module)
 * and a loader.
 * <p>
 * 
 * A module can be any instance of a class that can be loaded by an
 * implementation of this interface (the loader). Generally speaking a loader is
 * a type of a bean provider that can provide beans given their types.
 * <p>
 * The loading strategy is left to the implementation.
 * 
 * @author Elvis Ligu
 * @see DefaultModuleLoader
 */
public interface ModuleLoader {

   /**
    * Given a type get an instance of it.
    * <p>
    * 
    * @param clazz
    *           the type of required instance, must not be null
    * @return an instance of type {@code clazz}
    */
   <T> T load(Class<T> clazz);

   /**
    * Given a type get an instance of it using the provided loader.
    * <p>
    * 
    * @param clazz
    *           the type of required instance, must not be null
    * @param loader
    *           the loader to load the instance
    * @return an instance of type {@code clazz}
    */
   <T> T load(Class<T> clazz, Class<?> loader);

   /**
    * Given a type and a properties map, get an instance of it.
    * <p>
    * The properties given in this method will override any properties that the
    * module loader can resolve in order to build the module with its
    * dependencies. They will also override any properties of any dependency
    * that should be created (should be considered a module). That is, calling
    * this method will ensure that if a property that needs to be resolved by
    * the loader in order to build the module and all its dependencies is found
    * within these properties it will be used, otherwise it will use its default
    * strategy to resolve the properties. This is called dynamic loading.
    * 
    * @param clazz
    *           the type of required instance, must not be null
    * @param properties
    *           a map of properties to be used to load the instance, may be
    *           null.
    * @return an instance of type {@code clazz}
    */
   <T> T load(Class<T> clazz, Map<String, Map<String, Object>> properties);

   /**
    * Given a type and a property locator, get an instance of the type.
    * <p>
    * This method will use the provided property locator in order to resolve its
    * properties (dependencies or other config properties).
    * 
    * @param clazz
    *           the type of required instance, must not be null
    * @param properties
    *           a map of properties to be used to load the instance, may be
    *           null.
    * @return an instance of type {@code clazz}
    */
   <T> T load(Class<T> clazz, ModulePropertyLocator propertyLocator);
}