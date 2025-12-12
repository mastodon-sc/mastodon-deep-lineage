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

import org.mastodon.mamut.util.appose.ApposeProcess;
import org.mastodon.tracking.detection.DetectorKeys;

public class TrackastraUtils
{

	public static final String TRACKASTRA_VERSION = "0.4.2";

	public static final String ENV_NAME = "trackastra";

	public static final String ENV_FILE_CONTENT = "name: " + ENV_NAME + "\n"
			+ "channels:\n"
			+ "  - conda-forge\n"
			+ "channel_priority: strict\n"
			+ "dependencies:\n"
			+ "  - python=3.10\n"
			+ "  - pip\n"
			+ "  - pip:\n"
			+ "    - appose==" + ApposeProcess.APPOSE_PYTHON_VERSION + "\n"
			+ "    - trackastra==" + TRACKASTRA_VERSION + "\n";

	public static final String KEY_EDGE_THRESHOLD = "trackastraEdgeThreshold";

	public static final String KEY_TRACKASTRA_MODE = "trackastraMode";

	public static final String KEY_SOURCE = "trackastraSource";

	public static final String KEY_NUM_DIMENSIONS = "trackastraNumDimensions";

	public static final String KEY_MODEL = "trackastraModel";

	public static final String KEY_WINDOW_SIZE = "trackastraWindowSize";

	private static final int DEFAULT_SETUP_ID = 0;

	private static final double DEFAULT_EDGE_THRESHOLD = 0.05;

	public static final TrackastraMode DEFAULT_TRACKASTRA_MODE = TrackastraMode.GREEDY;

	public static final TrackastraModel DEFAULT_MODEL = TrackastraModel.CTC;

	private static final int DEFAULT_LEVEL = 0;

	private static final int DEFAULT_WINDOW_SIZE = 4;

	private TrackastraUtils()
	{
		// prevent instantiation
	}

	public static String getEnv()
	{
		return ENV_FILE_CONTENT;
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
		settings.put( KEY_WINDOW_SIZE, DEFAULT_WINDOW_SIZE );
		return settings;
	}
}
