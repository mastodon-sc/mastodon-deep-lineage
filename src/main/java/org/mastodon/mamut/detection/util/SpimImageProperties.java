package org.mastodon.mamut.detection.util;

/**
 * This class encapsulates the following properties: minTimepoint, maxTimepoint, setupId and resolutionLevel
 */
public class SpimImageProperties
{

	private final int minTimepoint;

	private final int maxTimepoint;

	private final int setupId;

	private final int resolutionLevel;

	public SpimImageProperties( int minTimepoint, int maxTimepoint, int setupId, int resolutionLevel )
	{
		this.minTimepoint = minTimepoint;
		this.maxTimepoint = maxTimepoint;
		this.setupId = setupId;
		this.resolutionLevel = resolutionLevel;
	}

	public int getMinTimepoint()
	{
		return minTimepoint;
	}

	public int getMaxTimepoint()
	{
		return maxTimepoint;
	}

	public int getSetupId()
	{
		return setupId;
	}

	public int getResolutionLevel()
	{
		return resolutionLevel;
	}

	@Override
	public String toString()
	{
		return "SpimImageProperties{" +
				"minTimepoint=" + minTimepoint +
				", maxTimepoint=" + maxTimepoint +
				", setupId=" + setupId +
				", resolutionLevel=" + resolutionLevel +
				'}';
	}
}
