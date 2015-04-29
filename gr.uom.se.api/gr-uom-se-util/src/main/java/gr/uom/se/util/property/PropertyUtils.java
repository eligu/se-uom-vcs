/**
 * 
 */
package gr.uom.se.util.property;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Elvis Ligu
 */
public class PropertyUtils {

   /**
    * Given a map of properties return a domain property provider for the map.
    * 
    * @param properties
    *           mapped properties of type domain:name
    * @return a domain property provider
    */
   public static DomainPropertyProvider newProvider(
         Map<String, Map<String, Object>> properties) {
      return new MapDomainPropertyProvider(properties);
   }

   /**
    * Given a map of properties return a domain property writer for the map.
    * 
    * @param properties
    *           mapped properties of type domain:name
    * @return a domain property writer
    */
   public static DomainPropertyWriter newWriter(
         Map<String, Map<String, Object>> properties) {
      return new MapDomainPropertyWriter(properties);
   }

   /**
    * Given a map of properties return a domain property handler for the map.
    * 
    * @param properties
    *           mapped properties of type domain:name
    * @return a domain property provider
    */
   public static DomainPropertyHandler newHandler(
         Map<String, Map<String, Object>> properties) {
      DomainPropertyProvider provider = newProvider(properties);
      DomainPropertyWriter writer = newWriter(properties);
      return newHandler(provider, writer);
   }

   /**
    * Create a property handler based on an HashMap.
    * <p>
    * 
    * @return
    */
   public static DomainPropertyHandler newHandler() {
      return newHandler(new HashMap<String, Map<String, Object>>());
   }

   /**
    * Given a map of properties and a list of alternative property providers
    * return a property handler that will write properties to the given map, but
    * will read properties from the given map and if not found from the given
    * providers in the order they are specified.
    * 
    * @param properties
    *           mapped properties of form domain:name.
    * @param providers
    *           the alternative providers
    * @return
    */
   public static DomainPropertyHandler newHandler(
         Map<String, Map<String, Object>> properties,
         DomainPropertyProvider... providers) {
      ArgsCheck.notNull("providers", providers);

      DomainPropertyProvider provider = newProvider(properties);
      DomainPropertyWriter writer = newWriter(properties);

      // Return a chained
      if (providers.length > 0) {
         int size = providers.length + 1;
         DomainPropertyProvider[] dest = new DomainPropertyProvider[size];
         DomainPropertyProvider[] src = providers;
         System.arraycopy(src, 0, dest, 1, providers.length);
         dest[0] = provider;
         provider = newProvider(dest);
      }

      return newHandler(provider, writer);
   }

   /**
    * Given a provider and a writer return a domain property handler for the
    * map.
    * 
    * @param provider
    *           the provider of properties
    * @param writer
    *           the writer of properties
    * @return a domain property provider
    */
   public static DomainPropertyHandler newHandler(
         DomainPropertyProvider provider, DomainPropertyWriter writer) {
      return new DefaultDomainPropertyHandler(provider, writer);
   }

   /**
    * Given a list of providers return a domain property provider for the map.
    * <p>
    * This will return a chained provider which means when querying for a
    * property the provider will look at the first provider if it was found if
    * not it will look at second and so on.
    * 
    * @param providers
    *           a list of providers.
    * @return a domain property provider
    */
   public static DomainPropertyProvider newProvider(
         DomainPropertyProvider... providers) {
      if (providers.length == 1) {
         DomainPropertyProvider provider = providers[0];
         ArgsCheck.notNull("provider", provider);
      }
      return new ChainDomainPropertyProvider(providers);
   }

   /**
    * Given a list of providers return a domain property handler for the map.
    * <p>
    * This will return a chained handler which means when querying for a
    * property the provider will look at the first provider if it was found if
    * not it will look at second and so on. The writings will be in the writer.
    * 
    * 
    * @param providers
    *           a list of providers.
    * @param writer
    *           the writer of properties
    * @return a domain property provider
    */
   public static DomainPropertyHandler newHandler(DomainPropertyWriter writer,
         DomainPropertyProvider... providers) {
      DomainPropertyProvider provider = newProvider(providers); // the chained
                                                                // providers
      return newHandler(provider, writer);
   }

}
