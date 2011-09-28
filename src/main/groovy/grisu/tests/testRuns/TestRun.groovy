package grisu.tests.testRuns

/**
 * A TestRun consists of a either one or a number of {@link Test}s that are run in parallel.
 *
 * The execution workflow is as follows:<br>
 * * setting up a TestRun<br>
 * * setting all single Tests (in serial)<br>
 * * executing all Tests (in parallel)<br>
 * * checking all Tests whether they were successful or not (in serial)<br>
 * * tearing down all single Tests (in serial)<br>
 * * tearing down the TestRun<br>
 *
 *
 * @author Markus Binsteiner
 *
 */
interface TestRun {

	/**
	 * Starts the TestRun execution worklflow outlined above
	 */
	void kickOffAndWaitForTestsToFinish()

	/**
	 * Prints out the results and details about the TestRun and all Tests involved to stdout (this will be improved later on)
	 */
	void printResults()

	/**
	 * Waits for the TestRun to cleanup, since this is done in the background after the tests have finished executing and were checked here: {@link #kickOffAndWaitForTestsToFinish())
	 */
	void waitForCleanUp()

	/**
	 * Sets the list of ServiceInterfaces used to run this TestRun.
	 *
	 * The reason why there can be several of those is that this enables the testing of multiple sessions for example in Tomcat. Every ServiceInterface would have it's own batch of parallel Tests to execute (or a single Test, that would obvioulsly be ok as well)
	 *
	 * @param sis the ServiceInterfaces
	 */
	void setServiceInterfaces(List sis)
}
