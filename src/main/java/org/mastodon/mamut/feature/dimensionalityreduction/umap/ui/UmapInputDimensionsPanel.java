package org.mastodon.mamut.feature.dimensionalityreduction.umap.ui;

import net.miginfocom.swing.MigLayout;
import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.util.UmapInputDimension;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.util.List;
import java.util.function.Supplier;

class UmapInputDimensionsPanel< V extends Vertex< E >, E extends Edge< V > > extends JPanel
		implements Supplier< List< UmapInputDimension< V, E > > >
{
	private final JList< UmapInputDimension< V, E > > featureList;

	private final DefaultListModel< UmapInputDimension< V, E > > listModel;

	private final Class< V > vertexType;

	private final Class< E > edgeType;

	private final FeatureModel featureModel;

	UmapInputDimensionsPanel( final Class< V > vertexType, final Class< E > edgeType, final FeatureModel featureModel )
	{
		super( new MigLayout( "insets 0 0 0 0, fill", "", "" ) );
		this.vertexType = vertexType;
		this.edgeType = edgeType;
		this.featureModel = featureModel;
		listModel = new DefaultListModel<>();
		featureList = new JList<>( listModel );
		updateItemList();
		selectAllDimensions();
		initBehavior();
		initLayout();
	}

	private void initLayout()
	{
		JScrollPane listScrollPane = new JScrollPane( featureList );
		add( new JLabel( "Features:" ) );
		add( listScrollPane, "span, growx, pushx, growy, pushy, wrap" );
	}

	private void initBehavior()
	{
		featureList.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
		featureList.getSelectionModel().clearSelection();
		featureList.getSelectionModel().setSelectionInterval( 0, listModel.getSize() - 1 );
		final FeatureModel.FeatureModelListener featureModelListener = this::updateItemList;
		featureModel.listeners().add( featureModelListener );
	}

	private void selectAllDimensions()
	{
		ListSelectionModel selectionModel = featureList.getSelectionModel();
		selectionModel.clearSelection();
		selectionModel.setSelectionInterval( 0, listModel.getSize() - 1 );
	}

	private void updateItemList()
	{
		listModel.clear();
		List< UmapInputDimension< V, E > > items = UmapInputDimension.getListFromFeatureModel( featureModel, vertexType, edgeType );
		for ( UmapInputDimension< V, E > item : items )
			listModel.addElement( item );
	}

	int getNumberOfFeatures()
	{
		featureList.getSelectedValuesList();
		return listModel.getSize();
	}

	@Override
	public List< UmapInputDimension< V, E > > get()
	{
		return featureList.getSelectedValuesList();
	}
}
