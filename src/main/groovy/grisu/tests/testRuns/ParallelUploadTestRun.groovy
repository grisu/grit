package grisu.tests.testRuns

import grisu.control.ServiceInterface
import grisu.model.FileManager
import grisu.model.GrisuRegistryManager
import grisu.tests.tests.Test
import grisu.tests.tests.UploadTest

class ParallelUploadTestRun extends AbstractTestRun implements TestRun {

	public String sourceFile
	public String targetDir
	public int repeats

	@Override
	public Test getTest(ServiceInterface si, int batch, int id) {
		return new UploadTest(si, batch, id, sourceFile, targetDir, repeats)
	}

	public void setup() {

		for ( si in serviceInterfaces ) {
			FileManager fm = GrisuRegistryManager.getDefault(si).getFileManager()
			try {
				addLog("Deleting target folder "+targetDir+"...")
				fm.deleteFile(targetDir)
				addLog("Target folder deleted.")
			} catch(all) {
			}
			addLog("Creating target folder...")
			si.mkdir(targetDir)
			addLog("Target folder created.")
		}
	}

	public void tearDown() {

		for ( si in serviceInterfaces ) {
			FileManager fm = GrisuRegistryManager.getDefault(si).getFileManager()
			try {
				addLog("Deleting target folder "+targetDir+"...")
				fm.deleteFile(targetDir)
				addLog("Target folder deleted.")
			} catch(all) {
				addLog("Delete target folder failed: "+all.getLocalizedMessage())
			}
		}
	}
}
