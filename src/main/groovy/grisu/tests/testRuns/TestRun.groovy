package grisu.tests.testRuns

import grisu.control.ServiceInterface
import grisu.tests.log.TestLogger
import grisu.tests.tests.AbstractTest
import grisu.tests.tests.Test

import java.lang.reflect.Method
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import org.apache.log4j.Logger

import com.google.common.collect.Lists



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
class TestRun {

	Logger myLogger = Logger.getLogger(TestRun.class.getName())

	public Map serviceInterfaces
	public int runs = 1

	public final long testrunCreated = new Date().getTime()

	private long testrunStart
	private long testrunEnd

	public boolean skip_setup = false
	public boolean skip_teardown = false

	def testLoggers = []

	public void setTestLoggers(def loggers) {

		this.testLoggers = []

		if ( loggers.respondsTo('each') ) {
			loggers.each { addTestLogger(it) }
		} else {
			this.testLoggers.add(loggers)
		}
	}

	public void addTestLogger(def l) {

		TestLogger tl = null
		if (l instanceof TestLogger) {
			tl = l
		} else {
			tl = l.newInstance()
		}
		try {
			tl.setTestRun(this)
		} catch (all) {
		}
		this.testLoggers.add(tl)
	}

	public void removeTestLogger(TestLogger l) {
		testLoggers.remove(l)
	}

	public setSkipSetup(boolean skip) {
		this.skip_setup = skip
	}

	public setSkipTearDown(boolean skip) {
		this.skip_teardown = skip
	}

	public void setRuns(int r) {
		this.runs = r
	}

	private String name = "n/a"

	final Map config = [:]

	final List tests = Collections.synchronizedList(Lists.newArrayList())
	Thread executionThread
	Thread cleanupThread

	private Class testClass
	private Map configMap


	public TestRun() {
	}

	public Map getServiceInterfaces() {
		return serviceInterfaces
	}

	public void setName(String name) {
		this.name = name
	}

	public void setConfigMap(Map configMap) {
		this.configMap = configMap
	}

	public void setTestClass(Class testClass) {
		this.testClass = testClass
	}

	public void addTestLog(AbstractTest test, String msg) {

		long timestamp = new Date().getTime()

		testLoggers.each {
			it.addTestLogMessage(test, timestamp, msg)
		}
	}

	public void addRunLog(String msg) {

		long timestamp = new Date().getTime()

		testLoggers.each {
			it.addTestRunLogMessage(this, timestamp, msg)
		}
	}

	/**
	 * Sets the list of ServiceInterfaces used to run this TestRun.
	 *
	 * The reason why there can be several of those is that this enables the testing of multiple sessions for example in Tomcat. Every ServiceInterface would have it's own batch of parallel Tests to execute (or a single Test, that would obvioulsly be ok as well)
	 *
	 * @param sis the ServiceInterfaces
	 */
	public void setServiceInterfaces(Map sis) {

		this.serviceInterfaces = sis
	}

