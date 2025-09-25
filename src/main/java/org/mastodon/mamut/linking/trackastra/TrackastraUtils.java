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

public class TrackastraUtils
{
	public static final String TRACKASTRA_VERSION = "0.4.0";

	public static final String KEY_EDGE_THRESHOLD = "trackastraEdgeThreshold";

	public static final String KEY_TRACKASTRA_MODE = "trackastraMode";

	public static final String KEY_SOURCE = "trackastraSource";

	public static final String KEY_NUM_DIMENSIONS = "trackastraNumDimensions";

	public static final String KEY_MODEL = "trackastraModel";

	private static final int DEFAULT_SETUP_ID = 0;

	private static final double DEFAULT_EDGE_THRESHOLD = 0.05;

	public static final TrackastraMode DEFAULT_TRACKASTRA_MODE = TrackastraMode.GREEDY;

	public static final TrackastraModel DEFAULT_MODEL = TrackastraModel.CTC;

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
				+ "    - trackastra==" + TRACKASTRA_VERSION + "\n"
				+ "    - geff==0.5.0" + "\n";
	}

	public static Map< String, Object > getDefaultTrackAstraSettingsMap()
	{
		final Map< String, Object > settings = new HashMap<>();
		settings.put( KEY_MIN_TIMEPOINT, DEFAULT_MIN_TIMEPOINT );
		settings.put( KEY_MAX_TIMEPOINT, DEFAULT_MAX_TIMEPOINT );
		settings.put( KEY_DO_LINK_SELECTION, DEFAULT_DO_LINK_SELECTION );

		settings.put( DetectorKeys.KEY_SETUP_ID, DEFAULT_SETUP_ID );
		settings.put( KEY_EDGE_THRESHOLD, DEFAULT_EDGE_THRESHOLD );
		settings.put( KEY_TRACKASTRA_MODE, DEFAULT_TRACKASTRA_MODE );
		settings.put( KEY_MODEL, DEFAULT_MODEL );
		settings.put( KEY_LEVEL, DEFAULT_LEVEL );
		return settings;
	}
}
