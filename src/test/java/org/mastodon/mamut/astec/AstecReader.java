package org.mastodon.mamut.astec;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImgToVirtualStack;
import net.imglib2.type.numeric.real.FloatType;
import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.clustering.util.ClusterUtils;
import org.mastodon.mamut.io.ProjectSaver;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.TagSetUtils;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Reads data that was converted from ASTEC file format to CSV files and saves it as a Mastodon project.
 * The data is expected to be in the following format in the directory where the program is run:
 * <ul>
 *     <li>spots.csv: id; label; timePoint; x; y; z; volume</li>
 *     <li>links.csv: source spot id; target spot id</li>
 *     <li>fates1.csv: tagValue</li>
 *     <li>fate_values1.csv: spot id; tagValue</li>
 *     <li>fates2.csv: tagValue</li>
 *     <li>fate_values2.csv: spot id; tagValue</li>
 *     <li>fates3.csv: tagValue</li>
 *     <li>fate_values3.csv: spot id; tagValue</li>
 * </ul>
 * The project is saved as pm1.mastodon in the directory where the program is run.
 *
 * @see <a href="https://figshare.com/projects/Phallusiamammillata_embryonic_development/64301">Phallusia mammillata embryonic development data</a>
 * @see <a href="https://github.com/leoguignard/LineageTree/tree/master/src/LineageTree/export_csv.py">Conversion from ASTEC to csv</a>
 *
 */
