package grisu.tests.tests

import grisu.control.ServiceInterface
import grisu.frontend.model.job.JobObject
import grisu.model.FileManager
import grisu.tests.util.Input

import java.beans.PropertyChangeEvent

import com.google.common.collect.Lists

class DeleteBigFolderTest extends AbstractTest implements Test {

	public static void setupTestRun(List<ServiceInterface> sis, Map config) {

		killAllJobsWithPrefix(sis, config)
	}


	public static void teardownTestRun(List<ServiceInterface> sis, Map config) {

		killAllJobsWithPrefix(sis, config)
	}

	protected List jobdirs = Collections.synchronizedList(Lists.newArrayList())


	public int no_of_files = 10
	public int no_of_folders = 10


	@Override
	protected void execute() {

		jobdirs.each { jobdir ->
			addLog("Deleting dir: "+jobdir)
			fm.deleteFile(jobdir);
			addLog("Deleted: "+jobdir)
		}
	}

	@Override
	protected void check() {

		success = true
		jobdirs.each { jobdir ->
			addLog("Checking whether folder still exists: "+jobdir)
			if ( fm.fileExists(jobdir) ) {
				addLog("Folder still exists, clean job failed: "+jobdir)
				success = false
			} else {
				addLog("Folder deleted, test successful.")
			}
		}
	}

	@Override
	protected void tearDown() {
	}

	@Override
	protected void setup() {

		this.application = 'UnixCommands'
		this.walltime = 60
		this.commandline = 'sh createFiles.sh '+no_of_folders+' '+no_of_files+' test0.txt'

		def script = Input.getFile('createFiles.sh')
		def file = Input.getFile('test0.txt')

		this.inputfiles = [script, file]

		prepareAndSubmitAllJobs(true)

		addLog("Getting all jobdirectories...")

		jobs.each { job ->
			String jobdir = job.getJobDirectoryUrl()
			jobdirs.add(jobdir)
			addLog("Adding jobdir: "+jobdir)
		}

		addLog("All jobdirectories added.")
	}
}
