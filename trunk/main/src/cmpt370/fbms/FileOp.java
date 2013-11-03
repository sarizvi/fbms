/*
 * FBMS: File Backup and Management System Copyright (C) 2013 Group 06
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package cmpt370.fbms;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class FileOp
{
	/**
	 * Copies the specified source (file or directory) into the specified destination directory. New
	 * directories are created as necessary. If a directory is copied, all its contents are also
	 * copied.
	 * 
	 * @param sourceFile
	 *            The source file or directory to copy.
	 * @param destFolder
	 *            The destination folder to place the copy into.
	 */
	public static void copy(Path sourceFile, Path destFolder)
	{
		// Declare copies of the attributes as final so they can be accessed inside anonymous child
		// classes
		final Path source = sourceFile;
		final Path target = destFolder;

		// If the path is a directory, we need to walk its tree
		if(sourceFile.toFile().isDirectory())
		{
			try
			{
				// from JDK document
				Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
						Integer.MAX_VALUE, new SimpleFileVisitor<Path>()
						{
							// Called when we encounter a directory in the file tree before we
							// actually visit the directory
							@Override
							public FileVisitResult preVisitDirectory(Path dir,
									BasicFileAttributes attrs)
							{
								// The target directory the destination folder resolved to the
								// difference between the folder we're copying and the location
								// we're currently at inside that folder. So for example, if the
								// source directory (which we're copying) is %src%, and the current
								// location in our file tree is %src%/a/b/c, then the target
								// directory will be %dest%/a/b/c
								Path targetDir = target.resolve(source.relativize(dir));

								try
								{
									Files.copy(dir, targetDir);

									Control.logger.debug("Copied " + dir.toString() + " to "
											+ targetDir.toString());
								}
								catch(FileAlreadyExistsException e)
								{
									if(!Files.isDirectory(targetDir))
									{
										Control.logger.error(
												"Could not copy " + targetDir.toString()
														+ "; Not actually a directory.", e);
									}
								}
								catch(IOException e)
								{
									Errors.nonfatalError("Could not copy directory", e);
								}

								// Keep walking through our tree
								return FileVisitResult.CONTINUE;
							}

							// Called when we encounter an actual file in our file tree
							@Override
							public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
							{
								try
								{
									// File is copied to folder relative to the folder that we are
									// copying (the source). Works like targetDir in
									// preVisitDirectory() above
									Path targetFile = target.resolve(source.relativize(file));
									Files.copy(file, targetFile,
											StandardCopyOption.REPLACE_EXISTING);

									Control.logger.debug("Copied " + file.toString() + " to "
											+ targetFile);
								}
								catch(IOException e)
								{
									Errors.nonfatalError("Could not copy file " + file.toString(),
											e);
								}

								return FileVisitResult.CONTINUE;
							}
						});
			}
			catch(IOException e)
			{
				Errors.nonfatalError("Could not copy source folder " + sourceFile.toString()
						+ "to " + destFolder.toString(), e);
			}
		}
		// Otherwise its just a regular file
		else
		{
			// Append the name of the source file to the destination folder
			Path destFile = destFolder.resolve(sourceFile.getFileName());
			try
			{
				// Make any necessary directories up to the parent of where our file is being copied
				// to
				destFolder.toFile().mkdirs();

				Files.copy(sourceFile, destFile, StandardCopyOption.REPLACE_EXISTING);
			}
			catch(IOException e)
			{
				Errors.nonfatalError("Could not copy " + sourceFile.toString() + " to "
						+ destFolder.toString(), e);
			}
		}


	}

	/**
	 * Copy everything in the list to the backup folder. Only copies files. To copy folders, use
	 * copy(Path, Path).
	 * 
	 * @param sourceFile
	 *            List of paths to files in the live directory that should be copied to the backup
	 *            directory.
	 */
	public static void copy(List<Path> sourceFiles)
	{
		for(Path path : sourceFiles)
		{
			try
			{
				// Get the path the file would have in the backup directory
				Path destPath = convertPath(path);

				// Get the folder that file is in (the parent) and create any necessary directories
				// such that this parent exists
				destPath.getParent().toFile().mkdirs();

				// Copy the actual file
				Files.copy(path, destPath, StandardCopyOption.REPLACE_EXISTING);
			}
			catch(IOException e)
			{
				Errors.nonfatalError("Could not copy " + path.toString() + "to "
						+ convertPath(path).toString(), e);
			}
		}
	}

	public static Path createDiff(Path beforeFile, Path afterFile)
	{
		return null;
	}

	public static Path applyDiff(Path sourceFile, Path afterFile)
	{
		return null;
	}

	public static void rename(Path file, String newName)
	{

	}

	/**
	 * Delete the file or directory specified by the Path.
	 * 
	 * @param file
	 *            The Path to the file or directory to delete.
	 */
	public static void delete(Path file)
	{
		File targetFile = file.toFile();

		// If a bad path is given...
		if(!targetFile.exists())
		{
			Control.logger.error("Unable to delete " + targetFile.toString()
					+ "  as it does not exist.");
			return;
		}

		// If a folder is given, recursively delete its sub-directories first.
		if(targetFile.isDirectory())
		{
			Control.logger.debug(targetFile.toString()
					+ " is a directory, recursively deleting contents");

			for(File childFile : targetFile.listFiles())
			{
				FileOp.delete(childFile.toPath());
			}
		}

		// Try and delete regular files, logging if unsuccessful
		if(!targetFile.delete())
		{
			Control.logger.error(file.toString() + " cannot be deleted. Operation aborted.");
		}
		else
		{
			Control.logger.debug("Successfully deleted " + file.toString());
		}

	}

	/**
	 * Return the file's size in bytes.
	 * 
	 * @param file
	 * @return the file's size in bytes.
	 */
	public static long fileSize(Path file)
	{
		File targetFile = file.toFile();

		return targetFile.length();
	}

	public static List<String> fileToList(Path file)
	{
		return null;
	}

	/**
	 * A utility function for converting a file to a String.
	 * 
	 * @author erickson <http://stackoverflow.com/users/3474/erickson> from
	 *         <http://stackoverflow.com/a/326440>
	 * 
	 * @param path
	 *            The path of the file to convert.
	 * @return A string containing the contents of the file.
	 * @throws IOException
	 *             If the file does not exist or cannot be opened.
	 */
	public static String fileToString(Path path) throws IOException
	{
		// Read the file as raw bytes
		byte[] encoded = Files.readAllBytes(path);

		// And encode those bytes as the default character set (eg, UTF 8)
		return Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
	}

	/**
	 * Check the given file. If it is a text file and <5 MB, returns true.
	 * 
	 * @param file
	 *            the file to be checked.
	 * @return true if file is text file and <= 5MB
	 */
	public static boolean fileValid(Path file)
	{
		String fileTypeString = null;
		try
		{
			fileTypeString = Files.probeContentType(file);
		}
		catch(Exception e)
		{
			// when failed, return false.
			return false;
		}
		if(fileSize(file) > 5242880)
		{
			return false;
		}
		return fileTypeString.startsWith("text");
	}

	/**
	 * Returns whether or not a path is a folder.
	 * 
	 * @param path
	 *            The path in question.
	 * @return True if a folder, false if a regular file.
	 */
	public static boolean isFolder(Path path)
	{
		return path.toFile().isDirectory();
	}


	/**
	 * Takes in a path to either the live directory or backup directory and converts it to the
	 * corresponding path to the opposite directory. For example, passing a path to something in the
	 * live directory will return the path to that file's location in the backup directory. Returns
	 * null if the file is not in the other directory.
	 * 
	 * @param inputPath
	 *            The path to convert.
	 * @return Path to file in opposite directory or null if it doesn't exist.
	 */
	public static Path convertPath(Path inputPath)
	{
		Path convertedPath = null;

		if(inputPath.startsWith(Control.liveDirectory))
		{
			// Remove the live directory from this path and add the backup directory to it
			String newPath = inputPath.toString().substring(
					Control.liveDirectory.toString().length());
			newPath = Control.backupDirectory.toString() + newPath;

			convertedPath = Paths.get(newPath).normalize();
		}
		else if(inputPath.startsWith(Control.backupDirectory))
		{
			// Remove the backup directory from this path and add the live directory to it
			String newPath = inputPath.toString().substring(
					Control.backupDirectory.toString().length());
			newPath = Control.liveDirectory.toString() + newPath;

			convertedPath = Paths.get(newPath).normalize();
		}

		// If the file did not exist or the input path did not start with either the backup or
		// live directory (ie, the path is not a child of either the live or backup directories),
		// we return null.
		return convertedPath;
	}

	/**
	 * Checks the bytes of a file to determine if a given file is equal to another.
	 * 
	 * @param file1
	 * @param file2
	 * @return
	 * @throws IOException
	 */
	public static boolean isEqual(Path file1, Path file2)
	{
		// Can't be equal if the file size is different
		if(file1.toFile().length() != file2.toFile().length())
		{
			return false;
		}

		// Try to compare the bytes otherwise
		byte[] file1Bytes = null;
		byte[] file2Bytes = null;
		try
		{
			file1Bytes = Files.readAllBytes(file1);
			file2Bytes = Files.readAllBytes(file2);
		}
		catch(IOException e)
		{
			Control.logger.error(
					"Could not compare files " + file1.toString() + " " + file2.toString(), e);
		}

		if(Arrays.equals(file1Bytes, file2Bytes))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}