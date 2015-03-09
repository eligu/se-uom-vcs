/**
 * 
 */
package gr.uom.se.vcs.analysis.version;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSChange;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSFileDiff;
import gr.uom.se.vcs.VCSFile;
import gr.uom.se.vcs.analysis.util.CommitEdits;
import gr.uom.se.vcs.analysis.util.KeyValueProcessor;
import gr.uom.se.vcs.analysis.version.provider.ConnectedVersionProvider;
import gr.uom.se.vcs.walker.filter.VCSFilter;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class FirstAndSecondChangeCounter extends
      KeyValueProcessor<CommitEdits, String, AtomicInteger> {

   private final Set<VCSChange.Type> types1;
   private final Set<VCSChange.Type> types2;
   private final VCSFilter<VCSFileDiff<?>> changeFilter1;
   private final VCSFilter<VCSFile> resourceFilter1;
   private final VCSFilter<VCSFileDiff<?>> changeFilter2;
   private final VCSFilter<VCSFile> resourceFilter2;
   private final ConnectedVersionProvider provider;
   private final boolean changed;

   /**
    * @param provider
    * @param id
    * @param changeFilter
    * @param resourceFilter
    * @param types
    */
   public FirstAndSecondChangeCounter(ConnectedVersionProvider provider, String id,
         boolean changed, Set<VCSChange.Type> types1,
         VCSFilter<VCSFileDiff<?>> changeFilter1,
         VCSFilter<VCSFile> resourceFilter1, Set<VCSChange.Type> types2,
         VCSFilter<VCSFileDiff<?>> changeFilter2,
         VCSFilter<VCSFile> resourceFilter2) {
      super(id);
      ArgsCheck.notNull("provider", provider);
      if (types1 != null) {
         ArgsCheck.notEmpty("types1", types1);
         this.types1 = new HashSet<VCSChange.Type>();
         this.types1.addAll(types1);
      } else {
         this.types1 = null;
      }
      if (types2 != null) {
         ArgsCheck.notEmpty("types2", types2);
         this.types2 = new HashSet<VCSChange.Type>();
         this.types2.addAll(types2);
      } else {
         this.types2 = null;
      }
      this.changeFilter1 = changeFilter1;
      this.resourceFilter1 = resourceFilter1;
      this.changeFilter2 = changeFilter2;
      this.resourceFilter2 = resourceFilter2;
      this.provider = provider;
      this.changed = changed;
   }

   @Override
   protected boolean processThis(CommitEdits entity) {
      VCSCommit commit = entity.getNewCommit();
      if (provider.isVersion(commit)
            && entity.getOldCommit().equals(provider.getPrevious(commit))) {
         return true;
      }

      int counter1 = 0;
      if (types1 == null) {
         counter1 = entity.getNumberOfFilesWithChange(null, resourceFilter1,
               changeFilter1);
      } else {
         for (VCSChange.Type type : types1) {
            counter1 += entity.getNumberOfFilesWithChange(type,
                  resourceFilter1, changeFilter1);
            if (counter1 > 0) {
               break;
            }
         }
      }

      // The first set of changed is not performed anyway
      if (counter1 == 0) {
         return true;
      }

      int counter2 = 0;
      if (types2 == null) {
         counter2 = entity.getNumberOfFilesWithChange(null, resourceFilter2,
               changeFilter2);
      } else {
         for (VCSChange.Type type : types2) {
            counter2 += entity.getNumberOfFilesWithChange(type,
                  resourceFilter2, changeFilter2);
            if (counter2 > 0) {
               break;
            }
         }
      }

      // If the same change is required from second set of files
      if (changed) {
         // The second set of files performed the same change
         if (counter2 > 0) {
            String ver = provider.findVersion(commit);
            values.get(ver).incrementAndGet();
         }
      } else {
         // The second set of changes must not be performed while the first one
         if (counter2 == 0) {
            String ver = provider.findVersion(commit);
            values.get(ver).incrementAndGet();
         }
      }

      return true;
   }

   @Override
   protected void startThis() {
      for (String ver : provider.getNames()) {
         AtomicInteger counter = values.get(ver);
         if (counter == null) {
            counter = new AtomicInteger(0);
         } else {
            counter.set(0);
         }
         values.put(ver, counter);
      }
   }
}
