package grisu.tests.tests

import grisu.control.ServiceInterface

import org.apache.log4j.Logger

import com.google.common.collect.Maps

abstract class AbstractTest implements Test {

	Logger myLogger = Logger.getLogger(AbstractTest.class.getName())

	final log = Maps.newTreeMap()

	public final ServiceInterface si
	public final int batch
	public final int parallel

	Date startedTime
	Date finishedTime
	long duration = 0L

	protected boolean success = false
	protected boolean success_check = false

	Exception exception = null
	Exception setupException = null
	Exception checkException = null
	Exception tearDownException = null

	protected AbstractTest(ServiceInterface si, int batch, int parallel) {
		this.si = si
		this.batch = batch
		this.parallel = parallel
	}

	/**
	 * The execution of the test.
	 */
	abstract protected void execute()
	/**
	 * Checks whether the test was successful.
	 *
	 * Needs to the {@link #success} variable to "true" if it was and an exception if it wasn't...
	 */
	abstract protected void check()
	/**
	 * Tears down the test.
	 */
	abstract protected void tearDown()
	/**
	 * Sets up the test.
	 */
	abstract protected void setup()

	public boolean getSuccess() {
		return this.success
	}

	protected void addLog(String msg) {

		println '  ( '+batch+' / '+parallel+' ): '+msg

		long time = new Date().getTime()

		while (log[time]) {
			time++
		}

		log[time] = msg
	}

	public void tearDownTest() {
		myLogger.debug("Tearing down test: "+batch+" / "+parallel)
		try {
			addLog "Start tearDown..."
			tearDown()
			addLog "Finished tearDown."
			myLogger.debug("Tearing down test "+batch+" / "+parallel+" successful.")
		} catch (all) {
			addLog "TearDown error: "+all.getLocalizedMessage()
			myLogger.debug("Tearing down test "+batch+" / "+parallel+" error: "+all.getLocalizedMessage())
			tearDownException = all
		}
	}

	public void setupTest() {
		myLogger.debug("Setting up test: "+batch+" / "+parallel)
		addLog "Start setup..."
		try {
			setup()
			addLog "Finished setup."
			myLogger.debug("Setting up test "+batch+" / "+parallel+" successful.")
		} catch (all) {
			addLog "Setup error: "+all.getLocalizedMessage()
			myLogger.debug("Setting up test "+batch+" / "+parallel+" error: "+all.getLocalizedMessage())
			setupException = all
		}
	}


	void checkTest() {
		myLogger.debug("Checking test: "+batch+" / "+parallel)

		if (!success) {
			myLogger.debug("Not checking test: "+batch+" /"+parallel)
			addLog("Not checking test since it didn't run successfully...")
			return
		}

		addLog "Start check..."
		try {
			check()
			addLog "Finished check."
			myLogger.debug("Checking test "+batch+" / "+parallel+" successful.")
			success_check = true
		} catch (all) {
			addLog "Check error: "+all.getLocalizedMessage()
			myLogger.debug("Checking test "+batch+" / "+parallel+" error: "+all.getLocalizedMessage())
			checkException = all
		}
	}

	void executeTest() {

		if ( setupException ) {
			addLog("Not starting test, setup failed...")
			return
		}

		myLogger.debug("Starting test: "+batch+" / "+parallel)

		try {

			startedTime = new Date()
			addLog "Start test..."

			execute()
			addLog "Finished test."
			finishedTime = new Date()
			this.success = true
			myLogger.debug("Finished test successfully: "+batch+" / "+parallel)
		} catch(all) {
			addLog "Test error: "+all.getLocalizedMessage()
			finishedTime = new Date()
			exception = all
			myLogger.debug("Finished test "+batch+" / "+parallel+ " with error: "+all.getLocalizedMessage())
		} finally {
			duration = finishedTime.getTime()-startedTime.getTime()
		}
	}

	public String getResult() {
		String r = "("+ batch + " / "+parallel+"):\n"
		r += "\tSuccess: "+ wasSuccessful() + "\n"
		r += "\tDuration: "+getDuration()+" ms" + "\n"
		r += "\tLog:" + "\n"
		for ( l in getLog() ) {
			r += "\t\t"+l.key +": "+l.value +"\n"
		}
		return r
	}

	public String getErrorMessage() {
		if ( exception ) {
			return exception.getLocalizedMessage()
		} else {
			return ""
		}
	}

	boolean isFinished() {
		return finishedTime != null
	}

	boolean isStarted() {
		return startedTime != null
	}

	boolean wasSuccessful() {
		return ( this.success && this.success_check )
	}
}
