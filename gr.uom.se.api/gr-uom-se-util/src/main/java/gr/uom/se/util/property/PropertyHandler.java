package gr.uom.se.util.property;

/**
 * A property handler interface which allows clients to read and write
 * properties from a source based on their name.
 * <p>
 * This is an aggregated interface where a property provider and a property
 * writer are composed together in order to provide property management.
 * 
 * @author Elvis Ligu
 */
public interface PropertyHandler extends PropertyProvider, PropertyWriter {
}
