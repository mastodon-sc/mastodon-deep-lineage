package org.mastodon.mamut.io.exporter.labelimage;

import mpicbg.spim.data.sequence.DefaultVoxelDimensions;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imagej.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.jupiter.api.Test;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportUtilsTest
{
	@Test
	void createImgPlusFromImg()
	{
		Img< IntType > img = ArrayImgs.ints( 10, 10, 10, 1 );
		VoxelDimensions voxelDimensions = new DefaultVoxelDimensions( 3 );
		ImgPlus< IntType > imgPlus = ExportUtils.createImgPlusFromImg( img, voxelDimensions );
		ImgPlus< IntType > imgPlusNullDimensions = ExportUtils.createImgPlusFromImg( img, null );

		assertEquals( 4, imgPlus.numDimensions() );
		assertEquals( 10, imgPlus.dimension( 0 ) );
		assertEquals( 10, imgPlus.dimension( 1 ) );
		assertEquals( 10, imgPlus.dimension( 2 ) );
		assertEquals( 1, imgPlus.dimension( 3 ) );
		assertEquals( "Result", imgPlus.getName() );

		assertEquals( 4, imgPlusNullDimensions.numDimensions() );
		assertEquals( 10, imgPlusNullDimensions.dimension( 0 ) );
		assertEquals( 10, imgPlusNullDimensions.dimension( 1 ) );
		assertEquals( 10, imgPlusNullDimensions.dimension( 2 ) );
		assertEquals( 1, imgPlusNullDimensions.dimension( 3 ) );
		assertNull( imgPlusNullDimensions.getName() );
	}

	@Test
	void saveImgPlusToFile() throws IOException
	{
		Img< IntType > img = ArrayImgs.ints( 10, 10, 10, 1 );
		VoxelDimensions voxelDimensions = new DefaultVoxelDimensions( 3 );
		ImgPlus< IntType > imgPlus = ExportUtils.createImgPlusFromImg( img, voxelDimensions );
		File tempFile = Files.createTempFile( "save-img-plus-test", ".tif" ).toFile();

		ExportUtils.saveImgPlusToFile( tempFile, imgPlus, new Context() );
		assertTrue( tempFile.exists() );
		assertTrue( tempFile.length() > 0 );
	}
}
