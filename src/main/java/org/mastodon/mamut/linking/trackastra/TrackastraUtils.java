package org.mastodon.mamut.linking.trackastra;

import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.KEY_LEVEL;
import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_MIN_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MIN_TIMEPOINT;
import static org.mastodon.tracking.linking.LinkerKeys.DEFAULT_DO_LINK_SELECTION;
import static org.mastodon.tracking.linking.LinkerKeys.KEY_DO_LINK_SELECTION;

import java.util.HashMap;
import java.util.Map;

import org.mastodon.tracking.detection.DetectorKeys;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.trackastra.TrackastraLinkerDescriptor;

public class TrackastraUtils
{
	private static final int DEFAULT_SETUP_ID = 0;

	private static final double DEFAULT_EDGE_THRESHOLD = 0.05;

	public static final TrackAstraMode DEFAULT_TRACKASTRA_MODE = TrackAstraMode.GREEDY;

	private static final int DEFAULT_LEVEL = 0;

	private TrackastraUtils()
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
		settings.put( TrackastraLinkerDescriptor.KEY_EDGE_THRESHOLD, DEFAULT_EDGE_THRESHOLD );
		settings.put( TrackastraLinkerDescriptor.KEY_TRACKASTRA_MODE, DEFAULT_TRACKASTRA_MODE );
		settings.put( KEY_LEVEL, DEFAULT_LEVEL );
		return settings;
	}
}
