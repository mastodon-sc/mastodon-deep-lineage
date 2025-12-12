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
package org.mastodon.mamut.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils
{

	private ZipUtils()
	{
		// prevent instantiation
	}

	private static final int BUFFER_SIZE = 8192;

	private static final long MAX_ZIP_ENTRY_SIZE = 200 * 1024 * 1024L; // 200 MB limit for uncompressed files for safety

	/**
	 * Downloads a file from the given URL into the given target path.
	 */
	private static void downloadZip( final URL url, final Path targetFile ) throws IOException
	{
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout( 10_000 );
		connection.setReadTimeout( 10_000 );

		try (
				InputStream in = connection.getInputStream();
				OutputStream out = Files.newOutputStream( targetFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING )
		)
		{
			byte[] buffer = new byte[ BUFFER_SIZE ];
			int len;
			while ( ( len = in.read( buffer ) ) > 0 )
			{
				out.write( buffer, 0, len );
			}
		}
	}

	/**
	 * Extracts a ZIP stored at the given path into the target directory.
	 */
	private static void unpackZip( final Path zipFile, final Path targetDir ) throws IOException
	{
		if ( !Files.exists( targetDir ) )
			Files.createDirectories( targetDir );

		try (ZipInputStream zis = new ZipInputStream( Files.newInputStream( zipFile ) ))
		{
			ZipEntry entry;

			while ( ( entry = zis.getNextEntry() ) != null )
			{
				// Reject entries with suspicious names
				if ( entry.getName().contains( ".." ) || entry.getName().startsWith( "/" ) || entry.getName().startsWith( "\\" ) )
					throw new IOException( "Unsafe ZIP entry: " + entry.getName() );

				Path outPath = targetDir.resolve( entry.getName() ).normalize();

				// Protect against ZIP Slip
				if ( !outPath.startsWith( targetDir ) )
					throw new IOException( "Zip entry escaped target dir: " + entry.getName() );

				if ( entry.isDirectory() )
					Files.createDirectories( outPath );
				else
				{

					Files.createDirectories( outPath.getParent() );

					// Additional safety: limit uncompressed entry size (Sonar rule fix)
					copyWithSizeLimit( zis, outPath );
				}
				zis.closeEntry();
			}
		}
	}

	/**
	 * Copies ZIP entry stream safely
	 */
	private static void copyWithSizeLimit( final InputStream in, final Path targetFile ) throws IOException
	{
		try (OutputStream out = Files.newOutputStream( targetFile ))
		{
			byte[] buffer = new byte[ BUFFER_SIZE ];
			long totalBytes = 0;
			int read;

			while ( ( read = in.read( buffer ) ) > 0 )
			{
				totalBytes += read;
				if ( totalBytes > ZipUtils.MAX_ZIP_ENTRY_SIZE )
					throw new IOException( "ZIP entry too large, exceeds safe limit: " + targetFile );
				out.write( buffer, 0, read );
			}
		}
	}

	/**
	 * Safe temp file creation: creates a private temp directory inside the target folder.
	 */
	private static Path createSafeTempFile( final Path parent ) throws IOException
	{
		Path tmpDir = parent.resolve( "_tmp" );
		if ( !Files.exists( tmpDir ) )
			Files.createDirectories( tmpDir );
		return Files.createTempFile( tmpDir, "download", ".zip" );
	}

	/**
	 * Downloads a ZIP from the given URL and unpacks it into the target directory.
	 */
	public static void downloadAndUnpack( final URL url, final Path targetDir ) throws IOException
	{
		Path safeTempZip = createSafeTempFile( targetDir );
		try
		{
			downloadZip( url, safeTempZip );
			unpackZip( safeTempZip, targetDir );
		}
		finally
		{
			Files.deleteIfExists( safeTempZip );
			Files.deleteIfExists( safeTempZip.getParent() );
		}
	}
}
