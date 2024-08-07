package org.mastodon.mamut;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestUtils
{

	/**
	 * Creates a temporary copy of the specified file and returns the temporary file.
	 * <br>
	 * This method creates a temporary file with a ".mastodon" extension, copies the contents of the specified file into the temporary file,
	 * and returns the temporary file. The temporary file is set to be deleted on exit.
	 *
	 * @param fileName The name of the file to be copied.
	 * @param prefix   The prefix string to be used in generating the temporary file's name.
	 * @param suffix   The suffix string to be used in generating the temporary file's name.
	 * @return A temporary File object containing the copy of the specified file.
	 * @throws IOException If an I/O error occurs during file creation or copying.
	 */
	public static File getTempFileCopy( final String fileName, final String prefix, final String suffix ) throws IOException
	{
		File tempFile1 = Files.createTempFile( prefix, suffix ).toFile();
		tempFile1.deleteOnExit();
		FileUtils.copyFile( new File( fileName ), tempFile1 );
		return tempFile1;
	}
}
