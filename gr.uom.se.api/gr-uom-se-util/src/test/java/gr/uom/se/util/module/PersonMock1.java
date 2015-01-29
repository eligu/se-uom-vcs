/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.module.annotations.Module;
import gr.uom.se.util.module.annotations.NULLVal;
import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.module.annotations.ProvideModule;

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
      provider = PersonLoader.class)
public class PersonMock1 {
   
   @Property(
         domain = PersonDefaults.PERSON_DOMAIN, 
         name = "name", 
         stringVal = PersonDefaults.PERSON_NAME_INJECTED)
   private String name;
   
   @Property(
         domain = PersonDefaults.PERSON_DOMAIN, 
         name = "age", 
         stringVal = PersonDefaults.PERSON_AGE_INJECTED)
   private int age;

   @Property(
         domain = PersonDefaults.PERSON_DOMAIN, 
         name = "address", 
         stringVal = NULLVal.NULL_STR)
   private String address;
   
   @Property(
         domain = PersonDefaults.PERSON_DOMAIN, 
         name = "partner", 
         stringVal = NULLVal.LOAD_STR)
   private PersonMock2 partner;
   
   public PersonMock1() {
      this.name = PersonDefaults.PERSON_NAME_CONSTRUCTOR;
      this.age = Integer.parseInt(PersonDefaults.PERSON_AGE_CONSTRUCTOR);
   }

   @ProvideModule
   public PersonMock1(
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

   @ProvideModule
   public static PersonMock1 newPerson(
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
      PersonMock1 person = new PersonMock1();
      person.name = name;
      person.age = age;
      return person;
   }

   public int getAge() {
      return age;
   }
   public String getName() {
      return name;
   }
   public String getAddress() {
      return address;
   }
   public PersonMock2 getPartner() {
      return partner;
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
      PersonMock1 other = (PersonMock1) obj;
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