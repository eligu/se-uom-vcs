package gr.uom.se.util.property;

/**
 * A property writer interface that defines method which allows clients to write
 * properties for a given context.
 * <p>
 * This interface allows its clients to write properties based on its names,
 * a.k.a named properties.
 * 
 * @author Elvis Ligu
 */
public interface PropertyWriter {
   
   /**
    * Write a property with the given name and the given value.
    * <p>
    * 
    * @param name
    *           the name of the property
    * @param value
    *           the value of the property
    */
   public void setProperty(String name, Object value);
}
