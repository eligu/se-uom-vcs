/**
 * 
 */
package gr.uom.se.vcs.analysis.version;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSChange;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSFile;
import gr.uom.se.vcs.VCSFileDiff;
import gr.uom.se.vcs.analysis.util.CommitEdits;
import gr.uom.se.vcs.analysis.util.KeyValueProcessor;
import gr.uom.se.vcs.walker.filter.VCSFilter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class VersionLinesCounterProcessor extends
      KeyValueProcessor<CommitEdits, String, Integer> {

   private final boolean newLines;
   private final VersionProvider provider;
   private final VCSChange.Type[] edits;
   private final VCSFilter<VCSFileDiff<?>> changeFilter;
   private final VCSFilter<VCSFile> resourceFilter;

   public VersionLinesCounterProcessor(VersionProvider provider, String id,
         boolean newLines, VCSChange.Type... types) {
      this(provider, id, newLines, null, null, types);
   }

   public VersionLinesCounterProcessor(VersionProvider provider, String id,
         boolean newLines, VCSFilter<VCSFileDiff<?>> changeFilter,
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
      this.newLines = newLines;
   }

   @Override
   protected boolean processThis(CommitEdits entity) {
      VCSCommit commit = entity.getNewCommit();
      if (provider.isVersion(commit) && entity.getOldCommit().equals(provider.getPrevious(commit))) {
         // Collect first the changes we want
         Collection<VCSFileDiff<?>> changes = null;
         if (edits == null) {
            changes = entity.getFileChanges();
         } else {
            changes = new ArrayList<VCSFileDiff<?>>();
            for (VCSChange.Type type : edits) {
               changes.addAll(entity.getChangesOf(type));
            }
         }
         
         int lines = 0;
         if(newLines) {
            lines = CommitEdits.getNewLines(changes, resourceFilter, changeFilter);
         } else {
            lines = CommitEdits.getOldLines(changes, resourceFilter, changeFilter);
         }
         values.put(provider.getName(commit), lines);
      }
      return true;
   }
}
