package org.mastodon.mamut.appose;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.apposed.appose.Appose;
import org.apposed.appose.BuildException;
import org.apposed.appose.Environment;
import org.apposed.appose.Service;
import org.apposed.appose.TaskException;
import org.apposed.appose.builder.Builders;
import org.apposed.appose.util.Environments;

public class ApposeCellpose3Example
{
	public static void main( String[] args ) throws InterruptedException, BuildException, TaskException
	{
		Img< FloatType > img = ArrayImgs.floats( 10, 10, 10 );
		Img< FloatType > shmImg = ShmImg.copyOf( img );

		final String sb = "name: cellpose3\n"
				+ "channels:\n"
				+ "  - nvidia\n"
				+ "  - pytorch\n"
				+ "  - conda-forge\n"
				+ "channel_priority: strict\n"
				+ "dependencies:\n"
				+ "  - python=3.10\n"
				+ "  - cellpose==3.1.1.2\n"
				+ "  - pytorch\n" // if pytorch is added to the env, the import of numpy leads to the script not completing on windows
				+ "  - pytorch-cuda\n" // if pytorch is added to the env, the import of numpy leads to the script not completing on windows
				+ "  - numpy\n"
				+ "  - pip\n"
				+ "  - pip:\n"
				+ "    - appose==0.7.1\n";
		System.out.println( "appose envs dir: " + Environments.apposeEnvsDir() );
		System.out.println( "envExists: " + Builders.canWrap( new File( Environments.apposeEnvsDir(), "cellpose3" ) ) );
		Environment env = Appose.mamba().content( sb ).scheme( "environment.yml" ).logDebug().build();
		System.out.println( "Created environment" );

		String script = "";
		script += "import appose" + "\n";
		script += "import torch" + "\n";
		script += "import numpy\n\n";
		script += "from cellpose import models" + "\n";
		script += "print('Hello world from python')" + "\n";

		try (Service python = env.python().init( "import numpy" )) // needed to run the script properly on Windows
		{
			// Store our Img into a map of inputs to the Python script.
			final Map< String, Object > inputs = new HashMap<>();
			inputs.put( "image", NDArrays.asNDArray( shmImg ) );
			python.debug( System.out::println );

			// Run the script using provided inputs.
			Service.Task task = python.task( script, inputs, "main" );
			task.waitFor();

			// Verify that it worked.
			if ( task.status != Service.TaskStatus.COMPLETE )
			{
				throw new RuntimeException( "Python script failed with error: " + task.error );
			}
			System.out.println( "Python script completed" );
		}
	}
}
