package grisu.tests.testRuns

import grisu.control.ServiceInterface
import grisu.tests.tests.ArchiveJobTest
import grisu.tests.tests.Test

class ArchiveJobTestRun extends AbstractTestRun implements TestRun {

	public String group
	public List inputfiles

	public void setup() {
	}

	public void tearDown() {
	}

	public Test getTest(ServiceInterface si, int batch, int id) {
		return new ArchiveJobTest(si, batch, id, group, inputfiles)
	}
}
