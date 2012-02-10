package grisu.tests.tests

import grisu.model.FileManager
import grisu.tests.testRuns.TestRun

class DownloadTest extends AbstractTest implements Test {

	public static void setupTestRun(TestRun tr, Map config) {

		def url = config.get("sourceUrl")
		tr.addRunLog("Getting filesize of source file: "+url)
		filesize = tr.getServiceInterfaces().values().iterator().next().getFileSize(url)
		tr.addRunLog("Filesize: "+filesize)
	}

	public String sourceUrl
	private static long filesize

	private File file

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
			addLog("Success: all downloads successful.")
			check_comment = "All downloads successful."
		} else {
			addLog("Test failed: at least one download didn't succeed.")
			check_comment = "At least one download didn't succeed."
		}
	}

	protected void execute() {
		for ( int i=1; i<=this.repetitions; i++ ) {
			addLog ("Downloading file... ("+i+". time)")
			this.file = fm.downloadFile(sourceUrl, true)

			addLog("Getting filesize for local file "+this.file.toString())
			resultSize = fm.getFileSize(this.file.getAbsolutePath())
			addLog("Filesize for file "+this.file.toString()+": "+resultSize)
			boolean s = (resultSize == filesize)
			successMap[i] = s
			if ( s ) {
				addLog("Success: local file exists and has got right filesize")
			} else {
				addLog("Error: local file has got wrong filesize")
			}
			addLog("Deleting local file...")
			this.file.delete()
			addLog("File deleted.")
			addLog ("Download finished ("+i+".time)")
		}
	}


	protected void setup() {
	}

	protected void tearDown() {
		addLog("Deleting local file: "+this.file.getAbsolutePath())
		this.file.delete()
		addLog("Local file deleted.")
	}
}
