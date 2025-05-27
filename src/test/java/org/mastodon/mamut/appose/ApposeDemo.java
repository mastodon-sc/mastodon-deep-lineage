package org.mastodon.mamut.appose;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apposed.appose.Appose;
import org.apposed.appose.Environment;
import org.apposed.appose.NDArray;
import org.apposed.appose.Service;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvStackSource;
import io.scif.img.ImgOpener;
import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

public class ApposeDemo
{
	public static void main( String[] args ) throws IOException, InterruptedException
	{
		// Specify the path to the TIFF file
		String filePath = "/home/pol_bia/stha735e/Documents/Mastodon/1135_n_stain_TO-PRO-3.tif";
		String starDistModelPath = "/home/pol_bia/stha735e/StarDist";
		ImgOpener imgOpener = new ImgOpener();
		Img< FloatType > img = imgOpener.openImgs( filePath, new FloatType() ).get( 0 );
		//Img< FloatType > img = ArrayImgs.floats( 20, 20, 20 ); // empty image
		Img< FloatType > shmImg = ShmImg.copyOf( img );

		String script = "import numpy as np" + "\n"
				+ "import appose" + "\n"
				+ "from csbdeep.utils import normalize" + "\n"
				+ "from stardist.models import StarDist3D" + "\n"
				+ "np.random.seed(6)" + "\n"
				+ "axes_normalize = (0, 1, 2)" + "\n"
				+ "print(\"Loading StarDist pretrained 3D model\")" + "\n"
				// sb.append( "model = StarDist3D.from_pretrained('3D_demo')" ).append( "\n" );
				+ "model = StarDist3D(None, name='stardist-plant-nuclei-3d', basedir=r\"" + starDistModelPath + "\")" + "\n"
				+ "narr = image.ndarray()" + "\n"
				+ "image_normalized = normalize(narr, 1, 99.8, axis=axes_normalize)" + "\n"
				+ "print(\"Image shape:\", image_normalized.shape)" + "\n"
				+ "guessed_tiles = model._guess_n_tiles(image_normalized)" + "\n"
				+ "print(\"Guessed tiling:\", guessed_tiles)" + "\n"
				+ "label_image, details = model.predict_instances(image_normalized, axes='ZYX', n_tiles=guessed_tiles)" + "\n"
				+ "shared = appose.NDArray(image.dtype, image.shape)" + "\n"
				+ "shared.ndarray()[:] = label_image" + "\n"
				+ "task.outputs['label_image'] = shared" + "\n";

		System.out.println( "Script: " + script );

		String envYmlPath = "target/test-classes/org/mastodon/mamut/stardist.yml";
		File envYmlFile = new File( envYmlPath );
		System.out.println( "env yml file: " + envYmlFile.getAbsoluteFile() );
		Environment env = Appose.file( envYmlFile, "environment.yml" ).logDebug().build();
		System.out.println( "Created environment" );

		// Display the first image in a new BDV instance
		BdvStackSource<?> bdvSource1 = BdvFunctions.show(img, "Original Image");

		try (Service python = env.python())
		{
			// Store our Img into a map of inputs to the Python script.
			final Map< String, Object > inputs = new HashMap<>();
			inputs.put( "image", NDArrays.asNDArray( shmImg ) );

			// Run the script!
			Service.Task task = python.task( script, inputs );
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

			// Add the second image as a second channel in the same BDV instance
			BdvFunctions.show( labelImage, "Segmentation", Bdv.options().addTo( bdvSource1.getBdvHandle() ) );

			System.out.println( "label image size: " + labelImage.size() );
		}
	}
}
