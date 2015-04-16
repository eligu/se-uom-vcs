/**
 * 
 */
package gr.uom.se.util.context;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Elvis Ligu
 */
public abstract class AbstractContextManager implements ContextManager {

   /**
    * The context providers.
    */
   private final Map<Class<?>, ContextProvider> providers = new HashMap<>();

   /**
    * Providers lock.
    */
   private final ReadWriteLock lock = new ReentrantReadWriteLock();
   
   /**
    * {@inheritDoc}
    */
   @Override
   public ContextProvider getProvider(Class<?> type) {
      ArgsCheck.notNull("type", type);
      lock.readLock().lock();
      try {
         return providers.get(type);
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void registerProvider(ContextProvider provider) {
      ArgsCheck.notNull("provider", provider);
      Collection<Class<?>> types = provider.getTypes();
      ArgsCheck.notEmpty("provider types", types);
      lock.writeLock().lock();
      try {
         for(Class<?> t : types) {
            providers.put(t, provider);
         }
      } finally {
         lock.writeLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <C extends Context> C lookupContext(Object instance,
         Class<C> contextType) {
      ArgsCheck.notNull("instance", instance);
      ContextProvider provider = getProvider(instance.getClass());
      if(provider == null) {
         return null;
      }
      return provider.getContext(instance, contextType);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <C extends Context> C lookupContext(Class<?> type,
         Class<C> contextType) {
      ArgsCheck.notNull("type", type);
      ContextProvider provider = getProvider(type);
      if(provider == null) {
         return null;
      }
      return provider.getContext(type, contextType);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void removeProviderFor(Class<?> type) {
      ArgsCheck.notNull("type", type);
      lock.writeLock().lock();
      try {
         providers.remove(type);
      } finally {
         lock.writeLock().unlock();
      }
   }
   
}
