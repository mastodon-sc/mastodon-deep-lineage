package org.mastodon.mamut.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MathUtils
{

	private MathUtils()
	{
		// prevent instantiation
	}

	/**
	 * Round a double to a certain number of significant digits and return it as a string.
	 * <br>
	 * Examples:
	 * <ul>
	 *     <li>1234.5678, 3 -&gt; 1230</li>
	 *     <li>-1234.5678, 3 -&gt; -1230</li>
	 *     <li>0, 3 -&gt; 0</li>
	 *     <li>1.2345678, 1 -&gt; 1</li>
	 *     <li>12345678, 5 -&gt; 12346000</li>
	 *     <li>1.2345678, 3 -&gt; 1.23</li>
	 *     <li>0.00012345678, 3 -&gt; 0.000123</li>
	 * </ul>
	 *
	 * @param value the value to round
	 * @param significantDigits the number of significant digits
	 * @return the rounded value as a string
	 */
	public static String roundToSignificantDigits( double value, int significantDigits )
	{
		if ( value == 0 )
			return "0";
		final double d = Math.ceil( Math.log10( value < 0 ? -value : value ) );
		final int power = significantDigits - ( int ) d;

		final double magnitude = Math.pow( 10, power );
		final long shifted = Math.round( value * magnitude );
		double result = shifted / magnitude;
		// avoid decimal points for integer values
		if ( result == ( long ) result )
			return String.valueOf( ( long ) result );
		else
		{
			// avoid scientific notation for numbers with absolute value < 1
			if ( Math.abs( result ) < 1 )
			{
				int zeros = countZerosAfterDecimalPoint( result );
				DecimalFormat decimalFormat = new DecimalFormat( "0.00" );
				DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
				decimalFormat.setDecimalFormatSymbols( symbols );
				decimalFormat.setMaximumFractionDigits( zeros + significantDigits );
				return String.valueOf( decimalFormat.format( result ) );
			}
			else
			{
				DecimalFormat format = new DecimalFormat();
				char decimalSeparator = format.getDecimalFormatSymbols().getDecimalSeparator();
				return String.valueOf( result ).replace( '.', decimalSeparator );
			}
		}
	}

	/**
	 * Divides the numerator by the denominator and subsequently rounds up to the next Integer value.
	 * <br>
	 * Some examples:
	 * <br>
	 * <ul>
	 *     <li>numerator: 1,  denominator:  1 -&gt;  1</li>
	 *     <li>numerator: 1,  denominator: 10 -&gt;  1</li>
	 *     <li>numerator: 10, denominator: 10 -&gt;  1</li>
	 *     <li>numerator: 10, denominator:  1 -&gt; 10</li>
	 *     <li>numerator: 11, denominator: 10 -&gt;  2</li>
	 *     <li>numerator: 20, denominator: 10 -&gt;  2</li>
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

	/**
	 * Counts the number of zeros after the decimal point of the given number before the first non-zero digit.<br>
	 * For numbers greater or equal to 1, 0 is returned.
	 * If the number is 0, 0 is returned.
	 * E.g.
	 * <ul>
	 *     <li>5.01 -&gt; 0</li>
	 *     <li>0.1 -&gt; 0</li>
	 *     <li>0.01 -&gt; 1</li>
	 *     <li>0.001 -&gt; 2</li>
	 * </ul>
	 * @param number the number to count the zeros after the decimal point
	 * @return the number of zeros after the decimal point of the given number before the first non-zero digit
	 */
	public static int countZerosAfterDecimalPoint( final double number )
	{
		if ( number == 0 )
			return 0;
		return ( int ) Math.max( 0, -Math.floor( Math.log10( Math.abs( number ) ) + 1 ) );
	}
}
