package grisu.tests.testRuns

import grisu.control.ServiceInterface
import grisu.tests.tests.CleanJobTest
import grisu.tests.tests.Test

class CleanJobTestRun extends AbstractTestRun implements TestRun {

	public String group
	public int no_of_files
	public String queue

	public void setup() {
	}

	public void tearDown() {
	}

	public Test getTest(ServiceInterface si, int batch, int id) {
		return new CleanJobTest(si, batch, id, group, queue, no_of_files)
	}
}
