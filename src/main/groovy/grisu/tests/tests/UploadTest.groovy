package grisu.tests.tests

import grisu.control.ServiceInterface
import grisu.jcommons.utils.FileAndUrlHelpers
import grisu.model.FileManager
import grisu.model.GrisuRegistryManager
import grisu.tests.util.Input

class UploadTest extends AbstractTest implements Test {



	final static String DEFAULT_FILE = 'test0.txt'

	final FileManager fm

	final String file
	final String filename
	final long filesize
	final String target
	final String remoteDir
	final String remoteFile

	long resultSize = -1L

	public UploadTest(ServiceInterface si, int batch, int id, String target) {
		this(si, batch, id, Input.getFile(DEFAULT_FILE), target)
	}

	public UploadTest(ServiceInterface si, int batch, int id, String source, String target) {
		super(si, batch, id)
		this.fm = GrisuRegistryManager.getDefault(si).getFileManager()
		this.target = target

		this.file = source
		this.filename = FileAndUrlHelpers.getFilename(file)
		this.filesize = new File(file).length()
		this.remoteDir = target+"/"+id
		this.remoteFile = remoteDir + "/"+filename
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

		addLog ("Uploading file...")
		fm.uploadUrlToDirectory(file, remoteDir, false)
		addLog ("Upload finished.")
	}


	protected void setup() {
	}

	protected void tearDown() {
	}
}
