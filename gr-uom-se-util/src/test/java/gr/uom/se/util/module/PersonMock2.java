/**
 * 
 */
package gr.uom.se.util.module;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class PersonMock2 {

   private String name;
   private int age;
   
   public PersonMock2() {
      this.name = PersonDefaults.PERSON_NAME_DEFAULT_CONSTRUCTOR;
      this.age = Integer.parseInt(PersonDefaults.PERSON_AGE_DEFAULT_CONSTRUCTOR);
   }
   
   public String getName() {
      return name;
   }
   
   public int getAge() {
      return age;
   }

   public void setName(String name) {
      this.name = name;
   }
   public void setAge(int age) {
      this.age = age;
   }
}
