package org.mastodon.mamut;

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
import org.apposed.appose.Environment;
import org.apposed.appose.NDArray;
import org.apposed.appose.Service;

import io.scif.img.ImgOpener;

public class ApposeCellposeExample
{
	public static void main( String[] args ) throws IOException, InterruptedException
	{
		// Specify the path to the TIFF file
		String filePath = "target/test-classes/org/mastodon/mamut/nuclei_3d.tif";
		ImgOpener imgOpener = new ImgOpener();
		// Img< FloatType > img = imgOpener.openImgs( filePath, new FloatType() ).get( 0 );
		Img< FloatType > img = ArrayImgs.floats( 20, 20, 20 );
		Img< FloatType > shmImg = ShmImg.copyOf( img );

		StringBuilder sb = new StringBuilder();
		//sb.append( "import numpy as np" ).append( "\n" );
		//sb.append( "from cellpose import models" ).append( "\n" );
		sb.append( "import logging" ).append( "\n" );
		//sb.append( "narr = image.ndarray()" ).append( "\n" );

		//sb.append( "print(\"Image shape: \", image.shape)" ).append( "\n" );
		//sb.append( "model = models.Cellpose(model_type=model_type, gpu=True)" ).append( "\n\n" );
		//sb.append( "logging.info(\"Starting Cellpose segmentation...\")" ).append( "\n" );

		//sb.append( "logging.info(\"Starting Cellpose segmentation...\")" ).append( "\n\n" );
		//sb.append(
		//		"segmentation, flows, styles, diams = model.eval(image, diameter=None, channels=[0, 0],do_3D=True, anisotropy=1.0, z_axis=0, normalize=True)" )
		//		.append( "\n" );
		//sb.append( "logging.info(\"Segmentation complete.\")" ).append( "\n" );
		//sb.append( "shared = appose.NDArray(image.dtype, image.shape)" ).append( "\n" );
		//sb.append( "shared.ndarray()[:] = segmentation" ).append( "\n" );
		//sb.append( "task.outputs['label_image'] = shared" ).append( "\n" );

		String script = sb.toString();

		String envYmlPath = "target/test-classes/org/mastodon/mamut/cellpose.yml";
		File envYmlFile = new File( envYmlPath );
		System.out.println( envYmlFile.getAbsoluteFile() );
		Environment env = Appose.file( envYmlFile, "environment.yml" ).logDebug().build();
		System.out.println( "Created environment" );

		try (Service python = env.python())
		{
			// Store our Img into a map of inputs to the Python script.
			final Map< String, Object > inputs = new HashMap<>();
			inputs.put( "image", NDArrays.asNDArray( shmImg ) );

			// Run the script!
			// Service.Task task = python.task( script, inputs );
			Service.Task task = python.task( script );
			task.waitFor();

			// Verify that it worked.
			if ( task.status != Service.TaskStatus.COMPLETE )
			{
				throw new RuntimeException( "Python script failed with error: " + task.error );
			}

			System.out.println( "Python script completed" );

			// Access the `label_image` output NDArray.
			NDArray labelImageArray = ( NDArray ) task.outputs.get( "label_image" );
			// Wrap the NDArray to an Img.
			Img< FloatType > labelImage = new ShmImg<>( labelImageArray );

			// System.out.println( "label image size: " + labelImage.size() );
		}
	}
}
