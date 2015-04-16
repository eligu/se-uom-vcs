package gr.uom.se.util.context;

import gr.uom.se.util.module.ModuleLoader;

/**
 * A context for a given instance or instances of the same type.
 * <p>
 * A context is a dynamic environment for a given instance within an
 * application. It will normally provide a bridge between configuration
 * properties, modules API, and managers API and the client of the context. That
 * is, if a client needs to load some kind of a module based on an instance and
 * he has to look up managers, module loader, property providers and so on, he
 * can not use directly the APIs because they give a centralized view for all
 * modules, managers and properties. In this case the client may use the
 * managers API to look up for a {@link ContextManager} and by passing the type
 * of the instance he can resolve a context provider. Having a context provider
 * then the client will look up the context for the given instance. Suppose we
 * want to have a connection between the client and another end point (let say
 * remote). We do not know at the compile time what is this end point, is this a
 * database, a remote REST server or a file. The client only needs to read some
 * property from the endpoint for a given application entity. The code for doing
 * it is:
 * 
 * <pre>
 * ContextManager ctxm = mainManager.getManager(ContextManager.class);
 * ContextProvider&lt;Project&gt; ctxp = ctxm.getProvider(Project.class);
 * PropertyContext ctx = ctxp.getContext(project, PropertyContext.class);
 * Date startDate = ctx.getProperty(&quot;startDate&quot;, Date.class);
 * </pre>
 * 
 * In the above scenario the startDate will be a property not in the Project
 * entity itself but probably from a db, file, or some other source.
 * <p>
 * A type context can be also used in scenarios where modules that are loaded
 * dynamically have different meaning which is based on the type instance. For
 * example suppose that we have a Project entity and based on the type of this
 * entity we want to load a ProjectSolver. If we use the modules API we can
 * define module loaders for a ProjectSolver however that would be for all
 * project solvers that should be loaded. But in our scenario we want to load a
 * project solver based on a property from the project (for example projectType
 * which can be retrieved using project.getProjectType()). This kind of dynamic
 * loading is possible in modules API by calling
 * {@link ModuleLoader#load(java.lang.Class, java.util.Map) load with properties
 * } method of the loader. However using this method would require the clients
 * to create the properties map by hand. That is, the logic of creating such
 * properties each time, for a given entity will be spread all over the
 * application. Using instead a type context we can have a centralized place
 * from where these properties are constructed, and so we can resolve the right
 * module for the entity:
 * 
 * <pre>
 * ContextManager ctxm = mainManager.getManager(ContextManager.class);
 * ModulesContext&lt;Project&gt; ctxp = ctxm.getProvider(Project.class);
 * Context ctx = ctxp.getContext(project, ModulesContext.class);
 * ModulesContext projectSolver = ctx.load(ProjectSolver.class);
 * </pre>
 * 
 * In the above scenario if (supposedly) project.getType() returns LINEAR_SOLVER
 * than it will return LinearProjectSolver instance. If it returns
 * COMPLEX_SOLVER than it will return a ComplexProjectSolver. That means that
 * the returned (created, cached or whatever) solver will be created based on
 * the context of the project. Another point to note is that the client is not
 * required to know what kind of solver type he should use, except only the
 * context provider and the context itself.
 * <p>
 * Because the meaning of context is a dynamic environment for a given entity in
 * a given application domain, the context doesn't provide any special
 * functionality to the clients. However the application may define contexts for
 * specific cases which should be known to the clients. That is, in the above
 * scenarios we have two type of contexts, a PropertyContext and a
 * ModulesContext. These two contexts are just definitions of two specialized
 * types of context.
 *
 * @author Elvis Ligu
 */
public interface Context {

   /**
    * Get the type this context applies to.
    * <p>
    * Note that if the type is Class&lt;Object&gt; the clients may infer the
    * given context is a default context for all types.
    * 
    * @return the type this context support. Generally speaking if a context
    *         support a given type, it must support all its subtypes. Although
    *         this is not required usually a default context definition and
    *         implementation for all types would be based on Object types.
    */
   Class<?> getType();
}
