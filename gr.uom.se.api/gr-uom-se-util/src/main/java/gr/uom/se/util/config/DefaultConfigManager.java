/**
 *
 */
package gr.uom.se.util.config;

import gr.uom.se.util.manager.ManagerConstants;
import gr.uom.se.util.manager.annotations.Init;
import gr.uom.se.util.module.annotations.Property;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final Logger logger = Logger.getLogger(DefaultConfigManager.class.getName());

    /**
     * Will try to load the default config domain, but if a problem occurs it
     * will not throw an exception, instead it will print a message in stderr.
     * <p>
     */
    @Init
    public void init() {
        String domain = getDefaultConfigDomain();
        try {
            this.loadAndMergeDomain(domain);
        } catch (Exception e) {
         // TODO this line must be checked for consistency
            // Probably a logger would be a better idea
            logger.log(Level.WARNING, 
                    "Default domain {0} could not be loaded. Reason: {1}",
                    new Object[]{domain, e.getMessage()});
        }
    }
}
