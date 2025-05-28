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
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose.Cellpose4DetectorDescriptor;
import org.scijava.Context;

class Cellpose4DetectorDescriptorTest
{
	@Test
	void testDescriptor()
	{
		Assertions.assertDoesNotThrow(
				() -> {
					Cellpose4DetectorDescriptor descriptor = new Cellpose4DetectorDescriptor();
					try (Context context = new Context())
					{
						descriptor.setContext( context );
						Model model = new Model();
						Img< FloatType > img = ArrayImgs.floats( 10, 10, 10 );
						ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, model, context );
						descriptor.aboutToHidePanel();
						descriptor.setAppModel( projectModel );
						Settings settings = new Settings();
						TrackMate trackMate = new TrackMate( settings, model, projectModel.getSelectionModel() );
						descriptor.setTrackMate( trackMate );
					}
				}
		);
	}
}
