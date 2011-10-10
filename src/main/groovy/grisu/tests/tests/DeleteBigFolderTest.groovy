package grisu.tests.tests

import grisu.control.ServiceInterface
import grisu.control.exceptions.NoSuchJobException
import grisu.frontend.model.job.JobObject
import grisu.model.FileManager
import grisu.model.GrisuRegistryManager
import grisu.tests.util.Input

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class DeleteBigFolderTest extends AbstractTest implements Test, PropertyChangeListener {

	private final jobname

	final String group

	final int walltime = 60
	final String application = "UnixCommands"

	final FileManager fm

	final String queue

	String jobdir

	private final JobObject job
	final int no_of_files;
	final int no_of_folders;

	public DeleteBigFolderTest(ServiceInterface si, int batch, int id, String group, String queue, int no_of_folders, int no_of_files) {
		super(si, batch, id)
		fm = GrisuRegistryManager.getDefault(si).getFileManager()
		this.queue = queue
		this.no_of_files = no_of_files
		this.no_of_folders = no_of_folders
		this.group = group
		this.jobname = 'deleteBigFolderTest_'+batch+'_'+id
		job = new JobObject(si)
	}

	@Override
	protected void execute() {

		jobdir = job.getJobDirectoryUrl()
		addLog("Deleting folder: "+jobdir)
		fm.deleteFile(jobdir);
		addLog("Folder deleted.")
	}

	@Override
	protected void check() {

		if ( fm.fileExists(jobdir) ) {
			addLog("Folder still exists, clean job failed: "+jobdir)
			success = false
		} else {
			addLog("Folder deleted, test successful.")
			success = true
		}
	}

	@Override
	protected void tearDown() {

		try {
			addLog("Killing job, just in case...")
			si.kill(this.jobname, true)
			addLog("Killing job finished.")
		} catch (NoSuchJobException e) {
			addLog("Killing job not necessary.")
		}
		addLog("Deleting jobdir: "+jobdir)
		try {
			fm.deleteFile(jobdir)
			addLog("Deleting jobdir finished.")
		} catch (all) {
			addLog("Deleting jobdir failed: "+all.getLocalizedMessage())
		}

		job.removePropertyChangeListener(this)
	}

	@Override
	protected void setup() {
		try {
			addLog("Trying to kill possibly existing job "+jobname)
			si.kill(this.jobname, true)
		} catch (NoSuchJobException e) {
		}

		job.addPropertyChangeListener(this)

		job.setJobname(this.jobname)

		job.setApplication(application)
		job.setCommandline('sh createFiles.sh '+no_of_folders+' '+no_of_files+' test0.txt')

		job.setSubmissionLocation(queue)

		def script = Input.getFile('createFiles.sh')
		def file = Input.getFile('test0.txt')
		job.addInputFileUrl(script)
		job.addInputFileUrl(file)


		job.setWalltimeInSeconds(walltime)
		addLog("Creating job...")
		job.createJob(group)

		addLog("Submitting job...")
		job.submitJob(true)

		addLog ('Waiting for job to finish...')
		job.waitForJobToFinish(5)
		addLog ('Job finished.')
	}

	void propertyChange(PropertyChangeEvent e) {

		if (!e.getPropertyName().equals("submissionLog") && !e.getPropertyName().equals("status") ) {
			addLog("Submission property "+e.getPropertyName()+" changed from "+e.getOldValue()+" to "+e.getNewValue())
		}
	}
}
