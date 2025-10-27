package org.mastodon.mamut.linking.trackastra.appose.computation;

import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.BORDER_DIST;
import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.COORDS;
import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.DIAMETER;
import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.EDGES;
import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.INERTIA_TENSOR;
import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.INTENSITY;
import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.LABELS;
import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.TIMEPOINTS;
import static org.mastodon.mamut.linking.trackastra.TrackastraUtils.KEY_EDGE_THRESHOLD;
import static org.mastodon.mamut.linking.trackastra.TrackastraUtils.KEY_NUM_DIMENSIONS;
import static org.mastodon.mamut.linking.trackastra.TrackastraUtils.KEY_TRACKASTRA_MODE;
import static org.mastodon.mamut.linking.trackastra.TrackastraUtils.KEY_WINDOW_SIZE;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;

import net.imglib2.RandomAccess;
import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.type.numeric.real.FloatType;

import org.apposed.appose.NDArray;
import org.apposed.appose.Service;
import org.mastodon.mamut.linking.trackastra.TrackastraMode;
import org.mastodon.mamut.linking.trackastra.TrackastraModel;
import org.mastodon.mamut.linking.trackastra.TrackastraUtils;
import org.mastodon.mamut.linking.trackastra.appose.types.RegionProps;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.util.ApposeProcess;
import org.mastodon.mamut.util.ResourceUtils;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.tracking.linking.EdgeCreator;
import org.scijava.Cancelable;
import org.scijava.app.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkPrediction extends ApposeProcess
{

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final org.scijava.log.Logger uiLogger;

	private final Map< String, Object > settings;
	private final SpatioTemporalIndex< Spot > index;
	private final EdgeCreator< Spot > edgeCreator;

	private final RegionProps regionProps;

	private final Cancelable cancelable;

	private final StatusService statusService;

	public LinkPrediction( final Map< String, Object > settings, final SpatioTemporalIndex< Spot > index,
			final EdgeCreator< Spot > edgeCreator, final RegionProps regionProps, final org.scijava.log.Logger uiLogger,
			final Cancelable cancelable, final StatusService statusService
	) throws IOException
	{
		super();
		this.settings = settings;
		this.index = index;
		this.edgeCreator = edgeCreator;
		this.regionProps = regionProps;
		this.uiLogger = uiLogger;
		this.cancelable = cancelable;
		this.statusService = statusService;
	}

	public void predictAndCreateLinks() throws IOException
	{
		try
		{
			stopWatch.split();
			String message = "Copied region prop data to shared memory for linking.";
			uiLogger.info( message + "\n" );
			message += "Time elapsed: " + stopWatch.formatSplitTime();
			log.info( message );

			inputs.put( LABELS, NDArrays.asNDArray( regionProps.labels ) );
			inputs.put( TIMEPOINTS, NDArrays.asNDArray( regionProps.timepoints ) );
			inputs.put( COORDS, NDArrays.asNDArray( regionProps.coords ) );
			inputs.put( DIAMETER, NDArrays.asNDArray( regionProps.diameters ) );
			inputs.put( INTENSITY, NDArrays.asNDArray( regionProps.intensities ) );
			inputs.put( INERTIA_TENSOR, NDArrays.asNDArray( regionProps.inertiaTensors ) );
			inputs.put( BORDER_DIST, NDArrays.asNDArray( regionProps.borderDists ) );

			Service.Task result = runScript();
			if ( cancelable.isCanceled() )
			{
				uiLogger.info( "Link prediction canceled by user.\n" );
				return;
			}
			uiLogger.info( "Link prediction finished. Now writing edges.\n" );
			statusService.showProgress( 9, 10 ); // 90% after prediction
			writeEdges( result );
			statusService.showProgress( 1, 1 ); // 100% after writing edges
		}
		finally
		{
			regionProps.close();
		}
	}

	private void writeEdges( final Service.Task task )
	{
		NDArray edges = ( NDArray ) task.outputs.get( "edges" );
		if ( edges == null )
		{
			log.warn( "No edges were predicted." );
			uiLogger.info( "No edges were predicted.\n" );
			return;
		}
		ShmImg< ? > edgesData = new ShmImg<>( edges );

		long[] dims = edgesData.dimensionsAsLongArray();
		int rows = ( int ) dims[ 1 ];
		log.debug( "Edges: {}", rows );
		RandomAccess< ? > randomAccess = edgesData.randomAccess();
		int linksCreated = 0;

		for ( int row = 0; row < rows; row++ )
		{
			if ( cancelable.isCanceled() )
			{
				uiLogger.info( "Link creation canceled by user.\n" );
				return;
			}
			int startFrame = getInt( randomAccess, 0, row );
			int startId = getInt( randomAccess, 1, row ) - 1; // Trackastra uses 1-based labels
			int endFrame = getInt( randomAccess, 2, row );
			int endId = getInt( randomAccess, 3, row ) - 1; // Trackastra uses 1-based labels
			float weight = getFloat( randomAccess, 4, row );

			Spot source = null;
			Spot target = null;
			for ( Spot spot : this.index.getSpatialIndex( startFrame ) )
			{
				if ( spot.getInternalPoolIndex() == startId )
				{
					source = spot;
					break;
				}
			}
			for ( Spot spot : this.index.getSpatialIndex( endFrame ) )
			{
				if ( spot.getInternalPoolIndex() == endId )
				{
					target = spot;
					break;
				}
			}
			if ( source == null || target == null )
			{
				log.warn( "Could not find source or target spot for edge: {}({}) -> {}({})", startId, startFrame, endId, endFrame );
				continue;
			}
			edgeCreator.createEdge( source, target, weight );
			linksCreated++;
		}
		uiLogger.info( "Created " + linksCreated + " links.\n" );
	}

	private int getInt( RandomAccess< ? > ra, int col, int row )
	{
		ra.setPosition( new long[] { col, row } );
		return ( int ) ( ( FloatType ) ra.get() ).getRealFloat();
	}

	private float getFloat( RandomAccess< ? > ra, int col, int row )
	{
		ra.setPosition( new long[] { col, row } );
		return ( ( FloatType ) ra.get() ).getRealFloat();
	}

	@Override
	protected String generateEnvFileContent()
	{
		return TrackastraUtils.getEnv();
	}

	@Override
	protected String generateImportStatements()
	{
		return ResourceUtils.readResourceAsString( "org/mastodon/mamut/linking/trackastra/appose/link_prediction_imports.py", getClass() );
	}

	@Override
	protected String generateScript()
	{
		String model = ( ( TrackastraModel ) settings.get( TrackastraUtils.KEY_MODEL ) ).getName();
		String mode = ( ( TrackastraMode ) settings.get( KEY_TRACKASTRA_MODE ) ).getName();
		String template =
				ResourceUtils.readResourceAsString( "org/mastodon/mamut/linking/trackastra/appose/link_prediction.py", getClass() );
		return template
				.replace( "{LABELS}", LABELS )
				.replace( "{TIMEPOINTS}", TIMEPOINTS )
				.replace( "{COORDS}", COORDS )
				.replace( "{DIAMETER}", DIAMETER )
				.replace( "{INTENSITY}", INTENSITY )
				.replace( "{INERTIA_TENSOR}", INERTIA_TENSOR )
				.replace( "{BORDER_DIST}", BORDER_DIST )
				.replace( "{MODEL}", model )
				.replace( "{WINDOW_SIZE}", settings.get( KEY_WINDOW_SIZE ).toString() )
				.replace( "{EDGE_THRESHOLD}", settings.get( KEY_EDGE_THRESHOLD ).toString() )
				.replace( "{NUM_DIMENSIONS}", settings.get( KEY_NUM_DIMENSIONS ).toString() )
				.replace( "{MODE}", mode )
				.replace( "{EDGES}", EDGES );
	}
}
