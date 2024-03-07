package org.mastodon.mamut.io.importer.labelimage.ui;

import net.imagej.ImgPlus;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.LabelImageUtils;
import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import java.util.Arrays;

@Plugin( type = Command.class, label = "Import spots from ImageJ image" )
public class ImportSpotsFromImgPlusView< T > extends ContextCommand
{

	private static final int WIDTH = 15;

	private static final String TEMPLATE = "<html>\n"
			+ "<body width=" + WIDTH + "cm align=left>\n"
			+ "<h1>Import spots from ImageJ image.</h1>\n"
			+ "<p>This command can import spots from an image that contains a segmentation that has been processed outside Mastodon. The segmentation is assumed to be represented as a label image that matches the existing big data viewer image in all dimensions (x,y,z and t). The existing labels will be used as spot names.</p>\n"
			+ "<p>The value σ can be chosen. This value determines where Mastodon will draw the resulting ellipsoid. Default is 2.2σ.</p>"
			+ "<p>The active image in ImageJ is: %s.<br>\n"
			+ "<p>It has the these dimensions: x=%s, y=%s, z=%s, t=%s.</p>\n"
			+ "<p>The big data viewer image has these dimensions: x=%s, y=%s, z=%s, t=%s.</p>\n"
			+ "<p>%s</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	@Parameter( type = ItemIO.INPUT, validater = "validateImageData" )
	private ImgPlus< T > imgPlus;

	@SuppressWarnings( "unused" )
	@Parameter( visibility = ItemVisibility.MESSAGE, required = false, persist = false, initializer = "initMessage" )
	private String documentation;

	@SuppressWarnings( "unused" )
	@Parameter
	private ProjectModel projectModel;

	@SuppressWarnings( "all" )
	@Parameter( label = "Sigma", min = "0", description = "Deviations from center to draw the ellipsoid border" )
	private double sigma = 2.2;

	@SuppressWarnings( "unused" )
	private void validateImageData()
	{
		if ( !LabelImageUtils.dimensionsMatch( projectModel.getSharedBdvData(), imgPlus ) )
		{
			String imgPlusDimensions = Arrays.toString( LabelImageUtils.getImgPlusDimensions( imgPlus ) );
			String bdvDimensions = Arrays.toString( LabelImageUtils.getBdvDimensions( projectModel.getSharedBdvData() ) );
			cancel( String.format(
					"The dimensions of the ImageJ image (%s) do not match the dimensions of the big data viewer image (%s)."
							+ "\nThus no spots can be imported."
							+ "\nPlease make sure the dimensions match.",
					imgPlusDimensions, bdvDimensions ) );
		}
	}

	@Override
	public void run()
	{
		if ( isCanceled() )
			return;
		LabelImageUtils.importSpotsFromImgPlus( imgPlus, sigma, projectModel );
	}

	@SuppressWarnings( "unused" )
	private void initMessage()
	{
		if ( imgPlus == null )
			return;
		long[] bdvDimensions = LabelImageUtils.getBdvDimensions( projectModel.getSharedBdvData() );
		long[] imgPlusDimensions = LabelImageUtils.getImgPlusDimensions( imgPlus );
		boolean dimensionsMatch = Arrays.equals( bdvDimensions, imgPlusDimensions );
		String color = dimensionsMatch ? "green" : "red";
		String doNot = dimensionsMatch ? "" : " do not";
		String dimensionMatch = "<font color=" + color + ">The dimensions" + doNot + " match.</font>";
		documentation = String.format( TEMPLATE, imgPlus.getName(), imgPlusDimensions[ 0 ], imgPlusDimensions[ 1 ], imgPlusDimensions[ 2 ],
				imgPlusDimensions[ 3 ], bdvDimensions[ 0 ], bdvDimensions[ 1 ], bdvDimensions[ 2 ], bdvDimensions[ 3 ], dimensionMatch );
	}
}
