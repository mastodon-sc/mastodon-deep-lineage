/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
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
package org.mastodon.mamut.io.exporter.spotcounts;

import com.opencsv.CSVWriter;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.util.TreeUtils;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class ExportSpotCountsController
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Model model;

	private final StatusService statusService;

	public ExportSpotCountsController( final ProjectModel projectModel, final Context context )
	{
		// NB: Use the dimensions of the first source and the first time point only without checking if they are equal in other sources and time points.
		this( projectModel.getModel(), context );
	}

	protected ExportSpotCountsController( final Model model, final Context context )
	{
		this.model = model;
		this.statusService = context.service( StatusService.class );
	}

	/**
	 * Writes all timepoints and the number of spots for each timepoint to the given file.
	 * <ul>
	 *     <li>The file will be overwritten if it already exists.</li>
	 *     <li>The file will be created if it does not exist.</li>
	 *     <li>The format is: "timepoint", "number of spots".</li>
	 *     <li>The first line is the header.</li>
	 * </ul>
	 *
	 * @param file the file to write to.
	 */
	public void writeSpotCountsToFile( final File file )
	{
		if ( file == null )
			throw new IllegalArgumentException( "Cannot write spot counts to file. Given file is null." );

		try (FileWriter fileWriter = new FileWriter( file ); CSVWriter csvWriter = new CSVWriter( fileWriter );)
		{
			csvWriter.writeNext( new String[] { "timepoint", "spots" } );
			int maxTimepoint = TreeUtils.getMaxTimepoint( model );
			for ( int timepoint = TreeUtils.getMinTimepoint( model ); timepoint <= maxTimepoint; timepoint++ )
			{
				int spots = model.getSpatioTemporalIndex().getSpatialIndex( timepoint ).size();
				csvWriter.writeNext( new String[] { String.valueOf( timepoint ), String.valueOf( spots ) } );
				statusService.showProgress( timepoint, maxTimepoint );
			}
		}
		catch ( IOException e )
		{
			logger.error( "Could not write spot counts to file: {}. Error message: {}", file.getAbsolutePath(), e.getMessage(), e );
		}
	}
}
