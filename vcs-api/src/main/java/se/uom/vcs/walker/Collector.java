/**
 * 
 */
package se.uom.vcs.walker;

import java.util.Collection;
import java.util.Iterator;

import se.uom.vcs.exceptions.VCSRepositoryException;

/**
 * Helper interface used as a collector.<p>
 * 
 * {@link #collect()} method should generally perform an expensive calculation and collect all
 * results to a {@link Collection}. The {@link Iterable} interface gives access to the iterator
 * of the results of a computation. That is, the programs should benefit using the iterator because
 * it doesn't require all the computations to be loaded into memory, each result may be computed when
 * {@link Iterator#next()} is called, and it is cleaner to use a for each loop.
 * 
 * @author Elvis Ligu
 */
public interface Collector<T> extends Iterable<T>{

	/**
	 * Collect the result of a computation to one collection, or
	 * give access to an iterator.
	 * 
	 * @return
	 * @throws VCSRepositoryException
	 */
	Collection<T> collect() throws VCSRepositoryException;
}
