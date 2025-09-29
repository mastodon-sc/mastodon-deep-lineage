package org.mastodon.mamut.linking.trackastra.appose;

import static org.mastodon.mamut.linking.trackastra.appose.Contants.BORDER_DIST;
import static org.mastodon.mamut.linking.trackastra.appose.Contants.COORDS;
import static org.mastodon.mamut.linking.trackastra.appose.Contants.DIAMETER;
import static org.mastodon.mamut.linking.trackastra.appose.Contants.INERTIA_TENSOR;
import static org.mastodon.mamut.linking.trackastra.appose.Contants.INTENSITY;
import static org.mastodon.mamut.linking.trackastra.appose.Contants.LABELS;
import static org.mastodon.mamut.linking.trackastra.appose.Contants.TIMEPOINTS;
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
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.util.ApposeProcess;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.tracking.linking.EdgeCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackastraLinkPrediction extends ApposeProcess
{

	private static final Logger slf4jLogger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final org.scijava.log.Logger logger;

	private final Map< String, Object > settings;
	private final SpatioTemporalIndex< Spot > index;
	private final EdgeCreator< Spot > edgeCreator;

	private final RegionProps regionProps;

	public TrackastraLinkPrediction( final Map< String, Object > settings, final SpatioTemporalIndex< Spot > index,
			final EdgeCreator< Spot > edgeCreator, final RegionProps regionProps, final org.scijava.log.Logger logger ) throws IOException
	{
		super();
		this.settings = settings;
		this.index = index;
		this.edgeCreator = edgeCreator;
		this.regionProps = regionProps;
		this.logger = logger;
	}

	public void compute() throws IOException
	{
		try
		{
			stopWatch.split();
			String message = "Copied region prop data to shared memory for linking.";
			logger.info( message + "\n" );
			message += "Time elapsed: " + stopWatch.formatSplitTime();
			slf4jLogger.info( message );

			inputs.put( LABELS, NDArrays.asNDArray( regionProps.labels ) );
			inputs.put( TIMEPOINTS, NDArrays.asNDArray( regionProps.timepoints ) );
			inputs.put( COORDS, NDArrays.asNDArray( regionProps.coords ) );
			inputs.put( DIAMETER, NDArrays.asNDArray( regionProps.diameters ) );
			inputs.put( INTENSITY, NDArrays.asNDArray( regionProps.intensities ) );
			inputs.put( INERTIA_TENSOR, NDArrays.asNDArray( regionProps.inertiaTensors ) );
			inputs.put( BORDER_DIST, NDArrays.asNDArray( regionProps.borderDists ) );

			Service.Task result = runScript();
			writesEdges( result );
		}
		finally
		{
			regionProps.close();
		}
	}

	private void writesEdges( final Service.Task task )
	{
		ShmImg< ? > edgesData = new ShmImg<>( ( NDArray ) task.outputs.get( "edges" ) );
		long[] dims = edgesData.dimensionsAsLongArray();
		int rows = ( int ) dims[ 1 ];
		slf4jLogger.debug( "Edges: {}", rows );
		RandomAccess< ? > randomAccess = edgesData.randomAccess();
		int linksCreated = 0;

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
				slf4jLogger.warn( "Could not find source or target spot for edge: {}({}) -> {}({})", start, startFrame, end, endFrame );
				continue;
			}
			edgeCreator.createEdge( source, target, weight );
			linksCreated++;
		}
		logger.info( "Created " + linksCreated + " links.\n" );
	}

	@Override
	protected String generateEnvFileContent()
	{
		return TrackastraUtils.getEnv( getApposeVersion() );
	}

	@Override
	protected String generateScript()
	{
		String mode = ( ( TrackastraMode ) settings.get( KEY_TRACKASTRA_MODE ) ).getName();
		String model = ( ( TrackastraModel ) settings.get( TrackastraUtils.KEY_MODEL ) ).getName();
		double t = ( Double ) settings.get( KEY_EDGE_THRESHOLD );
		int nDim = ( Integer ) settings.get( KEY_NUM_DIMENSIONS );
		int windowSize = ( Integer ) settings.get( KEY_WINDOW_SIZE );

		return "labels_ndarray = " + LABELS + ".ndarray()" + "\n"
				+ "timepoints_ndarray = " + TIMEPOINTS + ".ndarray()" + "\n"
				+ "coords_ndarray = " + COORDS + ".ndarray()" + "\n"
				+ "diameters_ndarray = " + DIAMETER + ".ndarray()" + "\n"
				+ "intensities_ndarray = " + INTENSITY + ".ndarray()" + "\n"
				+ "tensors_ndarray = " + INERTIA_TENSOR + ".ndarray()" + "\n"
				+ "border_dists_ndarray = " + BORDER_DIST + ".ndarray()" + "\n"
				+ "\n"
				+ "wrfeatures_list = []" + "\n"
				+ "timepoints = labels_ndarray.shape[1]" + "\n"
				+ "for t in range(0, timepoints):" + "\n"
				+ "  labels_t = labels_ndarray[:, t]" + "\n"
				+ "  num_labels_t = np.count_nonzero(labels_t)" + "\n"
				+ "  labels_t = labels_t[:num_labels_t]" + "\n"
				+ "  labels_flat = np.asarray(labels_t).ravel()" + "\n"
				+ "  sort_idx = np.argsort(labels_t)" + "\n"
				+ "  labels_t = labels_t[sort_idx]" + "\n"
				+ "  coords_t = coords_ndarray[:, :num_labels_t, t].T" + "\n"
				+ "  coords_t = coords_t[sort_idx]" + "\n"
				+ "  coords_t = coords_t[:, ::-1]" + "\n" // reverse order
				+ "  timepoints_t = timepoints_ndarray[:num_labels_t, t]" + "\n"
				+ "  timepoints_t = timepoints_t[sort_idx]" + "\n"
				+ "  diameters_t = diameters_ndarray[:num_labels_t, [t]]" + "\n"
				+ "  diameters_t = diameters_t[sort_idx]" + "\n"
				+ "  intensities_t = intensities_ndarray[:num_labels_t, [t]]" + "\n"
				+ "  intensities_t = intensities_t[sort_idx]" + "\n"
				+ "  tensors_t = tensors_ndarray[:, :num_labels_t, t].T" + "\n"
				+ "  tensors_t = tensors_t[sort_idx]" + "\n"
				+ "  tensors_t = tensors_t[:, ::-1]" + "\n" // reverse order
				+ "  border_dists_t = border_dists_ndarray[:num_labels_t, [t]]" + "\n"
				+ "  border_dists_t = border_dists_t[sort_idx]" + "\n"
				+ "  features_t = OrderedDict()" + "\n"
				+ "  features_t['equivalent_diameter_area'] = diameters_t" + "\n"
				+ "  features_t['intensity_mean'] = intensities_t" + "\n"
				+ "  features_t['inertia_tensor'] = tensors_t" + "\n"
				+ "  features_t['border_dist'] = border_dists_t" + "\n"
				+ "  wrfeatures_t = wrfeat.WRFeatures(coords=coords_t,labels=labels_t,timepoints=timepoints_t,features=features_t)" + "\n"
				+ "  wrfeatures_list.append(wrfeatures_t)" + "\n"
				+ "\n"
				+ "task.update(message='Read data from Region Props')" + "\n"
				+ "\n"
				+ "features = tuple(wrfeatures_list)" + "\n"
				+ "name='" + model + "'" + "\n"
				+ "device='cpu'" + "\n"
				+ "download_dir=Path.home() / '.local' / 'share' / 'appose' / 'trackastra' / 'pretrained_models'" + "\n"
				+ "folder = pretrained.download_pretrained(name=name, download_dir=download_dir)" + "\n"
				+ "model = Trackastra.from_folder(dir=folder, device=device)" + "\n"
				+ "\n"
				+ "task.update(message=f\"(Downloaded) and loaded pretrained model. Folder: {folder}\")" + "\n"
				+ "\n"
				+ "window_size = " + windowSize + "\n"
				+ "windows = wrfeat.build_windows(features, window_size, tqdm, True)" + "\n"
				+ "\n"
				+ "task.update(message='Window building from features completed')" + "\n"
				+ "\n"
				+ "model.transformer.eval()" + "\n"
				+ "predictions = predict.predict_windows(windows,features,model.transformer,0,1," + t + "," + nDim + ",1,tqdm)" + "\n"
				+ "task.update(message=\"Predictions completed\")" + "\n"
				+ "\n"
				+ "track_graph = model._track_from_predictions(predictions,'" + mode + "')" + "\n"
				+ "nodes = track_graph.number_of_nodes()" + "\n"
				+ "n_edges = track_graph.number_of_edges()" + "\n"
				+ "\n"
				+ "task.update(message='Tracking graph construction completed. Nodes: '+str(nodes)+', Edges: '+str(n_edges)+'')" + "\n"
				+ "\n"
				+ "edges_table = utils.graph_to_edge_table(track_graph)" + "\n"
				+ "edges = edges_table.to_numpy(dtype=np.float32)" + "\n"
				+ "shared_edges = appose.NDArray(str(edges.dtype), edges.shape)" + "\n"
				+ "shared_edges.ndarray()[:] = edges" + "\n"
				+ "task.outputs['edges'] = shared_edges" + "\n"
				+ "\n"
				+ "task.update(message=str(n_edges) + ' edges saved to shared memory')" + "\n";
	}

	@Override
	protected String generateImportStatements()
	{
		return "import appose\n"
				+ "import numpy as np" + "\n"
				+ "\n"
				+ "import trackastra.data.wrfeat as wrfeat" + "\n"
				+ "import trackastra.model.predict as predict" + "\n"
				+ "import trackastra.model.pretrained as pretrained" + "\n"
				+ "import trackastra.tracking.utils as utils" + "\n"
				+ "from trackastra.model import Trackastra" + "\n"
				+ "\n"
				+ "from tqdm import tqdm" + "\n"
				+ "from pathlib import Path" + "\n"
				+ "from collections import OrderedDict" + "\n"
				+ "\n"
				+ "task.update(message=\"Imports completed\")" + "\n"
				+ "\n"
				+ "task.export(np=np,appose=appose,wrfeat=wrfeat,predict=predict,pretrained=pretrained,utils=utils,Trackastra=Trackastra,tqdm=tqdm,Path=Path,OrderedDict=OrderedDict)"
				+ "\n";
	}
}
