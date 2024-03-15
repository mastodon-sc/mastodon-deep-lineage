package org.mastodon.mamut.io.importer.labelimage.ui;

import bdv.viewer.SourceAndConverter;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.LabelImageUtils;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

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
			+ "<h1>Import spots from instance segmentation in BDV channel</h1>\n"
			+ "<p>This command can import spots from label image data contained in a channel of the Big Data Viewer. The image data in that channel is assumed to represent an instance segmentation (i.e. a label image) that has been processed outside Mastodon. The existing labels in that channel will be used as spot names.</p>\n"
			+ "<p>The BDV channel that contains the labels has to be chosen.</p>\n"
			+ "<p>The ellipsoid scaling factor can be used to increase (>1) or decrease (&lt;1) the size of the resulting ellipsoid. 1 is equivalent of ellipsoids drawn at 2.2σ.</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	@SuppressWarnings( "unused" )
	@Parameter
	private ProjectModel projectModel;

	@Parameter( label = "Instance segmentation source", initializer = "initImgSourceChoices" )
	public String imgSourceChoice = "";

	@SuppressWarnings( "all" )
	@Parameter( label = "Ellipsoid scaling factor", min = "0", description = "Changes the size of the resulting ellipsoid in all dimensions. 1 means that the ellipsoid is drawn at 2.2σ, which is the default." )
	private double scaleFactor = 1;

	@SuppressWarnings( "unused" )
	private void initImgSourceChoices()
	{
		List< String > choices = LabelImageUtils.getSourceNames( projectModel.getSharedBdvData() );
		getInfo().getMutableInput( "imgSourceChoice", String.class ).setChoices( choices );
	}

	@Override
	public void run()
	{
		Optional< SourceAndConverter< ? > > sourceAndConverter = projectModel.getSharedBdvData().getSources().stream()
				.filter( source -> source.getSpimSource().getName().equals( imgSourceChoice ) ).findFirst();
		if ( !sourceAndConverter.isPresent() )
			return;
		LabelImageUtils.importSpotsFromBdvChannel( projectModel, sourceAndConverter.get().getSpimSource(), scaleFactor );
	}
}
