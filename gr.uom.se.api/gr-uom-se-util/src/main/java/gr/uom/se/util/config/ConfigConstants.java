/**
 * 
 */
package gr.uom.se.util.config;

/**
 * Different constants used by the configuration manager.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ConfigConstants {

   /**
    * Default value of default config.
    * <p>
    */
   public static final String DEFAULT_CONFIG_DOMAIN = "default";

   /**
    * The name of the property where the config manager should be saved.
    * <p>
    */
   public static final String DEFAULT_CONFIG_MANAGER_PROPERTY = "configManager";
   
   /**
    * Property name of the of the default config domain.
    * <p>
    */
   public static final String DEFAULT_CONFIG_DOMAIN_PROPERTY = "configDomain";
   /**
    * Default value of the default config folder.
    * <p>
    */
   public static final String DEFAULT_CONFIG_FOLDER = "config";
   /**
    * Property name of the default config folder value.
    * <p>
    */
   public static final String DEFAULT_CONFIG_FOLDER_PROPERTY = "configFolder";
   /**
    * Default name of the default config file.
    * <p>
    */
   public static final String DEFAULT_CONFIG_FILE = "default.config";
   /**
    * Property name of the default config file value.
    * <p>
    */
   public static final String DEFAULT_CONFIG_FILE_PROPERTY = "configFileName";

   /**
    * Property name of the default mapper factory.
    * <p>
    */
   public static final String DEFAULT_MAPPER_FACTORY_PROPERTY = "mapperFactory";

   /**
    * Given a property name return a new property name appended {@code .class} at
    * the end of the name.
    * <p>
    * 
    * @param name
    *           the property name
    * @return the property name of the class of the given property name
    */
   public static String getPropertyNameForConfigClass(String name) {
      return name + ".class";
   }
}
