package grisu.tests.tests

import grisu.control.ServiceInterface
import grisu.jcommons.utils.FileAndUrlHelpers
import grisu.model.FileManager
import grisu.model.GrisuRegistryManager

class UploadTest extends AbstractTest implements Test {

	public static setupTestRun(List<ServiceInterface> sis, Map config) {

		def targetDir = config.get("targetDir")

		deleteTargetDir(sis, targetDir)
	}

	private static deleteTargetDir(List<ServiceInterface> sis, String targetDir) {
		for ( si in sis ) {
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

	public static teardownTestRun(List<ServiceInterface> sis, Map config) {

		def targetDir = config.get("targetDir")

		deleteTargetDir(sis, targetDir)
	}



	final static String DEFAULT_FILE = 'test0.txt'

	final FileManager fm

	final String file
	final String filename
	final long filesize
	final String target
	final String remoteDir
	final String remoteFile

	final int repeats

	long resultSize = -1L


	public UploadTest(ServiceInterface si, int batch, int id, String source, String target, int repeats) {
		super(si, batch, id)
		this.fm = GrisuRegistryManager.getDefault(si).getFileManager()
		this.target = target

		this.file = source
		this.filename = FileAndUrlHelpers.getFilename(file)
		this.filesize = new File(file).length()
		this.remoteDir = target+"/"+id
		this.remoteFile = remoteDir + "/"+filename

		this.repeats = repeats
	}

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
		for ( i in 1..repeats ) {
			addLog ("Uploading file... ("+i+". time)")
			fm.uploadUrlToDirectory(file, remoteDir, true)
			addLog ("Upload finished.")
		}
	}


	protected void setup() {
	}

	protected void tearDown() {
	}
}
