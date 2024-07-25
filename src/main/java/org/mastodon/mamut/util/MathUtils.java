package org.mastodon.mamut.util;

public class MathUtils
{

	private MathUtils()
	{
		// prevent instantiation
	}

	/**
	 * Round a double to a certain number of significant digits.
	 *
	 * @param value the value to round
	 * @param significantDigits the number of significant digits
	 * @return the rounded value
	 */
	public static double roundToSignificantDigits( double value, int significantDigits )
	{
		if ( value == 0 )
			return 0;
		final double d = Math.ceil( Math.log10( value < 0 ? -value : value ) );
		final int power = significantDigits - ( int ) d;

		final double magnitude = Math.pow( 10, power );
		final long shifted = Math.round( value * magnitude );
		return shifted / magnitude;
	}
}
