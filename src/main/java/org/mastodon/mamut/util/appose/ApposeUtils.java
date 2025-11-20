package org.mastodon.mamut.util.appose;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.tuple.Pair;
import org.apposed.appose.Appose;
import org.apposed.appose.builder.Builders;
import org.apposed.appose.util.Environments;
import org.mastodon.mamut.util.ByteFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApposeUtils
{
	private ApposeUtils()
	{
		// prevent instantiation
	}

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	/**
	 * Installs a Python environment based on the provided content inside the appose environments directory.
	 * Use mamba to handle the installation process. The content should be in YAML format.
	 *
	 * @param envContent The YAML content that defines the Python environment configuration.
	 *                   This should include dependencies and other necessary details
	 *                   to set up the environment.
	 * @throws IOException If an I/O error occurs during the installation process.
	 */
	public static void installEnvironment( final String envContent ) throws IOException
	{
		Appose.mamba().content( envContent ).scheme( "environment.yml" ).logDebug().rebuild();
	}

	/**
	 * Deletes the specified Python environment and all its associated files from the appose environments directory.
	 * If the deletion fails, an error message is logged and displayed in a dialog box.
	 *
	 * @param envName The name of the environment to be deleted. This corresponds to the directory name in the appose environments directory.
	 * @param parent  The parent component for displaying any error dialog in case the deletion fails.
	 */
	public static void deleteEnvironment( final String envName, final Component parent )
	{
		File envDir = new File( Environments.apposeEnvsDir(), envName );
		try
		{
			deleteDirectory( envDir );
		}
		catch ( IOException e )
		{
			logger.error( "Deletion failed for environment: {}. Reason: {}", envName, e.getMessage(), e );
			SwingUtilities.invokeLater( () -> JOptionPane.showMessageDialog(
					parent,
					"Could not delete directory " + envDir + ". Reason: " + e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE
			) );
		}
	}

	/**
	 * Checks if a specific Python environment is installed in the appose environments directory.
	 * The method verifies the existence of the environment by checking if the directory
	 * corresponding to the environment name can be wrapped or accessed.
	 *
	 * @param envName The name of the environment to check. This should match the directory
	 *                name inside the appose environments directory.
	 * @return true if the environment is installed and accessible, false otherwise.
	 */
	public static boolean checkEnvironmentInstalled( final String envName )
	{
		return Builders.canWrap( new File( Environments.apposeEnvsDir(), envName ) );
	}

	/**
	 * Calculates the total size of a specified python environment managed by appose and returns the size
	 * as a human-readable string. If the directory does not exist, it returns "N/A".
	 *
	 * @param envName The name of the environment directory to calculate the size for.
	 * @return The size of the specified environment directory as a human-readable string,
	 *         or "N/A" if the directory does not exist.
	 */
	public static String calculateEnvironmentSize( final String envName )
	{
		File envDir = new File( Environments.apposeEnvsDir(), envName );
		if ( !envDir.exists() )
			return "N/A";
		long size = calculateDirectorySize( envDir );
		return ByteFormatter.humanReadableByteCount( size );
	}

	/**
	 * Deletes the specified directory and all its contents, including subdirectories and files.<br>
	 * This method performs a recursive deletion of the directory and its contents. If the directory
	 * does not exist or is null, the method does nothing.
	 *
	 * @param dir The directory to be deleted. If null or does not exist, the method will return without any action.
	 * @throws IOException If an I/O error occurs while deleting files or directories.
	 */
	public static void deleteDirectory( final File dir ) throws IOException
	{
		if ( dir == null || !dir.exists() )
			return;

		File[] files = dir.listFiles();
		if ( files != null )
		{
			for ( File file : files )
			{
				if ( file.isDirectory() )
					deleteDirectory( file );
				else
					Files.deleteIfExists( file.toPath() );
			}
		}
		Files.deleteIfExists( dir.toPath() );
	}

	/**
	 * Calculates the total size of a directory, including all files and subdirectories.
	 *
	 * @param directory The directory whose size is to be calculated. Must not be null and must represent a valid directory.
	 * @return The total size of the directory in bytes. If the directory does not exist or is null, the method will return 0.
	 */
	public static long calculateDirectorySize( final File directory )
	{
		if ( directory == null || !directory.exists() )
		{
			return 0;
		}

		String os = System.getProperty( "os.name" ).toLowerCase();
		boolean isUnix = os.contains( "nix" ) || os.contains( "nux" ) || os.contains( "mac" );

		if ( isUnix )
		{
			try
			{
				return calculateDiskUsageUnix( directory.toPath() );
			}
			catch ( IOException e )
			{
				// fallback to generic method if attribute not supported
				return calculateFileLengthRecursive( directory );
			}
		}
		else
		{
			return calculateFileLengthRecursive( directory );
		}
	}

	// --- Linux/macOS: actual disk usage, like du -sb ---
	private static long calculateDiskUsageUnix( final Path directory ) throws IOException
	{
		final long[] total = { 0 };
		boolean hasUnixView = supportsUnixAttributes();

		Files.walkFileTree( directory, new SimpleFileVisitor< Path >()
		{
			@Nonnull
			@Override
			public FileVisitResult preVisitDirectory( @Nonnull Path dir, @Nonnull BasicFileAttributes attrs ) throws IOException
			{
				return Files.isSymbolicLink( dir )
						? FileVisitResult.SKIP_SUBTREE
						: FileVisitResult.CONTINUE;
			}

			@Nonnull
			@Override
			public FileVisitResult visitFile( @Nonnull Path file, @Nonnull BasicFileAttributes attrs ) throws IOException
			{
				if ( !Files.isSymbolicLink( file ) )
					total[ 0 ] += getFileDiskUsage( file, hasUnixView );
				return FileVisitResult.CONTINUE;
			}
		} );

		return total[ 0 ];
	}

	private static boolean supportsUnixAttributes()
	{
		Set< String > views = FileSystems.getDefault().supportedFileAttributeViews();
		return views.contains( "unix" );
	}

	/**
	 * Returns the disk usage (in bytes) for a single file.
	 * Uses actual allocated blocks if the unix view is supported.
	 */
	private static long getFileDiskUsage( final Path file, final boolean hasUnixView ) throws IOException
	{
		if ( !hasUnixView )
			return Files.size( file );

		try
		{
			Object blocksAttr = Files.getAttribute( file, "unix:blocks", LinkOption.NOFOLLOW_LINKS );
			if ( blocksAttr instanceof Number )
				return ( ( Number ) blocksAttr ).longValue() * 512L; // du uses 512-byte blocks
		}
		catch ( IllegalArgumentException | UnsupportedOperationException e )
		{
			// ignore and fall back to logical size
		}
		return Files.size( file );
	}

	// --- Windows / fallback: logical file sizes only ---
	private static long calculateFileLengthRecursive( final File directory )
	{
		long size = 0;
		File[] files = directory.listFiles();
		if ( files != null )
		{
			for ( File file : files )
			{
				if ( Files.isSymbolicLink( file.toPath() ) )
					continue;
				if ( file.isFile() )
					size += file.length();
				else if ( file.isDirectory() )
					size += calculateFileLengthRecursive( file );
			}
		}
		return size;
	}

	/**
	 * Checks whether a specific Python environment is installed and prompts the user to install it
	 * if it is not already installed. The user is presented with a dialog box to confirm the installation.
	 * If the user declines, the method a pair consisting of {@code false} and an appropriate message.
	 *
	 * @param envName The name of the environment to check. This should match the directory
	 *                name inside the appose environments directory.
	 * @param log     The logger instance used for logging messages during the environment check
	 *                and installation process.
	 * @return A pair where the first element is a boolean indicating if the environment is installed
	 *         or was successfully triggered for installation, and the second element is a message
	 *         providing additional information about the process or failure reason.
	 */
	public static Pair< Boolean, String > confirmEnvInstallation( final String envName, final org.scijava.log.Logger log )
	{
		boolean isEnvInstalled = checkEnvironmentInstalled( envName );
		if ( !isEnvInstalled )
		{
			log.info( "Python environment not yet installed, but can be installed now.\n" );
			int result = JOptionPane.showConfirmDialog(
					null,
					"The required Python environment is not installed.\nWould you like to install it now?\n\nNote: This requires an internet connection and may take several minutes.",
					"Python Environment Installation",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE
			);
			if ( result != JOptionPane.YES_OPTION )
				return Pair.of( false, "Python environment installation was declined.\nCannot proceed without the required environment." );
			log.info( "User confirmed installation. Installing Python environment.\n" );
			log.info( "Installation progress can be observed using FIJI console: FIJI > Window > Console.\n" );
		}
		return Pair.of( true, "" );
	}
}
