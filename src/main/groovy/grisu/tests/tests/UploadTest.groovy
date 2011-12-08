package grisu.tests.tests

import grisu.control.ServiceInterface
import grisu.model.FileManager
import grisu.model.GrisuRegistryManager
import grisu.tests.testRuns.TestRun

class UploadTest extends AbstractTest implements Test {

	public static setupTestRun(TestRun tr, Map config) {

		def targetDir = config.get("targetDir")

		deleteTargetDir(tr, targetDir)
		filename = FileManager.getFilename(config.get("sourceFile"))
		tr.addRunLog("Getting filesize of sourcefile...")
		filesize = new File(config.get("sourceFile")).length()
		tr.addRunLog("Filesize: "+filesize)
	}

	private static deleteTargetDir(TestRun tr, String targetDir) {
		for ( si in tr.getServiceInterfaces().values() ) {
			FileManager fm = GrisuRegistryManager.getDefault(si).getFileManager()
			try {
				tr.addRunLog ("Delete target dir...")
				fm.deleteFile(targetDir)
			} catch(all) {
			}

			tr.addRunLog("Create target dir...")
			si.mkdir(targetDir)
		}
	}

	public static teardownTestRun(TestRun tr, Map config) {

		def targetDir = config.get("targetDir")

		deleteTargetDir(tr, targetDir)
	}



	public String sourceFile
	public String targetDir
	private static String filename
	private static long filesize
	private String targetTestDir
	private String remoteFile

	private long resultSize = -1L

	private Map successMap = [:]


	protected void check() {

		success = true
		for ( def s : successMap.values() ) {
			if ( ! s) {
				success = false
				break;
			}
		}

		if ( success ) {
			addLog("Success: all uploads successful.")
			check_comment = "All uploads successful"
		} else {
			addLog("Test failed: at least one upload didn't succeed.")
			check_comment = "At least one upload didn't succeed"
		}
	}

	protected void execute() {
		for ( i in 1..repetitions ) {
			addLog ("Uploading file... ("+i+". time)")
			fm.uploadUrlToDirectory(sourceFile, targetTestDir, true)
			addLog ("Upload finished.")

			addLog("Getting filesize for file "+remoteFile)
			resultSize = fm.getFileSize(remoteFile)
			addLog("Filesize for file "+remoteFile+": "+resultSize)

			boolean s = (resultSize == filesize)
			successMap[i] = s
			if (s) {
				addLog("Upload successful: filesizes are the same")
			} else {
				addLog("Upload failed, filesizes differ")
			}

			addLog("Deleting remote file: "+remoteFile)
			fm.deleteFile(remoteFile)
			addLog("Remote file deleted.")
		}
	}


	protected void setup() {
		this.targetTestDir = targetDir+"/"+getCertName()+"/"+getParallelId()
		this.remoteFile = targetTestDir + "/"+filename

		addLog("Creating target directory: "+this.targetTestDir)
		si.mkdir(this.targetTestDir)
		addLog("Target directory created.")
	}

	protected void tearDown() {
	}
}
