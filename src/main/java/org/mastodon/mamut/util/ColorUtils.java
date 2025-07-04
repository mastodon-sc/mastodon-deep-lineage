package org.mastodon.mamut.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ColorUtils
{
	private ColorUtils()
	{
		// Prevent instantiation
	}

	/**
	 * Generates a list of colors that are variants of the given base color,
	 * varying in saturation.
	 * Saturation is interpolated linearly between the saturation of the given color and 10%.
	 * The first color in the list will be the given color, and the last color will be derived with 0.1 saturation.
	 *
	 * @param baseColor The base color to generate variants from.
	 * @param count The number of saturation variants to generate.
	 * @return A list of colors with decreasing saturation values.
	 */
	public static List< Color > generateSaturationFade( final Color baseColor, final int count )
	{
		return generateSaturationFade( baseColor, count, 0.1 );
	}

	/**
	 * Generates a list of colors that are variants of the given base color,
	 * varying in saturation.
	 * Saturation is interpolated linearly between the saturation of the given color and the given min saturation.
	 * The first color in the list will be the given color, and the last color will be derived with the minimum saturation.
	 *
	 * @param baseColor The base color to generate variants from.
	 * @param count The number of saturation variants to generate.
	 * @param minSaturation The minimum saturation value (0.0 to 1.0).
	 * @return A list of colors with decreasing saturation values.
	 */
	public static List< Color > generateSaturationFade(
			final Color baseColor,
			final int count,
			double minSaturation
	)
	{
		if ( count <= 0 )
			throw new IllegalArgumentException( "Count must be greater than 0" );
		if ( minSaturation < 0 )
			minSaturation = 0.0;
		if ( minSaturation > 1 )
			minSaturation = 1.0;

		List< Color > colors = new ArrayList<>( count );

		// Convert base color to HSB
		float[] hsb = Color.RGBtoHSB( baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null );
		float hue = hsb[ 0 ];
		float brightness = hsb[ 2 ];
		float baseSaturation = hsb[ 1 ];

		// Clamp minSaturation if it exceeds base saturation
		float clampedMinSat = ( float ) Math.min( minSaturation, baseSaturation );

		for ( int i = 0; i < count; i++ )
		{
			double fraction = i / ( double ) ( count - 1 );
			float saturation = baseSaturation - ( float ) ( fraction * ( baseSaturation - clampedMinSat ) );
			Color newColor = Color.getHSBColor( hue, saturation, brightness );
			colors.add( newColor );
		}

		return colors;
	}
}
