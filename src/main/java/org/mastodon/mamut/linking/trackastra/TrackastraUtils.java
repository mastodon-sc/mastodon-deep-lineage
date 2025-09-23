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

import org.mastodon.mamut.model.Spot;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.tracking.detection.DetectorKeys;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.trackastra.TrackastraLinkerDescriptor;

public class TrackastraUtils
{
	public static final String TRACKASTRA_VERSION = "0.3.2";
	// TODO: upgrade to trackastra 0.4.0

	private static final int DEFAULT_SETUP_ID = 0;

	private static final double DEFAULT_EDGE_THRESHOLD = 0.05;

	public static final TrackastraMode DEFAULT_TRACKASTRA_MODE = TrackastraMode.GREEDY;

	private static final int DEFAULT_LEVEL = 0;

	private TrackastraUtils()
	{
		// prevent instantiation
	}

	public static String getEnv( final String apposeVersion )
	{
		return "name: trackastra\n"
				+ "channels:\n"
				+ "  - conda-forge\n"
				+ "channel_priority: strict\n"
				+ "dependencies:\n"
				+ "  - python=3.10\n"
				+ apposeVersion.substring( 2 ) + "\n"
				+ "  - pip\n"
				+ "  - pip:\n"
				+ "    - trackastra==" + TRACKASTRA_VERSION + "\n";
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

	static int getMaxSpots( final int minTimepoint, final int maxTimepoint, final SpatioTemporalIndex< Spot > index )
	{
		int maxSpots = Integer.MIN_VALUE;
		for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++ )
		{
			int nSpots = index.getSpatialIndex( timepoint ).size();
			if ( nSpots > maxSpots )
				maxSpots = nSpots;
		}
		return maxSpots;
	}
}
