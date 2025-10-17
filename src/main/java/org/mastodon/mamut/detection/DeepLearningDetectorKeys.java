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
	 * Key for the parameter specifying what GPU to use. Expected values must be
	 * {@link Integer} representing the GPU ID, or {@code null} to use the CPU.
	 */
	public static final String KEY_GPU_ID = "GPU_ID";

	/**
	 * Key for the parameter specifying what fraction of the GPU memory to allocate. Expected values must be
	 * {@link Double} between 0 and 1.
	 */
	public static final String KEY_GPU_MEMORY_FRACTION = "GPU_MEMORY_FRACTION";

	/**
	 * Default value for the {@link #KEY_LEVEL} parameter.
	 * <br>
	 * The highest resolution level (0).
	 */
	public static final int DEFAULT_LEVEL = 0;

	/**
	 * Default value for the {@link #KEY_GPU_ID} parameter.
	 * <br>
	 * The first gpu (0).
	 */
	public static final int DEFAULT_GPU_ID = 0;

	/**
	 * Default value for the {@link #KEY_GPU_MEMORY_FRACTION} parameter.
	 * <br>
	 * Half of the GPU memory (0.5).
	 */
	public static final double DEFAULT_GPU_MEMORY_FRACTION = 0.5d;
}
