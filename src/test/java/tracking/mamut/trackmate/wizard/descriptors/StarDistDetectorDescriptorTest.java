/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package tracking.mamut.trackmate.wizard.descriptors;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.tracking.mamut.trackmate.Settings;
import org.mastodon.tracking.mamut.trackmate.TrackMate;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.StarDistDetectorDescriptor;
import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.log.Logger;

class StarDistDetectorDescriptorTest
{
	@Test
	void testDescriptor()
	{
		Assertions.assertDoesNotThrow(
				() -> {
					StarDistDetectorDescriptor descriptor = new StarDistDetectorDescriptor();
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
