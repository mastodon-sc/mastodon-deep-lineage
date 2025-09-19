package org.mastodon.mamut.linking.trackastra;

import java.lang.invoke.MethodHandles;

import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.Benchmark;
import net.imglib2.util.Cast;

import org.mastodon.Ref;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.tracking.linking.graph.AbstractGraphParticleLinkerOp;
import org.mastodon.tracking.linking.graph.GraphParticleLinkerOp;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Plugin( type = GraphParticleLinkerOp.class )
public class TrackAstraLinker< V extends Vertex< E > & HasTimepoint & RealLocalizable & Ref< V >, E extends Edge< V > >
		extends AbstractGraphParticleLinkerOp< V, E >
		implements Benchmark
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Override
	public void mutate1( final ReadOnlyGraph< V, E > graph, final SpatioTemporalIndex< V > index )
	{
		logger.info( "perform track astra linking" );
		try (final TrackAstraLinkPrediction trackAstraLinkPrediction =
				new TrackAstraLinkPrediction( settings, featureModel, Cast.unchecked( index ), Cast.unchecked( edgeCreator ) ))
		{
			trackAstraLinkPrediction.compute();
		}
		catch ( Exception e )
		{
			logger.error( "Error during TrackAstra linking", e );
			ok = false;
			errorMessage = e.getMessage();
			return;
		}
		ok = true;
	}

	@Override
	public long getProcessingTime()
	{
		return 0;
	}

	@Override
	public boolean isSuccessful()
	{
		return ok;
	}

	@Override
	public String getErrorMessage()
	{
		return "All good!";
	}
}
