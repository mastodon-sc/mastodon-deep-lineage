package org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.AbstractSpotDetectorDescriptor;

public abstract class CellposeDetectorDescriptor extends AbstractSpotDetectorDescriptor
{
	protected JSpinner cellProbabilityThreshold;

	protected JSpinner flowThreshold;

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

		addRespectAnisotropyCheckbox( panel );
	}
}
