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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class ResourceUtils
{

	public static String readResourceAsString( final String resourcePath, final Class< ? > clazz )
	{
		// Try the absolute path via Class.getResourceAsStream (leading slash allowed)
		InputStream in = clazz.getResourceAsStream( resourcePath );
		// Fallback: try ClassLoader (no leading slash)
		if ( in == null )
		{
			String p = resourcePath.startsWith( "/" ) ? resourcePath.substring( 1 ) : resourcePath;
			in = clazz.getClassLoader().getResourceAsStream( p );
		}
		if ( in == null )
		{
			throw new IllegalStateException( "Resource not found on classpath: " + resourcePath );
		}

		try (InputStream is = in; ByteArrayOutputStream out = new ByteArrayOutputStream())
		{
			byte[] buf = new byte[ 4096 ];
			int r;
			while ( ( r = is.read( buf ) ) != -1 )
			{
				out.write( buf, 0, r );
			}
			return new String( out.toByteArray(), StandardCharsets.UTF_8 );
		}
		catch ( IOException e )
		{
			throw new UncheckedIOException( "Failed to read resource: " + resourcePath, e );
		}
	}
}
