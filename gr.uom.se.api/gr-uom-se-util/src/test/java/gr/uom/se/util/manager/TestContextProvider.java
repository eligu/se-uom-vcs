/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.context.Context;
import gr.uom.se.util.context.ContextProvider;
import gr.uom.se.util.module.annotations.ProvideModule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Elvis Ligu
 */
public class TestContextProvider implements ContextProvider {

   Bean.BeanContext context = new Bean.BeanContext();
   
   MainManager manager;
   /**
    * 
    */
   @ProvideModule
   public TestContextProvider(MainManager manager) {
      this.manager = manager;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public <C extends Context> C getContext(Object instance, Class<C> contextType) {
      return getContext((Class<?>)null, null);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public <C extends Context> C getContext(Class<?> type, Class<C> contextType) {
      return (C) context;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<Class<?>> getTypes() {
      return new HashSet<Class<?>>(Arrays.asList(Bean.class));
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public Set<Class<? extends Context>> getContextTypes() {
      Set<Class<? extends Context>> context = new HashSet<>();
      context.add(Bean.BeanContext.class);
      return context;
   }

}
