package tracking.mamut.trackmate.wizard.descriptors.cellpose;


import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.tracking.mamut.trackmate.Settings;
import org.mastodon.tracking.mamut.trackmate.TrackMate;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose.Cellpose3DetectorDescriptor;
import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.log.Logger;

class Cellpose3DetectorDescriptorTest
{
	@Test
	void testDescriptor()
	{
		Assertions.assertDoesNotThrow(
				() -> {
					Cellpose3DetectorDescriptor descriptor = new Cellpose3DetectorDescriptor();
					try (Context context = new Context())
					{
						descriptor.setContext( context );
						Logger logger = context.getService( LogService.class );
						descriptor.setLogger( logger );
						Model model = new Model();
						Img< FloatType > img = ArrayImgs.floats( 10, 10, 10 );
						ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, model, context );
						Settings settings = new Settings();
						TrackMate trackMate = new TrackMate( settings, model, projectModel.getSelectionModel() );
						descriptor.setAppModel( projectModel );
						descriptor.setTrackMate( trackMate );
						descriptor.aboutToHidePanel();
					}
				}
		);
	}
}
