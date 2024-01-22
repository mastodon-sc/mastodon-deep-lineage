package org.mastodon.mamut.feature;

import org.mastodon.graph.Vertex;

public interface ValueIsSetEvaluator< V extends Vertex< ? > >
{
	boolean valueIsSet( final V vertex );
}
