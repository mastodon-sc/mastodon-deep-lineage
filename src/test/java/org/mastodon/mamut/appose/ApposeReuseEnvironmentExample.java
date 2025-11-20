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

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.commons.lang3.time.StopWatch;
import org.apposed.appose.Appose;
import org.apposed.appose.Environment;
import org.apposed.appose.Service;
import org.apposed.appose.TaskEvent;
import org.apposed.appose.TaskException;
import org.apposed.appose.BuildException;
import org.mastodon.mamut.detection.cellpose.Cellpose3;

public class ApposeReuseEnvironmentExample
{
	public static void main( String[] args ) throws InterruptedException, BuildException, TaskException
	{
		Environment env = Appose.mamba().scheme( "environment.yml" ).content( Cellpose3.ENV_FILE_CONTENT ).logDebug().build();
		System.out.println( "Created environment" );
		StopWatch stopWatch = StopWatch.createStarted();

		String importScript = "import networkx as nx\n"
				+ "import appose\n"
				+ "from pandas import DataFrame\n"
				+ "\n"
				+ "task.update(message='Hello from import script')\n"
				+ "task.export(nx=nx, appose=appose, DataFrame=DataFrame)\n";

		String script = "task.update(message='Creating a graph...')\n"
				+ "G = nx.Graph()\n"
				+ "G.add_nodes_from(['A', 'B', 'C'])\n"
				+ "G.add_edges_from([('A', 'B'), ('B', 'C')])\n"
				+ "print('Nodes:', G.nodes())\n"
				+ "print('Edges:', G.edges())\n"
				+ "data = {'name': ['Alice', 'Bob'], 'age': [25, 30]}\n"
				+ "df = DataFrame(data)\n"
				+ "task.update(message='Graph created with ' + str(len(G.nodes())) + ' nodes and ' + str(len(G.edges())) + ' edges.')\n"
				+ "task.update(message='DataFrame created: ' + df.to_string())\n"
				+ "task.outputs['result']=len(G.nodes()) + len(G.edges())\n";

		try (Service python = env.python().init( "import numpy" ))
		{
			stopWatch.split();
			System.out.println( "Python service started: " + stopWatch.formatSplitTime() );
			Service.Task task1 = python.task( importScript, "main" );
			task1.listen( getTaskListener() );
			task1.waitFor();
			System.out.println( "Import script ran successfully." );
			stopWatch.split();
			System.out.println( "Import task finished: " + stopWatch.formatSplitTime() );

			// Now run the payload script that uses the imported modules
			Service.Task task2 = python.task( script );
			task2.listen( getTaskListener() );
			task2.waitFor();
			Object result = task2.outputs.get( "result" );
			System.out.println( "Nodes + Edges (result): " + result );

			stopWatch.split();
			System.out.println( "Payload task finished: " + stopWatch.formatSplitTime() );
		}
	}

	private static Consumer< TaskEvent > getTaskListener()
	{
		return taskEvent -> {
			String message = taskEvent.responseType.toString();
			message += taskEvent.message == null ? "" : ": " + taskEvent.message;
			System.out.println( message );
		};
	}
}
