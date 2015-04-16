package gr.uom.se.util.property;

/**
 * A property handler interface that allows its clients to read and write
 * properties from/to a source, based on their domain and name.
 * <p>
 * This handler is an aggregated interface which brings together a domain
 * property provider and a domain property writer.
 * 
 * @author Elvis Ligu
 */
public interface DomainPropertyHandler extends DomainPropertyProvider, 
        DomainPropertyWriter{
}
