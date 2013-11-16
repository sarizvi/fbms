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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Allows the modification and retrieval of revision data (the file history).
 */
public class FileHistory
{
	/**
	 * Gets the specified revision and outputs it to a temporary file, returning the path to this
	 * file.
	 * 
	 * @param file
	 *            The path of the file (in the live directory) to get the revision of.
	 * @param timestamp
	 *            The time stamp of the specific revision.
	 * @return A path to the temporary file containing the diff that makes up that revision.
	 */
	public static Path getRevisionInfo(Path file, long timestamp)
	{
		// Get the revision in question
		RevisionInfo revision = DbManager.getSpecificRevision(file, timestamp);
		Path pathToTempFile = null;
		PrintWriter output = null;

		try
		{
			// Create a temporary file for the revision
			pathToTempFile = Files.createTempFile("revision", ".txt");

			// Write the diff to that temp file
			output = new PrintWriter(pathToTempFile.toFile());
			output.print(revision.diff);

			Main.logger.info("Created temporary file at " + pathToTempFile.toString()
					+ " for revision " + file.toString() + " (" + timestamp + ")");
		}
		catch(IOException e)
		{
			Errors.nonfatalError("Could not create temporary file for revision.", e);
		}
		finally
		{
			if(output != null)
			{
				output.close();
			}
		}

		return pathToTempFile;
	}

	/**
	 * Stores the revision in the database with the supplied information.
	 * 
	 * @param file
	 *            the path to the actual file (in the live directory).
	 * @param diff
	 *            the path to the diff file.
	 * @param filesize
	 *            the new file size (ie, the file size of the file in the live directory).
	 * @param delta
	 *            change in file size.
	 */
	public static void storeRevision(Path file, Path diff, long filesize, long delta)
	{
		// Get the diff as a String
		String diffString = null;
		try
		{
			diffString = FileOp.fileToString(diff);
		}
		catch(IOException e)
		{
			Errors.nonfatalError("Could not store " + file.toString() + " to database.");
		}

		DbManager.insertRevision(file, diffString, null, delta, filesize);

		Main.logger.debug("Revision stored for file " + file.toString() + " (file size: "
				+ filesize + "; delta: " + delta + ")");
	}

	/**
	 * Obtain the content of the file at a specific revision on the given time stamp.
	 * 
	 * The time stamp should be valid, otherwise this method will restore the file revision nearest
	 * to the given time stamp.
	 * 
	 * Returns a Path to the patched file. If error occurs, null will be returned.
	 * 
	 * @param file
	 *            a Path to a file in backup directory.
	 * @param timestamp
	 *            a long representing the file version.
	 * @return a Path of patched file. null if failed.
	 */
	public static Path obtainRevisionContent(Path file, long timestamp)
	{
		// Retrieve data from database
		List<RevisionInfo> fileRevisionList = DbManager.getFileRevisions(FileOp.convertPath(file));
		LinkedList<RevisionInfo> patchList = new LinkedList<>();

		// Add the records we needed to a linked list
		for(RevisionInfo revisionInfo : fileRevisionList)
		{
			if(revisionInfo.time > timestamp)
			{
				patchList.add(revisionInfo);
			}
		}


		// Sort the linked list in reverse order
		Collections.sort(patchList);
		Collections.reverse(patchList);


		// Apply diff to the file. return null if error occurs
		Path newestFile = file;
		Path tempPatchFile = null;
		try
		{
			tempPatchFile = Files.createTempFile("FBMS", ".tmp");

			for(RevisionInfo revisionInfo : patchList)
			{
				FileOp.stringToFile(revisionInfo.diff, tempPatchFile);
				newestFile = FileOp.applyPatch(newestFile, tempPatchFile);
			}
			return newestFile;
		}
		catch(IOException e)
		{
			Errors.nonfatalError("Error occurs while applying patches.", e);
			return null;
		}
	}

	/**
	 * Renames all instances of a certain file to a new name in the revisions database. This is just
	 * a wrapper for the DbManager function.
	 * 
	 * @param file
	 *            The path of the file we are renaming.
	 * @param newName
	 *            The new name of the file. Note this does not include the full path: just the file
	 *            name (and extension).
	 */
	public static void renameRevision(Path file, String newName)
	{
		DbManager.renameRevisions(file, newName);
		Main.logger.info("File " + file.toString() + " is renamed to " + newName);
	}
}