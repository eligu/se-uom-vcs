package gr.uom.se.util.manager;

import gr.uom.se.util.manager.annotations.Init;
import gr.uom.se.util.manager.annotations.Stop;
import gr.uom.se.util.module.ModuleManager;

/**
 * A main manager whose purpose is to register the other managers and to manage
 * their lifecycle.
 * <p>
 * A manager is an instance that can be used as an entry point for a system
 * module. A system module a.k.a. as subsystem is a collection of
 * class/interface files that provide related operations, such as a DB module.
 * In order to minimize the coupling between two system modules, it is advisable
 * that there should be an instance called manager that will be responsible for
 * the initialization of the subsystem and deal with the internals of the
 * subsystem. The reason for the manager is to minimize the coupling between two
 * modules, that means the subsystems should use their respective managers to
 * access the operations of each other. By providing an entry point (manager)
 * for a subsystem the designer can hide the real implementations of its
 * subsystem and provide to its clients access through interfaces, and this
 * should be the ideal case.
 * <p>
 * A main manager is responsible for the lifecycle of available managers. In
 * order to tell this manager to register an implementation of another manager,
 * the caller must {@code register} the implementation class first. The client
 * can then use an interface of the registered manager to access it. When the
 * client need an instance of the manager it should be {@code loaded} by telling
 * this manager. However the loading of a registered manager just call the
 * constructor to be executed. If however the client has a specialized operation
 * that can {@code initialize} the manager, there should be an annotated method
 * with {@link Init} and it will be called if the client request so calling this
 * main manager to {@code start} the given manager. On the opposite the client
 * may want to shut down the manager so he can request from this main manager to
 * {@code stop} the manager. In this case the manager implementation should have
 * an annotated method with {@link Stop}. If the client needs to discard the
 * manager instance at all, it should {@code unload} by requesting this main
 * manager to do so. That will remove the instance of the manager from the
 * register of this main manager. However it will still have the manager's class
 * registered, so it can be started again if requested. If the client want to
 * remove the registered class from this main manager it can request
 * {@code remove} from this manager. All the above operations are made on client
 * request, that means that the client is responsible for the lifecycle of the
 * manager. However this main manager offers the {@link #getManager(Class)}
 * method which when called will look up for a registered manager class, will
 * load it if it is not already loaded, and will start it if it is not already
 * started. By doing so the client can leave the creation and initialization of
 * the manager in the hand of this main manager, and that would be the case of a
 * lazy initialization. In order for the manager to be initialized it should be
 * a concrete implementation (when calling {@link #getManager(Class)}) - in such
 * case it will register by default - or it can be an interface which would
 * require the manager implementation to be registered first.
 * <p>
 * All the operations supported by main manager, are requested given a class
 * instance. In all cases the class instance may be an interface, or an abstract
 * class. However the only case when main manager will request a concrete
 * implementation is when registering it for the first time. That means, if a
 * manager implementation is registered, it can be managed using its interface.
 * Thus, when calling the {@link #getManager(Class)} method if a manager of the
 * given type is not registered, the class instance should be a concrete class.
 * However if an implementation is already registered all the methods can be
 * called using an interface (except {@link #registerManager(Class)} which
 * accepts only concrete implementations.
 * <p>
 * Normally the clients doesn't create the managers not or do they initialize
 * them, that means that the creation of each manager and the resolving of its
 * dependencies is taken care of by this main manager. In order to do so a
 * manager and all its dependencies that should be resolved must comply with
 * {@link ModuleManager} or the module loader subsystem, which is used by this
 * main manager.
 * <p>
 * How this main manager exactly works and how managers should be constructed,
 * it depends on the implementation of this interface, and the implementation of
 * the module subsystem.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 * @see AbstractMainManager
 */
public interface MainManager {

   /**
    * Register the given manager implementation class to the register of
    * managers.
    * <p>
    * Although the provided class must be a concrete implementation of a
    * manager, this can not be always true, and it depends on the
    * implementation.
    * 
    * @param managerClass
    *           the class of the manager to register, must not be null.
    */
   void registerManager(Class<?> managerClass);

   /**
    * Register the instance of the given manager as a loaded manager.
    * <p>
    * This is the case when the client knows how to load the manager and or it
    * can not be loaded by the implementation of this manager.
    * 
    * @param manager
    *           the instance of the manager to be registered as loaded, must not
    *           be null.
    */
   void registerLoaded(Object manager);

