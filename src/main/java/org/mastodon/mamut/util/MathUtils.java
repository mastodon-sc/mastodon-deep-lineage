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

	/**
	 * Divides the numerator by the denominator and subsequently rounds up to the next Integer value.
	 * <br>
	 * Some examples:
	 * <br>
	 * <ul>
	 *     <li>timepoints: 1,  frame rate reduction:  1 -&gt;  1 frame</li>
	 *     <li>timepoints: 1,  frame rate reduction: 10 -&gt;  1 frame</li>
	 *     <li>timepoints: 10, frame rate reduction: 10 -&gt;  1 frame</li>
	 *     <li>timepoints: 10, frame rate reduction:  1 -&gt; 10 frames</li>
	 *     <li>timepoints: 11, frame rate reduction: 10 -&gt;  2 frames</li>
	 *     <li>timepoints: 20, frame rate reduction: 10 -&gt;  2 frames</li>
	 * </ul>
	 *
	 * @param numerator the numerator
	 * @param denominator the denominator
	 * @return the resulting value
	 */
	public static int divideAndRoundUp( int numerator, int denominator )
	{
		return ( numerator + denominator - 1 ) / denominator;
	}
}
