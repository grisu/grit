package grisu.tests.testRuns

import grisu.control.ServiceInterface
import grisu.model.FileManager
import grisu.model.GrisuRegistryManager
import grisu.tests.tests.Test
import grisu.tests.tests.UploadTest

class ParallelUploadTestRun extends AbstractTestRun implements TestRun {

	public String sourceFile
	public String targetDir

	@Override
	public Test getTest(ServiceInterface si, int batch, int id) {
		return new UploadTest(si, batch, id, sourceFile, targetDir)
	}

	public void setup() {

		for ( si in serviceInterfaces ) {
			FileManager fm = GrisuRegistryManager.getDefault(si).getFileManager()
			try {
				fm.deleteFile(targetDir)
			} catch(all) {
			}
			si.mkdir(targetDir)
		}
	}

	public void tearDown() {

		for ( si in serviceInterfaces ) {
			FileManager fm = GrisuRegistryManager.getDefault(si).getFileManager()
			try {
				fm.deleteFile(targetDir)
			} catch(all) {
			}
		}
	}
}
