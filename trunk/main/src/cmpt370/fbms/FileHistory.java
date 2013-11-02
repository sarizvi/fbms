package cmpt370.fbms;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

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
	public static Path getRevision(Path file, long timestamp)
	{
		// Get the revision in question
		RevisionInfo revision = DbManager.getRevisionInfo(file, timestamp);
		Path pathToTempFile = null;
		PrintWriter output = null;

		try
		{
			// Create a temporary file for the revision
			pathToTempFile = Files.createTempFile("revision", ".txt");

			// Write the diff to that temp file
			output = new PrintWriter(pathToTempFile.toFile());
			output.print(revision.diff);

			Control.logger.info("Created temporary file at " + pathToTempFile.toString()
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

	public static void storeRevision(Path file, Path diff, long filesize, long delta)
	{

	}

	public static Path obtainRevision(Path file, long timestamp)
	{
		return null;
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
		DbManager.renameFile(file, newName);
	}
}
