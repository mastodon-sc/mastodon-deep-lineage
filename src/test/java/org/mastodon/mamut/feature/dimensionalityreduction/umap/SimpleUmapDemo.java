/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
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
package org.mastodon.mamut.feature.dimensionalityreduction.umap;

import tagbio.umap.Umap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import org.mastodon.mamut.feature.dimensionalityreduction.RandomDataTools;

public class SimpleUmapDemo extends JPanel
{

	public static void main( final String[] args )
	{
		double[][] sampleData = RandomDataTools.generateSampleData();
		Umap umap = setUpUmap();
		double[][] umapResult = umap.fitTransform( sampleData );
		plot( sampleData, umapResult );
	}

	private static void plot( final double[][] sampleData, final double[][] umapResult )
	{
		PlotPoints plotPoints = new PlotPoints( sampleData, umapResult );
		JFrame frame = new JFrame( "Simple Umap Demo. Reduction from 3 dimensions to 2." );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setSize( 600, 400 );
		frame.add( plotPoints );
		frame.setVisible( true );
	}

	static Umap setUpUmap()
	{
		Umap umap = new Umap();
		umap.setVerbose( true );
		umap.setNumberComponents( 2 );
		umap.setMinDist( 0.1f );
		umap.setNumberNearestNeighbours( 15 );
		umap.setRandom( new Random( 42 ) );
		return umap;
	}

	private static class PlotPoints extends JPanel
	{

		private final double[][] points;

		private final double[][] umapResult;

		private PlotPoints( final double[][] points, final double[][] umapResult )
		{
			this.points = points;
			this.umapResult = umapResult;
		}

		@Override
		protected void paintComponent( Graphics g )
		{
			super.paintComponent( g );
			for ( int i = 0; i < points.length; i++ )
			{
				int x = ( int ) points[ i ][ 0 ];
				int y = ( int ) points[ i ][ 1 ];
				int z = ( int ) points[ i ][ 2 ];
				int umapX = ( int ) umapResult[ i ][ 0 ];
				int umapY = ( int ) umapResult[ i ][ 1 ];
				if ( umapX > 0 )
					g.setColor( Color.RED );
				else
					g.setColor( Color.BLUE );

				System.out.println( "i = " + i + ", x = " + x + ", y = " + y + ", z= " + z + ", umapX = " + umapX + ", umapY = " + umapY );
				g.fillOval( x, y, 5, 5 );
				g.fillRect( umapX + 200, umapY + 100, 2, 2 );
			}
		}
	}
}
