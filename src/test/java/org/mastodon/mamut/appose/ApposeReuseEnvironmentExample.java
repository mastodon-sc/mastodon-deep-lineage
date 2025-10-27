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
package org.mastodon.mamut.appose;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.lang3.time.StopWatch;
import org.apposed.appose.Appose;
import org.apposed.appose.Environment;
import org.apposed.appose.Service;

public class ApposeReuseEnvironmentExample
{
	public static void main( String[] args ) throws IOException, InterruptedException
	{
		String content = "name: cellpose\n"
				+ "channels:\n"
				+ "  - nvidia\n"
				+ "  - pytorch\n"
				+ "  - conda-forge\n"
				+ "dependencies:\n"
				+ "  - python=3.10\n"
				+ "  - pip\n"
				+ "  - pip:\n"
				+ "    - cellpose[gui]\n"
				+ "    - appose\n"
				+ "  - pytorch\n"
				+ "  - pytorch-cuda=11.8\n"
				+ "  - numpy=2.0.2\n";

		Environment env = Appose.mamba().scheme( "environment.yml" ).content( content ).logDebug().build();
		System.out.println( "Created environment" );
		StopWatch stopWatch = StopWatch.createStarted();

		String script = "import numpy as np" + "\n"
				+ "from cellpose import models" + "\n"
				+ "import appose" + "\n\n"
				+ "5 + 6" + "\n";

		try (Service python = env.python())
		{
			stopWatch.split();
			System.out.println( "Python service started: " + stopWatch.formatSplitTime() );
			Service.Task task1 = python.task( script );
			task1.waitFor();
			Object result1 = task1.outputs.get( "result" );
			System.out.println( "result1: " + result1 );
			stopWatch.split();
			System.out.println( "Python task1 finished: " + stopWatch.formatSplitTime() );

			Service.Task task2 = python.task( script );
			task2.waitFor();
			Object result2 = task2.outputs.get( "result" );
			System.out.println( "result2: " + result2 );

			stopWatch.split();
			System.out.println( "Python task2 finished: " + stopWatch.formatSplitTime() );
		}
	}
}
