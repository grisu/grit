package grisu.tests.log

import grisu.tests.testRuns.TestRun
import grisu.tests.tests.AbstractTest

import java.text.DecimalFormat

class DefaultTestLogger implements TestLogger {

	private def format = new DecimalFormat('0000000')

	public boolean displayTestLogs = false
	private TestRun tr

	public boolean disableFileOutput = false

	private int success = 0
	private int failed = 0

	private long started
	private long ended

	private def failedTests = []
	private def checkedTests = []

	private boolean init = false

	public String logDir = null

	public DefaultTestLogger(TestRun tr, boolean displayTestLogs, File logdir=new File('.')) {
		setTestRun(tr)
		this.logDir = logdir
		this.displayTestLogs = displayTestLogs
	}

	private DefaultTestLogger() {
	}


	public void setTestRun(TestRun tr) {
		this.tr = tr
		getLogFile(tr).delete()
		getLogFile(tr).createNewFile()
	}

	public void addTestLogMessage(AbstractTest source, long timestamp, String msg) {

		if ( displayTestLogs ) {
			println '\tTest '+source.getName()+': '+msg
		}

		writeToLogFile(source.getTestRun(), source.getName(), timestamp, msg)
	}

	public String getLogDir() {
		if ( ! logDir ) {
			return "."
		} else {
			return this.logDir
		}
	}

	private void setLogDir(String logDir) {
		this.logDir = logDir;
	}

	private void writeToLogFile(TestRun tr, String testName, long timestamp, String msg) {

		if ( disableFileOutput ) {
			return
		}

		if ( ! init ) {
			if ( ! new File(this.logDir).exists() ) {
				boolean success = new File(this.logDir).mkdirs()
			}
			init = true
		}

		synchronized (tr) {
			String line = format.format(timestamp-tr.testrunCreated)

			if (testName) {
				line = line + '    ['+testName+']\t'
				getLogFile(tr).append(line+msg+'\n')
			} else if ( timestamp >= 0 ){
				line = line + '  '+tr.getName()+'\t'
				getLogFile(tr).append(line+msg+'\n')
			} else {
				getLogFile(tr).append('\n\n'+msg+'\n')
			}
		}
	}

	private File getLogFile(TestRun tr) {
		File logFile = new File(getLogDir(), tr.getName()+'_'+tr.testrunCreated+'.log')
		return logFile
	}

	public void addTestRunLogMessage(TestRun tr, long timestamp, String msg) {


		println '['+tr.getName()+']: '+msg

		writeToLogFile(tr, '', timestamp, msg)
	}

	void testExecuted(AbstractTest test, boolean success) {
		if (! success ) {
			String msg = 'n/a'
			if ( test.getExecuteException() ) {
				msg = test.getExecuteException().getLocalizedMessage()
			}
			println '\tTest '+test.getName()+' failed to execute: '+msg
		} else {
			println '\tTest '+test.getName()+' executed'
		}
	}

	void testChecked(AbstractTest test, boolean suceeded) {
		checkedTests.add(test)
		if (! suceeded) {
			String msg = 'n/a'
			if ( test.getCheckException() ) {
				msg = test.getCheckException().getLocalizedMessage()
			}
			println '\tTest '+test.getName()+' failed: '+msg
			failed++
			failedTests.add(test)
		} else {
			println '\tTest '+test.getName()+' success!'
			success++
		}
	}

	void getStatus() {
		String msg = 'Failed tests: '+failed+'\n'
		if ( failed ) {
			msg += println '\tError(s):\n'
			for ( def test : failedTests ) {
				msg += '\t\t'+ test.getName()+': '+test.getCheckComment()+'\n'
			}
		}
		msg += 'Successful tests: '+success+'\n\n'
		msg += 'Details:\n'
		for ( AbstractTest t : checkedTests ) {
			msg += '\t'+t.getName()+': '+t.getCheckComment()+'\n\n'
		}
		msg += 'Duration: '+(tr.getDuration()/1000)+' seconds\n'

		println msg

		writeToLogFile(tr, null, -1, msg)
	}
}
