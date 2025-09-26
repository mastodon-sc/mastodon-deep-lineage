package org.mastodon.mamut.linking.trackastra.appose;

import static org.mastodon.mamut.linking.trackastra.appose.Contants.BORDER_DIST;
import static org.mastodon.mamut.linking.trackastra.appose.Contants.COORDS;
import static org.mastodon.mamut.linking.trackastra.appose.Contants.DIAMETER;
import static org.mastodon.mamut.linking.trackastra.appose.Contants.INERTIA_TENSOR;
import static org.mastodon.mamut.linking.trackastra.appose.Contants.INTENSITY;
import static org.mastodon.mamut.linking.trackastra.appose.Contants.LABELS;
import static org.mastodon.mamut.linking.trackastra.appose.Contants.TIMEPOINTS;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;

import org.apposed.appose.NDArray;
import org.apposed.appose.Service;
import org.mastodon.mamut.detection.PythonRuntimeException;
import org.mastodon.mamut.io.exporter.labelimage.ExportLabelImageUtils;
import org.mastodon.mamut.linking.trackastra.TrackastraUtils;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.util.ApposeProcess;
import org.mastodon.mamut.util.ImgUtils;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bdv.viewer.Source;

public class TrackastraRegionProps extends ApposeProcess
{
	private static final Logger slf4Logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final org.scijava.log.Logger logger;

	private static final String IMAGE = "image";

	private static final String MASK = "mask";

	public TrackastraRegionProps( final org.scijava.log.Logger logger ) throws IOException
	{
		super();
		this.logger = logger;
	}

