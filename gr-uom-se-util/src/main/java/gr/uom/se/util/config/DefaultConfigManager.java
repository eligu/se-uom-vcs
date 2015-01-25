/**
 * 
 */
package gr.uom.se.util.config;

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
public class DefaultConfigManager implements ConfigManager {

   /**
    * The map of domains.
    * <p>
    */
   private ConcurrentHashMap<String, ConfigDomain> domains = new ConcurrentHashMap<>();

   /**
    * {@inheritDoc}
    * <p>
    * Same as calling {@link #getProperty(String, String)} where the domain name
    * is obtained by {@link ConfigConstants#DEFAULT_CONFIG_DOMAIN}.
    */
   @Override
   public <T> T getProperty(String name) {
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
   public <T> T getProperty(String domain, String name) {
      ConfigDomain cfgDomain = getDomain(domain);
      T val = null;
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
    * folder will be looked up by getting the property named
    * {@link ConfigConstants.DEFAULT_CONFIG_FOLDER_PROPERTY}.
    */
   @Override
   public void loadDomain(String domain) {

      ArgsCheck.notNull("domain", domain);

      String configFolder = this
            .getProperty(ConfigConstants.DEFAULT_CONFIG_FOLDER_PROPERTY);
      if (configFolder == null) {
         configFolder = ConfigConstants.DEFAULT_CONFIG_FOLDER;
      }

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
   void createDomain(String domain) {
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
}
