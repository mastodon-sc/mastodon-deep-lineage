/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