	public List< RegionProps > compute( final Source< ? > source, final int level, final SpatioTemporalIndex< Spot > spatioTemporalIndex,
			final int minTimepoint, final int maxTimepoint )
	{
		int todo = ( maxTimepoint - minTimepoint + 1 );
		int done = 0;
		slf4Logger.info( "Computing region props for source: {}", source.getName() );
		logger.info( "Computing region props for source: " + source.getName() + "\n" );
		List< RegionProps > list = new ArrayList<>();
		for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++, done++ )
		{
			if ( spatioTemporalIndex.getSpatialIndex( timepoint ).isEmpty() )
			{
				slf4Logger.info( "No spots. Skipping region props computation for timepoint: {}", timepoint );
				logger.info( "No spots. Skipping region props computation for timepoint: " + timepoint + "\n" );
				done++;
				continue;
			}
			RegionProps regionProps = computeSource( source, timepoint, level, spatioTemporalIndex );
			list.add( regionProps );
			double progress = ( double ) done / todo;
			NumberFormat nf = NumberFormat.getPercentInstance();
			nf.setMinimumFractionDigits( 0 );
			nf.setMaximumFractionDigits( 0 );
			String message = String.format( "Computed region props for timepoint %d/%d. Progress: %s", timepoint, maxTimepoint,
					nf.format( progress ) );
			slf4Logger.info( message );
			logger.info( message + "\n" );
		}
		return list;
	}

	private RegionProps computeSource( final Source< ? > source, final int timepoint, final int level,
			final SpatioTemporalIndex< Spot > spatioTemporalIndex )
	{
		AffineTransform3D transform = new AffineTransform3D();
		source.getSourceTransform( timepoint, level, transform );
		RandomAccessibleInterval< ? > image = source.getSource( timepoint, level );
		String imageDimensions = ImgUtils.getImageDimensionsAsString( image );
		slf4Logger.info( "Processing timepoint: {}", timepoint );
		slf4Logger.info( "Getting features from image with {} dimensions: ({}) of type: {}", image.numDimensions(), imageDimensions,
				image.getType().getClass().getSimpleName() );
		RandomAccessibleInterval< IntType > masksImage = ExportLabelImageUtils.getLabelImageFromSpots( transform,
				image.dimensionsAsLongArray(), level, timepoint, spatioTemporalIndex );
		// Prepare inputs.
		try (ShmImg< ? > sharedMemoryImage = ShmImg.copyOf( Cast.unchecked( image ) );
				ShmImg< ? > sharedMemoryMasks = ShmImg.copyOf( Cast.unchecked( masksImage ) ))
		{
			stopWatch.split();
			if ( slf4Logger.isInfoEnabled() )
				slf4Logger.info( "Copied image and masks to shared memory. Time elapsed: {}", stopWatch.formatSplitTime() );
			NDArray imageNDArray = NDArrays.asNDArray( sharedMemoryImage );
			NDArray masksNDArray = NDArrays.asNDArray( sharedMemoryMasks );
			stopWatch.split();
			if ( slf4Logger.isInfoEnabled() )
				slf4Logger.info( "Converted image and masks to nd arrays: {} and {}. Time elapsed: {}", imageNDArray, masksNDArray,
						stopWatch.formatSplitTime() );

			inputs.put( IMAGE, imageNDArray );
			inputs.put( MASK, masksNDArray );

			logger.info( "Starting python process.\n" );
			logger.info( "On first time use, this installs a Python new environment, which can take a while.\n" );

			Service.Task result = runScript();
			ShmImg< IntType > labels = new ShmImg<>( ( NDArray ) result.outputs.get( LABELS ) );
			LoopBuilder.setImages( labels ).multiThreaded().forEachPixel( p -> p.set( p.get() - 1 ) ); // make labels zero based again
			ShmImg< IntType > timepoints = new ShmImg<>( ( NDArray ) result.outputs.get( TIMEPOINTS ) );
			LoopBuilder.setImages( timepoints ).multiThreaded().forEachPixel( p -> p.set( timepoint ) ); // all timepoints are the same
			ShmImg< FloatType > coords = new ShmImg<>( ( NDArray ) result.outputs.get( COORDS ) );
			ShmImg< FloatType > diameter = new ShmImg<>( ( NDArray ) result.outputs.get( DIAMETER ) );
			ShmImg< FloatType > intensity = new ShmImg<>( ( NDArray ) result.outputs.get( INTENSITY ) );
			ShmImg< FloatType > inertiaTensor = new ShmImg<>( ( NDArray ) result.outputs.get( INERTIA_TENSOR ) );
			ShmImg< FloatType > borderDist = new ShmImg<>( ( NDArray ) result.outputs.get( BORDER_DIST ) );
			return new RegionProps( labels, timepoints, coords, diameter, intensity, inertiaTensor, borderDist );
		}
		catch ( IOException e )
		{
			throw new PythonRuntimeException( e.getMessage() );
		}
	}

	@Override
	protected String generateEnvFileContent()
	{
		return TrackastraUtils.getEnv( getApposeVersion() );
	}

	@Override
	protected String generateScript()
	{
		return "image_ndarray = " + IMAGE + ".ndarray()" + "\n"
				+ "mask_ndarray = " + MASK + ".ndarray().astype('int32')" + "\n"
				+ "\n"
				+ "ndim = image_ndarray.ndim" + "\n"
				+ "if ndim == 3:" + "\n"
				+ "  image = np.expand_dims(np.transpose(image_ndarray, (2, 1, 0)), axis=0)" + "\n" // reorder to z,y,x + add a dummy t axis
				+ "  mask = np.expand_dims(np.transpose(mask_ndarray, (2, 1, 0)), axis=0)" + "\n"
				+ "else:" + "\n"
				+ "  image = np.expand_dims(np.transpose(image_ndarray, (1, 0)), axis=0) " + "\n" // reorder to y,x + add a dummy t axis
				+ "  mask = np.expand_dims(np.transpose(mask_ndarray, (1, 0)), axis=0)" + "\n"
				+ "\n"
				+ "task.update(message=\"Image and mask loaded into numpy arrays\")" + "\n"
				+ "\n"
				+ "image = utils.normalize(image)" + "\n"
				+ "features = wrfeat.get_features(mask,image,'wrfeat',ndim,0,tqdm)" + "\n"
				+ "\n"
				+ "task.update(message=\"Feature computation completed\")" + "\n"
				+ "\n"
				+ "labels = features[0].labels" + "\n"
				+ "shared_labels = appose.NDArray(str(labels.dtype), labels.shape)" + "\n"
				+ "shared_labels.ndarray()[:] = labels" + "\n"
				+ "timepoints = features[0].timepoints" + "\n"
				+ "shared_timepoints = appose.NDArray(str(timepoints.dtype), timepoints.shape)" + "\n"
				+ "shared_timepoints.ndarray()[:] = timepoints" + "\n"
				+ "coords = features[0].coords.T" + "\n"
				+ "shared_coords = appose.NDArray(str(coords.dtype), coords.shape)" + "\n"
				+ "shared_coords.ndarray()[:] = coords" + "\n"
				+ "diameter = features[0].features['equivalent_diameter_area']" + "\n"
				+ "diameter = diameter[:, 0]" + "\n"
				+ "shared_diameter = appose.NDArray(str(diameter.dtype), diameter.shape)" + "\n"
				+ "shared_diameter.ndarray()[:] = diameter" + "\n"
				+ "intensity = features[0].features['intensity_mean']" + "\n"
				+ "intensity = intensity[:, 0]" + "\n"
				+ "shared_intensity = appose.NDArray(str(intensity.dtype), intensity.shape)" + "\n"
				+ "shared_intensity.ndarray()[:] = intensity" + "\n"
				+ "inertia_tensor = features[0].features['inertia_tensor'].T" + "\n"
				+ "shared_inertia_tensor = appose.NDArray(str(inertia_tensor.dtype), inertia_tensor.shape)" + "\n"
				+ "shared_inertia_tensor.ndarray()[:] = inertia_tensor" + "\n"
				+ "border_dist = features[0].features['border_dist']" + "\n"
				+ "border_dist = border_dist[:, 0]" + "\n"
				+ "shared_border_dist = appose.NDArray(str(border_dist.dtype), border_dist.shape)" + "\n"
				+ "shared_border_dist.ndarray()[:] = border_dist" + "\n"
				+ "\n"
				+ "task.outputs['" + LABELS + "'] = shared_labels" + "\n"
				+ "task.outputs['" + TIMEPOINTS + "'] = shared_timepoints" + "\n"
				+ "task.outputs['" + COORDS + "'] = shared_coords" + "\n"
				+ "task.outputs['" + DIAMETER + "'] = shared_diameter" + "\n"
				+ "task.outputs['" + INTENSITY + "'] = shared_intensity" + "\n"
				+ "task.outputs['" + INERTIA_TENSOR + "'] = shared_inertia_tensor" + "\n"
				+ "task.outputs['" + BORDER_DIST + "'] = shared_border_dist" + "\n"
				+ "\n"
				+ "task.update(message=\"Feature extraction completed. Found {} objects\".format(len(labels)))" + "\n";
	}

	@Override
	protected String generateImportStatements()
	{
		return "import appose" + "\n"
				+ "import numpy as np" + "\n"
				+ "\n"
				+ "import trackastra.data.wrfeat as wrfeat\n"
				+ "import trackastra.utils as utils" + "\n"
				+ "\n"
				+ "from tqdm import tqdm" + "\n"
				+ "from pathlib import Path" + "\n"
				+ "\n"
				+ "task.update(message=\"Imports completed\")" + "\n"
				+ "\n"
				+ "task.export(np=np,appose=appose,wrfeat=wrfeat,utils=utils,tqdm=tqdm,Path=Path)" + "\n";
	}
}
