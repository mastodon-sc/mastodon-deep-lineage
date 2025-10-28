package org.mastodon.mamut.util.appose;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

class PythonEnvironmentManagerUITest
{
	@Test
	void testPythonEnvironmentManagerUI()
	{
		PythonEnvironmentManagerUI pythonEnvironmentManagerUI = new PythonEnvironmentManagerUI( null );
		int width = 600;
		int height = 600;
		Image image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		Graphics g = image.getGraphics();
		assertDoesNotThrow( () -> pythonEnvironmentManagerUI.paint( g ) );
	}
}