public class AstecReader
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Map< Integer, Spot > spotMap;

	private final Model model;

	private final static String MASTODON_FILE = "Pm10.mastodon";

	private AstecReader()
	{
		this.spotMap = new HashMap<>();
		this.model = new Model();
	}

	public static void main( String[] args ) throws IOException, CsvValidationException
	{
		AstecReader reader = new AstecReader();
		reader.readSpots();
		reader.readLinks();
		reader.readTags( "fates1", "fates1.csv", "fate_values1.csv" );
		reader.readTags( "fates2", "fates2.csv", "fate_values2.csv" );
		reader.readTags( "fates3", "fates3.csv", "fate_values3.csv" );
		reader.saveProject();
	}

	private void saveProject() throws IOException
	{
		Context context = new Context();
		final Img< FloatType > dummyImg = ArrayImgs.floats( 10, 10, 10 );
		final ImagePlus dummyImagePlus =
				ImgToVirtualStack.wrap( new ImgPlus<>( dummyImg, "image", new AxisType[] { Axes.X, Axes.Y, Axes.Z } ) );
		SharedBigDataViewerData dummyBdv = Objects.requireNonNull( SharedBigDataViewerData.fromImagePlus( dummyImagePlus ) );
		File projectRoot = new File( MASTODON_FILE ).getAbsoluteFile();
		File xmlFile = new File( "dummy.xml" ).getAbsoluteFile();
		logger.info( "Saving project to file: {} ", projectRoot );
		MamutProject project = new MamutProject( projectRoot, xmlFile );
		final ProjectModel appModel = ProjectModel.create( context, model, dummyBdv, project );
		ProjectSaver.saveProject( projectRoot, appModel );
		logger.info( "Project successfully saved." );
	}

	/**
	 * id; label; timePoint; x; y; z; volume
	 */
	private void readSpots() throws IOException, CsvValidationException
	{
		File file = new File( "spots.csv" );
		logger.info( "Reading spots from {}", file.getAbsolutePath() );
		FileReader fileReader = new FileReader( file );
		try (CSVReader reader = new CSVReader( fileReader ))
		{
			String[] line;
			reader.skip( 1 );
			while ( ( line = reader.readNext() ) != null )
			{
				int id = Integer.parseInt( line[ 0 ] );
				String label = line[ 1 ];
				int timePoint = Integer.parseInt( line[ 2 ] );
				double[] position =
						new double[] { Double.parseDouble( line[ 3 ] ), Double.parseDouble( line[ 4 ] ), Double.parseDouble( line[ 5 ] ) };
				double volume = Double.parseDouble( line[ 6 ] );
				addSpot( id, timePoint, position, volume, label );
			}
		}
	}

	/**
	 * sourceId; targetId
	 */
	private void readLinks() throws IOException, CsvValidationException
	{
		File file = new File( "links.csv" );
		logger.info( "Reading links from {}", file.getAbsolutePath() );
		FileReader fileReader = new FileReader( file );
		try (CSVReader reader = new CSVReader( fileReader ))
		{
			reader.skip( 1 );
			String[] line;
			while ( ( line = reader.readNext() ) != null )
			{
				int sourceId = Integer.parseInt( line[ 0 ] );
				int targetId = Integer.parseInt( line[ 1 ] );
				addEdge( sourceId, targetId );
			}
		}
	}

	private void readTags( final String tagSetName, final String tagSetFileName, final String tagValuesFileName )
	{
		try
		{
			TagSetStructure.TagSet tagSet = readTagSet( tagSetFileName, tagSetName );
			readTagValues( tagSet, tagValuesFileName );
		}
		catch ( FileNotFoundException e )
		{
			logger.info( "No complete tag set files found for tag set name: {}. This tag set will be ignored. ", tagSetName );
		}
		catch ( IOException | CsvValidationException e )
		{
			logger.info( "CSV files corrupt for tag set name: {}. This tag set will be ignored. ", tagSetName );
		}

	}

	/**
	 * tagValue
	 */
	private TagSetStructure.TagSet readTagSet( final String filename, final String tagSetName ) throws IOException, CsvValidationException
	{
		File file = new File( filename );
		logger.info( "Reading tag set from {}", file.getAbsolutePath() );
		FileReader fileReader = new FileReader( file );
		try (CSVReader reader = new CSVReader( fileReader ))
		{
			reader.skip( 1 );
			String[] line;
			List< String > tags = new ArrayList<>();
			while ( ( line = reader.readNext() ) != null )
				tags.add( line[ 0 ] );
			List< Pair< String, Integer > > tagsAndColors = new ArrayList<>();
			List< Integer > colors = ClusterUtils.getGlasbeyColors( tags.size() );
			tags.forEach( tag -> tagsAndColors.add( Pair.of( tag, colors.get( tags.indexOf( tag ) ) ) ) );
			return TagSetUtils.addNewTagSetToModel( this.model, tagSetName, tagsAndColors );
		}
	}

	/**
	 * spotId; tagValue
	 */
	private void readTagValues( final TagSetStructure.TagSet tagSet, final String filename ) throws IOException, CsvValidationException
	{
		File file = new File( filename );
		logger.info( "Reading tag set values from {}", file.getAbsolutePath() );
		FileReader fileReader = new FileReader( file );
		try (CSVReader reader = new CSVReader( fileReader ))
		{
			reader.skip( 1 );
			String[] line;
			while ( ( line = reader.readNext() ) != null )
			{
				int spotId = Integer.parseInt( line[ 0 ] );
				String tagValue = line[ 1 ];
				addTag( tagSet, spotId, tagValue );
			}
		}
	}

	private void addTag( final TagSetStructure.TagSet tagSet, final int spotId, final String tagValue )
	{
		Spot spot = spotMap.get( spotId );
		TagSetStructure.Tag tag = tagSet.getTags().stream().filter( t -> t.label().equals( tagValue ) ).findFirst().orElse( null );
		TagSetUtils.tagSpotAndIncomingEdges( model, spot, tagSet, tag );
	}

	private void addEdge( final int sourceId, final int targetId )
	{
		Spot source = spotMap.get( sourceId );
		Spot target = spotMap.get( targetId );
		model.getGraph().addEdge( source, target );
	}

	private void addSpot( final int id, final int timePoint, final double[] position, final double volume, final String label )
	{
		double radius = getRadius( volume );
		Spot spot = model.getGraph().addVertex().init( timePoint, position, radius );
		spot.setLabel( label );
		spotMap.put( id, spot );
	}

	/**
	 * Get the radius of a sphere given its volume.
	 * @param volume the volume of the sphere
	 * @return the radius of the sphere
	 */
	private static double getRadius( final double volume )
	{
		return Math.pow( ( 3 * volume ) / ( 4 * Math.PI ), 1.0 / 3.0 );
	}
}
