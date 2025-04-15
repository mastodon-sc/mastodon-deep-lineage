package org.mastodon.mamut.detection;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvStackSource;
import io.scif.img.ImgOpener;

public class DetectionDemo
{
	public static void main( String[] args )
	{
		String filePath = "/home/pol_bia/stha735e/Documents/Mastodon/1135_n_stain_TO-PRO-3.tif";
		String starDistModelPath = "/home/pol_bia/stha735e/StarDist";
		ImgOpener imgOpener = new ImgOpener();
		Img< FloatType > img = imgOpener.openImgs( filePath, new FloatType() ).get( 0 );
		try
		{
			long startTime = System.currentTimeMillis();
			Img< FloatType > starDistSegmentation = Segmentation3D.segmentImage( img, Algorithm.STAR_DIST_3D, starDistModelPath );
			long endTime = System.currentTimeMillis();
			System.out.println( "StarDist segmentation time: " + ( endTime - startTime ) + " ms" );
			startTime = System.currentTimeMillis();
			Img< FloatType > cellPoseSegmentation = Segmentation3D.segmentImage( img, Algorithm.CELL_POSE, null );
			endTime = System.currentTimeMillis();
			System.out.println( "CellPose segmentation time: " + ( endTime - startTime ) + " ms" );

			// Display the first image in a new BDV instance
			BdvStackSource< ? > bdvSource1 = BdvFunctions.show( img, "Original Image" );

			// Add the second image as a second channel in the same BDV instance
			if ( starDistSegmentation != null )
				BdvFunctions.show( starDistSegmentation, "StarDist Segmentation", Bdv.options().addTo( bdvSource1.getBdvHandle() ) );

			// Add the third image as a second channel in the same BDV instance
			if ( cellPoseSegmentation != null )
				BdvFunctions.show( cellPoseSegmentation, "CellPose Segmentation", Bdv.options().addTo( bdvSource1.getBdvHandle() ) );
		}
		catch ( InterruptedException e )
		{
			System.err.println( e.getMessage() );
		}
	}
}
