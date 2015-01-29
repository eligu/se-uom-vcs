package gr.uom.se.util.config;

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
public interface ConfigManager {

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
   public Object getProperty(String domain, String name);

   /**
    * Get the value of the property in the specified domain.
    * <p>
    * The type of the returned property should be of that of the given
    * {@code propertyType}. The manager may make conversion of properties on the
    * fly, if it is possible. For example if the requested type is int and the
    * property type within this manager is String it may try to convert the the
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
   public <T> T getProperty(String doman, String name, Class<T> propertyType);

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
    */
   public void loadDomain(String domain);

   /**
    * Load the domain based on a given class.
    * <p>
    * This method may use a specific mechanism for loading an instance given its
    * type. It should rely on gr.uom.se.util.module package and its classes.
    * 
    * @param domain
    *           the type of the domain to load
    * @return an instance of the given domain
    */
   public <T extends ConfigDomain> T loadDomain(Class<T> domain);

   /**
    * Get the instance of the domain with the given name.
    * <p>
    * 
    * @param domain
    *           the name of the domain
    * @return the instance of the domain
    */
   public ConfigDomain getDomain(String domain);
}
