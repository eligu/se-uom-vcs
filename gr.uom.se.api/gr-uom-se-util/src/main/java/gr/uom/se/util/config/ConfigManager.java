package gr.uom.se.util.config;

import gr.uom.se.util.property.DomainPropertyHandler;
import gr.uom.se.util.property.PropertyHandler;

/**
 * A manager that deal with different configuration domains.
 * <p>
 * 
 * Instances of this type should be able to manage the available configuration
 * domains, such as the creation of a domain in memory or the loading of a
 * domain from a file or DB connection.
 * 
 * @author Elvis Ligu
 */
public interface ConfigManager extends PropertyHandler, DomainPropertyHandler {

   /**
    * Return the value of the property at the default configuration domain.
    * <p>
    * The default config domain is resolvable getting the property at
    * {@link ConfigConstants#DEFAULT_CONFIG_DOMAIN_PROPERTY}.
    * 
    * @param name
    *           the name of the property
    * @return the value of the property
    */
   @Override
   public Object getProperty(String name);

   /**
    * Get the value of the property in the default domain.
    * <p>
    * The type of the returned property should be of that of the given
    * {@code propertyType}. The manager may make conversion of properties on the
    * fly, if it is possible. For example if the requested type is int and the
    * property type within this manager is String it may try to convert the the
    * value to int. Keep in mind, though, that if a property is available and
    * can not be converted then it this method may throw an exception.
    * <p>
    * The default config domain is resolvable getting the property at
    * {@link ConfigConstants#DEFAULT_CONFIG_DOMAIN_PROPERTY}.
    * 
    * @param name
    *           the name of the property
    * @param propertyType
    *           the type of the requested property
    * @return the value of the property
    */
   @Override
   public <T> T getProperty(String name, Class<T> propertyType);

   /**
    * Set a property with the given name and the given value.
    * <p>
    * The property domain will be the default config domain, and later can be
    * retrieved using {@link #getProperty(String)}.
    * 
    * @param name
    *           the name of the property
    * @param value
    *           the value of the property
    */
   @Override
   public void setProperty(String name, Object value);

   /**
    * Get the value of the property in the specified domain.
    * <p>
    * 
    * @param domain
    *           from where the property comes from
    * @param name
    *           the name of the property
    * @return the value of the property
    */
   @Override
   public Object getProperty(String domain, String name);

   /**
    * Get the value of the property in the specified domain.
    * <p>
    * The type of the returned property should be of that of the given
    * {@code propertyType}. The manager may make conversion of properties on the
    * fly, if it is possible. For example if the requested type is int and the
    * property type within this manager is String it may try to convert the
    * value to int. Keep in mind, though, that if a property is available and
    * can not be converted then it this method may throw an exception.
    * 
    * @param domain
    *           from where the property comes from
    * @param name
    *           the name of the property
    * @param propertyType
    *           the type of the requested property
    * @return the value of the property
    */
   @Override
   public <T> T getProperty(String domain, String name, Class<T> propertyType);

   /**
    * Set the property in the specified domain, with the given value.
    * <p>
    * 
    * @param domain
    *           from where the property comes from
    * @param name
    *           the name of the property
    * @param value
    *           the value of the property
    */
   @Override
   public void setProperty(String domain, String name, Object value);

   /**
    * Load the given domain, in an implementation specific manner.
    * <p>
    * The implementations may use to load a domain with the given name from a DB
    * or from a .config file. Generally speaking each implementation must
    * provide a default way of loading domains, and it should be transparent to
    * the caller. Therefore this method is very implementation specific.
    * 
    * @param domain
    *           the name of the domain to load
    * @return
    */
   public ConfigDomain loadDomain(String domain);

   /**
    * Get the instance of the domain with the given name.
    * <p>
    * 
    * @param domain
    *           the name of the domain
    * @return the instance of the domain
    */
   public ConfigDomain getDomain(String domain);

   /**
    * Set the given domain to this manager.
    * <p>
    * This will override any existing domain with the same name.
    * 
    * @param domain
    *           to be added to this manager, must not be null.
    */
   void setDomain(ConfigDomain domain);
   
   /**
    * Load the given domain into this manager, and merge the new properties to
    * an old domain with the same name, if any.
    * <p>
    * This will load the given domain. If there is an old domain with the same
    * name it will merge the new domain to old one.
    * 
    * @param domain
    *           the domain type to be loaded, must not be null.
    * @return the new domain created
    */
   ConfigDomain loadAndMergeDomain(String domain);
}
