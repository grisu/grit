package grisu.tests.tests

import grisu.control.ServiceInterface
import grisu.jcommons.utils.FileAndUrlHelpers
import grisu.model.FileManager
import grisu.model.GrisuRegistryManager

class CopyTest extends AbstractTest implements Test {



	final FileManager fm

	final String source
	final String filename
	long filesize
	final String target
	final String remoteDir
	final String remoteFile

	final int repeats

	long resultSize = -1L


	public CopyTest(ServiceInterface si, int batch, int id, String source, String target, int repeats) {
		super(si, batch, id)
		this.fm = GrisuRegistryManager.getDefault(si).getFileManager()
		this.target = target

		this.source = source
		this.filename = FileAndUrlHelpers.getFilename(source)
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

			addLog ("Copying file...")
			fm.cp(source, target, true)
			addLog ("Copying finished.")
		}
	}


	protected void setup() {

		try {
			addLog("Deleting potentially existing target file...")
			fm.deleteFile(remoteFile)
			addLog("Target file deleted.")
		} catch (all) {
			// that's ok
		}
		this.filesize = fm.getFileSize(source)
	}

	protected void tearDown() {
	}
}
