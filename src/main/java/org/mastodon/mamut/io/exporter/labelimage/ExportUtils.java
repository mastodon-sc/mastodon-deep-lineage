package org.mastodon.mamut.io.exporter.labelimage;

import io.scif.codec.CompressionType;
import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgSaver;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.img.Img;
import org.scijava.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;

/**
 * Utility class for exporting images.
 */
public class ExportUtils
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private ExportUtils()
	{
		// prevent instantiation
	}

	/**
	 * Create an {@link ImgPlus} from an {@link Img}. Sets the voxel dimensions using {@link VoxelDimensions}, if not null.
	 * <br>
	 * The expected order of dimensions is XYZT.
	 * @param img the {@link Img} to create the {@link ImgPlus} from
	 * @param voxelDimensions the {@link VoxelDimensions} to use for the {@link ImgPlus}
	 * @return the created {@link ImgPlus}
	 * @param <T> the pixel type of the {@link Img}
	 */
	public static < T > ImgPlus< T > createImgPlusFromImg( final Img< T > img, final VoxelDimensions voxelDimensions )
	{
		if ( voxelDimensions == null )
			return new ImgPlus<>( img );
		final CalibratedAxis[] axes = { new DefaultLinearAxis( Axes.X, voxelDimensions.dimension( 0 ) ),
				new DefaultLinearAxis( Axes.Y, voxelDimensions.dimension( 1 ) ),
				new DefaultLinearAxis( Axes.Z, voxelDimensions.dimension( 2 ) ), new DefaultLinearAxis( Axes.TIME ) };
		logger.debug( "voxelDimensions size: {}", voxelDimensions.numDimensions() );
		for ( int i = 0; i < voxelDimensions.numDimensions(); i++ )
			logger.debug( "voxelDimensions: {}:{}", i, voxelDimensions.dimension( i ) );
		return new ImgPlus<>( img, "Result", axes );
	}

	/**
	 * Save an {@link ImgPlus} to a given file.
	 * <br>
	 * Use {@link CompressionType#LZW} for compression and allows to overwrite the file, if it exists.
	 * @param file the file to save the {@link ImgPlus} to. The file extension determines the file format. Tif is recommended.
	 * @param imgplus the {@link ImgPlus} to save
	 * @param context the {@link Context} to use for saving. Must not be null.
	 * @param <T> the pixel type of the {@link ImgPlus}
	 */
	public static < T > void saveImgPlusToFile( final File file, final ImgPlus< T > imgplus, final Context context )
	{
		SCIFIOConfig config = new SCIFIOConfig();
		config.writerSetCompression( CompressionType.LZW );
		config.writerSetFailIfOverwriting( false );
		ImgSaver saver = new ImgSaver( context );
		saver.saveImg( file.getAbsolutePath(), imgplus, config );
	}
}
