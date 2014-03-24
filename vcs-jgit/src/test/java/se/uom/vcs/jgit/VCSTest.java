/**
 * 
 */
package se.uom.vcs.jgit;

import org.junit.After;
import org.junit.Before;

/**
 * A super class of all test cases that creates a {@link TestUtils} instance.<p>
 * @author elvis
 *
 */
public class VCSTest {

	protected TestUtils UTILS;

	@Before
	public void setUpBefore() throws Exception {
		// This will load the repository or clone it from remote if not there
		UTILS = TestUtils.instance();
	}

	@After
	public void tearDownAfter() throws Exception {
		UTILS.smallRepo().close();
	}
}
