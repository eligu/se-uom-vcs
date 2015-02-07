/**
 * 
 */
package gr.uom.se.util.config;

import java.util.Map;

/**
 * A set of <key value> properties for a specified domain.
 * <p>
 * 
 * A configuration domain, usually contains related properties that are specific
 * to the domain. For example a config domain for a DB connection may contain a
 * PORT and a INET property, however within the same system a remote service
 * configuration may contain the same properties. By categorizing properties
 * into domains we are able to deal with each domain independently.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface ConfigDomain {

   /**
    * Get the name of the domain.
    * <p>
    * Implementations should normally return a non null and non empty value.
    * 
    * @return the name of this domain
    */
   String getName();

   /**
    * Get the property for the given name.
    * <p>
    * 
    * @param name
    * @return
    */
   Object getProperty(String name);

   /**
    * Set the specified value for the given property.
    * <p>
    * If the value is null, it will remove the property from this domain.
    * 
    * @param name
    *           the name of the property
    * @param val
    *           the value of the property
    */
   void setProperty(String name, Object val);

   /**
    * Return a map of all properties within this domain.
    * <p>
    * Keep in mind that the map may be a copy of all properties contained within
    * this domain, or an internal map of the domain. If this is the last case be
    * careful when using this map as to not interfere with other threads.
    * However it depends on the implementations of this interface.
    * 
    * @return a map of all properties.
    */
   Map<String, Object> getProperties();

   /**
    * Get a map of all properties that starts with the given string.
    * <p>
    * 
    * @param prefix
    *           of all properties that are returned, must not be null.
    * @return a map of all properties with the given prefix.
    */
   Map<String, Object> getPropertiesWithPrefix(String prefix);

   /**
    * Get a map of all properties that ends with the given string.
    * <p>
    * 
    * @param suffix
    *           of all properties that are returned, must not be null.
    * @return a map of all properties with the given suffix.
    */
   Map<String, Object> getPropertiesWithSuffix(String suffix);

   /**
    * Given another domain, merge the properties of this domain with the
    * properties of the other domain.
    * <p>
    * If this domain contains a property and the other domain contains the
    * property then it will be replaced with the value of the other domain
    * property.
    * 
    * @param domain
    *           the domain to merge with this one, must not be null
    */
   void merge(ConfigDomain domain);
}
