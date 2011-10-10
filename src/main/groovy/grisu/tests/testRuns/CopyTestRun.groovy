package grisu.tests.testRuns

import grisu.control.ServiceInterface
import grisu.model.FileManager
import grisu.model.GrisuRegistryManager
import grisu.tests.tests.CopyTest
import grisu.tests.tests.Test

class CopyTestRun extends AbstractTestRun implements TestRun {

	public String sourceUrl
	public String targetDir
	public int repeats

	@Override
	public Test getTest(ServiceInterface si, int batch, int id) {
		return new CopyTest(si, batch, id, sourceUrl, targetDir, repeats)
	}

	public void setup() {

		for ( si in serviceInterfaces ) {
			FileManager fm = GrisuRegistryManager.getDefault(si).getFileManager()
			try {
				addLog ("Delete target dir...")
				fm.deleteFile(targetDir)
			} catch(all) {
			}

			addLog("Create target dir...")
			si.mkdir(targetDir)
		}
	}

	public void tearDown() {

		for ( si in serviceInterfaces ) {
			FileManager fm = GrisuRegistryManager.getDefault(si).getFileManager()
			try {
				addLog("Delete target dir...")
				fm.deleteFile(targetDir)
			} catch(all) {
			}
		}
	}
}
