package org.mastodon.mamut.linking.trackastra;

import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_MIN_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MIN_TIMEPOINT;
import static org.mastodon.tracking.linking.LinkerKeys.DEFAULT_DO_LINK_SELECTION;
import static org.mastodon.tracking.linking.LinkerKeys.KEY_DO_LINK_SELECTION;

import java.util.HashMap;
import java.util.Map;

import org.mastodon.tracking.detection.DetectorKeys;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.trackastra.TrackAstraLinkerDescriptor;

public class TrackAstraUtils
{
	private static final int DEFAULT_SETUP_ID = 0;

	private static final double DEFAULT_EDGE_THRESHOLD = 0.05;

	public static final TrackAstraMode DEFAULT_TRACKASTRA_MODE = TrackAstraMode.GREEDY;

	private TrackAstraUtils()
	{
		// prevent instantiation
	}

	public static Map< String, Object > getDefaultTrackAstraSettingsMap()
	{
		final Map< String, Object > settings = new HashMap<>();
		settings.put( KEY_MIN_TIMEPOINT, DEFAULT_MIN_TIMEPOINT );
		settings.put( KEY_MAX_TIMEPOINT, DEFAULT_MAX_TIMEPOINT );
		settings.put( KEY_DO_LINK_SELECTION, DEFAULT_DO_LINK_SELECTION );

		settings.put( DetectorKeys.KEY_SETUP_ID, DEFAULT_SETUP_ID );
		settings.put( TrackAstraLinkerDescriptor.KEY_EDGE_THRESHOLD, DEFAULT_EDGE_THRESHOLD );
		settings.put( TrackAstraLinkerDescriptor.KEY_TRACKASTRA_MODE, DEFAULT_TRACKASTRA_MODE );
		return settings;
	}
}
