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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for generating and transforming {@link java.awt.Color} objects.
 * <p>
 * This class provides methods to:
 * <ul>
 *     <li>Create linear gradients between two colors with optional alpha interpolation.</li>
 *     <li>Generate saturation variants of a base color, producing a sequence of
 *         colors that fade towards lower saturation values.</li>
 * </ul>
 *
 * <p>
 * The class is not meant to be instantiated; all methods are {@code static}.
 * </p>
 *
 * <h2>Example usage:</h2>
 *
 * <pre>{@code
 * // Generate a 5-step gradient from red to blue
 * List<Color> gradient = ColorGenerator.interpolateColors(Color.RED, Color.BLUE, 5);
 *
 * // Generate 4 variants of green with decreasing saturation
 * List<Color> faded = ColorGenerator.generateSaturationFade(Color.GREEN, 4);
 * }</pre>
 *
 * <p>
 * All methods will throw {@link IllegalArgumentException} if called with
 * invalid arguments such as non-positive step counts.
 * </p>
 */
public class ColorGenerator
{
	private ColorGenerator()
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
	public static List< Color > generateSaturationFade( final Color baseColor, final int count, double minSaturation )
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

		if ( count == 1 )
			return Collections.singletonList( baseColor );

		for ( int i = 0; i < count; i++ )
		{
			double fraction = i / ( double ) ( count - 1 );
			float saturation = baseSaturation - ( float ) ( fraction * ( baseSaturation - clampedMinSat ) );
			Color newColor = Color.getHSBColor( hue, saturation, brightness );
			colors.add( newColor );
		}
		return colors;
	}

	/**
	 * Creates a list of colors forming a linear gradient between two given colors.
	 *
	 * <ul>
	 *   <li>If {@code steps <= 0}, this method throws an {@link IllegalArgumentException}.</li>
	 *   <li>If {@code steps == 1}, the returned list contains only {@code c1}.</li>
	 *   <li>If {@code steps == 2}, the returned list contains {@code c1} and {@code c2}.</li>
	 *   <li>If {@code steps > 2}, the returned list contains {@code steps} colors:
	 *       starting with {@code c1}, ending with {@code c2}, and intermediate colors
	 *       interpolated linearly between them (including an alpha channel).</li>
	 * </ul>
	 *
	 * @param c1    the starting color (first element of the list), not {@code null}
	 * @param c2    the ending color (last element of the list), not {@code null}
	 * @param steps the number of colors in the gradient must be greater than 0
	 * @return a list of {@link Color} objects representing the gradient
	 * @throws IllegalArgumentException if {@code steps <= 0}
	 */
	public static List< Color > interpolateColors( final Color c1, final Color c2, final int steps )
	{
		if ( steps <= 0 )
			throw new IllegalArgumentException( "steps must be > 0" );

		if ( steps == 1 )
			return Collections.singletonList( c1 );

		if ( steps == 2 )
		{
			final List< Color > result = new ArrayList<>( 2 );
			result.add( c1 );
			result.add( c2 );
			return result;
		}

		final List< Color > gradient = new ArrayList<>( steps );
		for ( int i = 0; i < steps; i++ )
		{
			float ratio = ( float ) i / ( steps - 1 );
			int red = ( int ) ( c1.getRed() + ratio * ( c2.getRed() - c1.getRed() ) );
			int green = ( int ) ( c1.getGreen() + ratio * ( c2.getGreen() - c1.getGreen() ) );
			int blue = ( int ) ( c1.getBlue() + ratio * ( c2.getBlue() - c1.getBlue() ) );
			int alpha = ( int ) ( c1.getAlpha() + ratio * ( c2.getAlpha() - c1.getAlpha() ) );
			gradient.add( new Color( red, green, blue, alpha ) );
		}
		return gradient;
	}
}
