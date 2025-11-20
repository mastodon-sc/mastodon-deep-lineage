package org.mastodon.mamut.util.appose;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apposed.appose.BuildException;
import org.junit.jupiter.api.Test;

class ApposeUtilsTest
{
	@Test
	void testInstallDeleteExistsSize() throws BuildException
	{
		String testEnvName = "testenv";
		String testEnvContent = "name: " + testEnvName + "\n"
				+ "channels:\n"
				+ "  - conda-forge\n"
				+ "dependencies:\n"
				+ "  - python=3.10\n";

		ApposeUtils.installEnvironment( testEnvContent );
		assertTrue( ApposeUtils.checkEnvironmentInstalled( testEnvName ) );
		String size = ApposeUtils.calculateEnvironmentSize( testEnvName );
		String numberPart = size.split( " " )[ 0 ]; // "123,4"
		String integerPart = numberPart.split( "[.,]" )[ 0 ]; // "123"
		int sizeInt = Integer.parseInt( integerPart );
		assertTrue( sizeInt >= 100 && sizeInt <= 499, "Environment size should be between 100 MB and 499 MB but was " + size + " MB" );
		ApposeUtils.deleteEnvironment( testEnvName, null );
		assertFalse( ApposeUtils.checkEnvironmentInstalled( testEnvName ) );
	}

	@Test
	void testCalculateDirectorySize_withFilesAndSubDirectories() throws IOException
	{
		// Arrange: Create a temporary directory and nested structure with files
		File tempDir = Files.createTempDirectory( "sizeTestDir" ).toFile();
		tempDir.deleteOnExit();

		File nestedDir = new File( tempDir, "nestedDir" );
		assertTrue( nestedDir.mkdir() );

		File file1 = new File( tempDir, "file1.txt" );
		assertTrue( file1.createNewFile() );
		Files.write( file1.toPath(), "Hello World!".getBytes( StandardCharsets.UTF_8 ) );

		File file2 = new File( nestedDir, "file2.txt" );
		assertTrue( file2.createNewFile() );
		Files.write( file1.toPath(), "Java Testing!".getBytes( StandardCharsets.UTF_8 ) );

		// Act: Calculate the directory size
		long size = ApposeUtils.calculateDirectorySize( tempDir );

		// Assert: Verify the expected size is accurate
		assertTrue( size > 0 ); // Basic check for non-zero size
		assertEquals( file1.length() + file2.length(), size );
	}

	@Test
	void testCalculateDirectorySize_withEmptyDirectory() throws IOException
	{
		// Arrange: Create an empty temporary directory
		File tempDir = Files.createTempDirectory( "emptySizeTestDir" ).toFile();
		tempDir.deleteOnExit();

		// Act: Calculate the directory size
		long size = ApposeUtils.calculateDirectorySize( tempDir );

		// Assert: Verify the size is zero
		assertEquals( 0, size );
	}

	@Test
	void testCalculateDirectorySize_withNonExistentDirectory()
	{
		// Arrange: Reference a non-existent directory
		File nonExistentDir = new File( "nonExistentTestDirectory" );

		// Act: Calculate the directory size
		long size = ApposeUtils.calculateDirectorySize( nonExistentDir );

		// Assert: Verify the size is zero
		assertEquals( 0, size );
	}

	@Test
	void testDeleteDirectory_withExistingDirectory() throws IOException
	{
		// Arrange: Create a temporary directory with nested files and subdirectories
		File tempDir = Files.createTempDirectory( "testDir" ).toFile();
		tempDir.deleteOnExit();

		File nestedDir = new File( tempDir, "nestedDir" );
		assertTrue( nestedDir.mkdir() );

		File file1 = new File( tempDir, "file1.txt" );
		assertTrue( file1.createNewFile() );

		File file2 = new File( nestedDir, "file2.txt" );
		assertTrue( file2.createNewFile() );

		// Act: Delete the directory
		ApposeUtils.deleteDirectory( tempDir );

		// Assert: The entire structure should be deleted
		assertFalse( tempDir.exists() );
	}

	@Test
	void testDeleteDirectory_withEmptyDirectory() throws IOException
	{
		// Arrange: Create an empty temporary directory
		File tempDir = Files.createTempDirectory( "testEmptyDir" ).toFile();
		tempDir.deleteOnExit();

		// Act: Delete the directory
		ApposeUtils.deleteDirectory( tempDir );

		// Assert: The directory should be deleted
		assertFalse( tempDir.exists() );
	}

	@Test
	void testDeleteDirectory_withNonExistentDirectory() throws IOException
	{
		// Arrange: Reference a non-existent directory
		File nonExistentDir = new File( "nonExistentDirectory" );

		// Act: Delete the directory, which shouldn't throw an exception
		ApposeUtils.deleteDirectory( nonExistentDir );

		// Assert: Since the directory doesn't exist, no changes occur
		assertFalse( nonExistentDir.exists() );
	}

	@Test
	void testDeleteDirectory_withNullDirectory()
	{
		// Act and Assert: Passing null should not throw an exception
		assertDoesNotThrow( () -> ApposeUtils.deleteDirectory( null ) );
	}

	@Test
	void testDeleteDirectory_withInvalidDirectory() throws IOException
	{
		// Arrange: Create a file instead of a directory
		File tempFile = Files.createTempFile( "testFile", ".txt" ).toFile();
		tempFile.deleteOnExit();

		// Act and Assert: Trying to delete a file using deleteDirectory
		// should result in the file being deleted.
		ApposeUtils.deleteDirectory( tempFile );
		assertFalse( tempFile.exists() );
	}
}
