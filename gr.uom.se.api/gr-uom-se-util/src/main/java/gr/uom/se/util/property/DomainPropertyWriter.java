package gr.uom.se.util.property;

/**
 * A domain property writer interface which allows clients to write properties
 * into a context based on their domains and their names.
 * <p>
 * This writer categorize properties into domains, so we can have properties
 * with the same name but with different domains. This allows a more flexible
 * management of properties.
 *
 * @author Elvis Ligu
 */
public interface DomainPropertyWriter {

   /**
    * Set a property with the given domain, name and value.
    * <p>
    *
    * @param domain the domain of the property
    * @param name the name of the property
    * @param value the value of the property
    */
   public void setProperty(String domain, String name, Object value);
}
