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
package org.mastodon.mamut.feature.dimensionalityreduction.ui;

import net.miginfocom.swing.MigLayout;
import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.feature.dimensionalityreduction.util.InputDimension;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.util.List;
import java.util.function.Supplier;

public class InputDimensionsPanel< V extends Vertex< E >, E extends Edge< V > > extends JPanel
		implements Supplier< List< InputDimension< V > > >
{
	private final JList< InputDimension< V > > featureList;

	private final DefaultListModel< InputDimension< V > > listModel;

	private final Class< V > vertexType;

	private final Class< E > edgeType;

	private final FeatureModel featureModel;

	public InputDimensionsPanel( final Class< V > vertexType, final Class< E > edgeType, final FeatureModel featureModel )
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
		List< InputDimension< V > > items =
				InputDimension.getListFromFeatureModel( featureModel, vertexType, edgeType );
		for ( InputDimension< V > item : items )
			listModel.addElement( item );
	}

	public int getNumberOfFeatures()
	{
		featureList.getSelectedValuesList();
		return listModel.getSize();
	}

	@Override
	public List< InputDimension< V > > get()
	{
		return featureList.getSelectedValuesList();
	}
}
