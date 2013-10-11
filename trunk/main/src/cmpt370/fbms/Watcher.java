package cmpt370.fbms;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.contentobjects.jnotify.JNotifyListener;

public class Watcher implements JNotifyListener
{
	/**
	 * Event handler for files that are renamed. The JNotify watcher will call this method if it
	 * detects a file rename operation. Such files will be added to the appropriate list in Control.
	 * Since rename operations must store both the old name and the new name, a PoD object is used
	 * to combine both Paths into one object.
	 * 
	 * @param wd
	 *            Unused
	 * @param rootPath
	 *            The path the file is located in
	 * @param oldName
	 *            The file's previous name
	 * @param newName
	 *            The file's new name
	 */
	public void fileRenamed(int wd, String rootPath, String oldName, String newName)
	{
		// Strings must be converted into Path objects
		Path oldPath = Paths.get(rootPath + File.separator + oldName);
		Path newPath = Paths.get(rootPath + File.separator + newName);

		RenamedFile listObject = new RenamedFile();
		listObject.oldName = oldPath;
		listObject.newName = newPath;

		Control.renamedFiles.add(listObject);

		// Use the logger in Control to issue messages
		Control.logger.info("Renamed file " + oldName + " to " + newName + " in " + rootPath);
	}

	/**
	 * Event handler for files that are modified. The JNotify watcher will call this method if it
	 * detects a file rename operation. Such files will be added to the appropriate list in Control.
	 * 
	 * @param wd
	 *            Unused
	 * @param rootPath
	 *            The path the file is located in
	 * @param name
	 *            The file's name
	 */
	public void fileModified(int wd, String rootPath, String name)
	{
		Path path = Paths.get(rootPath + File.separator + name);
		Control.modifiedFiles.add(path);

		Control.logger.info("Modified file " + path);
	}

	/**
	 * Event handler for files that are deleted. The JNotify watcher will call this method if it
	 * detects a file rename operation. Such files will be added to the appropriate list in Control.
	 * 
	 * @param wd
	 *            Unused
	 * @param rootPath
	 *            The path the file is located in
	 * @param name
	 *            The file's name
	 */
	public void fileDeleted(int wd, String rootPath, String name)
	{
		Path path = Paths.get(rootPath + File.separator + name);
		Control.deletedFiles.add(path);

		Control.logger.info("Deleted file " + path);
	}

	/**
	 * Event handler for files that are created. The JNotify watcher will call this method if it
	 * detects a file rename operation. Such files will be added to the appropriate list in Control.
	 * 
	 * @param wd
	 *            Unused
	 * @param rootPath
	 *            The path the file is located in
	 * @param name
	 *            The file's name
	 */
	public void fileCreated(int wd, String rootPath, String name)
	{
		Path path = Paths.get(rootPath + File.separator + name);
		Control.createdFiles.add(path);

		Control.logger.info("Created file " + path);
	}
}
