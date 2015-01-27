/**
 * 
 */
package gr.uom.se.util.config;

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
}
