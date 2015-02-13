/**
 *
 */
package gr.uom.se.util.config;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract implementation of a configuration domain.
 * <p>
 * Uses internally a concurrent hash map to obtain values. If a null value is
 * specified for a property it will remove the old value if any. That means null
 * values are allowed.
 *
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractConfigDomain implements ConfigDomain {

    /**
     * The domain name of this configuration.
     * <p>
     */
    protected final String name;

    /**
     * The properties of this domain.
     * <p>
     * The map is a concurrent map.
     */
    protected ConcurrentHashMap<String, Object> properties;

    /**
     * Create an instance with the given name.
     * <p>
     */
    public AbstractConfigDomain(String name) {
        ArgsCheck.notEmpty("name", name);
        this.name = name;
        this.properties = new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc)
     *
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc)
     *
     */
    @Override
    public Object getProperty(String name) {
        ArgsCheck.notEmpty("name", name);
        return properties.get(name);
    }

    /**
     * {@inheritDoc)
     */
    @Override
    public void setProperty(String name, Object val) {
        set(name, val);
    }

    /**
     * Will check if the given value is null it will remove the last mapping
     * from the map, otherwise it will put the mapping.
     *
     * @param name
     * @param val
     * @return
     */
    protected Object set(String name, Object val) {
        ArgsCheck.notEmpty("name", name);
        if (val == null) {
            return properties.remove(name);
        } else {
            return properties.put(name, val);
        }
    }

    public void merge(ConfigDomain domain) {
    }
}
