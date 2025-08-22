package org.mastodon.mamut.detection;

public class DeepLearningDetectorKeys
{
	private DeepLearningDetectorKeys()
	{
		// Prevent instantiation
	}

	/**
	 * Key for the parameter specifying what resolution level to perform on. Expected values must be
	 * {@link Integer} larger or equal to 0.
	 */
	public static final String KEY_LEVEL = "LEVEL";

	/**
	 * Default value for the {@link #KEY_LEVEL} parameter.
	 * <br>
	 * The highest resolution level (0).
	 */
	public static final int DEFAULT_LEVEL = 0;
}
