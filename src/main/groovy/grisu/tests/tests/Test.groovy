package grisu.tests.tests


/**
 * A Test is the implementation of a single Test execution.
 *
 * It consists of setting up the Test, executing it, checking whether the expected result was achived and then tearing down the test.
 *
 * @author Markus Binsteiner
 *
 */
interface Test {

	/**
	 * Sets up the test.
	 */
	void setupTest()
	/**
	 * Tears down the test.
	 */
	void tearDownTest()
	/**
	 * Executes the test.
	 */
	void executeTest()

	/**
	 * Checks whether the test was successful using Test-specific logic.
	 */
	void checkTest()

	/**
	 * Returns whether the test was successful.
	 *
	 * @return the success
	 */
	boolean wasSuccessful()
}
