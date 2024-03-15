package org.mastodon.mamut.io.importer.labelimage.ui;

import bdv.viewer.SourceAndConverter;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.LabelImageUtils;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Plugin( type = Command.class, label = "Import spots from BDV channel" )
public class ImportSpotsFromBdvChannelView extends DynamicCommand
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

	@Parameter( label = "Instance segmentation source", initializer = "initImgSourceChoices" )
	public String imgSourceChoice = "";

	@SuppressWarnings( "all" )
	@Parameter( label = "Sigma", min = "0", description = "Deviations from center to draw the ellipsoid border" )
	private double sigma = 2.1;

	@SuppressWarnings( "unused" )
	private void initImgSourceChoices()
	{
		final ArrayList< SourceAndConverter< ? > > sources = projectModel.getSharedBdvData().getSources();
		List< String > choices = new ArrayList<>();
		for ( SourceAndConverter< ? > source : sources )
			choices.add( source.getSpimSource().getName() );
		getInfo().getMutableInput( "imgSourceChoice", String.class ).setChoices( choices );
	}

	@Override
	public void run()
	{
		Optional< SourceAndConverter< ? > > sourceAndConverter = projectModel.getSharedBdvData().getSources().stream()
				.filter( source -> source.getSpimSource().getName().equals( imgSourceChoice ) ).findFirst();
		if ( !sourceAndConverter.isPresent() )
			return;
		LabelImageUtils.importSpotsFromBdvChannel( projectModel, sourceAndConverter.get().getSpimSource(), sigma );
	}
}
