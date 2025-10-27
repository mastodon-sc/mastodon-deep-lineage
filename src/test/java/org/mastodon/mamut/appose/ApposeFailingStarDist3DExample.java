/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.appose;

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

		String content = "name: stardist\n"
				+ "channels:\n"
				+ "  - conda-forge\n"
				+ "dependencies:\n"
				+ "  - python=3.10\n"
				+ "  - cudatoolkit=11.2\n"
				+ "  - cudnn=8.1.0\n"
				+ "  - numpy<1.24\n"
				+ "  - pip\n"
				+ "  - pip:\n"
				+ "    - numpy<1.24\n"
				+ "    - tensorflow==2.10\n"
				+ "    - stardist==0.8.5\n"
				+ "    - appose";
		Environment env = Appose.mamba().scheme( "environment.yml" ).content( content ).logDebug().build();
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
