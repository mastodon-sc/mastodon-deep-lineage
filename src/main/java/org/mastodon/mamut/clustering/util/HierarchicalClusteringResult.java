/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
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
package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.Cluster;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.clustering.config.HasName;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.util.MathUtils;
import org.mastodon.model.tag.TagSetStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A class that encapsulates the result of a hierarchical clustering algorithm.<br>
 * It contains:
 *     <ul>
 *         <li>the root {@link Cluster} object, from which the results of the algorithm can be accessed. Clusters are hierarchically organized.</li>
 *         <li>a {@link List} of {@link Group} objects, where each objects contains:
 *         	  <ul>
 *               <li>a {@link Cluster} object, which represents the objects in the dendrogram</li>
 *               <li>a {@link Set} of objects, which are assigned into the same group</li>
 *               <li>a color, which is associated with that group</li>
 *           </ul>
 *         </li>
 *         <li>the cutoff value of the hierarchical clustering, i.e. where the dendrogram is cut</li>
 *         <li>the median of the upper triangle values of the distance matrix that this hierarchical clustering represents</li>
 *     </ul>
 * @author Stefan Hahmann
 * @param <T> the type of the objects that are clustered
 */
public class HierarchicalClusteringResult< T >
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final List< Group< T > > groups;

	private final Cluster rootCluster;

	private final double cutoff;

	private final double median;

	private final int objectCount;

	private final Map< Cluster, T > clusterNodesToObjects;

	/**
	 * Creates a new {@link HierarchicalClusteringResult} object.
	 * @param groups a {@link List} of {@link Pair} objects, where each pair contains:
	 * 						<ul>
	 * 						    <li>a {@link Set} of objects, which are assigned into the same group by the hierarchical clustering</li>
	 * 						    <li>a {@link Cluster} object, which represents the objects in the dendrogram</li>
	 * 						</ul>
	 * @param rootCluster the root {@link Cluster} object, from which the results of the algorithm can be accessed
	 * @param cutoff the cutoff value of hierarchical clustering, i.e. where the dendrogram is cut
	 * @param median the median of the upper triangle values of the distance matrix that this hierarchical clustering result represents
	 */
	public HierarchicalClusteringResult( final List< Pair< Set< T >, Cluster > > groups, final Cluster rootCluster, final double cutoff,
			final double median )
	{
		this( groups, rootCluster, cutoff, median, null );
	}

	/**
	 * Creates a new {@link HierarchicalClusteringResult} object.
	 * @param groups a {@link List} of {@link Pair} objects, where each pair contains:
	 * 						<ul>
	 * 						    <li>a {@link Set} of objects, which are assigned into the same group by the hierarchical clustering</li>
	 * 						    <li>a {@link Cluster} object, which represents the objects in the dendrogram</li>
	 * 						</ul>
	 * @param rootCluster the root {@link Cluster} object, from which the results of the algorithm can be accessed
	 * @param cutoff the cutoff value of hierarchical clustering, i.e. where the dendrogram is cut
	 * @param median the median of the upper triangle values of the distance matrix that this hierarchical clustering result represents
	 * @param clusterNodesToObjects a mapping between the cluster objects represented by the {@code rootCluster} and its descendants and the clustered objects they represent
	 */
	public HierarchicalClusteringResult( final List< Pair< Set< T >, Cluster > > groups, final Cluster rootCluster, final double cutoff,
			final double median, final Map< Cluster, T > clusterNodesToObjects )

	{
		this.groups = new ArrayList<>();
		List< Integer > glasbeyColors = HierarchicalClusteringUtils.getGlasbeyColors( groups.size() );
		int count = 0;
		for ( int i = 0; i < groups.size(); i++ )
		{
			Pair< Set< T >, Cluster > clusterGroupPair = groups.get( i );
			Cluster cluster = clusterGroupPair.getRight();
			Set< T > groupedObjects = clusterGroupPair.getLeft();
			this.groups.add(
					new Group<>( glasbeyColors.get( i ), cluster, groupedObjects, "Group " + ( i + 1 ) ) );
			if ( groupedObjects != null )
				count += groupedObjects.size();
		}
		this.rootCluster = rootCluster;
		this.cutoff = cutoff;
		this.median = median;
		this.objectCount = count;
		this.clusterNodesToObjects = clusterNodesToObjects;
		updateClusterNames();
	}

	/**
	 * Returns a {@link List} of {@link Group} objects, where each objects contain:
	 *    <ul>
	 *        <li>a {@link Cluster} object, which represents the clustered objects in the dendrogram</li>
	 *        <li>a {@link Set} of objects, which are assigned into the same group</li>
	 *        <li>a color, which is associated with that group</li>
	 *    </ul>
	 * @return a {@link Set} of {@link Group} objects
	 */
	public List< Group< T > > getGroups()
	{
		return groups;
	}

	/**
	 * Returns the root {@link Cluster} object, from which the results of the algorithm can be accessed.
	 * @return the root {@link Cluster} object
	 */
	public Cluster getRootCluster()
	{
		return rootCluster;
	}

	/**
	 * Returns the cutoff value of the hierarchical clustering, i.e. where the value, where the dendrogram is cut.
	 *
	 * @return the cutoff value of the hierarchical clustering
	 */
	public double getCutoff()
	{
		return cutoff;
	}

	/**
	 * Returns the median of the upper triangle values of the distance matrix that this hierarchical clustering result represents.
	 *
	 * @return the median of the upper triangle values of the distance matrix
	 */
	public double getMedian()
	{
		return median;
	}

	/**
	 * Returns the number of objects that have been clustered.
	 * @return the number of objects
	 */
	public int getObjectCount()
	{
		return objectCount;
	}

	Set< Set< T > > getClusteredObjects()
	{
		return groups.stream().map( Group::getObjects ).collect( Collectors.toSet() );
	}

	/**
	 * Gets a mapping between the nodes of the dendrogram that represent this clustering and the clustered objects themselves
	 * @return the mapping
	 */
	public Map< Cluster, T > getClusterNodesToObjects()
	{
		return clusterNodesToObjects;
	}

	/**
	 * Updates the names of the leaf clusters within this hierarchical clustering result to the string representation of the clustered objects.
	 */
	public void updateClusterNames()
	{
		if ( clusterNodesToObjects == null )
			return;
		clusterNodesToObjects.forEach( ( cluster, object ) -> {
			if ( cluster.isLeaf() )
				cluster.setName( object.toString() );
		} );
	}

	/**
	 * Exports the hierarchical clustering result into a CSV file.
	 * <br>
	 * This method creates a CSV file containing the groups that have been created via hierarchical clustering data, including cell names, tag labels, group names, and group similarity scores.
	 * The CSV file is created with a header and ";" as delimiter.
	 *
	 * @param file The file to which the CSV data will be written.
	 * @param tagSet The tag set used for the clustering, which may be null.
	 */
	public void exportCsv( final File file, final TagSetStructure.TagSet tagSet )
	{
		char separator = ';';
		String[] header = new String[] { "Cell name", tagSet == null ? "" : tagSet.getName(), "Group", "Group similarity score" };
		CSVFormat csvFormat = CSVFormat.Builder.create().setHeader( header ).setDelimiter( separator ).setQuote( '"' ).setEscape( '"' )
				.setRecordSeparator( "\n" ).build();
		try (
				FileWriter writer = new FileWriter( file );
				CSVPrinter csvPrinter = new CSVPrinter( writer, csvFormat ))
		{
			for ( Group< T > group : getGroups() )
			{
				for ( T object : group.getObjects() )
				{
					String name = object instanceof HasName ? ( ( HasName ) object ).getName() : object.toString();
					String tagLabel = object instanceof BranchSpotTree ? ( ( BranchSpotTree ) object ).getTagLabel() : "";
					String groupName = group.getName();
					String similarity =
							MathUtils.roundToSignificantDigits( group.getCluster().getDistance().getDistance(), 2 );
					Object[] line = new String[] { name, tagLabel, groupName, similarity };
					csvPrinter.printRecord( line );
					logger.debug( "Cell name: {}, Tag label: {}, Group name: {}, Similarity of group: {}", name, tagLabel, groupName,
							similarity );
				}
			}
			csvPrinter.flush();
		}
		catch ( IOException e )
		{
			logger.error( "Could not save result of hierarchical clustering to File: {}. Message: {}", file.getAbsolutePath(),
					e.getMessage() );
		}
	}

	/**
	 * A class that encapsulates the result of a clustering algorithm for a single cluster.<br>
	 * It contains:
	 *    <ul>
	 *        <li>a {@link Cluster} object, which represents the cluster objects in the dendrogram</li>
	 *        <li>a {@link Set} of objects, which are assigned into the same group</li>
	 *        <li>a color, which is associated with this group</li>
	 *    </ul>
	 */
	public static class Group< T >
	{
		private final int color;

		private final Cluster cluster;

		private final Set< T > objects;

		private final String name;

		private Group( final int color, final Cluster cluster, final Set< T > objects, final String name )
		{
			this.color = color;
			this.cluster = cluster;
			this.objects = objects;
			this.name = name;
		}

		/**
		 * Returns the color associated with this group.
		 * @return the color
		 */
		public int getColor()
		{
			return color;
		}

		/**
		 * Returns the {@link Cluster} object, which represents the clustered objects in the dendrogram.
		 * @return the {@link Cluster} object
		 */
		public Cluster getCluster()
		{
			return cluster;
		}

		/**
		 * Returns the {@link Set} of objects, which are assigned into the same group.
		 * @return the {@link Set} of objects
		 */
		public Set< T > getObjects()
		{
			return objects;
		}

		/**
		 * Returns the name of the group
		 * @return the name
		 */
		public String getName()
		{
			return name;
		}
	}
}
