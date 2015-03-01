/**
 * 
 */
package gr.uom.se.util.config;

import gr.uom.se.util.module.DefaultModuleLoader;
import gr.uom.se.util.module.annotations.Module;
import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.module.annotations.ProvideModule;
import gr.uom.se.util.validation.ArgsCheck;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * The default config domain implementation.
 * <p>
 * This is the default implementation of the config domain. The implementation
 * is agnostic to the properties and the properties are loaded into domain
 * however when using this instance as a loader keep in mind that the provider
 * method {@link #load(String, String, String)} will load the properties from a
 * .properties Java file. It is a module designed to be loaded by an instance of
 * {@link DefaultModuleLoader}.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
@Module(properties = {
      @Property(name = ConfigConstants.DEFAULT_CONFIG_FOLDER_PROPERTY, stringVal = ConfigConstants.DEFAULT_CONFIG_FOLDER),
      @Property(name = ConfigConstants.DEFAULT_CONFIG_FILE_PROPERTY, stringVal = ConfigConstants.DEFAULT_CONFIG_FILE),
      @Property(name = ConfigConstants.DEFAULT_CONFIG_DOMAIN_PROPERTY, stringVal = ConfigConstants.DEFAULT_CONFIG_DOMAIN) }, provider = DefaultConfigDomain.class)
public class DefaultConfigDomain extends AbstractPropertyChangeConfigDomain {

   public DefaultConfigDomain(String name) {
      super(name);
   }

   /**
    * Provider method for a default config domain.
    * <p>
    * This method will require a config file of .properties type to be present
    * in the specified path to load the domain.
    * 
    * @param domain
    *           the name of the domain to load
    * @param configFolder
    *           the path of the config folder to look for config file
    * @param configFile
    *           the name of the config file to load
    * @return an implementation of default config domain
    * @throws IOException
    *            if the file can not be loaded
    */
   @ProvideModule
   public static DefaultConfigDomain load(
         @Property(name = ConfigConstants.DEFAULT_CONFIG_DOMAIN_PROPERTY) String domain,
         @Property(name = ConfigConstants.DEFAULT_CONFIG_FOLDER_PROPERTY) String configFolder,
         @Property(name = ConfigConstants.DEFAULT_CONFIG_FILE_PROPERTY) String configFile)
         throws IOException {

      ArgsCheck.notEmpty("domain", domain);
      DefaultConfigDomain config = new DefaultConfigDomain(domain);
      return load(configFolder, configFile, config);
   }

   /**
    * Given an instance of config domain, load a .property file into the domain.
    * <p>
    * 
    * @param configFolder
    *           the path of the config folder to look for config file
    * @param configFile
    *           the name of the config file to load
    * @param config
    *           the config domain into which the .properties file will be loaded
    * @return the same config domain
    * @throws IOException
    *            if the file can not be loaded
    */
   public static <T extends ConfigDomain> T load(String configFolder,
         String configFile, T config) throws IOException {

      ArgsCheck.notEmpty("configFolder", configFolder);
      ArgsCheck.notEmpty("configFile", configFile);

      // Check if the path exists and is readable
      Path path = Paths.get(configFolder, configFile);
      if (!Files.exists(path)) {
         throw new IllegalArgumentException("path under: " + path
               + " doesn't exists");
      }
      if (!Files.isReadable(path)) {
         throw new IllegalArgumentException("path under: " + path
               + " is not readable");
      }

      // Load the properties
      try (InputStream is = Files.newInputStream(path)) {
         load(config, is);
      }
      return config;
   }

   /**
    * Given an instance of config domain, load a .property file into the domain.
    * <p>
    * This will return the same instance provided, but with properties inserted
    * into it. If the file can not be found it will return null.
    * 
    * @param configFolder
    *           the path of the config folder to look for config file
    * @param configFile
    *           the name of the config file to load
    * @param config
    *           the config domain into which the .properties file will be loaded
    * @return the same config domain, or null if the file was not found
    * @throws IOException
    *            if the file can not be loaded
    */
   public static <T extends ConfigDomain> T loadIfExist(String configFolder,
         String configFile, T config) throws IOException {

      ArgsCheck.notEmpty("configFolder", configFolder);
      ArgsCheck.notEmpty("configFile", configFile);

      // Check if the path exists and is readable
      Path path = Paths.get(configFolder, configFile);
      if (!Files.exists(path)) {
         return null;
      }
      if (!Files.isReadable(path)) {
         return null;
      }

      // Load the properties
      try (InputStream is = Files.newInputStream(path)) {
         load(config, is);
      }
      return config;
   }

   /**
    * Given an instance of config domain, load properties from the given input
    * stream into the domain.
    * <p>
    * 
    * @param config
    *           the config domain into which the properties will be loaded
    * @param inputStream
    *           the stream containing the properties. The specified stream
    *           remain open after this method returns
    * @return the domain with properties
    * @throws IOException
    *            if a problem occurs while reading from stream
    */
   public static <T extends ConfigDomain> T load(T config,
         InputStream inputStream) throws IOException {
      ArgsCheck.notNull("config", config);
      ArgsCheck.notNull("inputStream", inputStream);

      // Load the properties
      Properties properties = new Properties();
      properties.load(inputStream);
      // Copy each property to domain
      for (Object key : properties.keySet()) {
         Object val = properties.get(key);
         config.setProperty(key.toString(), val);
      }
      return config;
   }
}