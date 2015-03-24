/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.manager.annotations.Activator;
import gr.uom.se.util.module.AbstractMethodConstructorExecutor;
import gr.uom.se.util.module.ModuleManager;
import gr.uom.se.util.module.ModulePropertyLocator;
import gr.uom.se.util.module.ModuleUtils;
import gr.uom.se.util.module.ParameterProvider;
import gr.uom.se.util.validation.ArgsCheck;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractActivatorManager implements ActivatorManager {

   /**
    * The logger.
    */
   private static final Logger logger = Logger.getLogger(ActivatorManager.class
         .getName());

   /**
    * Contains all activator types that has been previously activated.
    * <p>
    */
   private Set<Class<?>> actives = new HashSet<>();

   /**
    * A lock for synchronizing writes to active activators, and for allowing
    * reads where there are no writes.
    */
   private ReentrantReadWriteLock activesLock = new ReentrantReadWriteLock();

   /**
    * Used to execute init and stop methods.
    */
   private Executor executor = new Executor();

   /**
    * {@inheritDoc}
    */
   @Override
   public void activate(Class<?> activator) {
      ArgsCheck.notNull("activator", activator);
      if (isActive(activator)) {
         return;
      }

      activesLock.writeLock().lock();
      try {
         activate0(activator, new HashSet<Class<?>>());
      } finally {
         activesLock.writeLock().unlock();
      }
   }

   /**
    * Given a fully qualified name for a given activator class it will try to
    * resolve it first and then to activate it.
    * <p>
    * 
    * @param activatorClassName
    *           the fully qualified name of the activator class, must not be
    *           null
    */
   public void activate(String activatorClassName) {
      ArgsCheck.notNull("activatorClassName", activatorClassName);
      try {
         Class<?> actClass = Class.forName(activatorClassName);
         this.activate(actClass);
      } catch (ClassNotFoundException cnfe) {
         throw new RuntimeException(cnfe);
      }
   }

   private void activate0(Class<?> activator, Set<Class<?>> deps) {
      if (isActive0(activator)) {
         return;
      }
      // Check if an activation is required for this activator and this is
      // in the list of non activated dependencies
      if (deps.contains(activator)) {
         throw new IllegalArgumentException(
               "Cyclic dependencies dedected for activator: " + activator);
      } else {
         deps.add(activator);
      }
      // Activate each dependency first
      Class<?>[] adeps = getDependencies(activator);
      for (Class<?> dep : adeps) {
         if (dep.equals(activator)) {
            throw new IllegalArgumentException(
                  "Can not set a dependency for its self, activator: "
                        + activator);
         }
         activate0(dep, deps);
      }

      // Activate now the activator
      Method method = ManagerUtils.findStaticInitMethod(activator,
            this.getClass());
      if (method != null) {
         executor.execute(null, activator, method,
               ModuleUtils.resolveModuleConfig(activator), null);
      } else {
         // Create an instance first
         Object instance = getModuleManager().getLoader(activator).load(
               activator);
         // Find the init method of this class
         method = ManagerUtils.findInstanceInitMethod(instance.getClass(),
               getClass());

         if (method == null) {
            throw new IllegalArgumentException(
                  "A method annotated with @Init is required for activator "
                        + activator);
         }
         // Execute the activator method
         executor.execute(instance, instance.getClass(), method,
               ModuleUtils.resolveModuleConfig(instance.getClass()), null);
      }
      deps.remove(activator);
      actives.add(activator);
      logger.info("Activated: " + activator.getName());
   }

   private boolean isActive(Class<?> activator) {
      activesLock.readLock().lock();
      try {
         return isActive0(activator);
      } finally {
         activesLock.readLock().unlock();
      }
   }

   private boolean isActive0(Class<?> activator) {
      if (!isActivator(activator)) {
         throw new IllegalArgumentException("Type is not an activator "
               + activator);
      }
      return this.actives.contains(activator);
   }

   private boolean isActivator(Class<?> type) {
      return type.getAnnotation(Activator.class) != null;
   }

   private Class<?>[] getDependencies(Class<?> activator) {
      Activator an = activator.getAnnotation(Activator.class);
      return an.dependencies();
   }

   /**
    * Subclass should provide access to a {@link MainManager}. This method
    * should never return null.
    * <p>
    * 
    * @return the main manager
    */
   protected abstract MainManager getMainManager();

   private ModuleManager getModuleManager() {
      return getMainManager().getManager(ModuleManager.class);
   }

   /**
    * An executor to execute init and stop methods.
    * 
    * @author Elvis Ligu
    * @version 0.0.1
    * @since 0.0.1
    */
   private class Executor extends AbstractMethodConstructorExecutor {

      @Override
      protected ParameterProvider resolveParameterProvider(Class<?> type,
            Map<String, Map<String, Object>> properties,
            ModulePropertyLocator propertyLocator) {
         // Ask first the property locator if he can find the provider,
         // if not then ask module manager
         ParameterProvider provider = propertyLocator.getParameterProvider(
               type, null, properties);
         if(provider == null) {
            provider = getModuleManager().getParameterProvider(type);
         }
         return provider;
      }
   }
}
