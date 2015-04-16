/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.property.DomainPropertyHandler;

/**
 * The default module context that can be used to specify modules' properties
 * for their default places.
 * <p>
 * This implementation works the same as the {@link DefaultModuleManager}.
 * However it provides a convenient way to specify modules properties. The
 * supported type for this context is Object which means that any module config
 * property specified using this context will be applied to all modules. If the
 * client wants to specify a default property (such as loader or injector) for
 * all modules it should specify an Object.class type or a null type. For
 * example to define the default module loader use
 * {@linkplain #setLoader(ModuleLoader, Class) setLoader(loader, null)}.
 * 
 * @author Elvis Ligu
 */
public class DefaultModuleContext extends AbstractModuleContext {

   private final Class<?> type;
   
   /**
    * Create an instance of default module context by specifying a property
    * handler (property source) and a property locator.
    * <p>
    * 
    * @param propertyHandler
    *           used to get the properties to construct modules.
    * @param locator
    *           used to locate config properties for modules, such as loaders
    *           and injectors.
    * @param parent
    *           the parent context to look for properties if they are not found
    *           within this context.
    * 
    */
   public DefaultModuleContext(DomainPropertyHandler propertyHandler,
         ModulePropertyLocator locator, ModuleContext parent) {
      this(propertyHandler, locator, parent, Object.class);
   }
   
   /**
    * Create an instance of default module context by specifying a property
    * handler (property source) and a property locator.
    * <p>
    * 
    * @param propertyHandler
    *           used to get the properties to construct modules.
    * @param locator
    *           used to locate config properties for modules, such as loaders
    *           and injectors.
    * @param parent
    *           the parent context to look for properties if they are not found
    *           within this context.
    * 
    */
   public DefaultModuleContext(DomainPropertyHandler propertyHandler,
         ModulePropertyLocator locator, ModuleContext parent, Class<?> type) {
      super(propertyHandler, locator, parent);
      if(type == null) {
         type = Object.class;
      }
      this.type = type;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<?> getType() {
      return this.type;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected String getPropertyNameForConfig(String name, Class<?> type) {
      if (type == null || type.equals(Object.class)) {
         // Here we return the name itself as long as this is the default
         // context that applies to all modules so we do not want these
         // properties to be specific for a module but for all modules
         return name;
      }
      return ModuleConstants.getPropertyNameForConfig(type, name);
   }
}
