package org.mastodon.mamut.clustering.ui;

import org.mastodon.mamut.clustering.ClusterRootNodesController;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchGraphSynchronizer;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.ui.UIService;

public class ClusterRootNodesViewDemo
{

    public static void main( String[] args )
    {
        @SuppressWarnings("all")
        Context context = new Context();
        UIService ui = context.service( UIService.class );
        CommandService cmd = context.service( CommandService.class );

        final Model model = new Model();

        final BranchGraphSynchronizer synchronizer = new BranchGraphSynchronizer( null, null );

        ui.showUI();
        cmd.run( ClusterRootNodesView.class, true, "controller", new ClusterRootNodesController( model, synchronizer ) );
    }
}
