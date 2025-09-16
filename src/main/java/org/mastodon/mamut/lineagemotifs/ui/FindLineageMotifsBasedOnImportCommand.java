package org.mastodon.mamut.lineagemotifs.ui;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImgToVirtualStack;
import net.imglib2.type.numeric.real.FloatType;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.io.importer.graphml.GraphMLImporter;
import org.mastodon.mamut.lineagemotifs.util.InvalidLineageMotifException;
import org.mastodon.mamut.lineagemotifs.util.LineageMotifsUtils;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.SelectionModel;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;

import ij.ImagePlus;

/**
 * Command to find similar lineage modules based on an imported lineage motif.
 */
@Plugin( type = Command.class, name = "Find similar lineage motifs based on an imported motif" )
public class FindLineageMotifsBasedOnImportCommand extends AbstractFindLineageMotifsCommand
{
	private ProjectModel tempProjectModel;

	@SuppressWarnings( "all" )
	@Parameter( visibility = ItemVisibility.MESSAGE, required = false, persist = false )
	private String documentation = "<html>\n"
			+ "<body width=" + WIDTH + "cm align=left>\n"
			+ "<h1>Find similar lineage motifs</h1>\n"
			+ "<p>This command finds a specifiable number of lineage motifs that are similar to a lineage motif that can be imported using a file in GraphML format.</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	@SuppressWarnings( "unused" )
	@Parameter( label = "Load motif from file" )
	private File motifFile;

	@Parameter( label = "Number of similar lineage motifs", min = "1", max = "1000", stepSize = "1" )
	private int numberOfSimilarLineage = 10;

	@Parameter( label = "Color" )
	private ColorRGB color = new ColorRGB( "red" );

	@Parameter( label = "Similarity measure", initializer = "initSimilarityMeasureChoices", callback = "update" )
	private String similarityMeasure = SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE.getName();

	@Parameter( label = "Scaling of the search motif", min = "0", description = "Scaling applied to the search motif. This is useful, if the motif loaded from the file is known to have a different time scale than the motifs to be searched in this project." )
	private double scaleFactor = 1d;

	@Parameter( label = "Run on branch graph", required = false, description = "Running this command on the branch graph (recommended option) will be much faster, but a bit less accurate. Running it on the model graph will be more accurate, but slower." )
	private boolean runOnBranchGraph = true;

	@Override
	protected BranchSpotTree getMotif() throws InvalidLineageMotifException, IOException
	{
		tempProjectModel = createTempProjectModel();
		try
		{
			GraphMLImporter.importGraphML( motifFile.getAbsolutePath(), tempProjectModel, 0, 1d );
			tempProjectModel.getBranchGraphSync().sync();
			selectAllSpotsAndLinks( tempProjectModel );
			return LineageMotifsUtils.getSelectedMotif( tempProjectModel );
		}
		catch ( IOException e )
		{
			throw new IOException( "Cannot import lineage motif from file: ", e );
		}
	}

	@Override
	protected double getScaleFactor()
	{
		return 1d / scaleFactor;
	}

	@Override
	protected void cleanUp()
	{
		if ( tempProjectModel != null )
			tempProjectModel.close();
	}

	private ProjectModel createTempProjectModel()
	{
		final Img< FloatType > img = ArrayImgs.floats( 1, 1, 1 );
		Model model = new Model();
		final SharedBigDataViewerData sharedBigDataViewerData = asSharedBdvDataXyz( img );
		return ProjectModel.create( getContext(), model, sharedBigDataViewerData, null );
	}

	private static SharedBigDataViewerData asSharedBdvDataXyz( final Img< FloatType > image1 )
	{
		final ImagePlus image =
				ImgToVirtualStack.wrap( new ImgPlus<>( image1, "image", new AxisType[] { Axes.X, Axes.Y, Axes.Z, Axes.TIME } ) );
		return Objects.requireNonNull( SharedBigDataViewerData.fromImagePlus( image ) );
	}

	private static void selectAllSpotsAndLinks( final ProjectModel pm )
	{
		final Model model = pm.getModel();
		final ModelGraph modelGraph = model.getGraph();
		final SelectionModel< Spot, Link > selection = pm.getSelectionModel();
		final ReentrantReadWriteLock lock = model.getGraph().getLock();
		lock.readLock().lock();
		try
		{
			selection.pauseListeners();
			selection.clearSelection();
			selection.setVerticesSelected( modelGraph.vertices(), true );
			selection.setEdgesSelected( modelGraph.edges(), true );
		}
		finally
		{
			selection.resumeListeners();
			lock.readLock().unlock();
		}
	}

	@Override
	protected int getNumberOfSimilarLineage()
	{
		return numberOfSimilarLineage;
	}

	@Override
	protected ColorRGB getColor()
	{
		return color;
	}

	@Override
	protected String getSimilarityMeasure()
	{
		return similarityMeasure;
	}

	@Override
	protected boolean isRunOnBranchGraph()
	{
		return runOnBranchGraph;
	}
}
