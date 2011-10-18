package grisu.tests.tests

import grisu.control.ServiceInterface
import grisu.model.FileManager

import org.apache.commons.io.FileUtils

import com.google.common.collect.Lists

class DownloadJobTest extends AbstractTest implements Test {

	List downloadFolders = Collections.synchronizedList(Lists.newArrayList())
	List jobdirs = Collections.synchronizedList(Lists.newArrayList())
	def failedTests = []

	private static File downloadDir = new File(System.getProperty("java.io.tmpdir"), "test-downloads")

	private final static Map ifiles = [:]
	private final List failures = []

	public static void setupTestRun(List<ServiceInterface> sis, Map config) {

		FileUtils.deleteDirectory(downloadDir)

		killAllJobsWithPrefix(sis, config)

		List files = config.get("inputfiles")

		ServiceInterface si = sis[0]

		addRunLog("Getting filesizes of input files...")
		for ( def tmp : files ) {
			def file = FileManager.getFilename(tmp)
			long size = si.getFileSize(tmp)
			ifiles[file] = size
			addRunLog("Filesize for "+file+": "+size)
		}
	}

	public static void teardownTestRun(List<ServiceInterface> sis, Map config) {

		killAllJobsWithPrefix(sis, config.get("jobname_prefix"))

		FileUtils.deleteDirectory(downloadDir)
	}



	@Override
	protected void execute() {

		jobs.each { job ->
			def url = job.getJobDirectoryUrl()
			File downloadTestDir = new File(downloadDir, job.getJobname())
			downloadFolders.add(downloadTestDir)
			addLog("Downloading "+ url + " to: "+downloadTestDir.getAbsolutePath())
			fm.downloadUrl(url, downloadDir, true)
			addLog("Download finished.")
		}
	}


	@Override
	protected void check() {

		downloadFolders.each { folder ->

			for ( f in ifiles ) {
				def name = f.key
				def isize = f.value

				File dFile = new File(folder, name)
				if ( dFile.exists() ) {
					def dSize = dFile.length()
					addLog("Checking filesize for file "+name)
					addLog("Expected size: "+isize+" / size: "+dSize)
					if ( dSize != isize ) {
						addLog("Downloaded file "+name+" has wrong filesize in folder: "+folder.getAbsolutePath())
						failures.add(dFile)
					} else {
						addLog("File "+name+" exists and has right filesize in folder "+folder.getAbsolutePath())
					}
				} else {
					addLog("Input file "+name+" doesn't exist in downloaded job folder "+folder.getAbsolutePath())
					failures.add(dFile)
				}
			}
		}

		if ( failures ) {
			addLog(failures.size()+" downloads failed: ")
			for ( f in failures ) {
				addLog("\t"+f.getAbsolutePath())
			}
			success = false
		} else {
			addLog("All downloads finished successfully.")
			success = true
		}
	}

	@Override
	protected void tearDown() {
	}

	@Override
	protected void setup() {

		prepareAndSubmitAllJobs(true)
	}
}
