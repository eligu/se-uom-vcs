package gr.uom.se.util.property;

/**
 * A property provider interface that defines methods to read properties from a
 * source.
 * <p>
 * A property provider is a source which provide named properties. Instead of
 * providing only Object instances it is able to look up a property based on its
 * name and its type. That means, the clients may check if a property provided
 * by the provider is of the given type, and use it. However implementations may
 * choose to make conversions on the fly when a property of the given type is
 * required.
 *
 * @author Elvis Ligu
 */
public interface PropertyProvider {

   /**
    * Get a property from this context.
    * <p>
    *
    * @param name the property name, must not be null.
    * @return a property from the given context or null if the property is not
    * found.
    */
   Object getProperty(String name);

   /**
    * Get a property from this context.
    * <p>
    *
    * @param name the property name, must not be null.
    * @param type the type of the property to look for, the property value may
    * be a String or another value different type of the given type. If this is
    * the case the implementation may try to convert it and may fail if it can
    * not convert it.
    * @param <T> The type of the property to be resolved
    * @return a property from the given context or null if the property is not
    * found.
    */
   <T> T getProperty(String name, Class<T> type);
}
