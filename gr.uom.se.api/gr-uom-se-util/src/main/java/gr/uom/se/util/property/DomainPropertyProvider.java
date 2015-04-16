package gr.uom.se.util.property;

/**
 * A domain property provider that allows its clients to read properties from a
 * source, and to categorize them into domains.
 * <p>
 * A domain property provider is an extension of {@link PropertyProvider} 
 * however it is able to categorize properties into domains. That is, each
 * property will have two coordinates, the domain and its name.
 * <p>
 * 
 * @author Elvis Ligu
 */
public interface DomainPropertyProvider {
   
   /**
    * Get a property from this context.
    * <p>
    *
    * @param domain the domain of the property, must not be null.
    * @param name the property name, must not be null.
    * @return a property from the given context or null if the property is not
    * found.
    */
   Object getProperty(String domain, String name);

   /**
    * Get a property from this context.
    * <p>
    *
    * @param domain the domain of the property, must not be null.
    * @param name the property name, must not be null.
    * @param type the type of the property to look for, the property value may
    * be a String or another value different type of the given type. If this is
    * the case the implementation may try to convert it and may fail if it can
    * not convert it.
    * @param <T> The type of the property to be resolved
    * @return a property from the given context or null if the property is not
    * found.
    */
   <T> T getProperty(String domain, String name, Class<T> type);
}
