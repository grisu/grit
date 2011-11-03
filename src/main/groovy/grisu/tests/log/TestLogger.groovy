package grisu.tests.log

import grisu.tests.testRuns.TestRun
import grisu.tests.tests.AbstractTest

interface TestLogger {

	void addTestLogMessage(AbstractTest source, String msg)

	void addTestRunLogMessage(TestRun source, String msg)
}
