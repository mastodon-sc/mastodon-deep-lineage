package org.mastodon.tracking.detection;

import io.bioimage.modelrunner.apposed.appose.MambaInstallException;
import io.bioimage.modelrunner.bioimageio.description.exceptions.ModelSpecsException;
import io.bioimage.modelrunner.exceptions.LoadEngineException;
import io.bioimage.modelrunner.exceptions.LoadModelException;
import io.bioimage.modelrunner.exceptions.RunModelException;
import io.bioimage.modelrunner.model.Stardist3D;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.util.Util;
import org.apache.commons.compress.archivers.ArchiveException;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.LabelImageUtils;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.scijava.app.StatusService;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

@Plugin( type = MamutPlugin.class )
public class StarDist3DDetectorPlugin implements MamutPlugin
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private ProjectModel projectModel;

	private static final String MODEL_NAME = "StarDist Plant Nuclei 3D ResNet";

	private static final String ITEM_NAME = "Star Dist 3D based Detection";

	private static final String[] STAR_DIST_3D_DETECTION_KEYS = { "not mapped" };

	private final AbstractNamedAction detectStarDist3D;

	@Override
	public void setAppPluginModel( ProjectModel projectModel )
	{
		this.projectModel = projectModel;
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList( menu( "Plugins", item( ITEM_NAME ) ) );
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( detectStarDist3D, STAR_DIST_3D_DETECTION_KEYS );
	}

	public StarDist3DDetectorPlugin()
	{
		detectStarDist3D = new RunnableAction( ITEM_NAME, this::detect );
	}

	private < T extends RealType< T > & NativeType< T > > void detect()
	{
		try
		{
			Stardist3D.installRequirements();
			logger.info( "Installed requirements" );
			// prediction
			Stardist3D model = Stardist3D.fromPretained( MODEL_NAME, false );
			logger.info( "Loaded model" );

			IntFunction< RandomAccessibleInterval< FloatType > > frameProvider = frameNumber -> {
				RandomAccessibleInterval< T > source = Cast.unchecked(
						projectModel.getSharedBdvData().getSpimData().getSequenceDescription().getImgLoader().getSetupImgLoader( 0 )
								.getImage( frameNumber ) );
				// TODO: UnsignedShortType is not supported by StarDist3D - need to convert How to find out the type of the image and if conversion is needed?
				Util.getTypeFromInterval( source ).getClass(); // e.g. UnsignedShortType
				RandomAccessibleInterval< FloatType > prediction = null;
				RandomAccessibleInterval< FloatType > input =
						Converters.convert( source, ( i, o ) -> o.setReal( i.getRealFloat() ), new FloatType() );
				try
				{
					logger.info( "Predicting spots using StarDist3D" );
					prediction = model.predict( input );
				}
				catch ( ModelSpecsException | LoadModelException | LoadEngineException | IOException | RunModelException
						| InterruptedException e )
				{
					logger.error( "Error while predicting spots using StarDist3D: {}", e.getMessage() );
				}
				return prediction;
			};

			logger.info( "Creating spots from label image" );
			LabelImageUtils.createSpotsFromLabelImage( frameProvider, projectModel.getModel(), 1d, true,
					projectModel.getSharedBdvData().getSpimData().getSequenceDescription(),
					projectModel.getContext().getService( StatusService.class ) );

		}
		catch ( IOException | InterruptedException | RuntimeException | MambaInstallException | ModelSpecsException | ArchiveException
				| URISyntaxException e )
		{
			logger.error( "Error while predicting spots using StarDist3D: {}", e.getMessage(), e );
		}
	}
}
