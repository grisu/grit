package grisu.tests.util

import org.apache.commons.io.IOUtils

class Input {

	static File INPUT_FILES_DIR = new File(System.getProperty("java.io.tmpdir"), "grisu-integration-input");


	static String getFile(String fileName) {

		File file = new File(INPUT_FILES_DIR, fileName)

		if (!file.exists()) {
			INPUT_FILES_DIR.mkdirs()
			InputStream inS = Input.class.getResourceAsStream("/" + fileName)
			IOUtils.copy(inS, new FileOutputStream(file))
			inS.close()
		}

		return file.getAbsolutePath()
	}
}
