package grisu.tests.testRuns

import grisu.control.ServiceInterface
import grisu.control.serviceInterfaces.AbstractServiceInterface
import grisu.tests.tests.Test

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import org.apache.log4j.Logger

import com.google.common.collect.Lists
import com.google.common.collect.Maps

abstract class AbstractTestRun {

	final log = Maps.newTreeMap()

	Logger myLogger = Logger.getLogger(AbstractServiceInterface.class.getName())

	public List serviceInterfaces
	public int runs
	public int batches

	private String name = "n/a"

	final Map config = [:]

	final List tests = Collections.synchronizedList(Lists.newArrayList())
	Thread executionThread
	Thread cleanupThread


	public AbstractTestRun() {
	}

	public void setName(String name) {
		this.name = name
	}

	private synchronized void addLog(String msg) {

		printMessage(msg)

		long time = new Date().getTime()

		while (log[time]) {
			time++
		}

		log[time] = msg
	}

	public void setServiceInterfaces(List sis) {

		if ( sis.size() < batches ) {
			throw new IllegalArgumentException("Not enough serviceinterfaces to run "+batches+" batches of tests.")
		}

		this.serviceInterfaces = sis
	}

	private void init() {

		for ( b in 1..batches) {

			for ( no in 1..runs ) {

				Test t = getTest(serviceInterfaces.get(b-1), b, no)
				tests.add(t)
			}
		}

		executionThread = new Thread() {
					public void run() {
						kickOffExecution()
					}
				}
	}

	private void printMessage(String msg) {

		println '['+name+']: '+msg
	}

	/**
	 * Should return a {@link Test} object.
	 *
	 * @param si the serviceinterface to use with this Test
	 * @param batch the batch no (usually 1, but when there are more serviceInterfaces/sessions to be used in parallel then it is the index of the serviceInterface to use)
	 * @param parallel_id the id of the Test. Specifies which id within a TestRun the test will have.
	 * @return
	 */
	abstract Test getTest(ServiceInterface si, int batch, int parallel_id)

	public void kickOff() {

		init()

		executionThread.start()
	}

	public void waitForTestsToFinish() {

		executionThread.join()
	}

	public void waitForCleanUp() {

		executionThread.join()
		cleanupThread.join()
	}

	public void kickOffAndWaitForTestsToFinish() {
		kickOff(true, false)
	}

	public void kickOff(boolean waitForTests, boolean waitCleanup) {
		kickOff()
		if (waitForTests) {
			waitForTestsToFinish()
		}
		if (waitCleanup) {
			waitForCleanUp()
		}
	}

	private void kickOffExecution() {

		myLogger.debug("Setting up testrun...")
		addLog "Setting up testrun..."
		setup()
		addLog "Setup of testrun finished."

		int poolsize = runs * batches
		ExecutorService executor = Executors.newFixedThreadPool(poolsize)

		myLogger.debug("Setting of tests...")
		addLog "Setting up tests..."
		tests.each{ t ->
			t.setupTest()
		}
		addLog "Setup of tests finished"

		myLogger.debug("Starting tests...")
		def execute = { t ->
			executor.submit(t as Callable)
		}
		addLog "Starting test run..."
		tests.each { t ->
			execute{ t.executeTest() }
		}

		myLogger.debug("Waiting for tests to execute...")
		executor.shutdown()
		executor.awaitTermination(10, TimeUnit.HOURS)
		addLog "Test run finished."

		myLogger.debug("Starting to check tests...")
		addLog "Checking tests..."
		tests.each { t ->
			t.checkTest()
		}
		addLog "Checking of tests finished."

		myLogger.debug("Starting checking cleanup...")
		cleanupThread = new Thread() {
					public void run() {
						cleanUp()
					}
				}

		cleanupThread.start()
	}

	private void cleanUp() {

		def tearDownTest = { t ->
			myLogger.debug("Tearing down test "+t.parallel+"...")
			t.tearDownTest()
			myLogger.debug("Teared down test "+t.parallel+"...")
		}
		addLog "Tearing down tests..."
		tests.each(tearDownTest)
		addLog "Teardown of tests finished."
		myLogger.debug("Tearing down test run...")

		addLog "Tearing down testrun..."
		tearDown()
		addLog "Teardown of testrun finished."
		myLogger.debug("Teared down test run...")
	}

	public String getLog() {
		String r = ""

		def tmp = log.clone()

		for ( l in tmp ) {
			r += l.key+": "+l.value+"\n"
		}
		if ( executionThread != null && executionThread.isAlive() ) {
			r += "[...still executing...]\n"
		} else if (cleanupThread != null && cleanupThread.isAlive()) {
			r += "[...still cleaning up...]\n"
		}

		return r
	}

	public void printResults() {

		println "Testrun: " + this.name +"\n"
		println "Class: "+ this.class.getName()
		println ""

		int total = 0
		int success = 0
		int fail = 0

		for (t in tests) {
			total ++
			if ( t.wasSuccessful() ) {
				success++
			} else {
				fail++
			}
		}

		println "Total: "+total
		println "Success: "+success
		println "Failed: "+fail
		println ""
		println "Log:"
		println getLog()
		println ""

		for (t in tests) {
			println t.getResult()
			println ""
		}
	}
	/**
	 * Setup a TestRun.
	 *
	 * In contrast to setting up a single Test this does a "TestRun-global" setup (which will be executed before the setting up of the single Tests). I.e. prepare a directory all Tests are using.
	 */
	abstract void setup()

	/**
	 * Tearing down a TestRun.
	 *
	 * In contrast to tearing down a single Test this does a "TestRun-global" teardown (which will be executed after the teardown of the single Tests). I.e. delete a directory all Tests were using.
	 */
	abstract void tearDown()
}
