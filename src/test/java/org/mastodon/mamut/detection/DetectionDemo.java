package org.mastodon.mamut.detection;

import java.io.IOException;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvStackSource;
import io.scif.img.ImgOpener;

public class DetectionDemo
{
	public static void main( String[] args ) throws IOException
	{
		String filePath = "/home/pol_bia/stha735e/Documents/Mastodon/1135_n_stain_TO-PRO-3.tif";
		//String filePath =
		//		"D:\\DeepLineage\\Datasets\\StarDist Plant Nuclei 3D\\Training image dataset_Tiff Files\\1135\\1135_n_stain_TO-PRO-3.tif";
		String starDistModelPath = "/home/pol_bia/stha735e/StarDist";
		ImgOpener imgOpener = new ImgOpener();
		Img< FloatType > img = imgOpener.openImgs( filePath, new FloatType() ).get( 0 );
		// Display the first image in a new BDV instance
		BdvStackSource< ? > bdvSource1 = BdvFunctions.show( img, "Original Image" );
		try (Cellpose4 cellpose = new Cellpose4( Cellpose4.MODEL_TYPE.CYTO ))
		{
			long startTime = System.currentTimeMillis();
			Img< FloatType > cellposeSegmentation = cellpose.segmentImage( img );
			long endTime = System.currentTimeMillis();
			System.out.println( "Cellpose segmentation time: " + ( endTime - startTime ) + " ms" );
			// Add cellpose segmentation as a second channel in the same BDV instance
			if ( cellposeSegmentation != null )
				BdvFunctions.show( cellposeSegmentation, "Cellpose Segmentation", Bdv.options().addTo( bdvSource1.getBdvHandle() ) );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
		try (StarDist3D starDist3D = new StarDist3D( starDistModelPath ))
		{
			long startTime = System.currentTimeMillis();
			Img< FloatType > starDistSegmentation = starDist3D.segmentImage( img );
			long endTime = System.currentTimeMillis();
			System.out.println( "StarDist segmentation time: " + ( endTime - startTime ) + " ms" );
			// Add star dist segmentation as a third channel in the same BDV instance
			if ( starDistSegmentation != null )
				BdvFunctions.show( starDistSegmentation, "StarDist Segmentation", Bdv.options().addTo( bdvSource1.getBdvHandle() ) );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
}
