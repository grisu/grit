package grisu.tests.tests

import grisu.control.ServiceInterface
import grisu.jcommons.utils.FileAndUrlHelpers
import grisu.model.FileManager
import grisu.model.GrisuRegistryManager

class CopyTest extends AbstractTest implements Test {

	public static setupTestRun(List<ServiceInterface> sis, Map config) {

		def targetDir = config.get("targetDir")
		deleteTargetDir(sis, targetDir)

		addRunLog("Getting filesize to test against after test execution...")
		def sourceUrl = config.get("sourceUrl")
		filesize = sis.get(0).getFileSize(sourceUrl)
		addRunLog("Filesize of "+sourceUrl+": "+filesize)
		filename = FileAndUrlHelpers.getFilename(sourceUrl)
	}

	private static deleteTargetDir(List<ServiceInterface> sis, String targetDir) {
		for ( si in sis ) {
			FileManager fm = GrisuRegistryManager.getDefault(si).getFileManager()
			try {
				addRunLog ("Delete target dir: "+targetDir)
				fm.deleteFile(targetDir)
			} catch(all) {
			}
		}
	}

	public static teardownTestRun(List<ServiceInterface> sis, Map config) {

		def targetDir = config.get("targetDir")

		deleteTargetDir(sis, targetDir)
	}

	public String sourceUrl
	private static String filename
	private static long filesize
	public String targetDir
	private String testTargetDir
	private String remoteFile

	public int repetitions = 1

	private long resultSize = -1L


	protected void check() {

		addLog("Getting filesize for file "+remoteFile)
		resultSize = fm.getFileSize(remoteFile)
		addLog("Filesize for file "+remoteFile+": "+resultSize)

		success = (resultSize == filesize)
		if (success) {
			addLog("Test successful")
		} else {
			addLog("Test failed, filesizes differ")
		}
	}

	protected void execute() {

		for ( i in 1..repetitions ) {

			addLog ("Copying file... ("+i+". time)")
			fm.cp(sourceUrl, testTargetDir, true)
			addLog ("Copying finished ("+i+". time)")
		}
	}


	protected void setup() {

		this.testTargetDir = targetDir+"/"+getBatchId()+"/"+getParallelId()
		this.remoteFile = testTargetDir + "/"+filename
	}

	protected void tearDown() {
	}
}
