package grisu.tests.tests

import grisu.control.ServiceInterface
import grisu.control.exceptions.NoSuchJobException
import grisu.frontend.model.job.JobObject
import grisu.model.FileManager
import grisu.model.GrisuRegistryManager

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class LsJobTest extends AbstractTest implements Test, PropertyChangeListener {

	private final jobname

	final String group
	final List inputfiles

	final int walltime = 60
	final String application = "UnixCommands"

	final FileManager fm


	private final JobObject job
	private final def ifiles = [:]

	public LsJobTest(ServiceInterface si, int batch, int id, String group, List inputFiles) {
		super(si, batch, id)
		fm = GrisuRegistryManager.getDefault(si).getFileManager()

		this.inputfiles = inputFiles
		this.group = group
		this.jobname = 'testJob_'+batch+'_'+id
		job = new JobObject(si)
	}

	protected void check() {

		addLog ('Waiting for job to finish...')
		job.waitForJobToFinish(5)
		addLog ('Job finished.')

		addLog ('Downloading stdout...')
		String stdout = job.getStdOutContent()
		addLog ('Checking whether all input files are listed in stdout...')
		for ( file in ifiles.keySet() ) {
			addLog("Checking file: "+file)
			def filename = FileManager.getFilename(file)

			if ( ! stdout.contains(filename) ) {
				throw new Exception("Stdout does not contain filename: "+filename)
			} else {
				addLog("Filename "+filename+" found.")
			}

			addLog("Checking filesize.")
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

	protected void execute() {


		addLog("Submitting job...")
		job.submitJob(true)
		addLog("Test finished")

		job.removePropertyChangeListener(this)
	}

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
	}

	protected void tearDown() {

		si.kill(this.jobname, true)
	}


	void propertyChange(PropertyChangeEvent e) {

		if (!e.getPropertyName().equals("submissionLog") && !e.getPropertyName().equals("status") ) {
			addLog("Submission property "+e.getPropertyName()+" changed to "+e.getNewValue())
		}
	}
}
