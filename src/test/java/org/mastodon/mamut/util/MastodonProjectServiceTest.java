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
