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

import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.DEFAULT_GPU_ID;
import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.DEFAULT_GPU_MEMORY_FRACTION;
import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.KEY_GPU_ID;
import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.KEY_GPU_MEMORY_FRACTION;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.NumberFormatter;

import org.apache.commons.lang.StringUtils;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.AbstractSpotDetectorDescriptor;

import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;

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

	protected JComboBox< GpuEntry > gpuId;

	protected JFormattedTextField gpuMemoryFraction;

	protected abstract void addModelTypeSelection( final JPanel contentPanel );

	protected abstract void addRespectAnisotropyCheckbox( final JPanel contentPanel );

	@Override
	protected void configureDetectorSpecificFields( final JPanel contentPanel )
	{

		addModelTypeSelection( contentPanel );

		cellProbabilityThreshold = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 6.0, 0.1 ) );
		cellProbabilityThreshold.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		String cellProbText =
				"<html>Cell probability threshold:<br>0 ... more detections<br>6 ... viewer detections (in dim regions)</html>";
		JLabel cellProbLabel = new JLabel( cellProbText );
		cellProbLabel.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		contentPanel.add( cellProbLabel, "align left, wmax 220, growx, wrap" );
		contentPanel.add( cellProbabilityThreshold, "align left, grow" );

		flowThreshold = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 6.0, 0.1 ) );
		flowThreshold.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		String flowText =
				"<html>Flow threshold:<br>0 ... viewer (ill shaped) detections<br>6 ... more detections</html>";
		JLabel flowLabel = new JLabel( flowText );
		contentPanel.add( flowLabel, "align left, wmax 220, growx, wrap" );
		flowLabel.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		contentPanel.add( flowThreshold, "align left, grow" );

		diameter = new JFormattedTextField( getNumberFormatter( 0d, 1000d ) );
		diameter.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		diameter.setColumns( 10 );

		String diameterText = "<html>If you have a rough estimate of the diameter of a typical cell (in pixels), enter it here.<br></html>";
		JLabel diameterLabel = new JLabel( diameterText );
		diameterLabel.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		contentPanel.add( diameterLabel, "align left, wmax 220, growx, wrap" );
		contentPanel.add( diameter, "align left, grow" );

		SystemInfo si = new SystemInfo();
		List< GraphicsCard > gpus = si.getHardware().getGraphicsCards();
		gpuId = new JComboBox<>();
		gpuId.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		for ( int i = 0; i < gpus.size(); i++ )
		{
			gpuId.addItem( new GpuEntry( i, gpus.get( i ).getName() ) );
		}
		String gpuText = "<html>GPU to use for detection (if any):<br></html>";
		JLabel gpuLabel = new JLabel( gpuText );
		gpuLabel.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		contentPanel.add( gpuLabel, "align left, wmax 220, growx, wrap" );
		contentPanel.add( gpuId, "align left, grow" );

		gpuMemoryFraction = new JFormattedTextField( getNumberFormatter( 0d, 1d ) );
		gpuMemoryFraction.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		gpuMemoryFraction.setColumns( 10 );

		String gpuMemText = "<html>Fraction of GPU memory to use (0.0 - 1.0):<br></html>";
		JLabel gpuMemLabel = new JLabel( gpuMemText );
		gpuMemLabel.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		contentPanel.add( gpuMemLabel, "align left, wmax 220, growx, wrap" );
		contentPanel.add( gpuMemoryFraction, "align left, grow" );

		addRespectAnisotropyCheckbox( contentPanel );
	}

	@Override
	protected void getSettingsAndUpdateConfigPanel()
	{
		super.getSettingsAndUpdateConfigPanel();
		// Get the values.
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();

		final Object gpuIdObject = detectorSettings.get( KEY_GPU_ID );
		final int gpuIdValue;
		if ( null == gpuIdObject )
			gpuIdValue = DEFAULT_GPU_ID; // default
		else
			gpuIdValue = Integer.parseInt( String.valueOf( gpuIdObject ) );
		this.gpuId.setSelectedIndex( gpuIdValue );

		final Object gpuMemFractionObject =
				detectorSettings.get( KEY_GPU_MEMORY_FRACTION );
		final double gpuMemFractionValue;
		if ( null == gpuMemFractionObject )
			gpuMemFractionValue = DEFAULT_GPU_MEMORY_FRACTION; // default
		else
			gpuMemFractionValue = Double.parseDouble( String.valueOf( gpuMemFractionObject ) );
		this.gpuMemoryFraction.setValue( gpuMemFractionValue );
	}

	@Override
	protected void persistSettings()
	{
		super.persistSettings();
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		CellposeDetectorDescriptor.GpuEntry selected = ( GpuEntry ) this.gpuId.getSelectedItem();
		if ( selected != null )
			detectorSettings.put( KEY_GPU_ID, selected.id );
		detectorSettings.put( KEY_GPU_MEMORY_FRACTION, this.gpuMemoryFraction.getValue() );
	}

	@Override
	protected void grabSettings()
	{
		super.grabSettings();
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		CellposeDetectorDescriptor.GpuEntry selected = ( GpuEntry ) this.gpuId.getSelectedItem();
		if ( selected != null )
			detectorSettings.put( KEY_GPU_ID, selected.id );
		detectorSettings.put( KEY_GPU_MEMORY_FRACTION, this.gpuMemoryFraction.getValue() );
	}

	private static NumberFormatter getNumberFormatter( final double min, final double max )
	{
		// Create a NumberFormat for doubles
		NumberFormat doubleFormat = NumberFormat.getNumberInstance();
		doubleFormat.setGroupingUsed( false ); // disables thousand separators

		// Create a NumberFormatter that restricts values between min and max
		NumberFormatter numberFormatter = new NumberFormatter( doubleFormat );
		numberFormatter.setValueClass( Double.class );
		numberFormatter.setMinimum( min );
		numberFormatter.setMaximum( max );
		numberFormatter.setAllowsInvalid( false ); // Disallow invalid input
		numberFormatter.setCommitsOnValidEdit( true ); // Commit edits as soon as valid
		return numberFormatter;
	}

	// Wrap GPU info in a simple model class
	protected static class GpuEntry
	{
		int id;

		String name;

		GpuEntry( int id, String name )
		{
			this.id = id;
			this.name = name;
		}

		@Override
		public String toString()
		{
			return StringUtils.abbreviate( id + " – " + name, 30 );
		}
	}
}
