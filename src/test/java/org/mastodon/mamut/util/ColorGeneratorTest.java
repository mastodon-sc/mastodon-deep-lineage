package org.mastodon.mamut.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.List;

import org.junit.jupiter.api.Test;

class ColorGeneratorTest
{
	@Test
	void testInterpolateZeroStepsThrows()
	{
		assertThrows( IllegalArgumentException.class, () -> ColorGenerator.interpolateColors( Color.BLACK, Color.WHITE, 0 ) );
	}

	@Test
	void testInterpolateOneStep()
	{
		List< Color > result = ColorGenerator.interpolateColors( Color.RED, Color.BLUE, 1 );
		assertEquals( 1, result.size() );
		assertEquals( Color.RED, result.get( 0 ) );
	}

	@Test
	void testInterpolateTwoSteps()
	{
		List< Color > result = ColorGenerator.interpolateColors( Color.RED, Color.BLUE, 2 );
		assertEquals( 2, result.size() );
		assertEquals( Color.RED, result.get( 0 ) );
		assertEquals( Color.BLUE, result.get( 1 ) );
	}

	@Test
	void testInterpolateFiveSteps()
	{
		List< Color > result = ColorGenerator.interpolateColors( Color.BLACK, Color.WHITE, 5 );
		assertEquals( 5, result.size() );
		assertEquals( Color.BLACK, result.get( 0 ) );
		assertEquals( Color.WHITE, result.get( 4 ) );
		// midpoint should be approximately gray
		Color mid = result.get( 2 );
		assertEquals( 128, mid.getRed(), 1 );
		assertEquals( 128, mid.getGreen(), 1 );
		assertEquals( 128, mid.getBlue(), 1 );
	}

	@Test
	void testInterpolateAlpha()
	{
		Color c1 = new Color( 255, 0, 0, 0 ); // transparent red
		Color c2 = new Color( 255, 0, 0, 255 ); // opaque red
		List< Color > result = ColorGenerator.interpolateColors( c1, c2, 3 );
		assertEquals( 3, result.size() );
		assertEquals( 0, result.get( 0 ).getAlpha() );
		assertEquals( 127, result.get( 1 ).getAlpha(), 1 );
		assertEquals( 255, result.get( 2 ).getAlpha() );
	}

	@Test
	void testSaturationFadeZeroThrows()
	{
		assertThrows( IllegalArgumentException.class, () -> ColorGenerator.generateSaturationFade( Color.RED, 0 ) );
	}

	@Test
	void testSaturationFadeOneColor()
	{
		List< Color > result = ColorGenerator.generateSaturationFade( Color.RED, 1 );
		assertEquals( 1, result.size() );
		assertEquals( Color.RED.getRGB(), result.get( 0 ).getRGB() );
	}

	@Test
	void testSaturationFadeThreeColors()
	{
		List< Color > result = ColorGenerator.generateSaturationFade( Color.RED, 3 );
		assertEquals( 3, result.size() );
		// First color: same as base
		assertEquals( Color.RED.getRGB(), result.get( 0 ).getRGB() );
		// Last color: less saturated (should be closer to white)
		Color faded = result.get( 2 );
		assertNotEquals( Color.RED.getRGB(), faded.getRGB() );
	}

	@Test
	void testSaturationFadeWithMinSaturation()
	{
		Color blue = Color.BLUE;
		List< Color > result = ColorGenerator.generateSaturationFade( blue, 5, 0.5 );
		assertEquals( 5, result.size() );
		// Check that saturation decreases
		float[] hsbFirst = Color.RGBtoHSB( result.get( 0 ).getRed(),
				result.get( 0 ).getGreen(), result.get( 0 ).getBlue(), null );
		float[] hsbLast = Color.RGBtoHSB( result.get( 4 ).getRed(),
				result.get( 4 ).getGreen(), result.get( 4 ).getBlue(), null );
		assertTrue( hsbFirst[ 1 ] > hsbLast[ 1 ] );
	}

	@Test
	void testSaturationFadeClampsMinSat()
	{
		Color green = Color.GREEN;
		// Request minSaturation higher than base saturation
		List< Color > result = ColorGenerator.generateSaturationFade( green, 3, 2.0 );
		// Should clamp to base saturation, so all colors same
		assertEquals( green.getRGB(), result.get( 0 ).getRGB() );
		assertEquals( green.getRGB(), result.get( 1 ).getRGB() );
		assertEquals( green.getRGB(), result.get( 2 ).getRGB() );
	}
}
