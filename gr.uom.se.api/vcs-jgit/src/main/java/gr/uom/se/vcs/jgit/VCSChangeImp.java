package gr.uom.se.vcs.jgit;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSChange;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSChange.Type;

/**
 * Implementation of {@link VCSChange} based on JGit library.
 * <p>
 * 
 * This is an immutable object and is considered thread safe.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @param <T>
 *           the parameter must be of type {@link VCSResourceImp}
 * @see VCSChange
 */
public class VCSChangeImp<T extends VCSResourceImp> implements VCSChange<T> {

   /**
    * The new resource this change refers to.
    * <p>
    * 
    * May be null if the change type is {@link Type#DELETED}
    */
   protected T newResource;

   /**
    * The old resource this change refers to.
    * <p>
    * 
    * May be null if change type is {@link Type#ADDED}
    */
   protected T oldResource;

   /**
    * The type of change.
    * <p>
    */
   protected VCSChange.Type type;

   /**
    * Creates a new instance.
    * <p>
    * 
    * If the change is of type {@link Type#ADDED} the old resource may be null.
    * If the change is of type {@link Type#DELETED} the new resource may be
    * null. Otherwise both resources must not be null. The type is not allowed
    * to be null.
    * 
    * @param newR
    *           new resource
    * @param oldR
    *           old resource
    * @param type
    *           of change
    */
   public VCSChangeImp(final T newR, final T oldR, final VCSChange.Type type) {

      ArgsCheck.notNull("type", type);

      // If this is an addition then we need to check only the new resource for
      // null reference
      // If this is a deletion we need to check only the old resource
      // If no one of the above, we need to check both resources
      // We must ensure both resources come from same repository
      if (type.isAdd()) {

         ArgsCheck.notNull("newR", newR);

      } else if (type.isDelete()) {

         ArgsCheck.notNull("oldR", oldR);

      } else {

         ArgsCheck.notNull("newR", newR);
         ArgsCheck.notNull("oldR", oldR);

         if (!newR.commit.repo.equals(oldR.commit.repo)) {

            throw new IllegalArgumentException(
                  "It seems that repository of old resource is not equal to that of new resource");
         }
      }

      this.newResource = newR;
      this.oldResource = oldR;
      this.type = type;
   }

   /**
    * {@inheritDoc}
    * 
    * @see VCSChange#getNewResource()
    */
   @Override
   public T getNewResource() {
      return this.newResource;
   }

   /**
    * {@inheritDoc}
    * 
    * @see VCSChange#getOldResource()
    */
   @Override
   public T getOldResource() {
      return this.oldResource;
   }

   /**
    * {@inheritDoc}
    * 
    * @see VCSChange#getNewCommit()
    */
   @Override
   public VCSCommit getNewCommit() {
      // new resource may be null if this is a deletion
      if (this.newResource != null) {
         return this.newResource.getCommit();
      }
      return null;
   }

   /**
    * {@inheritDoc}
    * 
    * @see VCSChange#getOldCommit()
    */
   @Override
   public VCSCommit getOldCommit() {
      // old resource may be null if this is an addition
      if (this.oldResource != null) {
         return this.oldResource.getCommit();
      }
      return null;
   }

   /**
    * {@inheritDoc}
    * 
    * @see VCSChange#getType()
    */
   @Override
   public VCSChange.Type getType() {
      return this.type;
   }

   @Override
   public String toString() {
      return this.type + ":" + this.oldResource + ":" + this.newResource;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
            + ((newResource == null) ? 0 : newResource.hashCode());
      result = prime * result
            + ((oldResource == null) ? 0 : oldResource.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
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
      VCSChangeImp<?> other = (VCSChangeImp<?>) obj;
      if (newResource == null) {
         if (other.newResource != null)
            return false;
      } else if (!newResource.equals(other.newResource))
         return false;
      if (oldResource == null) {
         if (other.oldResource != null)
            return false;
      } else if (!oldResource.equals(other.oldResource))
         return false;
      if (type != other.type)
         return false;
      return true;
   }

}
