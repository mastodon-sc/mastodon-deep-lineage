package org.mastodon.mamut;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.apposed.appose.Appose;
import org.apposed.appose.Environment;
import org.apposed.appose.Service;

public class ApposeFailingStarDist3DExample
{
	public static void main( String[] args ) throws IOException, InterruptedException
	{
		Img< FloatType > img = ArrayImgs.floats( 10, 10, 10 );
		Img< FloatType > shmImg = ShmImg.copyOf( img );

		File envFile = Files.createTempFile( "env", "yml" ).toFile();
		try (BufferedWriter writer = new BufferedWriter( new FileWriter( envFile ) ))
		{
			writer.write( "name: stardist\n" );
			writer.write( "channels:\n" );
			writer.write( "  - conda-forge\n" );
			writer.write( "dependencies:\n" );
			writer.write( "  - python=3.10\n" );
			writer.write( "  - cudatoolkit=11.2\n" );
			writer.write( "  - cudnn=8.1.0\n" );
			writer.write( "  - numpy<1.24\n" );
			writer.write( "  - pip\n" );
			writer.write( "  - pip:\n" );
			writer.write( "    - numpy<1.24\n" );
			writer.write( "    - tensorflow==2.10\n" );
			writer.write( "    - stardist==0.8.5\n" );
			writer.write( "    - appose\n" );
		}
		envFile.deleteOnExit();
		Environment env = Appose.file( envFile, "environment.yml" ).logDebug().build();
		System.out.println( "Created environment" );

		String script = "import skimage" + "\n"
				+ "import numpy as np" + "\n"
				+ "import appose" + "\n"
				+ "from csbdeep.utils import normalize" + "\n"
				+ "from stardist.models import StarDist3D" + "\n\n" // With this line, the python task runs forever
		;

		try (Service python = env.python())
		{
			// Store our Img into a map of inputs to the Python script.
			final Map< String, Object > inputs = new HashMap<>();
			inputs.put( "image", NDArrays.asNDArray( shmImg ) );

			Service.Task task = python.task( script, inputs );
			task.listen( event -> {
				switch ( event.responseType )
				{
				case UPDATE:
					System.out.println( "Task in progress." );
					break;
				case COMPLETION:
					System.out.println( "Task completed." );
					break;
				case CANCELATION:
					System.out.println( "Task canceled." );
					break;
				case FAILURE:
					System.out.println( "Task failed: " + task.error );
					break;
				default:
					System.out.println( "Unknown task event: " + event.responseType );
					break;
				}
			} );
			task.start();

			if ( !task.status.isFinished() )
			{
				System.out.println( "Task takes too long; canceling..." );
				// Task is taking too long; request a cancellation.
				task.cancel();
			}
			task.waitFor();
			System.out.println( "Task finished." );
		}
	}
}
