package org.mastodon.mamut.util;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MastodonProjectServiceTest
{
	@Test
	void test() throws IOException, SpimDataException
	{
		Model model = new Model();
		Img< FloatType > image = ArrayImgs.floats( 1, 1, 1 );
		File mastodonFile1 = DemoUtils.saveAppModelToTempFile( image, model );
		File mastodonFile2 = DemoUtils.saveAppModelToTempFile( image, model );
		try (Context context = new Context())
		{
			MastodonProjectService service = new MastodonProjectService();
			service.setContext( context );
			ProjectSession projectSession1 = service.createSession( mastodonFile1 );
			assertEquals( 1, service.activeSessions() );
			ProjectSession projectSession2 = service.createSession( mastodonFile1 );
			assertEquals( 1, service.activeSessions() );
			ProjectSession projectSession3 = service.createSession( mastodonFile2 );
			assertEquals( 2, service.activeSessions() );
			projectSession1.close();
			assertEquals( 2, service.activeSessions() );
			projectSession2.close();
			assertEquals( 1, service.activeSessions() );
			projectSession3.close();
			assertEquals( 0, service.activeSessions() );
		}
	}
}
