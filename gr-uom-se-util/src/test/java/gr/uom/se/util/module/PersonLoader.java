package gr.uom.se.util.module;

import gr.uom.se.util.module.annotations.ProvideModule;
import gr.uom.se.util.module.annotations.Property;

public class PersonLoader {
   
   static int counter = 0;
   
   public PersonLoader() {
      counter++;
   }

   @ProvideModule
   public PersonMock1 createPerson1(
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
      return new PersonMock1(name, age);
   }
   
   @ProvideModule
   public PersonMock2 createPerson2(
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
      PersonMock2 person = new PersonMock2();
      person.setName(name);
      person.setAge(age);
      return person;
   }
}