/**
 * 
 */
package se.uom.vcs.jgit.walker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.OrTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import se.uom.vcs.VCSResource;
import se.uom.vcs.walker.filter.resource.ChildFilter;
import se.uom.vcs.walker.filter.resource.ModifiedFilter;
import se.uom.vcs.walker.filter.resource.PathPrefixFilter;
import se.uom.vcs.walker.filter.resource.SuffixFilter;
import se.uom.vcs.walker.filter.resource.VCSResourceAndFilter;
import se.uom.vcs.walker.filter.resource.VCSResourceFilter;
import se.uom.vcs.walker.filter.resource.VCSResourceNotFilter;
import se.uom.vcs.walker.filter.resource.VCSResourceOrFilter;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ResourceFilter {

    public OptimizedResourceFilter<VCSResource> and(
	    Collection<OptimizedResourceFilter<VCSResource>> filters) {
	List<TreeFilter> revFilters = new ArrayList<TreeFilter>();
	for (OptimizedResourceFilter<?> f : filters) {
	    revFilters.add(f.getCurrent());
	}
	return new OptimizedResourceFilter<VCSResource>(
		AndTreeFilter.create(revFilters));
    }

    public OptimizedResourceFilter<VCSResource> not(OptimizedResourceFilter<?> filter) {
	return new OptimizedResourceFilter<VCSResource>(filter.getCurrent()
		.negate());
    }

    public OptimizedResourceFilter<VCSResource> or(
	    Collection<OptimizedResourceFilter<VCSResource>> filters) {
	List<TreeFilter> revFilters = new ArrayList<TreeFilter>();
	for (OptimizedResourceFilter<?> f : filters) {
	    revFilters.add(f.getCurrent());
	}
	return new OptimizedResourceFilter<VCSResource>(
		OrTreeFilter.create(revFilters));
    }

    public OptimizedResourceFilter<VCSResource> pathPrefix(
	    Collection<String> patterns) {
	return new OptimizedResourceFilter<VCSResource>(
		PathFilterGroup.createFromStrings(patterns));
    }

    private static void checkStrings(Collection<String> patterns) {
	if (patterns == null) {
	    throw new IllegalArgumentException("patterns must not be null");
	}
	if (patterns.isEmpty()) {
	    throw new IllegalArgumentException("patterns must not be empty");
	}
	for (String str : patterns) {
	    if (str == null) {
		throw new IllegalArgumentException(
			"patterns must not contain null");
	    } else if (str.trim().isEmpty()) {
		throw new IllegalArgumentException(
			"patterns must not contain empty string");
	    }
	}
    }

    public OptimizedResourceFilter<VCSResource> name(Collection<String> patterns) {

	checkStrings(patterns);

	List<TreeFilter> filters = new ArrayList<TreeFilter>();
	TreeFilter filter = null;
	for (String p : patterns) {
	    filters.add(PathSuffixFilter.create(p));
	}
	if (filters.size() == 1) {
	    filter = filters.get(0);
	} else {
	    filter = AndTreeFilter.create(filters);
	}
	return new OptimizedResourceFilter<VCSResource>(filter);
    }

    public OptimizedResourceFilter<VCSResource> modification() {
	return new OptimizedResourceFilter<VCSResource>(TreeFilter.ANY_DIFF);
    }

    public OptimizedResourceFilter<VCSResource> parse(
	    VCSResourceFilter<VCSResource> filter, 
	    FilterParser<VCSResource, OptimizedResourceFilter<VCSResource>> parser) {

	if(parser != null) {
	    OptimizedResourceFilter<VCSResource> f = parser.parse(filter);
	    if(f != null) {
		return f;
	    }
	}
	
	// Case when filter is a path prefix filter
	if (PathPrefixFilter.class.isAssignableFrom(filter.getClass())) {
	    return this.parsePrefix((PathPrefixFilter<?>) filter);
	}

	// Case when filter is a name filter
	if (SuffixFilter.class.isAssignableFrom(filter.getClass())) {
	    return this.parseName((SuffixFilter<VCSResource>) filter);
	}

	// Case when filter is AND
	if (VCSResourceAndFilter.class.isAssignableFrom(filter.getClass())) {
	    return this.parseAnd((VCSResourceAndFilter<VCSResource>) filter, parser);
	}

	// Case when filter is OR
	if (VCSResourceOrFilter.class.isAssignableFrom(filter.getClass())) {
	    return this.parseOr((VCSResourceOrFilter<VCSResource>) filter, parser);
	}

	if(ModifiedFilter.class.isAssignableFrom(filter.getClass())) {
	    return this.parseModified((ModifiedFilter<VCSResource>) filter);
	}
	// Case the given filter is an optimized filter
	if (OptimizedResourceFilter.class.isAssignableFrom(filter.getClass())) {
	    return (OptimizedResourceFilter<VCSResource>) filter;
	}
	
	return null;
    }

    public OptimizedResourceFilter<VCSResource> parseChild(
	    ChildFilter<VCSResource> filter) {

	return new OptimizedResourceFilter<VCSResource>(new JGitChildFilter(new LinkedHashSet<String>(filter.getPaths())));
    }

    public OptimizedResourceFilter<VCSResource> parsePrefix(
	    PathPrefixFilter<?> filter) {
	return this.pathPrefix(filter.getPaths());
    }

    public OptimizedResourceFilter<VCSResource> parseName(SuffixFilter<?> filter) {
	return this.name(filter.getSuffixes());
    }

    public OptimizedResourceFilter<VCSResource> parseAnd(
	    VCSResourceAndFilter<VCSResource> filter, 
	    FilterParser<VCSResource, OptimizedResourceFilter<VCSResource>> parser) {

	Set<VCSResourceFilter<VCSResource>> filters = filter.getFilters();
	List<OptimizedResourceFilter<VCSResource>> oFilters = new ArrayList<OptimizedResourceFilter<VCSResource>>();

	for (VCSResourceFilter<VCSResource> f : filters) {
	    OptimizedResourceFilter<VCSResource> of = this.parse(f, parser);
	    if (of != null) {
		oFilters.add(of);
	    } else {
		return null;
	    }
	}

	if (oFilters.isEmpty()) {
	    return null;
	} else if (oFilters.size() == 1) {
	    return oFilters.get(0);
	} else {
	    return this.and(oFilters);
	}
    }

    public OptimizedResourceFilter<VCSResource> parseOr(
	    VCSResourceOrFilter<VCSResource> filter, 
	    FilterParser<VCSResource, OptimizedResourceFilter<VCSResource>> parser) {

	Set<VCSResourceFilter<VCSResource>> filters = filter.getFilters();
	List<OptimizedResourceFilter<VCSResource>> oFilters = new ArrayList<OptimizedResourceFilter<VCSResource>>();

	for (VCSResourceFilter<VCSResource> f : filters) {
	    OptimizedResourceFilter<VCSResource> of = this.parse(f, parser);
	    if (of != null) {
		oFilters.add(of);
	    } else {
		return null;
	    }
	}

	if (oFilters.isEmpty()) {
	    return null;
	} else if (oFilters.size() == 1) {
	    return oFilters.get(0);
	} else {
	    return this.or(oFilters);
	}
    }

    public OptimizedResourceFilter<?> parseNot(
	    VCSResourceNotFilter<VCSResource> filter,
	    FilterParser<VCSResource, OptimizedResourceFilter<VCSResource>> parser) {
	OptimizedResourceFilter<?> of = this.parse(filter.getFilter(), parser);
	if (of != null) {
	    return new OptimizedResourceFilter<VCSResource>(of.getCurrent()
		    .negate());
	}
	return null;
    }

    public OptimizedResourceFilter<VCSResource> parseModified(ModifiedFilter<VCSResource> filter) {
	return modification();
    }

    
}
