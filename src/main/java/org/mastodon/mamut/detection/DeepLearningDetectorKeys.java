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
