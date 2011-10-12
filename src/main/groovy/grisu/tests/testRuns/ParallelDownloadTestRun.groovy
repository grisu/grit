package grisu.tests.testRuns

import grisu.control.ServiceInterface
import grisu.tests.tests.DownloadTest
import grisu.tests.tests.Test

class ParallelDownloadTestRun extends AbstractTestRun implements TestRun {

	public String sourceFile
	public int repeats

	@Override
	public Test getTest(ServiceInterface si, int batch, int id) {
		return new DownloadTest(si, batch, id, sourceFile, int repeats)
	}

	public void setup() {
	}

	public void tearDown() {
	}
}
