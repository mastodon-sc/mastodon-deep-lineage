package org.mastodon.mamut.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.regex.Pattern;

public class MathUtils
{

	private static final Pattern REMOVE_TRAILING_ZEROS_PATTERN = Pattern.compile( "0*$" );

	private static final Pattern REMOVE_TRAILING_DECIMAL_POINT_PATTERN = Pattern.compile( "\\.$" );

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

		BigDecimal bigDecimal = BigDecimal.valueOf( value );
		MathContext mathContext = new MathContext( significantDigits, RoundingMode.HALF_UP );
		BigDecimal rounded = bigDecimal.round( mathContext );
		String result = rounded.toPlainString();

		// If the result is an integer, remove the decimal part
		if ( result.indexOf( '.' ) > -1 )
		{
			result = removeTrailingZerosAfterDecimalPoint( result );
			result = removeTrailingDecimalPoint( result );
		}
		return result;
	}

	private static String removeTrailingZerosAfterDecimalPoint( final String input )
	{
		return REMOVE_TRAILING_ZEROS_PATTERN.matcher( input ).replaceFirst( "" );
	}

	private static String removeTrailingDecimalPoint( final String input )
	{
		return REMOVE_TRAILING_DECIMAL_POINT_PATTERN.matcher( input ).replaceFirst( "" );
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
