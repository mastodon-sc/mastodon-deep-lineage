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
			+ "<h1>Import spots from label image in ImageJ.</h1>\n"
			+ "<p>This command can import spots from the active image in ImageJ that contains an instance segmentation that has been processed outside Mastodon. The label image is assumed to match the existing big data viewer image in all dimensions (x,y,z and t). The existing labels will be used as spot names.</p>\n"
			+ "<p>The ellipsoid scaling factor can be used to increase (>1) or decrease (&lt;1) the size of the resulting ellipsoid. 1 is equivalent of ellipsoids drawn at 2.2σ.</p>\n"
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
	@Parameter( label = "Ellipsoid scaling factor", min = "0", description = "Changes the size of the resulting ellipsoid in all dimensions. 1 means that the ellipsoid is drawn at 2.2σ, which is the default." )
	private double scaleFactor = 1;

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
		LabelImageUtils.importSpotsFromImgPlus( projectModel, imgPlus, scaleFactor );
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
