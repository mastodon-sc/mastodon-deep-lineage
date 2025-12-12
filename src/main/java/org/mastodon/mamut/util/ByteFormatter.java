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
