/**
 * 
 */
package gr.uom.se.util.property;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An implementation of chained property provider based on a set of providers.
 * <p>
 * This implementation will try to look up a property from a collection of
 * providers. The order of providers when this instance is created is maintained
 * within the lookup process.
 * 
 * @author Elvis Ligu
 */
public class ChainDomainPropertyProvider implements DomainPropertyProvider {

   private final Collection<DomainPropertyProvider> providers;

   /**
    * Create a chain property provider based on the given providers.
    * <p>
    */
   public ChainDomainPropertyProvider(DomainPropertyProvider... providers) {
      ArgsCheck.notEmpty("providers", providers);
      this.providers = new ArrayList<>(providers.length);
      for (DomainPropertyProvider provider : providers) {
         ArgsCheck.notNull("provider", provider);
         this.providers.add(provider);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object getProperty(String domain, String name) {
      for(DomainPropertyProvider provider : providers) {
         Object object = provider.getProperty(domain, name);
         if(object != null) {
            return object;
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getProperty(String domain, String name, Class<T> type) {
      for(DomainPropertyProvider provider : providers) {
         T object = provider.getProperty(domain, name, type);
         if(object != null) {
            return object;
         }
      }
      return null;
   }

}
