package grisu.tests.log

import grisu.tests.testRuns.TestRun
import grisu.tests.tests.AbstractTest

class StdOutTestLogger implements TestLogger {


	public void addTestLogMessage(AbstractTest source, String msg) {

		println '\t('+source.getName()+'): '+msg
	}

	public void addTestRunLogMessage(TestRun tr, String msg) {

		println '['+tr.getName()+']: '+msg
	}
}
