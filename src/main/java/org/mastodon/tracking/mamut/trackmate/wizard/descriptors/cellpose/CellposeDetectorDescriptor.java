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
package org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.NumberFormatter;

import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.AbstractSpotDetectorDescriptor;

/**
 * The {@code CellposeDetectorDescriptor} is an abstract superclass for CellposeDetector descriptors.<br>
 * It provides the common configuration fields for Cellpose-based spot detection,
 * including cell probability threshold, flow threshold, and diameter.<br>
 */
public abstract class CellposeDetectorDescriptor extends AbstractSpotDetectorDescriptor
{
	protected JSpinner cellProbabilityThreshold;

	protected JSpinner flowThreshold;

	protected JFormattedTextField diameter;

	protected abstract void addModelTypeSelection( final ConfigPanel panel );

	protected abstract void addRespectAnisotropyCheckbox( final ConfigPanel panel );

	@Override
	protected void configureDetectorSpecificFields( final ConfigPanel panel )
	{

		addModelTypeSelection( panel );

		cellProbabilityThreshold = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 6.0, 0.1 ) );
		String cellProbText =
				"<html>Cell probability threshold:<br>0 ... more detections<br>6 ... viewer detections (in dim regions)</html>";
		JLabel cellProbLabel = new JLabel( cellProbText );
		panel.add( cellProbLabel, "align left, wmin 200, wrap" );
		panel.add( cellProbabilityThreshold, "align left, grow" );

		flowThreshold = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 6.0, 0.1 ) );
		String flowText =
				"<html>Flow threshold:<br>0 ... viewer (ill shaped) detections<br>6 ... more detections</html>";
		JLabel flowLabel = new JLabel( flowText );
		panel.add( flowLabel, "align left, wmin 200, wrap" );
		panel.add( flowThreshold, "align left, grow" );

		diameter = new JFormattedTextField( getNumberFormatter() );
		diameter.setColumns( 10 );

		String diameterText = "<html>If you have a rough estimate of the diameter of a typical cell (in pixels), enter it here.<br></html>";
		JLabel diameterLabel = new JLabel( diameterText );
		panel.add( diameterLabel, "align left, wmin 200, wrap" );
		panel.add( diameter, "align left, grow" );

		addRespectAnisotropyCheckbox( panel );
	}

	private static NumberFormatter getNumberFormatter()
	{
		// Create a NumberFormat for doubles
		NumberFormat doubleFormat = NumberFormat.getNumberInstance();
		doubleFormat.setGroupingUsed( false ); // disables thousand separators

		// Create a NumberFormatter that restricts values between 0 and 1000
		NumberFormatter numberFormatter = new NumberFormatter( doubleFormat );
		numberFormatter.setValueClass( Double.class );
		numberFormatter.setMinimum( 0.0 );
		numberFormatter.setMaximum( 1000.0 );
		numberFormatter.setAllowsInvalid( false ); // Disallow invalid input
		numberFormatter.setCommitsOnValidEdit( true ); // Commit edits as soon as valid
		return numberFormatter;
	}
}
