package org.mastodon.mamut.util;

import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;

public class GpuCheckDemo
{
	public static void main( String[] args )
	{
		SystemInfo si = new SystemInfo();
		for ( GraphicsCard gpu : si.getHardware().getGraphicsCards() )
		{
			System.out.println( "GPU: " + gpu.getName() );
		}
	}
}
