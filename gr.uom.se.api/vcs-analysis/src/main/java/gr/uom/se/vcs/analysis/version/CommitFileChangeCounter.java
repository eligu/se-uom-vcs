package gr.uom.se.vcs.analysis.version;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSChange;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSFile;
import gr.uom.se.vcs.VCSFileDiff;
import gr.uom.se.vcs.analysis.util.CommitEdits;
import gr.uom.se.vcs.analysis.util.KeyValueProcessor;
import gr.uom.se.vcs.analysis.version.provider.ConnectedVersionProvider;
import gr.uom.se.vcs.walker.filter.VCSFilter;

import java.util.concurrent.atomic.AtomicInteger;

public class CommitFileChangeCounter extends
      KeyValueProcessor<CommitEdits, String, AtomicInteger> {

   private final ConnectedVersionProvider provider;
   private final VCSChange.Type[] edits;
   private final VCSFilter<VCSFileDiff<?>> changeFilter;
   private final VCSFilter<VCSFile> resourceFilter;

   public CommitFileChangeCounter(ConnectedVersionProvider provider, String id,
         VCSChange.Type... types) {
      this(provider, id, null, null, types);
   }

   public CommitFileChangeCounter(ConnectedVersionProvider provider, String id,
         VCSFilter<VCSFileDiff<?>> changeFilter,
         VCSFilter<VCSFile> resourceFilter, VCSChange.Type... types) {
      super(id);
      ArgsCheck.notNull("provider", provider);
      if (types != null) {
         ArgsCheck.containsNoNull("tpes", (Object[]) types);
      } else {
         types = VCSChange.Type.values();

      }
      this.provider = provider;
      this.edits = types;
      this.changeFilter = changeFilter;
      this.resourceFilter = resourceFilter;
   }

   @Override
   protected boolean processThis(CommitEdits entity) {
      VCSCommit commit = entity.getNewCommit();
      if (provider.isVersion(commit)
            && entity.getOldCommit().equals(provider.getPrevious(commit))) {
         return true;
      }

      int counter = 0;
      if (edits == null) {
         counter = entity.getNumberOfFilesWithChange(null, resourceFilter,
               changeFilter);
      } else {
         for (VCSChange.Type type : edits) {
            counter += entity.getNumberOfFilesWithChange(type, resourceFilter,
                  changeFilter);
            if(counter > 0) {
               break;
            }
         }
      }
      
      if (counter > 0) {
         String ver = provider.findVersion(commit);
         values.get(ver).incrementAndGet();
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
