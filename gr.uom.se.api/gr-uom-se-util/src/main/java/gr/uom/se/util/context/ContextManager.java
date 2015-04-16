package gr.uom.se.util.context;

import gr.uom.se.util.manager.MainManager;
import gr.uom.se.util.module.annotations.Module;

/**
 * Context manager which manages the context providers for different application
 * types.
 * <p>
 *
 * @author Elvis Ligu
 */
@Module(provider = DefaultContextManager.class)
public interface ContextManager {

   /**
    * Given a type of module get the context provider for that module.
    * <p>
    * Note that, you can have a special context that may deal with all
    * application types, and that would be the context registered for an
    * {@code Object} class. However this is not required as most of the aspects
    * that deal with all objects are covered by an instance of
    * {@link MainManager}.
    *
    * @param type
    *           the type to get the registered context provider, must not be
    *           null.
    * @return a context provider for the given type, or null if a provider is
    *         not registered for the given type.
    */
   ContextProvider getProvider(Class<?> type);

   /**
    * Register a context provider.
    * <p>
    * If previous provider is available for the type this provider support that
    * will be unregistered. Note that {@link ContextProvider#getTypes()
    * getTypes()} should return a non null type.
    *
    * @param provider
    *           the type of the provider, to register, must not be null.
    */
   void registerProvider(ContextProvider provider);

   /**
    * Remove the context provider for the given type.
    * <p>
    * 
    * @param type
    *           to remove its context provider, must not be null.
    */
   void removeProviderFor(Class<?> type);

   /**
    * Look up a context of the given type for the given instance.
    * <p>
    * This method is a shortcut to getting a provider from this manager for the
    * given instance type, and then resolving a context for the given context
    * type.
    * 
    * @param instance
    *           to get the context for.
    * @param contextType
    *           the type of the context to look for.
    * @return a context for the given instance of the given type, or null if no
    *         context (or provider was found).
    */
   <C extends Context> C lookupContext(Object instance, Class<C> contextType);

   /**
    * Look up a context of the given context type for the given type.
    * <p>
    * This method is a shortcut to getting a provider from this manager for the
    * given instance type, and then resolving a context for the given context
    * type.
    * 
    * @param type
    *           to get the context for.
    * @param contextType
    *           the type of the context to look for.
    * @return a context for the given instance of the given type, or null if no
    *         context (or provider was found).
    */
   <C extends Context> C lookupContext(Class<?> type, Class<C> contextType);
}
