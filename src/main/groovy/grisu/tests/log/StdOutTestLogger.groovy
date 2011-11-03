package grisu.tests.log

import grisu.tests.testRuns.TestRun
import grisu.tests.tests.AbstractTest

class StdOutTestLogger implements TestLogger {

	private boolean displayTestLogs = false

	private int success = 0
	private int failed = 0

	public StdOutTestLogger(boolean displayTestLogs) {
		this.displayTestLogs = displayTestLogs
	}

	public StdOutTestLogger() {
		this(false)
	}


	public void addTestLogMessage(AbstractTest source, long timestamp, String msg) {

		if ( displayTestLogs ) {
			println '\tTest '+source.getName()+': '+msg
		}
	}

	public void addTestRunLogMessage(TestRun tr, long timestamp, String msg) {

		println '['+tr.getName()+']: '+msg
	}

	void testExecuted(AbstractTest test, boolean success) {
		if (! success ) {
			String msg = 'n/a'
			if ( test.getExecuteException() ) {
				msg = test.getExecuteException().getLocalizedMessage()
			}
			println '\tTest '+test.getName()+' failed to execute: '+msg
		} else {
			println '\tTest '+test.getName()+' executed'
		}
	}

	void testChecked(AbstractTest test, boolean suceeded) {
		if (! suceeded) {
			String msg = 'n/a'
			if ( test.getCheckException() ) {
				msg = test.getCheckException().getLocalizedMessage()
			}
			println '\tTest '+test.getName()+' failed: '+msg
			failed++
		} else {
			println '\tTest '+test.getName()+' success!'
			success++
		}
	}

	void getStatus() {
		println 'Failed tests: '+failed
		println 'Successfull tests: '+success
	}
}
