package org.mastodon.mamut.linking.trackastra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mastodon.mamut.linking.trackastra.TrackastraUtils.KEY_EDGE_THRESHOLD;
import static org.mastodon.mamut.linking.trackastra.TrackastraUtils.KEY_NUM_DIMENSIONS;
import static org.mastodon.mamut.linking.trackastra.TrackastraUtils.KEY_SOURCE;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MAX_TIMEPOINT;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.ProjectManagerTest;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.tracking.linking.EdgeCreator;
import org.mastodon.tracking.linking.graph.AbstractGraphParticleLinkerOp;
import org.mastodon.tracking.mamut.linking.LinkCostFeature;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

class TrackastraLinkerTest
{
	private final Path exampleProject3D = resourceAsFile( "/org/mastodon/mamut/linking/trackastra/tgmm-mini_27_31.mastodon" );

	private final Path exampleProject2D = resourceAsFile( "/org/mastodon/mamut/linking/trackastra/ctc_fluo_n2dl_hela_11_15.mastodon" );

	private final Context context = new Context();

	@Disabled( "This test is disabled, because it has very long runtime (> 5 minutes)" )
	@Test
	void testLinking3D() throws IOException, SpimDataException, NoSuchFieldException, IllegalAccessException
	{
		final MamutProject project = MamutProjectIO.load( exampleProject3D.toFile().getAbsolutePath() );
		final ProjectModel appModel = ProjectLoader.open( project, context, false, true );
		ModelGraph graph = appModel.getModel().getGraph();
		assertEquals( 10, graph.vertices().size() );
		assertEquals( 0, graph.edges().size() );
		TrackastraLinker< Spot, Link > linker = new TrackastraLinker<>();
		Map< String, Object > settingsMap = TrackastraUtils.getDefaultTrackAstraSettingsMap();
		settingsMap.put( KEY_MAX_TIMEPOINT, 4 );
		settingsMap.put( KEY_SOURCE, appModel.getSharedBdvData().getSources().get( 0 ).getSpimSource() );
		settingsMap.put( KEY_NUM_DIMENSIONS, 3 );
		settingsMap.put( KEY_EDGE_THRESHOLD, 0 );
		Field settings = AbstractGraphParticleLinkerOp.class.getDeclaredField( "settings" );
		settings.setAccessible( true );
		settings.set( linker, settingsMap );

		org.scijava.log.Logger log = context.getService( org.scijava.log.LogService.class ).subLogger( "TrackastraLinkerTest" );
		Field logger = AbstractGraphParticleLinkerOp.class.getDeclaredField( "logger" );
		logger.setAccessible( true );
		logger.set( linker, log );

		Field edgeCreator = AbstractGraphParticleLinkerOp.class.getDeclaredField( "edgeCreator" );
		EdgeCreator< Spot > spotEdgeCreator = new MyEdgeCreator( appModel.getModel().getGraph() );
		edgeCreator.setAccessible( true );
		edgeCreator.set( linker, spotEdgeCreator );

		linker.mutate1( graph, appModel.getModel().getSpatioTemporalIndex() );
		assertEquals( 10, graph.vertices().size() );
		assertEquals( 8, graph.edges().size() );
		// close the project
		appModel.close();
	}

	@Disabled( "This test is disabled, because it has very long runtime (> 5 minutes)" )
	@Test
	void testLinking2D() throws IOException, SpimDataException, NoSuchFieldException, IllegalAccessException
	{
		final MamutProject project = MamutProjectIO.load( exampleProject2D.toFile().getAbsolutePath() );
		final ProjectModel appModel = ProjectLoader.open( project, context, false, true );
		ModelGraph graph = appModel.getModel().getGraph();
		assertEquals( 13, graph.vertices().size() );
		assertEquals( 0, graph.edges().size() );
		TrackastraLinker< Spot, Link > linker = new TrackastraLinker<>();
		Map< String, Object > settingsMap = TrackastraUtils.getDefaultTrackAstraSettingsMap();
		settingsMap.put( KEY_MAX_TIMEPOINT, 4 );
		settingsMap.put( KEY_SOURCE, appModel.getSharedBdvData().getSources().get( 0 ).getSpimSource() );
		settingsMap.put( KEY_NUM_DIMENSIONS, 2 );
		settingsMap.put( KEY_EDGE_THRESHOLD, 0 );
		Field settings = AbstractGraphParticleLinkerOp.class.getDeclaredField( "settings" );
		settings.setAccessible( true );
		settings.set( linker, settingsMap );

		org.scijava.log.Logger log = context.getService( org.scijava.log.LogService.class ).subLogger( "TrackastraLinkerTest" );
		Field logger = AbstractGraphParticleLinkerOp.class.getDeclaredField( "logger" );
		logger.setAccessible( true );
		logger.set( linker, log );

		Field edgeCreator = AbstractGraphParticleLinkerOp.class.getDeclaredField( "edgeCreator" );
		EdgeCreator< Spot > spotEdgeCreator = new MyEdgeCreator( appModel.getModel().getGraph() );
		edgeCreator.setAccessible( true );
		edgeCreator.set( linker, spotEdgeCreator );

		linker.mutate1( graph, appModel.getModel().getSpatioTemporalIndex() );
		assertEquals( 13, graph.vertices().size() );
		assertEquals( 11, graph.edges().size() );
		// close the project
		appModel.close();
	}

	private Path resourceAsFile( final String resourceName )
	{
		try
		{
			return Paths.get( Objects.requireNonNull( ProjectManagerTest.class.getResource( resourceName ) ).toURI() );
		}
		catch ( final URISyntaxException e )
		{
			throw new RuntimeException( e );
		}
	}

	private static class MyEdgeCreator implements EdgeCreator< Spot >
	{

		private final ModelGraph graph;

		private final Link ref;

		private final LinkCostFeature linkCostFeature;

		private MyEdgeCreator( final ModelGraph graph )
		{
			this.graph = graph;
			this.linkCostFeature = new LinkCostFeature( graph.edges().getRefPool() );
			this.ref = graph.edgeRef();
		}

		@Override
		public void createEdge( final Spot source, final Spot target, final double edgeCost )
		{
			final Link link = graph.addEdge( source, target, ref ).init();
			linkCostFeature.set( link, edgeCost );
		}

		@Override
		public void preAddition()
		{
			graph.getLock().writeLock().lock();
		}

		@Override
		public void postAddition()
		{
			graph.getLock().writeLock().unlock();
		}
	}
}
