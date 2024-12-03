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
		for ( int i = 0; i < points.length; i++ )
		{
			int x = ( int ) points[ i ][ 0 ];
			int y = ( int ) points[ i ][ 1 ];
			int z = ( int ) points[ i ][ 2 ];
			int resultX = ( int ) result[ i ][ 0 ];
			int resultY = ( int ) result[ i ][ 1 ];
			if ( filter.test( result[ i ] ) )
				g.setColor( Color.RED );
			else
				g.setColor( Color.BLUE );

			logger.debug( "i = {}, x = {}, y = {}, z= {}, dim reduced X = {}, dim reduced Y = {}", i, x, y, z, resultX, resultY );
			g.fillOval( x, y, 5, 5 );
			g.fillRect( resultX + offsetX, resultY + offsetY, 2, 2 );
		}
	}
}
