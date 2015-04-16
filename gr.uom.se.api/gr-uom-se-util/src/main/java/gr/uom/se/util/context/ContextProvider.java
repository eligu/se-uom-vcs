package gr.uom.se.util.context;

import java.util.Set;

/**
 * The context provider that manages the contexts for a given application type
 * category.
 * <p>
 * Each application type that has a special meaning to the system may have
 * context, which is a more specialized way of dealing with its properties. For
 * example an application may have different database back ends and hence a
 * context would mean a place were we can retrieve database properties for a
 * given database type, such as the connection provider, or the transaction
 * provider. Because those type of entities may be unknown at compile time we
 * can not specify them at configuration of the application because they would
 * be applied for all types. Therefore, having a context we can dynamically
 * specify properties of the types of the context, which will be applied to the
 * given types (be it all instances of e given type or discrete instances).
 * <p>
 * Another aspect of contexts is the fact that we can have different context
 * types for a given instance. For example for a project instance we may have
 * the modules API context to instantiate different project solvers based on the
 * project type, and a db context to read properties from a db. A provider
 * should be able to provide multiply contexts based on the given context type.
 * That means that given an entity and a context type the client is requesting a
 * context of the given entity and of the given type.
 * <p>
 * Note that when requiring a context given an instance, the provider may
 * provide a context that is applied to all instances of the type of the given
 * instance if no specialized context for that instance is provided.
 * 
 * @author Elvis Ligu
 * @param <T>
 *           the type this provider may provide contexts.
 */
public interface ContextProvider {

   /**
    * Given an instance get a specialized context for it that is of the given
    * type.
    * <p>
    * The instance must not be null. If the context is used to load the instance
    * so the instance can not be determine before calling this method, then use
    * {@link #getContext(java.lang.Class)} which will provide a context for all
    * instances of the generic type T. If this method can not provide a specific
    * context for the given instance it will return a context that is applied to
    * all instances of the given type T.
    * <p>
    * Because we may have different contexts for different domains of the
    * application, this is appropriate to look up the specialized context for
    * the instance. If the context of the given type is not found it will return
    * null.
    *
    * @param instance
    *           the object for which to get the specialized context, must not be
    *           null.
    * @param contextType
    *           the type of the context to be returned.
    * @return a context for the given instance. If this provider can provide
    *         specialized contexts based on the instance and its properties, it
    *         will provide that context. A null will be returned if no context
    *         of the given type can not be provided.
    */
   <C extends Context> C getContext(Object instance, Class<C> contextType);

   /**
    * Given a type, get a specialized context that applies to all instances of
    * the given type.
    * <p>
    * 
    * This method will return a context based on the context type provided. If
    * that is not found it will return null.
    * 
    * @param type
    *           the type to get the context for, must not be null.
    * @param contextType
    *           the type of the context to be returned.
    * @return a specialized context that applies to all instances of the given
    *         type.
    */
   <C extends Context> C getContext(Class<?> type,
         Class<C> contextType);

   /**
    * Return the types this provider can provide contexts for.
    * <p>
    * Generally speaking if the type is Object that it should be inferred that
    * this is a default context provider and provides contexts for all types.
    * 
    * @return the types this provider can provide contexts for. Should not return
    *         a null type.
    */
   Set<Class<?>> getTypes();

   /**
    * Get the context types this provider can provide.
    * <p>
    * A context provider may provide different contexts that can be applied in
    * different application domains. This method will return the types of
    * contexts this provides.
    * 
    * @return context type this provides.
    */
   <C extends Context> Set<Class<C>> getContextTypes();
}
