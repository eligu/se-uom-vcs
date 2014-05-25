/**
 * 
 */
package se.uom.vcs.jgit.walker.filter.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.OrTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import se.uom.vcs.VCSResource;
import se.uom.vcs.jgit.walker.FilterParser;
import se.uom.vcs.walker.filter.resource.ChildFilter;
import se.uom.vcs.walker.filter.resource.ModifiedFilter;
import se.uom.vcs.walker.filter.resource.PathFilter;
import se.uom.vcs.walker.filter.resource.PathPrefixFilter;
import se.uom.vcs.walker.filter.resource.SuffixFilter;
import se.uom.vcs.walker.filter.resource.TypeFilter;
import se.uom.vcs.walker.filter.resource.VCSResourceAndFilter;
import se.uom.vcs.walker.filter.resource.VCSResourceFilter;
import se.uom.vcs.walker.filter.resource.VCSResourceNotFilter;
import se.uom.vcs.walker.filter.resource.VCSResourceOrFilter;

/**
 * A utility class to deal with JGit tree filters.<p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ResourceFilter {

    /**
     * Given a collection of filters creates a new filter that apply an
     * AND operator to them.<p>
     * 
     * @param filters to AND
     * @return the new AND filter based on the given filters
     */
    public static OptimizedResourceFilter<VCSResource> and(
	    Collection<OptimizedResourceFilter<VCSResource>> filters) {
	List<TreeFilter> revFilters = new ArrayList<TreeFilter>();
	for (OptimizedResourceFilter<?> f : filters) {
	    revFilters.add(f.getCurrent());
	}
	return new OptimizedResourceFilter<VCSResource>(
		AndTreeFilter.create(revFilters));
    }

    /**
     * Given a filter creates a new one that negates its result.<p>
     * @param filter
     * @return
     */
    public static OptimizedResourceFilter<VCSResource> not(OptimizedResourceFilter<?> filter) {
	return new OptimizedResourceFilter<VCSResource>(filter.getCurrent()
		.negate());
    }

    /**
     * Given a collection of filters creates a new filter that apply an
     * OR operator to them.<p>
     * 
     * @param filters to OR
     * @return the new OR filter based on the given filters
     */
    public static OptimizedResourceFilter<VCSResource> or(
	    Collection<OptimizedResourceFilter<VCSResource>> filters) {
	List<TreeFilter> revFilters = new ArrayList<TreeFilter>();
	for (OptimizedResourceFilter<?> f : filters) {
	    revFilters.add(f.getCurrent());
	}
	return new OptimizedResourceFilter<VCSResource>(
		OrTreeFilter.create(revFilters));
    }

    /**
     * Given a collection of string paths, creates a new path prefix filter.<p>
     * 
     * @param patterns of the paths
     * @return the new prefix filter
     */
    public static OptimizedResourceFilter<VCSResource> pathPrefix(
	    Collection<String> patterns) {
	return new OptimizedResourceFilter<VCSResource>(
		PathFilterGroup.createFromStrings(patterns));
    }

    /**
     * Will throw an {@link IllegalArgumentException} if the 
     * collection contains a null or empty string.<p>
     * 
     * @param patterns to check
     */
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

    /**
     * Given a collection of string patterns, creates a new tree filter that check for suffixes.<p>
     * 
     * @param patterns the suffixes
     * @return a new suffix filter
     */
    public static OptimizedResourceFilter<VCSResource> suffix(Collection<String> patterns) {

	checkStrings(patterns);

	List<TreeFilter> filters = new ArrayList<TreeFilter>();
	TreeFilter filter = null;
	for (String p : patterns) {
	    filters.add(PathSuffixFilter.create(p));
	}
	if (filters.size() == 1) {
	    filter = filters.get(0);
	} else {
	    filter = OrTreeFilter.create(filters);
	}
	return new OptimizedResourceFilter<VCSResource>(filter);
    }

    /**
     * Return a ANY_DIFF tree filter.<p>
     * 
     * @return
     */
    public static OptimizedResourceFilter<VCSResource> modification() {
	return new OptimizedResourceFilter<VCSResource>(TreeFilter.ANY_DIFF);
    }

    /**
     * Creates a path filter that will allow only the specified paths.<p>
     * @param paths to filter
     * @return new path filter (not prefix filter)
     */
    public static OptimizedResourceFilter<VCSResource> path(Collection<String> paths) {
	checkStrings(paths);
	
	return new OptimizedResourceFilter<VCSResource>(new JGitPathFilter(paths));
    }
    
    /**
     * Creates a filter that will allow only the children of the specified paths
     * to be included.<p>
     * 
     * @param paths the parents of entries to be included
     * @return new child filter
     */
    public static OptimizedResourceFilter<VCSResource> child(Collection<String> paths) {
	return new OptimizedResourceFilter<VCSResource>(new JGitChildFilter(paths));
    }
    
    /**
     * Creates a filter that fill allow only the resources of the specified type.<p>
     * 
     * @param type of resource to be allowed
     * @return new type filter
     */
    public static OptimizedResourceFilter<VCSResource> type(VCSResource.Type type) {
	return new OptimizedResourceFilter<VCSResource>(new JGitTypeFilter(type));
    }
    
    /**
     * Will parse a {@link VCSResourceFilter} and try to convert it to a
     * JGit tree filter.<p>
     * 
     * @param filter to parse
     * @param parser if there is another parser available will check it first,
     * 		if he can parse the filter it will be returned otherwise
     * 		will try the default method.
     * @return
     */
    public static OptimizedResourceFilter<VCSResource> parse(
	    VCSResourceFilter<VCSResource> filter, 
	    FilterParser<VCSResource, OptimizedResourceFilter<VCSResource>> parser) {

	// Check the given parser if he can parse the filter
	// If not try to parse it with the default method
	if(parser != null) {
	    OptimizedResourceFilter<VCSResource> f = parser.parse(filter);
	    if(f != null) {
		return f;
	    }
	}
	
	// Case when filter is a path prefix filter
	if (PathPrefixFilter.class.isAssignableFrom(filter.getClass())) {
	    return parsePrefix((PathPrefixFilter<?>) filter);
	}

	// Case when filter is a name filter
	if (SuffixFilter.class.isAssignableFrom(filter.getClass())) {
	    return parseSuffix((SuffixFilter<VCSResource>) filter);
	}

	// Case when filter is AND
	if (VCSResourceAndFilter.class.isAssignableFrom(filter.getClass())) {
	    return parseAnd((VCSResourceAndFilter<VCSResource>) filter, parser);
	}

	// Case when filter is OR
	if (VCSResourceOrFilter.class.isAssignableFrom(filter.getClass())) {
	    return parseOr((VCSResourceOrFilter<VCSResource>) filter, parser);
	}

	// Case when filter is Modified (ANY_DIFF will be returned)
	if(ModifiedFilter.class.isAssignableFrom(filter.getClass())) {
	    return parseModified((ModifiedFilter<VCSResource>) filter);
	}
	
	// Case the given filter is an optimized filter
	if (OptimizedResourceFilter.class.isAssignableFrom(filter.getClass())) {
	    return (OptimizedResourceFilter<VCSResource>) filter;
	}
	
	// Case when filter is a path filter
	if(se.uom.vcs.walker.filter.resource.PathFilter.class.isAssignableFrom(filter.getClass())) {
	    return parsePath((PathFilter<VCSResource>) filter);
	}
	
	// Case when filter is a child filter
	if(ChildFilter.class.isAssignableFrom(filter.getClass())) {
	    return parseChild((ChildFilter<VCSResource>) filter);
	}
	
	// Case when filter is a type filter
	if(TypeFilter.class.isAssignableFrom(filter.getClass())) {
	    return parseType((TypeFilter<VCSResource>) filter);
	}
	
	// Case when the given filter is NOT filter
	if(VCSResourceNotFilter.class.isAssignableFrom(filter.getClass())) {
	    return parseNot((VCSResourceNotFilter<VCSResource>) filter, parser);
	}
	
	return null;
    }
    
    /**
     * Will return a new type filter for JGit library based on the given filter.<p>
     * 
     * @param filter to parse
     * @return a new {@link TreeFilter} implementation for the type of given filter
     */
    public static OptimizedResourceFilter<VCSResource> parseType(TypeFilter<VCSResource> filter) {
	return type(filter.getType());
    }
    
    /**
     * Will return a new path filter to be used within JGit library based on the given filter.<p>
     * 
     * @param filter to parse
     * @return a new {@link TreeFilter} implementation that corresponds to the given {@link PathFilter}
     */
    public static OptimizedResourceFilter<VCSResource> parsePath(se.uom.vcs.walker.filter.resource.PathFilter<VCSResource> filter) {
	return path(filter.getPaths());
    }

    /**
     * Will return a new child filter to be used within JGit library based on the given filter.<p>
     * 
     * @param filter to parse
     * @return a new {@link TreeFilter} implementation that corresponds to the given {@link ChildFilter}
     */
    public static OptimizedResourceFilter<VCSResource> parseChild(
	    ChildFilter<VCSResource> filter) {

	return child(filter.getPaths());
    }

    /**
     * Will return a new prefix filter to be used within JGit library based on the given filter.<p>
     * 
     * @param filter to parse
     * @return a new {@link TreeFilter} implementation that corresponds to the given {@link PathPrefixFilter}
     */
    public static OptimizedResourceFilter<VCSResource> parsePrefix(
	    PathPrefixFilter<?> filter) {
	return pathPrefix(filter.getPaths());
    }

    /**
     * Will return a new suffix filter to be used within JGit library based on the given filter.<p>
     * 
     * @param filter to parse
     * @return a new {@link TreeFilter} implementation that corresponds to the given {@link SuffixFilter}
     */
    public static OptimizedResourceFilter<VCSResource> parseSuffix(SuffixFilter<?> filter) {
	return suffix(filter.getSuffixes());
    }

    /**
     * Will return a new AND filter to be used within JGit library based on the given filter.<p>
     * 
     * @param filter to parse
     * @return a new {@link TreeFilter} implementation that corresponds to the given {@link VCSResourceAndFilter}
     */
    public static OptimizedResourceFilter<VCSResource> parseAnd(
	    VCSResourceAndFilter<VCSResource> filter, 
	    FilterParser<VCSResource, OptimizedResourceFilter<VCSResource>> parser) {

	Set<VCSResourceFilter<VCSResource>> filters = filter.getFilters();
	List<OptimizedResourceFilter<VCSResource>> oFilters = new ArrayList<OptimizedResourceFilter<VCSResource>>();

	for (VCSResourceFilter<VCSResource> f : filters) {
	    OptimizedResourceFilter<VCSResource> of = parse(f, parser);
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
	    return and(oFilters);
	}
    }

    /**
     * Will return a new OR filter to be used within JGit library based on the given filter.<p>
     * 
     * @param filter to parse
     * @return a new {@link TreeFilter} implementation that corresponds to the given {@link VCSResourceOrFilter}
     */
    public static OptimizedResourceFilter<VCSResource> parseOr(
	    VCSResourceOrFilter<VCSResource> filter, 
	    FilterParser<VCSResource, OptimizedResourceFilter<VCSResource>> parser) {

	Set<VCSResourceFilter<VCSResource>> filters = filter.getFilters();
	List<OptimizedResourceFilter<VCSResource>> oFilters = new ArrayList<OptimizedResourceFilter<VCSResource>>();

	for (VCSResourceFilter<VCSResource> f : filters) {
	    OptimizedResourceFilter<VCSResource> of = parse(f, parser);
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
	    return or(oFilters);
	}
    }

    /**
     * Will return a new NOT filter to be used within JGit library based on the given filter.<p>
     * 
     * @param filter to parse
     * @return a new {@link TreeFilter} implementation that corresponds to the given {@link VCSResourceNotFilter}
     */
    public static OptimizedResourceFilter<VCSResource> parseNot(
	    VCSResourceNotFilter<VCSResource> filter,
	    FilterParser<VCSResource, OptimizedResourceFilter<VCSResource>> parser) {
	OptimizedResourceFilter<?> of = parse(filter.getFilter(), parser);
	if (of != null) {
	    return new OptimizedResourceFilter<VCSResource>(of.getCurrent()
		    .negate());
	}
	return null;
    }

    /**
     * Will return a new ANY_DIFF filter to be used within JGit library based on the given filter.<p>
     * 
     * @param filter to parse
     * @return a new {@link TreeFilter} implementation that corresponds to the given {@link ModifiedFilter}
     */
    public static OptimizedResourceFilter<VCSResource> parseModified(ModifiedFilter<VCSResource> filter) {
	return modification();
    }
}
