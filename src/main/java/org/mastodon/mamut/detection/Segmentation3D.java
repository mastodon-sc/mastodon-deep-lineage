package org.mastodon.mamut.detection;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import org.apposed.appose.Environment;
import org.apposed.appose.NDArray;
import org.apposed.appose.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Segmentation3D
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	// private constructor to prevent instantiation
	private Segmentation3D()
	{}

	public static Img< FloatType > segmentImage( final Img< FloatType > inputImage, final Algorithm algorithm, final String modelPath )
			throws InterruptedException
	{
		Img< FloatType > sharedMemoryImage = ShmImg.copyOf( inputImage );
		Environment environment;
		try
		{
			switch ( algorithm )
			{
			case STAR_DIST_3D:
				environment = StarDist3D.setUpEnv();
				break;
			case CELL_POSE:
				environment = CellPose.setUpEnv();
				break;
			default:
				throw new IllegalArgumentException( "Unknown algorithm: " + algorithm );
			}
		}
		catch ( IOException e )
		{
			logger.error( "Could not create python environment for segmentation: {}", e.getMessage(), e );
			return null;
		}
		try (Service python = environment.python())
		{
			// Store the image into a map of inputs to the Python script.
			final Map< String, Object > inputs = new HashMap<>();
			inputs.put( "image", NDArrays.asNDArray( sharedMemoryImage ) );

			String script;
			switch ( algorithm )
			{
			case STAR_DIST_3D:
				script = StarDist3D.generateScript( modelPath );
				break;
			case CELL_POSE:
				script = CellPose.generateScript();
				break;
			default:
				throw new IllegalArgumentException( "Unknown algorithm: " + algorithm );
			}
			// Run the script!
			Service.Task task = python.task( script, inputs );
			task.waitFor();
			// Verify that it worked.
			if ( task.status != Service.TaskStatus.COMPLETE )
				throw new PythonRuntimeException( "Python task failed with error: " + task.error );

			logger.info( "Python task completed" );
			NDArray labelImageArray = ( NDArray ) task.outputs.get( "label_image" );
			return new ShmImg<>( labelImageArray );
		}
		catch ( IOException e )
		{
			logger.error( "Could not create python service: {}", e.getMessage(), e );
		}
		catch ( InterruptedException e )
		{
			throw e;
		}
		return null;
	}
}
