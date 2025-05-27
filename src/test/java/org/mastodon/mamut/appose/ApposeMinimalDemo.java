package org.mastodon.mamut.appose;

import java.io.File;
import java.io.IOException;

import org.apposed.appose.Appose;
import org.apposed.appose.Environment;
import org.apposed.appose.Service;

public class ApposeMinimalDemo
{
	public static void main( String[] args ) throws InterruptedException, IOException
	{
		Environment env = Appose.file( new File( "target/test-classes/org/mastodon/mamut/appose.yml" ), "environment.yml" ).build();
		try (Service python = env.python())
		{
			Service.Task task = python.task( "5 + 6" );
			task.waitFor();
			Object result = task.outputs.get( "result" );
			System.out.println( "result: " + result );
		}
	}
}
