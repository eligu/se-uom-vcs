/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.module.annotations.LoadModule;
import gr.uom.se.util.module.annotations.Module;
import gr.uom.se.util.module.annotations.Property;

/**
 * @author Elvis Ligu
 * 
 */
@Module(properties = {
      @Property(
            domain = PersonDefaults.PERSON_DOMAIN, 
            name = "name", 
            stringVal = PersonDefaults.PERSON_NAME_MODULE),
      @Property(
            domain = PersonDefaults.PERSON_DOMAIN, 
            name = "age", 
            stringVal = PersonDefaults.PERSON_AGE_MODULE) }, 
      loader = PersonLoader.class)
public class PersonMock {
   
   String name;
   int age;

   public PersonMock() {
      this.name = PersonDefaults.PERSON_NAME_CONSTRUCTOR;
      this.age = Integer.parseInt(PersonDefaults.PERSON_AGE_CONSTRUCTOR);
   }

   @LoadModule
   public PersonMock(
         @Property(
               domain = PersonDefaults.PERSON_DOMAIN, 
               name = "name", 
               stringVal = PersonDefaults.PERSON_NAME_CONSTRUCTOR) 
         String name,
         @Property(
               domain = PersonDefaults.PERSON_DOMAIN, 
               name = "age", 
               stringVal = PersonDefaults.PERSON_AGE_CONSTRUCTOR) 
         int age) {
      this.name = name;
      this.age = age;
   }

   @LoadModule
   public static PersonMock newPerson(
         @Property(
               domain = PersonDefaults.PERSON_DOMAIN, 
               name = "name", 
               stringVal = PersonDefaults.PERSON_NAME_STATIC_METHOD) 
         String name,
         @Property(
               domain = PersonDefaults.PERSON_DOMAIN, 
               name = "age", 
               stringVal = PersonDefaults.PERSON_AGE_STATIC_METHOD) 
         int age) {
      PersonMock person = new PersonMock();
      person.name = name;
      person.age = age;
      return person;
   }

   @Override
   public String toString() {
      return "Name: " + name + "\n" + "Age: " + age;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + age;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PersonMock other = (PersonMock) obj;
      if (age != other.age)
         return false;
      if (name == null) {
         if (other.name != null)
            return false;
      } else if (!name.equals(other.name))
         return false;
      return true;
   }
}
