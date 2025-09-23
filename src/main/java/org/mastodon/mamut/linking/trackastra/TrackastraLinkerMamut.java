package org.mastodon.mamut.linking.trackastra;

import java.util.Map;

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.tracking.mamut.linking.AbstractSpotLinkerOp;
import org.mastodon.tracking.mamut.linking.SpotLinkerOp;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotLinkerOp.class, priority = Priority.LOW, name = "Trackastra Linker", description = "<html>"
		+ "This linker uses Trackastra for linking. Trackastra has been published in:<br>"
		+ "<i>TRACKASTRA: Transformer-Based Cell Tracking for Live-Cell Microscopy </i> - "
		+ "<i>Gallusser, B. & Weigert, M.</i>, Computer Vision – ECCV 2024.<br><br>"
		+ "Trackastra uses a transformer based architecture to directly learn pairwise associations of cells within a temporal window from annotated data.<br>"
		+ "Trackastra can account for dividing objects such as cells and allows for accurate tracking even with simple greedy linking.<br>"
		+ "The architecture operates solely on the full spatio-temporal context of detections.<br><br>"
		+ "<strong>When this linker method is used for the first time, internet connection is needed, since an internal installation process is started. The installation consumes ~2.5GB hard disk space.</strong><br>"
		+ "</html>" )
public class TrackastraLinkerMamut extends AbstractSpotLinkerOp
{
	@Override
	public void mutate1( final ModelGraph graph, final SpatioTemporalIndex< Spot > spots )
	{
		exec( graph, spots, TrackastraLinker.class );
	}

	@Override
	public Map< String, Object > getDefaultSettings()
	{
		return TrackastraUtils.getDefaultTrackAstraSettingsMap();
	}
}
