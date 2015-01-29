/**
 * 
 */
package gr.uom.se.util.config;

import gr.uom.se.util.manager.ManagerConstants;
import gr.uom.se.util.module.annotations.Property;

/**
 * Just a concrete class of type {@link AbstractConfigManager}.
 * <p>
 * This is class is used to provide the default implementation of
 * {@link AbstractConfigManager}, in cases where a config manager is used.
 * However in an integrated environment where there should be initializations to
 * load the default configs and so on, it is probably suited a direct sub class
 * of {@link AbstractConfigManager}.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
@Property(domain = ManagerConstants.DEFAULT_DOMAIN, name = ConfigConstants.DEFAULT_CONFIG_MANAGER_PROPERTY)
public class DefaultConfigManager extends AbstractConfigManager {
}
