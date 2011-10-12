package grisu.tests.tests

import grisu.control.ServiceInterface
import grisu.model.FileManager
import grisu.model.GrisuRegistryManager

class DownloadTest extends AbstractTest implements Test {



	final static String DEFAULT_FILE = 'test0.txt'

	final FileManager fm

	final String source
	long filesize

	private File file

	private final int rep

	long resultSize = -1L


	public DownloadTest(ServiceInterface si, int batch, int id, String source, int r) {
		super(si, batch, id)
		this.fm = GrisuRegistryManager.getDefault(si).getFileManager()

		this.source = source
		this.rep = r
	}

	protected void check() {

		addLog("Getting filesize for local file "+this.file.toString())
		resultSize = fm.getFileSize(this.file.getAbsolutePath())
		addLog("Filesize for file "+this.file.toString()+": "+resultSize)

		success = (resultSize == filesize)
		if (success) {
			addLog("Test successful")
		} else {
			addLog("Test failed, filesizes differ")
		}
	}

	protected void execute() {
		for ( int i=1; i<=this.rep; i++ ) {
			addLog ("Downloading file... ("+i+". time)")
			this.file = fm.downloadFile(source, true)

			addLog ("Checking whether local file exists...")
			if ( ! this.file.exists() ) {
				addLog("Error: Local file doesn't exits.")
				throw new Exception("Local file doesn't exist.")
			}
			addLog("Success: Local file exists.")

			addLog ("Download finished.")
		}
	}


	protected void setup() {
		addLog("Getting filesize of source file "+source)
		this.filesize = fm.getFileSize(source)
		addLog("Filesize: "+this.filesize)
	}

	protected void tearDown() {
		addLog("Deleting local file: "+this.file.getAbsolutePath())
		this.file.delete()
		addLog("Local file deleted.")
	}
}
