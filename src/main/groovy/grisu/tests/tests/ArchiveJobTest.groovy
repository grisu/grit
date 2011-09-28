package grisu.tests.tests

import grisu.control.ServiceInterface
import grisu.control.exceptions.NoSuchJobException
import grisu.frontend.model.job.JobObject
import grisu.model.FileManager
import grisu.model.GrisuRegistryManager
import grisu.model.status.StatusObject

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class ArchiveJobTest extends AbstractTest implements Test, PropertyChangeListener {

	private final jobname

	final String group
	final List inputfiles

	final int walltime = 60
	final String application = "UnixCommands"

	final FileManager fm

	String archiveUrl

	private final JobObject job
	private final def ifiles = [:]

	public ArchiveJobTest(ServiceInterface si, int batch, int id, String group, List inputFiles) {
		super(si, batch, id)
		fm = GrisuRegistryManager.getDefault(si).getFileManager()

		this.inputfiles = inputFiles
		this.group = group
		this.jobname = 'archiveTestJob_'+batch+'_'+id
		job = new JobObject(si)
	}

	@Override
	protected void execute() {

		addLog("Kicking off archiving of job...")
		archiveUrl = job.archive()

		addLog("Waiting for archiving of job to finish.")
		StatusObject so =StatusObject.waitForActionToFinish(si, ServiceInterface.ARCHIVE_STATUS_PREFIX+jobname, 5, true, false)

		if (so.getStatus().isFailed()) {
			def ec = so.getStatus().getErrorCause()
			if ( ! ec ) {
				ec = "Archiving of job failed: unknown reason"
			} else {
				ec = "Archiving of job failed: "+ec
			}
			addLog(ec)
			throw new Exception(ec)
		} else {
			addLog("Archiving of job finished.")
		}
	}


	@Override
	protected void check() {


		addLog ('Checking whether all input files are listed in stdout...')
		for ( file in ifiles.keySet() ) {

			addLog("Checking filesize for file: "+file)
			def filename = FileManager.getFilename(file)
			long newSize = fm.getFileSize(job.getJobDirectoryUrl()+"/"+filename)
			if ( newSize != ifiles.get(file)) {
				throw new Exception("Filesize differs for source/jobinput file: "+filename)
			} else {
				addLog("Filesizes match.")
			}
		}

		addLog ("All files exist, test successful.")
		success = true
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
		addLog("Deleting archived jobdir: "+archiveUrl)
		try {
			fm.deleteFile(archiveUrl)
			addLog("Deleting archived jobdir finished.")
		} catch (all) {
			addLog("Deleting archived jobdir failed: "+all.getLocalizedMessage())
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
		job.setCommandline('ls -la')

		for ( def inputfile : inputfiles) {
			job.addInputFileUrl(inputfile)
			long size = fm.getFileSize(inputfile)
			ifiles.put(inputfile, size)
		}

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
