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
package org.mastodon.mamut.classification.util;

import com.apporiented.algorithm.clustering.Cluster;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.classification.config.HasName;
import org.mastodon.mamut.classification.treesimilarity.tree.BranchSpotTree;
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
 *         <li>a {@link List} of {@link ObjectClassification} objects, where each objects contains:
 *         	  <ul>
 *               <li>a {@link Cluster} object, which represents the classified objects in the dendrogram</li>
 *               <li>a {@link Set} of objects, which are classified into the same class</li>
 *               <li>a color, which is associated with that class</li>
 *           </ul>
 *         </li>
 *         <li>the cutoff value of classification, i.e. where the dendrogram is cut</li>
 *         <li>the median of the upper triangle values of the distance matrix that this classification represents</li>
 *     </ul>
 * @author Stefan Hahmann
 * @param <T> the type of the objects that are classified
 */
public class Classification< T >
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final List< ObjectClassification< T > > objectClassifications;

	private final Cluster rootCluster;

	private final double cutoff;

	private final double median;

	private final int objectCount;

	private final Map< Cluster, T > clusterNodesToObjects;

	/**
	 * Creates a new {@link Classification} object.
	 * @param classifiedObjects a {@link List} of {@link Pair} objects, where each pair contains:
	 * 						<ul>
	 * 						    <li>a {@link Set} of objects, which are classified into the same class</li>
	 * 						    <li>a {@link Cluster} object, which represents the classified objects in the dendrogram</li>
	 * 						</ul>
	 * @param rootCluster the root {@link Cluster} object, from which the results of the algorithm can be accessed
	 * @param cutoff the cutoff value of classification, i.e. where the dendrogram is cut
	 * @param median the median of the upper triangle values of the distance matrix that this classification represents
	 */
	public Classification( final List< Pair< Set< T >, Cluster > > classifiedObjects, final Cluster rootCluster, final double cutoff,
			final double median )
	{
		this( classifiedObjects, rootCluster, cutoff, median, null );
	}

	/**
	 * Creates a new {@link Classification} object.
	 * @param classifiedObjects a {@link List} of {@link Pair} objects, where each pair contains:
	 * 						<ul>
	 * 						    <li>a {@link Set} of objects, which are classified into the same class</li>
	 * 						    <li>a {@link Cluster} object, which represents the classified objects in the dendrogram</li>
	 * 						</ul>
	 * @param rootCluster the root {@link Cluster} object, from which the results of the algorithm can be accessed
	 * @param cutoff the cutoff value of classification, i.e. where the dendrogram is cut
	 * @param median the median of the upper triangle values of the distance matrix that this classification represents
	 * @param clusterNodesToObjects a mapping between the cluster objects represented by the {@code rootCluster} and its descendants and the classified objects they represent
	 */
	public Classification( final List< Pair< Set< T >, Cluster > > classifiedObjects, final Cluster rootCluster, final double cutoff,
			final double median, final Map< Cluster, T > clusterNodesToObjects )

	{
		this.objectClassifications = new ArrayList<>();
		List< Integer > glasbeyColors = ClassificationUtils.getGlasbeyColors( classifiedObjects.size() );
		int count = 0;
		for ( int i = 0; i < classifiedObjects.size(); i++ )
		{
			Pair< Set< T >, Cluster > clusterClassPair = classifiedObjects.get( i );
			this.objectClassifications.add(
					new ObjectClassification<>( glasbeyColors.get( i ), clusterClassPair.getRight(), clusterClassPair.getLeft(),
							"Class " + ( i + 1 ) ) );
			Set< T > classObject = clusterClassPair.getLeft();
			if ( classObject != null )
				count += clusterClassPair.getLeft().size();
		}
		this.rootCluster = rootCluster;
		this.cutoff = cutoff;
		this.median = median;
		this.objectCount = count;
		this.clusterNodesToObjects = clusterNodesToObjects;
		updateClusterNames();
	}

	/**
	 * Returns a {@link List} of {@link ObjectClassification} objects, where each objects contain:
	 *    <ul>
	 *        <li>a {@link Cluster} object, which represents the classified objects in the dendrogram</li>
	 *        <li>a {@link Set} of objects, which are classified into the same class</li>
	 *        <li>a color, which is associated with that class</li>
	 *    </ul>
	 * @return a {@link Set} of {@link ObjectClassification} objects
	 */
	public List< ObjectClassification< T > > getObjectClassifications()
	{
		return objectClassifications;
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
	 * Returns the cutoff value of classification, i.e. where the value, where the dendrogram is cut.
	 *
	 * @return the cutoff value of classification
	 */
	public double getCutoff()
	{
		return cutoff;
	}

	/**
	 * Returns the median of the upper triangle values of the distance matrix that this classification represents.
	 *
	 * @return the median of the upper triangle values of the distance matrix
	 */
	public double getMedian()
	{
		return median;
	}

	/**
	 * Returns the number of objects that are classified.
	 * @return the number of objects
	 */
	public int getObjectCount()
	{
		return objectCount;
	}

	Set< Set< T > > getClassifiedObjects()
	{
		return objectClassifications.stream().map( ObjectClassification::getObjects ).collect( Collectors.toSet() );
	}

	/**
	 * Gets a mapping between the nodes of the dendrogram that represents this classification the represented classified objects
	 * @return the mapping
	 */
	public Map< Cluster, T > getClusterNodesToObjects()
	{
		return clusterNodesToObjects;
	}

	/**
	 * Updates the names of the leaf clusters within this classification to the string representation of the classified objects.
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
	 * Exports the classification data to a CSV file.
	 * <br>
	 * This method creates a CSV file with the classification data, including cell names, tag labels, class names, and class similarity scores.
	 * The CSV file is created with a header and ";" as delimiter.
	 *
	 * @param file The file to which the CSV data will be written.
	 * @param tagSet The tag set used for classification, which may be null.
	 */
	public void exportCsv( final File file, final TagSetStructure.TagSet tagSet )
	{
		char separator = ';';
		String[] header = new String[] { "Cell name", tagSet == null ? "" : tagSet.getName(), "Class", "Class similarity score" };
		CSVFormat csvFormat = CSVFormat.Builder.create().setHeader( header ).setDelimiter( separator ).setQuote( '"' ).setEscape( '"' )
				.setRecordSeparator( "\n" ).build();
		try (
				FileWriter writer = new FileWriter( file );
				CSVPrinter csvPrinter = new CSVPrinter( writer, csvFormat ))
		{
			for ( Classification.ObjectClassification< T > objectClassification : getObjectClassifications() )
			{
				for ( T object : objectClassification.getObjects() )
				{
					String name = object instanceof HasName ? ( ( HasName ) object ).getName() : object.toString();
					String tagLabel = object instanceof BranchSpotTree ? ( ( BranchSpotTree ) object ).getTagLabel() : "";
					String className = objectClassification.getName();
					String similarity =
							MathUtils.roundToSignificantDigits( objectClassification.getCluster().getDistance().getDistance(), 2 );
					Object[] line = new String[] { name, tagLabel, className, similarity };
					csvPrinter.printRecord( line );
					logger.debug( "Cell name: {}, Tag label: {}, Class name: {}, Similarity of class: {}", name, tagLabel, className,
							similarity );
				}
			}
			csvPrinter.flush();
		}
		catch ( IOException e )
		{
			logger.error( "Could not save classification to File: {}. Message: {}", file.getAbsolutePath(), e.getMessage() );
		}
	}

	/**
	 * A class that encapsulates the result of a clustering algorithm for a single class.<br>
	 * It contains:
	 *    <ul>
	 *        <li>a {@link Cluster} object, which represents the classified objects in the dendrogram</li>
	 *        <li>a {@link Set} of objects, which are classified into the same class</li>
	 *        <li>a color, which is associated with that class</li>
	 *    </ul>
	 */
	public static class ObjectClassification< T >
	{
		private final int color;

		private final Cluster cluster;

		private final Set< T > objects;

		private final String name;

		private ObjectClassification( final int color, final Cluster cluster, final Set< T > objects, final String name )
		{
			this.color = color;
			this.cluster = cluster;
			this.objects = objects;
			this.name = name;
		}

		/**
		 * Returns the color associated with this classification.
		 * @return the color
		 */
		public int getColor()
		{
			return color;
		}

		/**
		 * Returns the {@link Cluster} object, which represents the classified objects in the dendrogram.
		 * @return the {@link Cluster} object
		 */
		public Cluster getCluster()
		{
			return cluster;
		}

		/**
		 * Returns the {@link Set} of objects, which are classified into the same class.
		 * @return the {@link Set} of objects
		 */
		public Set< T > getObjects()
		{
			return objects;
		}

		/**
		 * Returns the name of the class
		 * @return the name
		 */
		public String getName()
		{
			return name;
		}
	}
}