   /**
    * Remove the given manager class from this register.
    * <p>
    * Note that a client can provide an interface type, however the removed
    * manager will be the first found that implements this interface. If a
    * manager of the given type is not registered it will do nothing, and will
    * return null. It will also return null if the manager of the given type was
    * not loaded before.
    * 
    * @param managerClass
    *           the type of the manager to be removed, must not be null.
    * @return an instance of the loaded manager if it was one or null
    */
   <T> T removeManager(Class<T> managerClass);

   /**
    * Get an instance of a manager of the given type.
    * <p>
    * If a manager of the given type was not started (initialized) before it
    * will call {@link #startManager(Class)} to start the manager. The start
    * method will call {@link #loadManager(Class)} if the manager was not
    * created. And the load method will call {@link #registerManager(Class)} to
    * register the manager if it was not registered. This means that, if the
    * given type is an interface then an implementation of the given interface
    * should be registered first otherwise this will throw an exception. On the
    * other hand if the type is concrete but not registered, it will be
    * loaded/started before returned to the caller.
    * <p>
    * <b>WARNING:</b> if there are two or more registered managers that are
    * subtypes of the given type, this will return one of the available
    * subtypes, however there is no guarantee which manager will be returned
    * each time.
    * <p>
    * DO NOT use the main manager as a module loader, just to load modules with
    * reflection. Call instead {@code getManager(ModuleManager.class)} to get
    * the module manager and call {@code moduleManager.getLoader(Beans.class)}
    * to get a proper loader to load the given Bean.
    * 
    * @param manager
    *           the manager type to get from this main manager
    * @return an instance of the specified type
    */
   <T> T getManager(Class<T> manager);

   /**
    * Load the manager of the given type if it is not loaded. and return its
    * instance.
    * <p>
    * The given type may be an abstract type, as long as a concrete
    * implementation is already registered before this call. If the given type
    * is concrete but it is not registered, it will register it first as a
    * manager and then will load it.
    * 
    * @param manager
    *           the type of the manager to load, must not be null.
    * @return an instance of the loaded manager
    */
   <T> T loadManager(Class<T> manager);

   /**
    * Unload the given manager type from this main manager.
    * <p>
    * This method will simply clean the internals of this main manager and will
    * discard the instance of the manager that it contains. If no other client
    * retain an instance of the given manager it will allow for GC to remove the
    * manager from memory. This will have no effect if there is not a loaded
    * manager of the given type.
    * 
    * @param manager
    *           the type of manager to be unloaded, must not be null.
    * @return the manager if it was previously loaded or null.
    */
   <T> T unloadManager(Class<T> manager);

   /**
    * Start the given manager by calling an annotated with {@link Init} method
    * of the given manager type.
    * <p>
    * If there is a manager that is already registered and is a subtype of the
    * given type, it will use that manager. The chosen manager will be loaded if
    * it is not already loaded, and will be started by calling an instance
    * method of the manager that is annotated with {@link Init}. If no method is
    * found it will do nothing. If the manager is already started it will do
    * nothing. If the given type is not registered as a manager it will be
    * registered first and then loaded and started.
    * <p>
    * Usually this method will behave the same as the {@link #getManager(Class)}.
    * 
    * @param manager
    *           the type of the manager to start, must not be null.
    * @return an instance of the started manager.
    */
   <T> T startManager(Class<T> manager);

   /**
    * Stop the given manager type by calling a method on it annotated with
    * {@link Stop}.
    * <p>
    * If there is not a started manager of the given type it will do nothing. A
    * started manager means it should be registered, loaded, and started. To
    * register, load and start a manager there is no need to call the respective
    * methods of this main manager in this order, the client can just do all of
    * that by simply calling {@link #getManager(Class)}. If there is not a
    * stopper method then it will do nothing.
    * 
    * @param manager
    *           the type of the manager to be stopped, must not be null.
    * @return the manager instance that was stopped.
    */
   <T> T stopManager(Class<T> manager);

   /**
    * Return true if there is a manager of the given type, registered and
    * loaded.
    * <p>
    * 
    * @param manager
    *           the type of manager to check, must not be null.
    * @return true if a manager of the given type is loaded.
    */
   boolean isLoaded(Class<?> manager);

   /**
    * Return true if there is a manager of the given type registered.
    * <p>
    * 
    * @param manager
    *           the type of manager to check, must not be null.
    * @return true if a manager of the given type is registered.
    */
   boolean isRegistered(Class<?> manager);

   /**
    * Return true if there is a manager of the given type started.
    * <p>
    * A started manager means it will be registered and loaded to. So when
    * calling this method there is no need to check if the manager is loaded or
    * registered.
    * 
    * @param manager
    *           the type of manager to check, must not be null.
    * @return true if a manager of the given type is started.
    */
   boolean isStarted(Class<?> manager);
}