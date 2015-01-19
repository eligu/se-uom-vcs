package gr.uom.se.util.module;

import gr.uom.se.util.module.annotations.ProvideModule;
import gr.uom.se.util.module.annotations.Property;

public class PersonLoader {
   
   public PersonLoader() {
      
   }

   @ProvideModule
   public PersonMock createPerson(
         @Property(
               domain = PersonDefaults.PERSON_DOMAIN, 
               name = "name", 
               stringVal = PersonDefaults.PERSON_NAME_LOADER) 
         String name,
         @Property(
               domain = PersonDefaults.PERSON_DOMAIN, 
               name = "age", 
               stringVal = PersonDefaults.PERSON_AGE_LOADER) 
         int age) {
      return new PersonMock(name, age);
   }
}