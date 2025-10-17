package org.mastodon.mamut.linking.trackastra.appose.types;

import net.imglib2.appose.ShmImg;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;

public class SingleTimepointRegionProps implements AutoCloseable
{
	final ShmImg< IntType > labels;

	final ShmImg< IntType > timepoints;

	final ShmImg< FloatType > coords;

	final ShmImg< FloatType > diameters;

	final ShmImg< FloatType > intensities;

	final ShmImg< FloatType > inertiaTensors;

	final ShmImg< FloatType > borderDists;

	public SingleTimepointRegionProps( final ShmImg< IntType > labels, final ShmImg< IntType > timepoints, final ShmImg< FloatType > coords,
			final ShmImg< FloatType > diameters, final ShmImg< FloatType > intensities,
			final ShmImg< FloatType > inertiaTensors, final ShmImg< FloatType > borderDists )
	{
		this.labels = labels;
		this.timepoints = timepoints;
		this.coords = coords;
		this.diameters = diameters;
		this.intensities = intensities;
		this.inertiaTensors = inertiaTensors;
		this.borderDists = borderDists;
	}

	@Override
	public void close()
	{
		labels.close();
		timepoints.close();
		coords.close();
		diameters.close();
		intensities.close();
		inertiaTensors.close();
		borderDists.close();
	}
}
