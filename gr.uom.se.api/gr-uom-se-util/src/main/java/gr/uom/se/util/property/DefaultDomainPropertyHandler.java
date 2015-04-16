/**
 * 
 */
package gr.uom.se.util.property;

import gr.uom.se.util.validation.ArgsCheck;

/**
 * An implementation of property handler based on a map.
 * <p>
 * 
 * @author Elvis Ligu
 */
public class DefaultDomainPropertyHandler implements DomainPropertyHandler {

   private final DomainPropertyProvider provider;
   private final DomainPropertyWriter writer;

   /**
    * Create an instance based on a provider and a writer.
    * <p>
    * 
    * @param provider
    *           from where properties should be read.
    * @param writer
    *           to where properties should be written.
    */
   public DefaultDomainPropertyHandler(DomainPropertyProvider provider,
         DomainPropertyWriter writer) {
      ArgsCheck.notNull("provider", provider);
      ArgsCheck.notNull("writer", writer);
      this.provider = provider;
      this.writer = writer;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object getProperty(String domain, String name) {
      return provider.getProperty(domain, name);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getProperty(String domain, String name, Class<T> type) {
      return provider.getProperty(domain, name, type);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setProperty(String domain, String name, Object value) {
      writer.setProperty(domain, name, value);
   }

}
