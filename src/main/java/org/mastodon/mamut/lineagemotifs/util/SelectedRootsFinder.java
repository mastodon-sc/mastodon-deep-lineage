package org.mastodon.mamut.lineagemotifs.util;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.AbstractGraphAlgorithm;
import org.mastodon.model.SelectionModel;

/**
 * Small algorithm that returns a set of vertices that are selected and that have either no incoming edges or only incoming edges from non-selected vertices
 *
 * @param <V>
 *            the type of vertices in the graph.
 * @param <E>
 *            the type of edges in the graph.
 * @author Stefan Hahmann
 */
public class SelectedRootsFinder< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphAlgorithm< V, E >
{
	private final RefSet< V > roots;

	private final SelectionModel< V, E > selectionModel;

	private SelectedRootsFinder( final ReadOnlyGraph< V, E > graph, final SelectionModel< V, E > selectionModel )
	{
		super( graph );
		this.selectionModel = selectionModel;
		this.roots = createVertexSet();
		fetchRoots();
	}

	private RefSet< V > get()
	{
		return roots;
	}

	private void fetchRoots()
	{
		for ( final V vertex : selectionModel.getSelectedVertices() )
		{
			if ( vertex.incomingEdges().isEmpty() )
				roots.add( vertex );
			if ( !hasSelectedPredecessors( vertex, selectionModel ) )
				roots.add( vertex );
		}
	}

	/**
	 * Returns a set of vertices that are selected and have either no incoming edges
	 * or only incoming edges from non-selected vertices in a given graph.
	 *
	 * @param <V> the type of vertices in the graph.
	 * @param graph the graph being analyzed.
	 * @param selectionModel the selection model to determine selected vertices.
	 * @return a set of selected root vertices.
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public static < V extends Vertex< ? > > RefSet< V > getRoots( final ReadOnlyGraph< V, ? > graph,
			final SelectionModel< V, ? > selectionModel )
	{
		return new SelectedRootsFinder( graph, selectionModel ).get();
	}

	private boolean hasSelectedPredecessors( final V vertex, final SelectionModel< V, E > selectionModel )
	{
		for ( final E edge : vertex.incomingEdges() )
		{
			if ( selectionModel.isSelected( edge.getSource() ) )
				return true;
		}
		return false;
	}
}
