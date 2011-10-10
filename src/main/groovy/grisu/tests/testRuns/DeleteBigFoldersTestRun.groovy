package grisu.tests.testRuns

import grisu.control.ServiceInterface
import grisu.tests.tests.DeleteBigFolderTest
import grisu.tests.tests.Test

class DeleteBigFoldersTestRun extends AbstractTestRun implements TestRun {

	public String group
	public int no_of_files
	public String queue
	public int no_of_folders

	public void setup() {
	}

	public void tearDown() {
	}

	public Test getTest(ServiceInterface si, int batch, int id) {
		return new DeleteBigFolderTest(si, batch, id, group, queue, no_of_folders, no_of_files)
	}
}
