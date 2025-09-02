package org.mastodon.mamut.feature.spot.skimage;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;

import org.apposed.appose.NDArray;
import org.apposed.appose.Service;
import org.mastodon.feature.DefaultFeatureComputerService.FeatureComputationStatus;
import org.mastodon.mamut.detection.PythonRuntimeException;
import org.mastodon.mamut.feature.spot.skimage.SpotRegionPropsFeature.RegionProp;
import org.mastodon.mamut.io.exporter.labelimage.ExportLabelImageUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.util.ApposeProcess;
import org.mastodon.mamut.util.ImgUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;

public class TrackAstraRegionProps extends ApposeProcess
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	public TrackAstraRegionProps() throws IOException
	{
		super();
	}

	public void compute(
			final List< SourceAndConverter< ? > > sources, final Model model, final SpotRegionPropsFeature feature,
			final FeatureComputationStatus status, final int minTimepoint, final int maxTimepoint, final int sourceIndex, final int level
	)
	{
		int todo = ( maxTimepoint - minTimepoint + 1 ) * sources.size();
		int done = 0;
		Source< ? > source = sources.get( sourceIndex ).getSpimSource();
		logger.info( "Processing source: {}", source.getName() );
		for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++ )
		{
			computeSource( source, sourceIndex, timepoint, model, feature, level );
			double progress = ( double ) done++ / todo;
			logger.info( "Progress: {}", progress );
			if ( status != null )
				status.notifyProgress( progress );
		}
	}

	private void computeSource( final Source< ? > source, final int sourceIndex, final int timepoint, final Model model,
			final SpotRegionPropsFeature feature, final int level )
	{
		AffineTransform3D transform = new AffineTransform3D();
		source.getSourceTransform( timepoint, level, transform );
		RandomAccessibleInterval< ? > image = source.getSource( timepoint, level );
		String imageDimensions = ImgUtils.getImageDimensionsAsString( image );
		logger.info( "Processing timepoint: {}", timepoint );
		logger.info( "Getting features from image with {} dimensions: ({}) of type: {}", image.numDimensions(), imageDimensions,
				image.getType().getClass().getSimpleName() );
		RandomAccessibleInterval< IntType > masksImage =
				ExportLabelImageUtils.getLabelImageFromSpots( transform, image.dimensionsAsLongArray(), level, timepoint, model );

		// Prepare inputs.
		try (ShmImg< ? > sharedMemoryImage = ShmImg.copyOf( Cast.unchecked( image ) );
				ShmImg< ? > sharedMemoryMasks = ShmImg.copyOf( Cast.unchecked( masksImage ) ))
		{
			stopWatch.split();
			if ( logger.isInfoEnabled() )
				logger.info( "Copied image and masks to shared memory. Time elapsed: {}", stopWatch.formatSplitTime() );
			NDArray imageNDArray = NDArrays.asNDArray( sharedMemoryImage );
			NDArray masksNDArray = NDArrays.asNDArray( sharedMemoryMasks );
			stopWatch.split();
			if ( logger.isInfoEnabled() )
				logger.info( "Converted image and masks to nd arrays: {} and {}. Time elapsed: {}", imageNDArray, masksNDArray,
						stopWatch.formatSplitTime() );

			inputs.put( "image", imageNDArray );
			inputs.put( "mask", masksNDArray );

			Service.Task result = runScript();
			writeDiameterIntensityBorderDistToFeature( result, feature, model, sourceIndex, timepoint );
			writeInertiaTensorsToFeature( result, feature, model, sourceIndex, timepoint );
		}
		catch ( IOException e )
		{
			throw new PythonRuntimeException( e.getMessage() );
		}
	}

	private static void writeDiameterIntensityBorderDistToFeature( final Service.Task task, final SpotRegionPropsFeature feature,
			final Model model,
			final int sourceIndex, final int timepoint )
	{
		ShmImg< ? > labelsData = new ShmImg<>( ( NDArray ) task.outputs.get( "labels" ) );
		ShmImg< ? > diameterData = new ShmImg<>( ( NDArray ) task.outputs.get( "equivalent_diameter_area" ) );
		ShmImg< ? > intensityData = new ShmImg<>( ( NDArray ) task.outputs.get( "intensity_mean" ) );
		ShmImg< ? > borderDistData = new ShmImg<>( ( NDArray ) task.outputs.get( "border_dist" ) );

		Iterator< ? > labels = labelsData.getImg().iterator();
		Iterator< ? > diameters = diameterData.getImg().iterator();
		Iterator< ? > intensities = intensityData.getImg().iterator();
		Iterator< ? > borderDists = borderDistData.getImg().iterator();
		while ( labels.hasNext() && diameters.hasNext() && intensities.hasNext() && borderDists.hasNext() )
		{
			int label = ( int ) ( ( RealType< ? > ) labels.next() ).getRealDouble();
			for ( final Spot spot : model.getSpatioTemporalIndex().getSpatialIndex( timepoint ) )
			{
				if ( ( spot.getInternalPoolIndex() + 1 ) == label )
				{
					double diameter = ( ( RealType< ? > ) diameters.next() ).getRealDouble();
					double intensity = ( ( RealType< ? > ) intensities.next() ).getRealDouble();
					double borderDist = ( ( RealType< ? > ) borderDists.next() ).getRealDouble();
					feature.valuesByProp.get( RegionProp.EQUIVALENT_DIAMETER_AREA ).get( sourceIndex ).set( spot, diameter );
					feature.valuesByProp.get( RegionProp.INTENSITY_MEAN ).get( sourceIndex ).set( spot, intensity );
					feature.valuesByProp.get( RegionProp.BORDER_DIST ).get( sourceIndex ).set( spot, borderDist );
				}
			}
		}
	}

	private static void writeInertiaTensorsToFeature( final Service.Task result, final SpotRegionPropsFeature feature, final Model model,
			final int sourceIndex, final int timepoint )
	{
		Iterable< Spot > spotsAtTimepoint = model.getSpatioTemporalIndex().getSpatialIndex( timepoint );
		ShmImg< ? > labelsData = new ShmImg<>( ( NDArray ) result.outputs.get( "labels" ) );
		Iterator< ? > labels = labelsData.getImg().iterator();

		ShmImg< ? > inertiaTensorData = new ShmImg<>( ( NDArray ) result.outputs.get( "inertia_tensor" ) );
		RandomAccess< ? > inertiaTensors = inertiaTensorData.getImg().randomAccess();

		int labelCount = 0;
		while ( labels.hasNext() )
		{
			int label = ( int ) ( ( RealType< ? > ) labels.next() ).getRealFloat();

			RegionProp[] regionProps = Arrays.copyOfRange( RegionProp.values(), RegionProp.INERTIA_TENSOR_XX.ordinal(),
					RegionProp.INERTIA_TENSOR_ZZ.ordinal() + 1 );
			float[] values = new float[ regionProps.length ];
			for ( int tensorIndex = 0; tensorIndex < regionProps.length; tensorIndex++ )
			{
				inertiaTensors.setPosition( new int[] { tensorIndex, labelCount } );
				values[ tensorIndex ] = ( ( FloatType ) inertiaTensors.get() ).get();
			}

			for ( final Spot spot : spotsAtTimepoint )
			{
				if ( ( spot.getInternalPoolIndex() + 1 ) == label )
				{
					for ( int tensorIndex = 0; tensorIndex < regionProps.length; tensorIndex++ )
						feature.valuesByProp.get( regionProps[ tensorIndex ] ).get( sourceIndex ).set( spot, values[ tensorIndex ] );
					logger.trace( "Set inertia tensor to spot {}: [[{}, {}, {}], [{}, {}, {}], [{}, {}, {}]]", spot.getInternalPoolIndex(),
							values[ 0 ], values[ 1 ], values[ 2 ], values[ 3 ], values[ 4 ], values[ 5 ], values[ 6 ], values[ 7 ],
							values[ 8 ] );
				}
			}
			labelCount++;
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
		return "import appose" + "\n"
				+ "import numpy as np" + "\n"
				+ "from tqdm import tqdm" + "\n"
				+ "from trackastra.data.wrfeat import get_features\n"
				+ "\n"
				+ "from trackastra.data.wrfeat import build_windows" + "\n"
				+ "from trackastra.model.pretrained import download_pretrained" + "\n"
				+ "from pathlib import Path" + "\n"
				+ "from trackastra.model import Trackastra" + "\n"
				+ "from trackastra.utils import normalize" + "\n"
				+ "\n"
				+ "task.update(message=\"Imports completed\")" + "\n"
				+ "\n"
				+ "image_ndarray = image.ndarray()" + "\n"
				+ "mask_ndarray = mask.ndarray().astype('int32')" + "\n"
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
				+ "image = normalize(image)" + "\n"
				+ "features = get_features(detections=mask, imgs=image, ndim=ndim, n_workers=0, progbar_class=tqdm)" + "\n"
				+ "\n"
				+ "task.update(message=\"Feature computation completed\")" + "\n"
				+ "\n"
				+ "labels = features[0].labels" + "\n"
				+ "shared_labels = appose.NDArray(str(labels.dtype), labels.shape)" + "\n"
				+ "shared_labels.ndarray()[:] = labels" + "\n"
				+ "diameter = features[0].features['equivalent_diameter_area']" + "\n"
				+ "shared_diameter = appose.NDArray(str(diameter.dtype), diameter.shape)" + "\n"
				+ "shared_diameter.ndarray()[:] = diameter" + "\n"
				+ "intensity = features[0].features['intensity_mean']" + "\n"
				+ "shared_intensity = appose.NDArray(str(intensity.dtype), intensity.shape)" + "\n"
				+ "shared_intensity.ndarray()[:] = intensity" + "\n"
				+ "inertia_tensor = features[0].features['inertia_tensor']" + "\n"
				+ "shared_inertia_tensor = appose.NDArray(str(inertia_tensor.dtype), inertia_tensor.shape)" + "\n"
				+ "shared_inertia_tensor.ndarray()[:] = inertia_tensor" + "\n"
				+ "border_dist = features[0].features['border_dist']" + "\n"
				+ "shared_border_dist = appose.NDArray(str(border_dist.dtype), border_dist.shape)" + "\n"
				+ "shared_border_dist.ndarray()[:] = border_dist" + "\n"
				+ "\n"
				+ "task.outputs['labels'] = shared_labels" + "\n"
				+ "task.outputs['equivalent_diameter_area'] = shared_diameter" + "\n"
				+ "task.outputs['intensity_mean'] = shared_intensity" + "\n"
				+ "task.outputs['inertia_tensor'] = shared_inertia_tensor" + "\n"
				+ "task.outputs['border_dist'] = shared_border_dist" + "\n"
				+ "\n"
				+ "task.update(message=\"Feature extraction completed. Found {} objects\".format(len(labels)))" + "\n";
	}
}
