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
