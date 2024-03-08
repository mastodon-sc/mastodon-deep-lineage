package org.mastodon.mamut.io.importer.labelimage.ui;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.LabelImageUtils;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = Command.class, label = "Import spots from BDV channel" )
public class ImportSpotsFromBdvChannelView extends ContextCommand
{
	private static final int WIDTH = 15;

	@SuppressWarnings( "all" )
	@Parameter( visibility = ItemVisibility.MESSAGE, required = false, persist = false )
	private String documentation = "<html>\n"
			+ "<body width=" + WIDTH + "cm align=left>\n"
			+ "<h1>Import spots from BDV channel</h1>\n"
			+ "<p>This command can import spots from image data contained in a channel of the Big Data Viewer. The image data in that channel is assumed to represent a segmentation (i.e. a label image) that has been processed outside Mastodon. The existing labels in that channel will be used as spot names.</p>\n"
			+ "<p>The index of the BDV channel that contains the labels has to be chosen. Counting starts at 0. </p>\n"
			+ "<p>The value σ can be chosen. This value determines where Mastodon will draw the resulting ellipsoid. Default is 2.1σ.</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	@SuppressWarnings( "unused" )
	@Parameter
	private ProjectModel projectModel;

	@SuppressWarnings( "all" )
	@Parameter( label = "Index of channel with labels", min = "0", description = "The index of the BDV channel that contains the labels. Counting of label channel indeces starts at 0.", validater = "validateChannelIndex" )
	private int labelChannelIndex = 0;

	@SuppressWarnings( "all" )
	@Parameter( label = "Sigma", min = "0", description = "Deviations from center to draw the ellipsoid border" )
	private double sigma = 2.1;

	@SuppressWarnings( "unused" )
	private void validateChannelIndex()
	{
		int numChannels = projectModel.getSharedBdvData().getSources().size();
		if ( labelChannelIndex >= numChannels )
			cancel( "You have chosen " + labelChannelIndex + " as channel index, but the available big data viewer source only contains "
					+ numChannels + " channels.\n"
					+ "Please choose a lower channel index. Channel indices start at 0." );
	}

	@Override
	public void run()
	{
		if ( isCanceled() )
			return;
		LabelImageUtils.importSpotsFromBdvChannel( projectModel, labelChannelIndex, sigma );
	}
}
