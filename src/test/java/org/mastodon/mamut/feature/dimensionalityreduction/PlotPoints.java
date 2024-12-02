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
package org.mastodon.mamut.feature.dimensionalityreduction;

import java.awt.Color;
import java.awt.Graphics;
import java.lang.invoke.MethodHandles;
import java.util.function.Predicate;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotPoints extends JPanel
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final double[][] points;

	private final double[][] result;

	private final Predicate< double[] > filter;

	public static void plot( final double[][] sampleData, final double[][] umapResult, final Predicate< double[] > filter )
	{
		PlotPoints plotPoints = new PlotPoints( sampleData, umapResult, filter );
		JFrame frame = new JFrame( "Dimensionality Reduction Demo. Reduction from 3 dimensions to 2." );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setSize( 600, 400 );
		frame.add( plotPoints );
		frame.setVisible( true );
	}

	private PlotPoints( final double[][] points, final double[][] result, Predicate< double[] > filter )

	{
		this.points = points;
		this.result = result;
		this.filter = filter;
	}

	@Override
	protected void paintComponent( Graphics g )
	{
		super.paintComponent( g );
		int offsetX = 200;
		int offsetY = 100;
		g.drawLine( -10 + offsetX, offsetY, 10 + offsetX, offsetY );
		g.drawLine( offsetX, -10 + offsetY, offsetX, 10 + offsetY );
		for ( int i = 0; i < result.length; i++ )
		{
			int x = points != null ? ( int ) points[ i ][ 0 ] : 0;
			int y = points != null ? ( int ) points[ i ][ 1 ] : 0;
			int z = points != null ? ( int ) points[ i ][ 2 ] : 0;
			int resultX = ( int ) result[ i ][ 0 ];
			int resultY = ( int ) result[ i ][ 1 ];
			if ( filter != null && filter.test( result[ i ] ) )
				g.setColor( Color.RED );
			else
				g.setColor( Color.BLUE );

			logger.debug( "i = {}, x = {}, y = {}, z= {}, dim reduced X = {}, dim reduced Y = {}", i, x, y, z, resultX, resultY );
			g.fillOval( x, y, 5, 5 );
			g.fillRect( resultX + offsetX, resultY + offsetY, 2, 2 );
		}
	}
}
