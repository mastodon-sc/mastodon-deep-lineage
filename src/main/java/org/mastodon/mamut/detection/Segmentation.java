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

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;

import org.apposed.appose.NDArray;
import org.apposed.appose.Service;
import org.mastodon.mamut.util.appose.ApposeProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class providing a framework for image segmentation using Python-based processing.<br>
 * This class facilitates the transfer of images between the Java and Python environments
 * using shared memory and tracks task progress and execution time.
 */
public abstract class Segmentation extends ApposeProcess
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	protected Segmentation( final Service pythonService, final @Nullable org.scijava.log.Logger scijavaLogger )
	{
		super( pythonService, scijavaLogger );
	}

	/**
	 * Segments the input image using the configured Python runtime environment and
	 * returns the segmented image as an {@link Img}.
	 *
	 * @param inputImage the input image to be segmented.
	 * @param <T>        the type of the image.
	 * @return the segmented image.
	 * @throws IOException if there is an error during segmentation.
	 */
	public < T extends NativeType< T > > Img< T > segmentImage( final RandomAccessibleInterval< T > inputImage ) throws IOException
	{
		long[] dimensions = inputImage.dimensionsAsLongArray();
		String dimensionsAsString = Arrays.stream( dimensions ).mapToObj( String::valueOf ).collect( Collectors.joining( ", " ) );
		logger.info( "Segmenting image with {} dimensions: ({}) of type: {}", inputImage.numDimensions(), dimensionsAsString,
				inputImage.getType().getClass().getSimpleName() );

		try (ShmImg< T > sharedMemoryImage = ShmImg.copyOf( inputImage ))
		{
			stopWatch.split();
			if ( logger.isInfoEnabled() )
				logger.info( "Copied image to shared memory. Time elapsed: {}", stopWatch.formatSplitTime() );
			NDArray ndArray = NDArrays.asNDArray( sharedMemoryImage );
			stopWatch.split();
			if ( logger.isInfoEnabled() )
				logger.info( "Converted image to nd array: {}, Time elapsed: {}", ndArray, stopWatch.formatSplitTime() );

			inputs.put( "image", ndArray );
			Service.Task result = runScript();
			NDArray segmentedImageArray = ( NDArray ) result.outputs.get( "label_image" );
			ShmImg< T > segmentedImage = new ShmImg<>( segmentedImageArray );
			stopWatch.split();
			if ( logger.isInfoEnabled() )
				logger.info( "Converted output to image. Time elapsed: {}", stopWatch.formatSplitTime() );
			return segmentedImage;
		}
	}
}
