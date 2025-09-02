package org.mastodon.mamut.linking.trackastra;

import static org.mastodon.tracking.detection.DetectorKeys.KEY_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MIN_TIMEPOINT;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.trackastra.TrackastraLinkerDescriptor.KEY_EDGE_THRESHOLD;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.trackastra.TrackastraLinkerDescriptor.KEY_NUM_DIMENSIONS;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.trackastra.TrackastraLinkerDescriptor.KEY_TRACKASTRA_MODE;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import net.imglib2.RandomAccess;
import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;

import org.apposed.appose.NDArray;
import org.apposed.appose.Service;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.mamut.feature.spot.skimage.SpotRegionPropsFeature;
import org.mastodon.mamut.feature.spot.skimage.SpotRegionPropsFeature.RegionProp;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.util.ApposeProcess;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.tracking.linking.EdgeCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackastraLinkPrediction extends ApposeProcess
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Map< String, Object > settings;

	private final FeatureModel featureModel;

	private final SpatioTemporalIndex< Spot > index;

	private final EdgeCreator< Spot > edgeCreator;

	protected TrackastraLinkPrediction( final Map< String, Object > settings, final FeatureModel featureModel,
			final SpatioTemporalIndex< Spot > index, final EdgeCreator< Spot > edgeCreator )
			throws IOException
	{
		super();
		this.settings = settings;
		this.featureModel = featureModel;
		this.index = index;
		this.edgeCreator = edgeCreator;
	}

	void compute()
	{
		String script = generateScript();
		logger.debug( "Running script:\n{}", script );

		int sourceId = 0;

		final SpotRegionPropsFeature feature = Cast.unchecked( featureModel.getFeature( SpotRegionPropsFeature.FEATURE_SPEC ) );

		int minTimepoint = ( Integer ) settings.get( KEY_MIN_TIMEPOINT );
		int maxTimepoint = ( Integer ) settings.get( KEY_MAX_TIMEPOINT );
		int numTimepoints = maxTimepoint - minTimepoint + 1;

		int maxSpots = Integer.MIN_VALUE;
		for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++ )
		{
			int nSpots = index.getSpatialIndex( timepoint ).size();
			if ( nSpots > maxSpots )
				maxSpots = nSpots;
		}

		Img< IntType > labels = ArrayImgs.ints( numTimepoints, maxSpots );
		Img< IntType > timepoints = ArrayImgs.ints( numTimepoints, maxSpots );

		Img< FloatType > diameters = ArrayImgs.floats( numTimepoints, maxSpots );
		Img< FloatType > intensities = ArrayImgs.floats( numTimepoints, maxSpots );
		Img< FloatType > intertiaTensors = ArrayImgs.floats( numTimepoints, maxSpots, 9 );
		Img< FloatType > borderDists = ArrayImgs.floats( numTimepoints, maxSpots );

		Img< FloatType > coords = ArrayImgs.floats( numTimepoints, maxSpots, 3 );

		for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++ )
		{
			SpatialIndex< Spot > spatialIndex = index.getSpatialIndex( timepoint );
			long nSpots = spatialIndex.size();
			Iterator< Spot > spots = spatialIndex.iterator();

			for ( int spotNumber = 0; spotNumber < nSpots && spots.hasNext(); spotNumber++ )
			{
				Spot spot = spots.next();
				labels.getAt( timepoint, spotNumber ).setReal( spot.getInternalPoolIndex() );
				timepoints.getAt( timepoint, spotNumber ).setReal( spot.getTimepoint() );

				FeatureProjection< Spot > projection =
						feature.project( FeatureProjectionKey.key( RegionProp.EQUIVALENT_DIAMETER_AREA.spec(), sourceId ) );
				diameters.getAt( timepoint, spotNumber ).setReal( projection.value( spot ) );

				projection = feature.project( FeatureProjectionKey.key( RegionProp.INTENSITY_MEAN.spec(), sourceId ) );
				intensities.getAt( timepoint, spotNumber ).setReal( projection.value( spot ) );

				RegionProp[] regionProps = Arrays.copyOfRange( RegionProp.values(), RegionProp.INERTIA_TENSOR_XX.ordinal(),
						RegionProp.INERTIA_TENSOR_ZZ.ordinal() + 1 );
				for ( int j = 0; j < regionProps.length; j++ )
				{
					projection = feature.project( FeatureProjectionKey.key( regionProps[ j ].spec(), sourceId ) );
					intertiaTensors.getAt( timepoint, spotNumber, regionProps.length - 1 - j ).setReal( projection.value( spot ) );
				}

				projection = feature.project( FeatureProjectionKey.key( RegionProp.BORDER_DIST.spec(), sourceId ) );
				borderDists.getAt( timepoint, spotNumber ).setReal( projection.value( spot ) );

				regionProps = Arrays.copyOfRange( RegionProp.values(), RegionProp.X.ordinal(), RegionProp.Z.ordinal() + 1 );
				for ( int dimension = 0; dimension < regionProps.length; dimension++ )
				{
					projection = feature.project( FeatureProjectionKey.key( regionProps[ dimension ].spec(), sourceId ) );
					coords.getAt( timepoint, spotNumber, regionProps.length - 1 - dimension ).setReal( projection.value( spot ) );
				}
			}
		}

		try (ShmImg< FloatType > shmCoords = ShmImg.copyOf( coords );
				ShmImg< IntType > shmLabels = ShmImg.copyOf( labels );
				ShmImg< IntType > shmTimepoints = ShmImg.copyOf( timepoints );
				ShmImg< FloatType > shmDiameters = ShmImg.copyOf( diameters );
				ShmImg< FloatType > shmIntensities = ShmImg.copyOf( intensities );
				ShmImg< FloatType > shmTensors = ShmImg.copyOf( intertiaTensors );
				ShmImg< FloatType > shmBorderDists = ShmImg.copyOf( borderDists ))
		{
			stopWatch.split();
			if ( logger.isInfoEnabled() )
				logger.info( "Copied data to shared memory. Time elapsed: {}", stopWatch.formatSplitTime() );

			inputs.put( "coords", NDArrays.asNDArray( shmCoords ) );
			inputs.put( "labels", NDArrays.asNDArray( shmLabels ) );
			inputs.put( "timepoints", NDArrays.asNDArray( shmTimepoints ) );
			inputs.put( "diameters", NDArrays.asNDArray( shmDiameters ) );
			inputs.put( "intensities", NDArrays.asNDArray( shmIntensities ) );
			inputs.put( "tensors", NDArrays.asNDArray( shmTensors ) );
			inputs.put( "border_dists", NDArrays.asNDArray( shmBorderDists ) );

			Service.Task result = runScript();
			writesEdges( result );
		}
		catch ( IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	private void writesEdges( final Service.Task task )
	{
		ShmImg< ? > edgesData = new ShmImg<>( ( NDArray ) task.outputs.get( "edges" ) );
		long[] dims = edgesData.dimensionsAsLongArray();
		int rows = ( int ) dims[ 1 ];
		logger.debug( "Got edges: {}", rows );
		RandomAccess< ? > randomAccess = edgesData.randomAccess();

		for ( int row = 0; row < rows; row++ )
		{
			randomAccess.setPosition( new long[] { 0, row } );
			int startFrame = ( int ) ( ( FloatType ) randomAccess.get() ).getRealFloat();
			randomAccess.setPosition( new long[] { 1, row } );
			int start = ( int ) ( ( FloatType ) randomAccess.get() ).getRealFloat();
			randomAccess.setPosition( new long[] { 2, row } );
			int endFrame = ( int ) ( ( FloatType ) randomAccess.get() ).getRealFloat();
			randomAccess.setPosition( new long[] { 3, row } );
			int end = ( int ) ( ( FloatType ) randomAccess.get() ).getRealFloat();
			randomAccess.setPosition( new long[] { 4, row } );
			float weight = ( ( FloatType ) randomAccess.get() ).getRealFloat();

			Spot source = null;
			Spot target = null;
			for ( Spot spot : this.index.getSpatialIndex( startFrame ) )
			{
				if ( spot.getInternalPoolIndex() == start )
				{
					source = spot;
					break;
				}
			}
			for ( Spot spot : this.index.getSpatialIndex( endFrame ) )
			{
				if ( spot.getInternalPoolIndex() == end )
				{
					target = spot;
					break;
				}
			}
			if ( source == null || target == null )
			{
				logger.warn( "Could not find source or target spot for edge: {}({}) -> {}({})", start, startFrame, end, endFrame );
				continue;
			}
			edgeCreator.createEdge( source, target, weight );
		}
	}

	@Override
	protected String generateEnvFileContent()
	{
		return "name: trackastra\n"
				+ "channels:\n"
				+ "  - conda-forge\n"
				+ "channel_priority: strict\n"
				+ "dependencies:\n"
				+ "  - python=3.10\n"
				+ getApposeVersion().substring( 2 ) + "\n"
				+ "  - pip\n"
				+ "  - pip:\n"
				+ "    - trackastra==0.3.2\n";
	}

	@Override
	protected String generateScript()
	{
		String mode = ( ( TrackAstraMode ) settings.get( KEY_TRACKASTRA_MODE ) ).getName();
		double edgeThreshold = ( Double ) settings.get( KEY_EDGE_THRESHOLD );
		return "import appose\n"
				+ "import numpy as np" + "\n"
				+ "\n"
				+ "from trackastra.model import Trackastra" + "\n"
				+ "from trackastra.model.predict import predict_windows" + "\n"
				+ "from trackastra.model.pretrained import download_pretrained" + "\n"
				+ "from trackastra.data.wrfeat import build_windows" + "\n"
				+ "from trackastra.data.wrfeat import WRFeatures" + "\n"
				+ "from trackastra.tracking.utils import graph_to_edge_table" + "\n"
				+ "\n"
				+ "from tqdm import tqdm" + "\n"
				+ "from pathlib import Path" + "\n"
				+ "from collections import OrderedDict" + "\n"
				+ "\n"
				+ "task.update(message=\"Imports completed\")" + "\n"
				+ "\n"
				+ "coords_ndarray = coords.ndarray()" + "\n"
				+ "labels_ndarray = labels.ndarray()" + "\n"
				+ "timepoints_ndarray = timepoints.ndarray()" + "\n"
				+ "diameters_ndarray = diameters.ndarray()" + "\n"
				+ "intensities_ndarray = intensities.ndarray()" + "\n"
				+ "tensors_ndarray = tensors.ndarray()" + "\n"
				+ "border_dists_ndarray = border_dists.ndarray()" + "\n"
				+ "\n"
				+ "wrfeatures_list = []" + "\n"
				+ "timepoints = labels_ndarray.shape[1]" + "\n"
				+ "for t in range(0, timepoints):" + "\n"
				+ "  labels_t = labels_ndarray[:, t]" + "\n"
				+ "  num_labels_t = np.max(np.nonzero(labels_t)) + 1" + "\n"
				+ "  print(f\"Timepoint {t}: {num_labels_t} labels\")" + "\n"
				+ "  labels_t = labels_t[:num_labels_t]" + "\n"
				+ "  labels_flat = np.asarray(labels_t).ravel()" + "\n"
				+ "  sort_idx = np.argsort(labels_t)" + "\n"
				+ "  labels_t = labels_t[sort_idx]" + "\n"
				+ "  coords_t = coords_ndarray[:, :num_labels_t, t].T" + "\n"
				+ "  coords_t = coords_t[sort_idx]" + "\n"
				+ "  timepoints_t = timepoints_ndarray[:num_labels_t, t]" + "\n"
				+ "  timepoints_t = timepoints_t[sort_idx]" + "\n"
				+ "  diameters_t = diameters_ndarray[:num_labels_t, [t]]" + "\n"
				+ "  diameters_t = diameters_t[sort_idx]" + "\n"
				+ "  intensities_t = intensities_ndarray[:num_labels_t, [t]]" + "\n"
				+ "  intensities_t = intensities_t[sort_idx]" + "\n"
				+ "  tensors_t = tensors_ndarray[:, :num_labels_t, t].T" + "\n"
				+ "  tensors_t = tensors_t[sort_idx]" + "\n"
				+ "  border_dists_t = border_dists_ndarray[:num_labels_t, [t]]" + "\n"
				+ "  border_dists_t = border_dists_t[sort_idx]" + "\n"
				+ "  features_t = OrderedDict()" + "\n"
				+ "  features_t['equivalent_diameter_area'] = diameters_t" + "\n"
				+ "  features_t['intensity_mean'] = intensities_t" + "\n"
				+ "  features_t['inertia_tensor'] = tensors_t" + "\n"
				+ "  features_t['border_dist'] = border_dists_t" + "\n"
				+ "  wrfeatures_t = WRFeatures(coords=coords_t, labels=labels_t, timepoints=timepoints_t, features=features_t)" + "\n"
				+ "  wrfeatures_list.append(wrfeatures_t)" + "\n"
				+ "\n"
				+ "task.update(message='Read data from Trackastra Linker Feature')" + "\n"
				+ "\n"
				+ "features = tuple(wrfeatures_list)" + "\n"
				+ "name='ctc'" + "\n"
				+ "device='cpu'" + "\n"
				+ "download_dir=Path.home() / '.local' / 'share' / 'appose' / 'trackastra' / 'pretrained_models'" + "\n"
				+ "folder = download_pretrained(name=name, download_dir=download_dir)" + "\n"
				+ "model = Trackastra.from_folder(dir=folder, device=device)" + "\n"
				+ "\n"
				+ "task.update(message=f\"(Downloaded) and loaded pretrained model. Folder: {folder}\")" + "\n"
				+ "\n"
				+ "window_size = model.transformer.config['window']" + "\n" // TODO make it a setting - or does it have to match the model?
				+ "windows = build_windows(features=features, window_size=window_size, progbar_class=tqdm)" + "\n"
				+ "\n"
				+ "task.update(message='Window building from features completed')" + "\n"
				+ "\n"
				+ "ndim = " + settings.get( KEY_NUM_DIMENSIONS ) + "\n"
				+ "edge_threshold = " + edgeThreshold + "\n"
				+ "\n"
				+ "model.transformer.eval()" + "\n"
				+ "predictions = predict_windows(windows=windows, features=features, model=model.transformer, edge_threshold=0.05, spatial_dim=3, progbar_class=tqdm)"
				+ "\n"
				+ "task.update(message=\"Predictions completed\")" + "\n"
				+ "\n"
				+ "mode = '" + mode + "'\n"
				+ "track_graph = model._track_from_predictions(predictions, mode=mode)" + "\n"
				+ "\n"
				+ "task.update(message=\"Tracking graph construction by Trackastra completed\")" + "\n"
				+ "\n"
				+ "edges_table = graph_to_edge_table(graph=track_graph)" + "\n"
				+ "edges = edges_table.to_numpy(dtype=np.float32)" + "\n"
				+ "print(str(edges.dtype))" + "\n"
				+ "shared_edges = appose.NDArray(str(edges.dtype), edges.shape)" + "\n"
				+ "shared_edges.ndarray()[:] = edges" + "\n"
				+ "task.outputs['edges'] = shared_edges" + "\n"
				+ "\n"
				+ "task.update(message=\"Edges saved to shared memory\")" + "\n";
	}
}
