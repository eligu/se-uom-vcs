/**
 * 
 */
package gr.uom.se.util.config;

import gr.uom.se.util.mapper.Mapper;
import gr.uom.se.util.mapper.MapperFactory;
import gr.uom.se.util.module.ModuleLoader;
import gr.uom.se.util.module.ModuleUtils;
import gr.uom.se.util.validation.ArgsCheck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The default implementation of a config manager.
 * <p>
 * This manager is based an configuration domains, however it doesn't rely on a
 * specific {@link ConfigDomain} implementation. The only case when this manager
 * would use a {@link DefaultConfigDomain} is when it loads a domain provided
 * only its name {@link #loadDomain(String)}. If the domain should be loaded by
 * using its class, then use an implementation that is a module and can be
 * loaded by an instance of {@link ModuleLoader}.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractConfigManager implements ConfigManager {

   /**
    * The map of domains.
    * <p>
    */
   private ConcurrentHashMap<String, ConfigDomain> domains = new ConcurrentHashMap<>();

   public AbstractConfigManager() {
   }

   /**
    * {@inheritDoc}
    * <p>
    * Same as calling {@link #getProperty(String, String)} where the domain name
    * is obtained by {@link ConfigConstants#DEFAULT_CONFIG_DOMAIN}.
    */
   @Override
   public Object getProperty(String name) {
      return getProperty(ConfigConstants.DEFAULT_CONFIG_DOMAIN, name);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Same as calling {@link #setProperty(String, String, Object)} where the
    * domain name is obtained by {@link ConfigConstants#DEFAULT_CONFIG_DOMAIN}.
    */
   @Override
   public void setProperty(String name, Object value) {
      setProperty(ConfigConstants.DEFAULT_CONFIG_DOMAIN, name, value);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object getProperty(String domain, String name) {
      ConfigDomain cfgDomain = getDomain(domain);
      Object val = null;
      if (cfgDomain != null) {
         val = cfgDomain.getProperty(name);
      }
      return val;
   }

   /**
    * {@inheritDoc}
    * <p>
    * If the domain is not present when calling this method, it will create a
    * new domain with the given name {@code domain}.
    */
   @Override
   public void setProperty(String domain, String name, Object value) {
      ConfigDomain cfgDomain = getDomain(domain);
      if (cfgDomain == null) {
         createDomain(domain);
         cfgDomain = this.domains.get(domain);
      }
      cfgDomain.setProperty(name, value);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Will load an implementation of {@link DefaultConfigDomain} from a
    * .properties file. The name of the file must be the same as the name of the
    * domain having a suffix of .properties or .config. The default config
    * folder will be looked up by calling the method
    * {@link #getDefaultConfigFolder()} which will return a value of
    * {@link ConfigConstants#DEFAULT_CONFIG_FOLDER}.
    */
   @Override
   public void loadDomain(String domain) {

      ArgsCheck.notNull("domain", domain);

      String configFolder = getDefaultConfigFolder();

      Path folder = Paths.get(configFolder);
      if (!Files.isDirectory(folder)) {
         throw new IllegalArgumentException("Can not load domain " + domain
               + " directory " + folder + "doesn't exist");
      }

      Path file = Paths.get(configFolder, domain);
      if (!Files.exists(file)) {
         file = Paths.get(configFolder, domain + ".properties");
         if (!Files.exists(file)) {
            file = Paths.get(configFolder, domain + ".config");
         }
      }
      if (!Files.exists(file)) {
         throw new IllegalArgumentException("Can not load domain " + domain
               + " default file doesn't exist in directory " + folder);
      }
      if (!Files.isReadable(file)) {
         throw new IllegalArgumentException("Can not load domain " + domain
               + " file " + file + " is not readable");
      }
      try {
         DefaultConfigDomain cfgDomain = DefaultConfigDomain.load(domain,
               configFolder, file.getFileName().toString());
         addDomain(cfgDomain);
      } catch (IOException e) {
         throw new IllegalArgumentException(e);
      }
   }

   /**
    * Get the default config folder location.
    * <p>
    * This method will check under property named,
    * {@link ConfigConstants#DEFAULT_CONFIG_FOLDER_PROPERTY} to retrieve the
    * default config folder location. If no property was specified there it will
    * return {@link ConfigConstants#DEFAULT_CONFIG_FOLDER_PROPERTY}.
    * <p>
    * Subclasses may override this method in order to provide a default location
    * for default config folder.
    * 
    * @return
    */
   protected String getDefaultConfigFolder() {
      String configFolder = this.getProperty(
            ConfigConstants.DEFAULT_CONFIG_FOLDER_PROPERTY, String.class);
      if (configFolder == null) {
         configFolder = ConfigConstants.DEFAULT_CONFIG_FOLDER;
      }
      return configFolder;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T extends ConfigDomain> T loadDomain(Class<T> domain) {
      // Get a module loader, even if there is not a default
      // loader for the given type, get the default loader
      // implementation
      ModuleLoader loader = ModuleUtils.resolveLoader(domain, this, null);
      T instanceDomain = loader.load(domain);
      if (instanceDomain == null) {
         throw new IllegalArgumentException("Domain instance " + domain
               + " can not be loaded by " + loader);
      }
      addDomain(instanceDomain);
      return instanceDomain;
   }

   /**
    * Add a domain to the list of domains.
    * <p>
    * 
    * @param instanceDomain
    */
   private void addDomain(ConfigDomain instanceDomain) {
      String name = instanceDomain.getName();
      if (name == null) {
         throw new IllegalArgumentException("The loaded domain "
               + instanceDomain.getClass() + " doesn't provide a name");
      }
      this.domains.put(name, instanceDomain);
   }

   /**
    * Create a domain if there is not already a domain with the same name or
    * throw an exception.
    * 
    * @param domain
    */
   private void createDomain(String domain) {
      if (domains.containsKey(domain)) {
         throw new IllegalArgumentException("domain " + domain
               + " can not be created as it already exists");
      }
      DefaultConfigDomain cfgDomain = new DefaultConfigDomain(domain);
      ConfigDomain previous = domains.putIfAbsent(domain, cfgDomain);
      if (previous != null) {
         throw new IllegalArgumentException("Can not create domain " + domain
               + " it already exists");
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ConfigDomain getDomain(String domain) {
      ArgsCheck.notEmpty("domain", domain);
      return domains.get(domain);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getProperty(String name, Class<T> propertyType) {
      return getProperty(ConfigConstants.DEFAULT_CONFIG_DOMAIN, name,
            propertyType);
   }

   /**
    * Return a mapper factory.
    * <p>
    * The factory will be looked up at at default config domain. If a factory
    * the default factory instance will be returned.
    * 
    * @return
    */
   private MapperFactory getMapperFactory() {

      // Look for the default mapper factory under the default
      // domain
      String name = ConfigConstants.DEFAULT_MAPPER_FACTORY_PROPERTY;
      MapperFactory factory = getProperty(name, MapperFactory.class);

      if (factory != null) {
         return factory;
      }
      // If factory is null then look for the class
      // of mapper factory
      Class<?> fClass = getProperty(
            ConfigConstants.getPropertyNameForConfigClass(name), Class.class);

      // If class was found then load the mapper factory
      if (fClass != null) {
         // Check the class of mapper factory
         if (!MapperFactory.class.isAssignableFrom(fClass)) {
            throw new RuntimeException(
                  "Wrong class specified for mapper factory: " + fClass);
         }
         // Load the factory
         factory = ModuleUtils.resolveLoader(fClass, this,
               ModuleUtils.getModuleConfig(fClass)).load(MapperFactory.class);
      }

      // If a class was not found
      // then get the default mapper factory
      // and register it
      if (factory == null) {
         factory = MapperFactory.getInstance();
         setProperty(name, factory);
      }
      return factory;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> T getProperty(String domain, String name, Class<T> propertyType) {
      ArgsCheck.notNull("propertyType", propertyType);
      Object val = getProperty(domain, name);
      if (val == null) {
         return null;
      } else if (propertyType.isAssignableFrom(val.getClass())) {
         return (T) val;
      }
      // If the value is not of the same type we need
      // to make a conversion if it is possible
      MapperFactory factory = getMapperFactory();
      Mapper mapper = factory.getMapper(val.getClass(), propertyType);
      if (mapper == null) {
         throw new IllegalArgumentException("The property at domain " + domain
               + " named " + name + " with a type of " + val.getClass()
               + " can not be converted to " + propertyType);
      }
      return mapper.map(val, propertyType);
   }
}