	private void init() {

		//		Method m = testClass.getMethod("setTestRun", TestRun.class)
		//		m.invoke(null, this)

		for ( no in 1..runs ) {

			for ( name in serviceInterfaces.keySet()) {

				Test t = getTest(serviceInterfaces.get(name), name, no)
				t.setTestRun(this)
				tests.add(t)
			}
		}
		
//		for (Test t : tests) {
//			println t.getName()
//			println '\t'+t.getServiceInterface().getDN()
//		}

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
	private Test getTest(ServiceInterface si, String certname, int parallel_id) {

		def constructor = testClass.getConstructor()

		Test test = constructor.newInstance()

		test.setServiceInterface(si)
		test.setCertName(certname)
		test.setParallelId(parallel_id)

		for ( def key : configMap.keySet() ) {
			def field = test.class.getField(key)
			def value = configMap.get(key)
			field.set(test, value)
		}

		return test
	}

	public void kickOff() {

		init()

		executionThread.start()
	}

	public void waitForTestsToFinish() {

		executionThread.join()
	}

	/**
	 * Waits for the TestRun to cleanup, since this is done in the background after the tests have finished executing and were checked here: {@link #kickOffAndWaitForTestsToFinish())
	 */
	public void waitForCleanUp() {

		executionThread.join()
		if ( cleanupThread != null ) {
			cleanupThread.join()
		}
	}

	/**
	 * Starts the TestRun execution worklflow outlined above
	 */
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

	public void testExecuted(AbstractTest test, boolean success) {
		testLoggers.each {
			it.testExecuted(test, success)
		}
	}

	public void testChecked(AbstractTest test, boolean success) {
		testLoggers.each {
			it.testChecked(test, success)
		}
	}

	private void kickOffExecution() {


		if (!skip_setup) {
			myLogger.debug("Setting up testrun...")
			setup()
			myLogger.debug("Setting of tests...")
			addLog "Setting up tests..."
			tests.each{ t ->
				t.setupTest()
			}
			addLog "Setup of tests finished"
		} else {
			addLog("Skipping setting up testrun.")
		}

		ExecutorService executor = Executors.newFixedThreadPool(runs*serviceInterfaces.size())

		testrunStart = new Date().getTime()
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
		testrunEnd = new Date().getTime()

		addLog "Test run finished."

		myLogger.debug("Starting to check tests...")
		addLog "Checking tests..."
		tests.each { t ->
			t.checkTest()
		}
		addLog "Checking of tests finished."

		if (!skip_teardown) {
			myLogger.debug("Starting checking cleanup...")
			cleanupThread = new Thread() {
						public void run() {
							cleanUp()
						}
					}

			cleanupThread.start()
		} else {
			addLog("Skipping tearing down test.")
		}
	}

	public long getDuration() {
		if (testrunStart <= 0 ) {
			return 0L
		}
		if ( testrunEnd <= 0) {
			long now = new Date().getTime()
			return now-testrunStart
		}
		return testrunEnd-testrunStart
	}

	private void cleanUp() {

		def tearDownTest = { t ->
			myLogger.debug("Tearing down test "+t.getCertName()+ " / "+ t.getParallelId()+"...")
			t.tearDownTest()
			myLogger.debug("Teared down test "+t.getCertName()+ " / "+t.getParallelId()+"...")
		}
		addLog "Tearing down tests..."
		tests.each(tearDownTest)
		addLog "Teardown of tests finished."
		myLogger.debug("Tearing down test run...")

		tearDown()
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

	public void getStatus() {
		testLoggers.each { it.getStatus() }
	}

	/**
	 * Prints out the results and details about the TestRun and all Tests involved to stdout (this will be improved later on)
	 */
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

	public String getName() {
		return this.name
	}

	private void addLog(String msg) {
		addRunLog(msg)
	}
	/**
	 * Setup a TestRun.
	 *
	 * In contrast to setting up a single Test this does a "TestRun-global" setup (which will be executed before the setting up of the single Tests). I.e. prepare a directory all Tests are using.
	 */
	public void setup() {

		try {
			Method m = testClass.getMethod("setupTestRun", TestRun.class, Map.class)
			addLog("Setting up testrun...")
			m.invoke(null, this, configMap)
			addLog("Setting up of testrun completed.")
		} catch (NoSuchMethodException e) {
			// that's ok
			addLog("Not running testrun setup since no setupTestRun method implemented for this test...")
		} catch (Exception e) {
			addLog("Could not setup testrun successfully: "+e.getLocalizedMessage())
			throw e
		}
	}

	/**
	 * Tearing down a TestRun.
	 *
	 * In contrast to tearing down a single Test this does a "TestRun-global" teardown (which will be executed after the teardown of the single Tests). I.e. delete a directory all Tests were using.
	 */
	public void tearDown() {
		try {
			Method m = testClass.getMethod("teardownTestRun", TestRun.class, Map.class)
			addLog("Tearing down up testrun...")
			m.invoke(null, this, configMap)
			addLog("Teardown of testrun completed.")
		} catch (NoSuchMethodException e) {
			// that's ok
			addLog("Not tearing down testrun since no teartownTestRun method implemented for this test...")
		} catch (Exception e) {
			addLog("Could not tear down testrun successfully: "+e.getLocalizedMessage())
			throw e
		}
	}
}
