package org.mastodon.mamut.util;

/**
 * Utility class for formatting byte counts into human-readable string representations.
 */
public class ByteFormatter
{

	private ByteFormatter()
	{
		// Prevent instantiation
	}

	private static final String[] UNITS = {
			"Bytes", "KB", "MB", "GB", "TB", "PB", "EB"
	};

	/**
	 * Converts a byte count into a human-readable string using binary prefixes
	 * (e.g., KB, MB, GB, etc.) with a precision of one decimal place.
	 *
	 * @param bytes the number of bytes to be converted
	 * @return a human-readable string representation of the byte count
	 */
	public static String humanReadableByteCount( long bytes )
	{
		if ( bytes < 1024 )
			return bytes + " Bytes";
		int exp = ( int ) ( Math.log( bytes ) / Math.log( 1024 ) );
		String unit = UNITS[ exp ];
		double value = bytes / Math.pow( 1024, exp );
		return String.format( "%.1f %s", value, unit );
	}
}
