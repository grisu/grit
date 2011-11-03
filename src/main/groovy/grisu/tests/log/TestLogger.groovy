package grisu.tests.log

import grisu.tests.testRuns.TestRun
import grisu.tests.tests.AbstractTest

interface TestLogger {

	void addTestLogMessage(AbstractTest source, long timestamp, String msg)

	void addTestRunLogMessage(TestRun source, long timestamp, String msg)

	void testExecuted(AbstractTest test, boolean success)

	void testChecked(AbstractTest test, boolean success)

	void getStatus()
}
