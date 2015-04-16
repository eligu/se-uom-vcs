/**
 * 
 */
package gr.uom.se.util.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.config.DefaultConfigManager;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author Elvis Ligu
 */
public class DefaultModulePropertyLocatorTest {

   static ModulePropertyLocator locator = new DefaultModulePropertyLocator();

   @Test
   public void testGetLoader() {

      ConfigManager config = new DefaultConfigManager();
      // Get the default loader from default place
      MyModuleLoader defaultLoader = new MyModuleLoader();
      String defaultDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      String defaultName = ModuleConstants.LOADER_PROPERTY;
      config.setProperty(defaultDomain, defaultName, defaultLoader);
      ModuleLoader locatedLoader = locator
            .getLoader(Object.class, config, null);
      assertNotNull(locatedLoader);
      assertEquals(defaultLoader, locatedLoader);

      // Clear config
      config.setProperty(defaultDomain, defaultName, null);
      assertNull(locator.getLoader(Object.class, config, null));

      // Get the loader from class domain
      String classDomain = ModuleConstants.getDefaultConfigFor(Object.class);
      MyModuleLoader loaderAtClassDomain = new MyModuleLoader();
      config.setProperty(classDomain, defaultName, loaderAtClassDomain);
      locatedLoader = null;
      locatedLoader = locator.getLoader(Object.class, config, null);
      assertNotNull(locatedLoader);
      assertEquals(loaderAtClassDomain, locatedLoader);

      // Get the class loader from default domain
      config.setProperty(classDomain, defaultName, null);
      MyModuleLoader loaderAtDefaultDomain = new MyModuleLoader();
      locatedLoader = null;
      String loaderName = ModuleConstants.getLoaderNameFor(Object.class);
      config.setProperty(defaultDomain, loaderName, loaderAtDefaultDomain);
      locatedLoader = locator.getLoader(Object.class, config, null);
      assertNotNull(locatedLoader);
      assertEquals(loaderAtDefaultDomain, locatedLoader);

      // Now save all three loader at three places
      config.setProperty(defaultDomain, defaultName, defaultLoader);
      config.setProperty(classDomain, defaultName, loaderAtClassDomain);
      config.setProperty(defaultDomain, loaderName, loaderAtDefaultDomain);
      // We should expect the loader at class domain
      locatedLoader = locator.getLoader(Object.class, config, null);
      assertNotNull(locatedLoader);
      assertEquals(loaderAtClassDomain, locatedLoader);

      // Remove the loader from class domain
      config.setProperty(classDomain, defaultName, null);
      // We should expect the loader at default domain but with class name
      // as prefix
      locatedLoader = locator.getLoader(Object.class, config, null);
      assertNotNull(locatedLoader);
      assertEquals(loaderAtDefaultDomain, locatedLoader);

      // Clear the config
      config.setProperty(defaultDomain, defaultName, null);
      config.setProperty(classDomain, defaultName, null);
      config.setProperty(defaultDomain, loaderName, null);

      // Now make the test with only the map
      Map<String, Map<String, Object>> map = new HashMap<>();
      config.setProperty(defaultDomain, defaultName, defaultLoader);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      locatedLoader = locator.getLoader(Object.class, null, map);
      assertNotNull(locatedLoader);
      assertEquals(defaultLoader, locatedLoader);

      // Clear config
      config.setProperty(defaultDomain, defaultName, null);
      map.put(defaultDomain, null);
      assertNull(locator.getLoader(Object.class, null, map));

      // Get the loader from class domain
      classDomain = ModuleConstants.getDefaultConfigFor(Object.class);
      config.setProperty(classDomain, defaultName, loaderAtClassDomain);
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      locatedLoader = null;
      locatedLoader = locator.getLoader(Object.class, null, map);
      assertNotNull(locatedLoader);
      assertEquals(loaderAtClassDomain, locatedLoader);

      // Get the class loader from default domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      locatedLoader = null;
      config.setProperty(defaultDomain, loaderName, loaderAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      locatedLoader = locator.getLoader(Object.class, null, map);
      assertNotNull(locatedLoader);
      assertEquals(loaderAtDefaultDomain, locatedLoader);

      // Now save all three loader at three places
      config.setProperty(defaultDomain, defaultName, defaultLoader);
      config.setProperty(classDomain, defaultName, loaderAtClassDomain);
      config.setProperty(defaultDomain, loaderName, loaderAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      // We should expect the loader at class domain
      locatedLoader = locator.getLoader(Object.class, null, map);
      assertNotNull(locatedLoader);
      assertEquals(loaderAtClassDomain, locatedLoader);

      // Remove the loader from class domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      // We should expect the loader at default domain but with class name
      // as prefix
      locatedLoader = locator.getLoader(Object.class, null, map);
      assertNotNull(locatedLoader);
      assertEquals(loaderAtDefaultDomain, locatedLoader);

      // Now provide an other loader for properties, and leave
      // as is the config
      MyModuleLoader otherLoader = new MyModuleLoader();
      config.setProperty(defaultDomain, loaderName, otherLoader);
      // The properties map should be unchanged and the
      // locator should find that of config
      // We should expect the loader at default domain but with class name
      // as prefix. However it should be the otherLoader
      locatedLoader = locator.getLoader(Object.class, config, map);
      assertNotNull(locatedLoader);
      assertEquals(otherLoader, locatedLoader);
   }

   @Test
   public void getLoaderReverted() {
      ConfigManager config = new DefaultConfigManager();
      MyModuleLoader configLoader = new MyModuleLoader();
      DefaultModuleLoader mapLoader = new DefaultModuleLoader(null, null);
      String defaultDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      String defaultName = ModuleConstants.LOADER_PROPERTY;
      config.setProperty(defaultDomain, defaultName, configLoader);
      ModuleLoader locatedLoader = locator
            .getLoader(Object.class, config, null);
      assertNotNull(locatedLoader);
      assertEquals(configLoader, locatedLoader);
      Map<String, Map<String, Object>> properties = new HashMap<>();
      Map<String, Object> map = config.getDomain(defaultDomain).getProperties();
      properties.put(defaultDomain, map);
      map.put(defaultName, mapLoader);
      FastDynamicModulePropertyLocator revertedLocator = new FastDynamicModulePropertyLocator(
            properties);
      locatedLoader = revertedLocator.getLoader(Object.class, config,
            properties);
      assertEquals(mapLoader, locatedLoader);
   }

   @Test
   public void testGetParameterProvider() {
      ConfigManager config = new DefaultConfigManager();
      // Get the default provider from default place
      MyParameterProvider defaultProvider = new MyParameterProvider();
      String defaultDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      String defaultName = ModuleConstants.PARAMETER_PROVIDER_PROPERTY;
      config.setProperty(defaultDomain, defaultName, defaultProvider);
      ParameterProvider locatedProvider = locator.getParameterProvider(
            Object.class, config, null);
      assertNotNull(locatedProvider);
      assertEquals(defaultProvider, locatedProvider);

      // Clear config
      config.setProperty(defaultDomain, defaultName, null);
      assertNull(locator.getParameterProvider(Object.class, config, null));

      // Get the loader from class domain
      String classDomain = ModuleConstants.getDefaultConfigFor(Object.class);
      MyParameterProvider providerAtClassDomain = new MyParameterProvider();
      config.setProperty(classDomain, defaultName, providerAtClassDomain);
      locatedProvider = null;
      locatedProvider = locator
            .getParameterProvider(Object.class, config, null);
      assertNotNull(locatedProvider);
      assertEquals(providerAtClassDomain, locatedProvider);

      // Get the class loader from default domain
      config.setProperty(classDomain, defaultName, null);
      MyParameterProvider providerAtDefaultDomain = new MyParameterProvider();
      locatedProvider = null;
      String providerName = ModuleConstants
            .getParameterProviderNameFor(Object.class);
      config.setProperty(defaultDomain, providerName, providerAtDefaultDomain);
      locatedProvider = locator
            .getParameterProvider(Object.class, config, null);
      assertNotNull(locatedProvider);
      assertEquals(providerAtDefaultDomain, locatedProvider);

      // Now save all three providers at three places
      config.setProperty(defaultDomain, defaultName, defaultProvider);
      config.setProperty(classDomain, defaultName, providerAtClassDomain);
      config.setProperty(defaultDomain, providerName, providerAtDefaultDomain);
      // We should expect the provider at class domain
      locatedProvider = locator
            .getParameterProvider(Object.class, config, null);
      assertNotNull(locatedProvider);
      assertEquals(providerAtClassDomain, locatedProvider);

      // Remove the provider from class domain
      config.setProperty(classDomain, defaultName, null);
      // We should expect the provider at default domain but with class name
      // as prefix
      locatedProvider = locator
            .getParameterProvider(Object.class, config, null);
      assertNotNull(locatedProvider);
      assertEquals(providerAtDefaultDomain, locatedProvider);

      // Clear the config
      config.setProperty(defaultDomain, defaultName, null);
      config.setProperty(classDomain, defaultName, null);
      config.setProperty(defaultDomain, providerName, null);

      // Now make the test with only the map
      Map<String, Map<String, Object>> map = new HashMap<>();
      config.setProperty(defaultDomain, defaultName, defaultProvider);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      locatedProvider = locator.getParameterProvider(Object.class, null, map);
      assertNotNull(locatedProvider);
      assertEquals(defaultProvider, locatedProvider);

      // Clear config
      config.setProperty(defaultDomain, defaultName, null);
      map.put(defaultDomain, null);
      assertNull(locator.getParameterProvider(Object.class, null, map));

      // Get the provider from class domain
      classDomain = ModuleConstants.getDefaultConfigFor(Object.class);
      config.setProperty(classDomain, defaultName, providerAtClassDomain);
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      locatedProvider = null;
      locatedProvider = locator.getParameterProvider(Object.class, null, map);
      assertNotNull(locatedProvider);
      assertEquals(providerAtClassDomain, locatedProvider);

      // Get the class provider from default domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      locatedProvider = null;
      config.setProperty(defaultDomain, providerName, providerAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      locatedProvider = locator.getParameterProvider(Object.class, null, map);
      assertNotNull(locatedProvider);
      assertEquals(providerAtDefaultDomain, locatedProvider);

      // Now save all three providers at three places
      config.setProperty(defaultDomain, defaultName, defaultProvider);
      config.setProperty(classDomain, defaultName, providerAtClassDomain);
      config.setProperty(defaultDomain, providerName, providerAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      // We should expect the provider at class domain
      locatedProvider = locator.getParameterProvider(Object.class, null, map);
      assertNotNull(locatedProvider);
      assertEquals(providerAtClassDomain, locatedProvider);

      // Remove the provider from class domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      // We should expect the provider at default domain but with class name
      // as prefix
      locatedProvider = locator.getParameterProvider(Object.class, null, map);
      assertNotNull(locatedProvider);
      assertEquals(providerAtDefaultDomain, locatedProvider);

      // Now provide an other provider for properties, and leave
      // as is the config
      MyParameterProvider otherProvider = new MyParameterProvider();
      config.setProperty(defaultDomain, providerName, otherProvider);
      // The properties map should be unchanged and the
      // locator should find that of config
      // We should expect the loader at default domain but with class name
      // as prefix. However it should be the otherLoader
      locatedProvider = locator.getParameterProvider(Object.class, config, map);
      assertNotNull(locatedProvider);
      assertEquals(otherProvider, locatedProvider);
   }

   @Test
   public void testGetPropertyInjector() {
      ConfigManager config = new DefaultConfigManager();
      // Get the default injector from default place
      MyPropertyInjector defaultInjector = new MyPropertyInjector();
      String defaultDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      String defaultName = ModuleConstants.PROPERTY_INJECTOR_PROPERTY;
      config.setProperty(defaultDomain, defaultName, defaultInjector);
      PropertyInjector locatedInjector = locator.getPropertyInjector(
            Object.class, config, null);
      assertNotNull(locatedInjector);
      assertEquals(defaultInjector, locatedInjector);

      // Clear config
      config.setProperty(defaultDomain, defaultName, null);
      assertNull(locator.getPropertyInjector(Object.class, config, null));

      // Get the injector from class domain
      String classDomain = ModuleConstants.getDefaultConfigFor(Object.class);
      MyPropertyInjector injectorAtClassDomain = new MyPropertyInjector();
      config.setProperty(classDomain, defaultName, injectorAtClassDomain);
      locatedInjector = null;
      locatedInjector = locator.getPropertyInjector(Object.class, config, null);
      assertNotNull(locatedInjector);
      assertEquals(injectorAtClassDomain, locatedInjector);

      // Get the class injector from default domain
      config.setProperty(classDomain, defaultName, null);
      MyPropertyInjector injectorAtDefaultDomain = new MyPropertyInjector();
      locatedInjector = null;
      String providerName = ModuleConstants
            .getPropertyInjectorNameFor(Object.class);
      config.setProperty(defaultDomain, providerName, injectorAtDefaultDomain);
      locatedInjector = locator.getPropertyInjector(Object.class, config, null);
      assertNotNull(locatedInjector);
      assertEquals(injectorAtDefaultDomain, locatedInjector);

      // Now save all three injectors at three places
      config.setProperty(defaultDomain, defaultName, defaultInjector);
      config.setProperty(classDomain, defaultName, injectorAtClassDomain);
      config.setProperty(defaultDomain, providerName, injectorAtDefaultDomain);
      // We should expect the injector at class domain
      locatedInjector = locator.getPropertyInjector(Object.class, config, null);
      assertNotNull(locatedInjector);
      assertEquals(injectorAtClassDomain, locatedInjector);

      // Remove the injector from class domain
      config.setProperty(classDomain, defaultName, null);
      // We should expect the injector at default domain but with class name
      // as prefix
      locatedInjector = locator.getPropertyInjector(Object.class, config, null);
      assertNotNull(locatedInjector);
      assertEquals(injectorAtDefaultDomain, locatedInjector);

      // Clear the config
      config.setProperty(defaultDomain, defaultName, null);
      config.setProperty(classDomain, defaultName, null);
      config.setProperty(defaultDomain, providerName, null);

      // Now make the test with only the map
      Map<String, Map<String, Object>> map = new HashMap<>();
      config.setProperty(defaultDomain, defaultName, defaultInjector);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      locatedInjector = locator.getPropertyInjector(Object.class, null, map);
      assertNotNull(locatedInjector);
      assertEquals(defaultInjector, locatedInjector);

      // Clear config
      config.setProperty(defaultDomain, defaultName, null);
      map.put(defaultDomain, null);
      assertNull(locator.getPropertyInjector(Object.class, null, map));

      // Get the injector from class domain
      classDomain = ModuleConstants.getDefaultConfigFor(Object.class);
      config.setProperty(classDomain, defaultName, injectorAtClassDomain);
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      locatedInjector = null;
      locatedInjector = locator.getPropertyInjector(Object.class, null, map);
      assertNotNull(locatedInjector);
      assertEquals(injectorAtClassDomain, locatedInjector);

      // Get the class injector from default domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      locatedInjector = null;
      config.setProperty(defaultDomain, providerName, injectorAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      locatedInjector = locator.getPropertyInjector(Object.class, null, map);
      assertNotNull(locatedInjector);
      assertEquals(injectorAtDefaultDomain, locatedInjector);

      // Now save all three injectors at three places
      config.setProperty(defaultDomain, defaultName, defaultInjector);
      config.setProperty(classDomain, defaultName, injectorAtClassDomain);
      config.setProperty(defaultDomain, providerName, injectorAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      // We should expect the injector at class domain
      locatedInjector = locator.getPropertyInjector(Object.class, null, map);
      assertNotNull(locatedInjector);
      assertEquals(injectorAtClassDomain, locatedInjector);

      // Remove the injector from class domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      // We should expect the provider at default domain but with class name
      // as prefix
      locatedInjector = locator.getPropertyInjector(Object.class, null, map);
      assertNotNull(locatedInjector);
      assertEquals(injectorAtDefaultDomain, locatedInjector);

      // Now provide an other injector for properties, and leave
      // as is the config
      MyPropertyInjector otherInjector = new MyPropertyInjector();
      config.setProperty(defaultDomain, providerName, otherInjector);
      // The properties map should be unchanged and the
      // locator should find that of config
      // We should expect the injector at default domain but with class name
      // as prefix. However it should be the otherInjector
      locatedInjector = locator.getPropertyInjector(Object.class, config, map);
      assertNotNull(locatedInjector);
      assertEquals(otherInjector, locatedInjector);
   }

   @Test
   public void testGetLoaderClass() {
      ConfigManager config = new DefaultConfigManager();
      Class<? extends ModuleLoader> defaultClass = DefaultModuleLoader.class;
      Class<? extends ModuleLoader> classAtDefaultDomain = MyModuleLoader.class;
      Class<? extends ModuleLoader> classAtClassDomain = ModuleLoader.class;
      String defaultDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      String defaultName = ModuleConstants
            .getPropertyNameForConfigClass(ModuleConstants.LOADER_PROPERTY);
      String className = ModuleConstants.getLoaderClassNameFor(Object.class);
      Class<?> clazz = Object.class;
      Class<? extends ModuleLoader> otherClass = new ModuleLoader() {

         @Override
         public <T> T load(Class<T> clazz) {
            // TODO Auto-generated method stub
            return null;
         }

         @Override
         public <T> T load(Class<T> clazz, Class<?> loader) {
            // TODO Auto-generated method stub
            return null;
         }

         @Override
         public <T> T load(Class<T> clazz,
               Map<String, Map<String, Object>> properties) {
            // TODO Auto-generated method stub
            return null;
         }

         @Override
         public <T> T load(Class<T> clazz, ModulePropertyLocator propertyLocator) {
            // TODO Auto-generated method stub
            return null;
         }
      }.getClass();

      // Get the default loader class
      config.setProperty(defaultDomain, defaultName, defaultClass);
      Class<? extends ModuleLoader> locatedClass = locator.getLoaderClassFor(
            clazz, config, null);
      assertNotNull(locatedClass);
      assertEquals(defaultClass, locatedClass);

      // Clear config
      config.setProperty(defaultDomain, defaultName, null);
      assertEquals(defaultClass,
            locator.getLoaderClassFor(Object.class, config, null));

      // Get the loader class from class domain
      String classDomain = ModuleConstants.getDefaultConfigFor(clazz);

      config.setProperty(classDomain, defaultName, classAtClassDomain);
      locatedClass = null;
      locatedClass = locator.getLoaderClassFor(Object.class, config, null);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Get the class from default domain
      config.setProperty(classDomain, defaultName, null);

      locatedClass = null;
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      locatedClass = locator.getLoaderClassFor(Object.class, config, null);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Now save all three classes at three places
      config.setProperty(defaultDomain, defaultName, defaultClass);
      config.setProperty(classDomain, defaultName, classAtClassDomain);
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      // We should expect the injector at class domain
      locatedClass = locator.getLoaderClassFor(Object.class, config, null);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Remove the injector from class domain
      config.setProperty(classDomain, defaultName, null);
      // We should expect the injector at default domain but with class name
      // as prefix
      locatedClass = locator.getLoaderClassFor(Object.class, config, null);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Clear the config
      config.setProperty(defaultDomain, defaultName, null);
      config.setProperty(classDomain, defaultName, null);
      config.setProperty(defaultDomain, className, null);

      // Now make the test with only the map
      Map<String, Map<String, Object>> map = new HashMap<>();
      config.setProperty(defaultDomain, defaultName, defaultClass);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      locatedClass = locator.getLoaderClassFor(Object.class, null, map);
      assertNotNull(locatedClass);
      assertEquals(defaultClass, locatedClass);

      // Clear config
      config.setProperty(defaultDomain, defaultName, null);
      map.put(defaultDomain, null);
      assertEquals(defaultClass,
            locator.getLoaderClassFor(Object.class, config, null));

      // Get the injector from class domain
      classDomain = ModuleConstants.getDefaultConfigFor(Object.class);
      config.setProperty(classDomain, defaultName, classAtClassDomain);
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      locatedClass = null;
      locatedClass = locator.getLoaderClassFor(Object.class, null, map);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Get the class injector from default domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      locatedClass = null;
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      locatedClass = locator.getLoaderClassFor(Object.class, null, map);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Now save all three injectors at three places
      config.setProperty(defaultDomain, defaultName, defaultClass);
      config.setProperty(classDomain, defaultName, classAtClassDomain);
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      // We should expect the injector at class domain
      locatedClass = locator.getLoaderClassFor(Object.class, null, map);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Remove the injector from class domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      // We should expect the provider at default domain but with class name
      // as prefix
      locatedClass = locator.getLoaderClassFor(Object.class, null, map);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Now provide an other injector for properties, and leave
      // as is the config
      config.setProperty(defaultDomain, className, otherClass);
      // The properties map should be unchanged and the
      // locator should find that of config
      // We should expect the injector at default domain but with class name
      // as prefix. However it should be the otherInjector
      locatedClass = locator.getLoaderClassFor(Object.class, config, map);
      assertNotNull(locatedClass);
      assertEquals(otherClass, locatedClass);
   }

   @Test
   public void testGetParameterProviderClass() {
      ConfigManager config = new DefaultConfigManager();
      Class<? extends ParameterProvider> defaultClass = DefaultParameterProvider.class;
      Class<? extends ParameterProvider> classAtDefaultDomain = MyParameterProvider.class;
      Class<? extends ParameterProvider> classAtClassDomain = ParameterProvider.class;
      String defaultDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      String defaultName = ModuleConstants
            .getPropertyNameForConfigClass(ModuleConstants.PARAMETER_PROVIDER_PROPERTY);
      String className = ModuleConstants
            .getParameterProviderClassNameFor(Object.class);
      Class<?> clazz = Object.class;
      Class<? extends ParameterProvider> otherClass = new ParameterProvider() {

         @Override
         public <T> T getParameter(Class<T> parameterType,
               Annotation[] annotations,
               Map<String, Map<String, Object>> properties,
               ModulePropertyLocator propertyLocator) {
            // TODO Auto-generated method stub
            return null;
         }
      }.getClass();

      // Get the default loader class
      config.setProperty(defaultDomain, defaultName, defaultClass);
      Class<? extends ParameterProvider> locatedClass = locator
            .getParameterProviderClassFor(clazz, config, null);
      assertNotNull(locatedClass);
      assertEquals(defaultClass, locatedClass);

      // Clear config
      config.setProperty(defaultDomain, defaultName, null);
      assertEquals(defaultClass,
            locator.getParameterProviderClassFor(Object.class, config, null));

      // Get the loader class from class domain
      String classDomain = ModuleConstants.getDefaultConfigFor(clazz);

      config.setProperty(classDomain, defaultName, classAtClassDomain);
      locatedClass = null;
      locatedClass = locator.getParameterProviderClassFor(Object.class, config,
            null);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Get the class from default domain
      config.setProperty(classDomain, defaultName, null);

      locatedClass = null;
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      locatedClass = locator.getParameterProviderClassFor(Object.class, config,
            null);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Now save all three classes at three places
      config.setProperty(defaultDomain, defaultName, defaultClass);
      config.setProperty(classDomain, defaultName, classAtClassDomain);
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      // We should expect the injector at class domain
      locatedClass = locator.getParameterProviderClassFor(Object.class, config,
            null);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Remove the injector from class domain
      config.setProperty(classDomain, defaultName, null);
      // We should expect the injector at default domain but with class name
      // as prefix
      locatedClass = locator.getParameterProviderClassFor(Object.class, config,
            null);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Clear the config
      config.setProperty(defaultDomain, defaultName, null);
      config.setProperty(classDomain, defaultName, null);
      config.setProperty(defaultDomain, className, null);

      // Now make the test with only the map
      Map<String, Map<String, Object>> map = new HashMap<>();
      config.setProperty(defaultDomain, defaultName, defaultClass);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      locatedClass = locator.getParameterProviderClassFor(Object.class, null,
            map);
      assertNotNull(locatedClass);
      assertEquals(defaultClass, locatedClass);

      // Clear config
      config.setProperty(defaultDomain, defaultName, null);
      map.put(defaultDomain, null);
      assertEquals(defaultClass,
            locator.getParameterProviderClassFor(Object.class, config, null));

      // Get the injector from class domain
      classDomain = ModuleConstants.getDefaultConfigFor(Object.class);
      config.setProperty(classDomain, defaultName, classAtClassDomain);
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      locatedClass = null;
      locatedClass = locator.getParameterProviderClassFor(Object.class, null,
            map);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Get the class injector from default domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      locatedClass = null;
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      locatedClass = locator.getParameterProviderClassFor(Object.class, null,
            map);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Now save all three injectors at three places
      config.setProperty(defaultDomain, defaultName, defaultClass);
      config.setProperty(classDomain, defaultName, classAtClassDomain);
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      // We should expect the injector at class domain
      locatedClass = locator.getParameterProviderClassFor(Object.class, null,
            map);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Remove the injector from class domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      // We should expect the provider at default domain but with class name
      // as prefix
      locatedClass = locator.getParameterProviderClassFor(Object.class, null,
            map);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Now provide an other injector for properties, and leave
      // as is the config
      config.setProperty(defaultDomain, className, otherClass);
      // The properties map should be unchanged and the
      // locator should find that of config
      // We should expect the injector at default domain but with class name
      // as prefix. However it should be the otherInjector
      locatedClass = locator.getParameterProviderClassFor(Object.class, config,
            map);
      assertNotNull(locatedClass);
      assertEquals(otherClass, locatedClass);
   }

   @Test
   public void testGetParameterProviderClassReverted() {
      ConfigManager config = new DefaultConfigManager();
      Class<? extends ParameterProvider> configClass = DefaultParameterProvider.class;
      Class<? extends ParameterProvider> mapClass = MyParameterProvider.class;

      String defaultDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      String defaultName = ModuleConstants
            .getPropertyNameForConfigClass(ModuleConstants.PARAMETER_PROVIDER_PROPERTY);

      Class<?> clazz = Object.class;

      // Get the default loader class
      config.setProperty(defaultDomain, defaultName, configClass);
      Class<? extends ParameterProvider> locatedClass = locator
            .getParameterProviderClassFor(clazz, config, null);
      assertNotNull(locatedClass);
      assertEquals(configClass, locatedClass);

      Map<String, Map<String, Object>> properties = new HashMap<>();
      Map<String, Object> map = config.getDomain(defaultDomain).getProperties();
      properties.put(defaultDomain, map);
      map.put(defaultName, mapClass);
      FastDynamicModulePropertyLocator revertedLocator = new FastDynamicModulePropertyLocator(
            properties);
      locatedClass = revertedLocator.getParameterProviderClassFor(Object.class,
            config, properties);
      assertEquals(mapClass, locatedClass);
   }

   @Test
   public void testGetPropertyInjectorClass() {
      ConfigManager config = new DefaultConfigManager();
      Class<? extends PropertyInjector> defaultClass = DefaultPropertyInjector.class;
      Class<? extends PropertyInjector> classAtDefaultDomain = MyPropertyInjector.class;
      Class<? extends PropertyInjector> classAtClassDomain = PropertyInjector.class;
      String defaultDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      String defaultName = ModuleConstants
            .getPropertyNameForConfigClass(ModuleConstants.PROPERTY_INJECTOR_PROPERTY);
      String className = ModuleConstants
            .getPropertyInjectorClassNameFor(Object.class);
      Class<?> clazz = Object.class;
      Class<? extends PropertyInjector> otherClass = new PropertyInjector() {

         @Override
         public void injectProperties(Object bean) {
            // TODO Auto-generated method stub

         }

         @Override
         public void injectProperties(Object bean,
               Map<String, Map<String, Object>> properties) {
            // TODO Auto-generated method stub

         }

         @Override
         public void injectProperties(Object bean,
               ModulePropertyLocator propertyLocator) {
            // TODO Auto-generated method stub

         }
      }.getClass();

      // Get the default loader class
      config.setProperty(defaultDomain, defaultName, defaultClass);
      Class<? extends PropertyInjector> locatedClass = locator
            .getPropertyInjectorClassFor(clazz, config, null);
      assertNotNull(locatedClass);
      assertEquals(defaultClass, locatedClass);

      // Clear config
      config.setProperty(defaultDomain, defaultName, null);
      assertEquals(defaultClass,
            locator.getPropertyInjectorClassFor(Object.class, config, null));

      // Get the loader class from class domain
      String classDomain = ModuleConstants.getDefaultConfigFor(clazz);

      config.setProperty(classDomain, defaultName, classAtClassDomain);
      locatedClass = null;
      locatedClass = locator.getPropertyInjectorClassFor(Object.class, config,
            null);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Get the class from default domain
      config.setProperty(classDomain, defaultName, null);

      locatedClass = null;
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      locatedClass = locator.getPropertyInjectorClassFor(Object.class, config,
            null);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Now save all three classes at three places
      config.setProperty(defaultDomain, defaultName, defaultClass);
      config.setProperty(classDomain, defaultName, classAtClassDomain);
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      // We should expect the injector at class domain
      locatedClass = locator.getPropertyInjectorClassFor(Object.class, config,
            null);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Remove the injector from class domain
      config.setProperty(classDomain, defaultName, null);
      // We should expect the injector at default domain but with class name
      // as prefix
      locatedClass = locator.getPropertyInjectorClassFor(Object.class, config,
            null);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Clear the config
      config.setProperty(defaultDomain, defaultName, null);
      config.setProperty(classDomain, defaultName, null);
      config.setProperty(defaultDomain, className, null);

      // Now make the test with only the map
      Map<String, Map<String, Object>> map = new HashMap<>();
      config.setProperty(defaultDomain, defaultName, defaultClass);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      locatedClass = locator.getPropertyInjectorClassFor(Object.class, null,
            map);
      assertNotNull(locatedClass);
      assertEquals(defaultClass, locatedClass);

      // Clear config
      config.setProperty(defaultDomain, defaultName, null);
      map.put(defaultDomain, null);
      assertEquals(defaultClass,
            locator.getPropertyInjectorClassFor(Object.class, config, null));

      // Get the injector from class domain
      classDomain = ModuleConstants.getDefaultConfigFor(Object.class);
      config.setProperty(classDomain, defaultName, classAtClassDomain);
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      locatedClass = null;
      locatedClass = locator.getPropertyInjectorClassFor(Object.class, null,
            map);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Get the class injector from default domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      locatedClass = null;
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      locatedClass = locator.getPropertyInjectorClassFor(Object.class, null,
            map);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Now save all three injectors at three places
      config.setProperty(defaultDomain, defaultName, defaultClass);
      config.setProperty(classDomain, defaultName, classAtClassDomain);
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      // We should expect the injector at class domain
      locatedClass = locator.getPropertyInjectorClassFor(Object.class, null,
            map);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Remove the injector from class domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      // We should expect the provider at default domain but with class name
      // as prefix
      locatedClass = locator.getPropertyInjectorClassFor(Object.class, null,
            map);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Now provide an other injector for properties, and leave
      // as is the config
      config.setProperty(defaultDomain, className, otherClass);
      // The properties map should be unchanged and the
      // locator should find that of config
      // We should expect the injector at default domain but with class name
      // as prefix. However it should be the otherInjector
      locatedClass = locator.getPropertyInjectorClassFor(Object.class, config,
            map);
      assertNotNull(locatedClass);
      assertEquals(otherClass, locatedClass);
   }

   @Test
   public void testGetProvider() {
      ConfigManager config = new DefaultConfigManager();
      MyParameterProvider providerAtDefaultDomain = new MyParameterProvider();
      DefaultParameterProvider providerAtClassDomain = new DefaultParameterProvider(
            null, null);
      String defaultDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      String defaultName = ModuleConstants.PROVIDER_PROPERTY;
      Class<?> clazz = Object.class;
      String providerName = ModuleConstants.getProviderNameFor(clazz);
      Object otherProvider = new Object();
      Object locatedProvider = null;

      // Get the loader class from class domain
      String classDomain = ModuleConstants.getDefaultConfigFor(clazz);

      config.setProperty(classDomain, defaultName, providerAtClassDomain);
      locatedProvider = null;
      locatedProvider = locator.getModuleProvider(clazz, Object.class, config,
            null);
      assertNotNull(locatedProvider);
      assertEquals(providerAtClassDomain, locatedProvider);

      // Get the class from default domain
      config.setProperty(classDomain, defaultName, null);

      locatedProvider = null;
      config.setProperty(defaultDomain, providerName, providerAtDefaultDomain);
      locatedProvider = locator.getModuleProvider(clazz, Object.class, config,
            null);
      assertNotNull(locatedProvider);
      assertEquals(providerAtDefaultDomain, locatedProvider);

      // Now save all three classes at three places
      config.setProperty(classDomain, defaultName, providerAtClassDomain);
      config.setProperty(defaultDomain, providerName, providerAtDefaultDomain);
      // We should expect the injector at class domain
      locatedProvider = locator.getModuleProvider(clazz, Object.class, config,
            null);
      assertNotNull(locatedProvider);
      assertEquals(providerAtClassDomain, locatedProvider);

      // Remove the injector from class domain
      config.setProperty(classDomain, defaultName, null);
      // We should expect the injector at default domain but with class name
      // as prefix
      locatedProvider = locator.getModuleProvider(clazz, Object.class, config,
            null);
      assertNotNull(locatedProvider);
      assertEquals(providerAtDefaultDomain, locatedProvider);

      // Clear the config
      config.setProperty(classDomain, defaultName, null);
      config.setProperty(defaultDomain, providerName, null);

      // Now make the test with only the map
      Map<String, Map<String, Object>> map = new HashMap<>();

      // Clear config
      config.setProperty(defaultDomain, defaultName, null);
      map.put(defaultDomain, null);
      assertNull(locator.getModuleProvider(clazz, Object.class, config, null));

      // Get the injector from class domain
      classDomain = ModuleConstants.getDefaultConfigFor(Object.class);
      config.setProperty(classDomain, defaultName, providerAtClassDomain);
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      locatedProvider = null;
      locatedProvider = locator.getModuleProvider(clazz, Object.class, null,
            map);
      assertNotNull(locatedProvider);
      assertEquals(providerAtClassDomain, locatedProvider);

      // Get the class injector from default domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      locatedProvider = null;
      config.setProperty(defaultDomain, providerName, providerAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      locatedProvider = locator.getModuleProvider(clazz, Object.class, null,
            map);
      assertNotNull(locatedProvider);
      assertEquals(providerAtDefaultDomain, locatedProvider);

      // Now save all three injectors at three places
      config.setProperty(classDomain, defaultName, providerAtClassDomain);
      config.setProperty(defaultDomain, providerName, providerAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      // We should expect the injector at class domain
      locatedProvider = locator.getModuleProvider(clazz, Object.class, null,
            map);
      assertNotNull(locatedProvider);
      assertEquals(providerAtClassDomain, locatedProvider);

      // Remove the injector from class domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      // We should expect the provider at default domain but with class name
      // as prefix
      locatedProvider = locator.getModuleProvider(clazz, Object.class, null,
            map);
      assertNotNull(locatedProvider);
      assertEquals(providerAtDefaultDomain, locatedProvider);

      // Now provide an other injector for properties, and leave
      // as is the config
      config.setProperty(defaultDomain, providerName, otherProvider);
      // The properties map should be unchanged and the
      // locator should find that of config
      // We should expect the injector at default domain but with class name
      // as prefix. However it should be the otherInjector
      locatedProvider = locator.getModuleProvider(clazz, Object.class, config,
            map);
      assertNotNull(locatedProvider);
      assertEquals(otherProvider, locatedProvider);
   }

   @Test
   public void testGetProviderClass() {
      ConfigManager config = new DefaultConfigManager();
      Class<?> classAtDefaultDomain = MyParameterProvider.class;
      Class<?> classAtClassDomain = ParameterProvider.class;
      String defaultDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      String defaultName = ModuleConstants
            .getPropertyNameForConfigClass(ModuleConstants.PROVIDER_PROPERTY);
      Class<?> clazz = Object.class;
      String className = ModuleConstants.getProviderClassNameFor(clazz);
      Class<?> otherClass = DefaultParameterProvider.class;
      Class<?> locatedClass = null;

      // Get the loader class from class domain
      String classDomain = ModuleConstants.getDefaultConfigFor(clazz);

      config.setProperty(classDomain, defaultName, classAtClassDomain);
      locatedClass = null;
      locatedClass = locator.getModuleProviderClassFor(Object.class, config,
            null);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Get the class from default domain
      config.setProperty(classDomain, defaultName, null);

      locatedClass = null;
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      locatedClass = locator.getModuleProviderClassFor(Object.class, config,
            null);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Now save all three classes at three places
      config.setProperty(classDomain, defaultName, classAtClassDomain);
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      // We should expect the injector at class domain
      locatedClass = locator.getModuleProviderClassFor(Object.class, config,
            null);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Remove the injector from class domain
      config.setProperty(classDomain, defaultName, null);
      // We should expect the injector at default domain but with class name
      // as prefix
      locatedClass = locator.getModuleProviderClassFor(Object.class, config,
            null);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Clear the config
      config.setProperty(classDomain, defaultName, null);
      config.setProperty(defaultDomain, className, null);

      // Now make the test with only the map
      Map<String, Map<String, Object>> map = new HashMap<>();

      // Clear config
      config.setProperty(defaultDomain, defaultName, null);
      map.put(defaultDomain, null);
      assertNull(locator.getModuleProviderClassFor(Object.class, config, null));

      // Get the injector from class domain
      classDomain = ModuleConstants.getDefaultConfigFor(Object.class);
      config.setProperty(classDomain, defaultName, classAtClassDomain);
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      locatedClass = null;
      locatedClass = locator.getModuleProviderClassFor(Object.class, null, map);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Get the class injector from default domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      locatedClass = null;
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      locatedClass = locator.getModuleProviderClassFor(Object.class, null, map);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Now save all three injectors at three places
      config.setProperty(classDomain, defaultName, classAtClassDomain);
      config.setProperty(defaultDomain, className, classAtDefaultDomain);
      map.put(defaultDomain, config.getDomain(defaultDomain).getProperties());
      map.put(classDomain, config.getDomain(classDomain).getProperties());
      // We should expect the injector at class domain
      locatedClass = locator.getModuleProviderClassFor(Object.class, null, map);
      assertNotNull(locatedClass);
      assertEquals(classAtClassDomain, locatedClass);

      // Remove the injector from class domain
      config.setProperty(classDomain, defaultName, null);
      map.put(classDomain, null);
      // We should expect the provider at default domain but with class name
      // as prefix
      locatedClass = locator.getModuleProviderClassFor(Object.class, null, map);
      assertNotNull(locatedClass);
      assertEquals(classAtDefaultDomain, locatedClass);

      // Now provide an other injector for properties, and leave
      // as is the config
      config.setProperty(defaultDomain, className, otherClass);
      // The properties map should be unchanged and the
      // locator should find that of config
      // We should expect the injector at default domain but with class name
      // as prefix. However it should be the otherInjector
      locatedClass = locator.getModuleProviderClassFor(Object.class, config,
            map);
      assertNotNull(locatedClass);
      assertEquals(otherClass, locatedClass);
   }
}
