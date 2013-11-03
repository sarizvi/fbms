package cmpt370.fbms;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;

import org.apache.log4j.Logger;

public class FileOp
{
	private static Logger logger = Logger.getLogger(FileOp.class);
	private static String backupPath;
	private static String livePath;

	public static void copy(Path sourceFile, Path destFolder)
	{
		final Path source = sourceFile;
		final Path target = destFolder;
		File mFile = sourceFile.toFile();
		if(mFile.isDirectory())
		{
			try
			{
				// from JDK document
				Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
						Integer.MAX_VALUE, new SimpleFileVisitor<Path>()
						{
							@Override
							public FileVisitResult preVisitDirectory(Path dir,
									BasicFileAttributes attrs) throws IOException
							{
								Path targetdir = target.resolve(source.relativize(dir));
								try
								{
									Files.copy(dir, targetdir);
								}
								catch(FileAlreadyExistsException e)
								{
									if(!Files.isDirectory(targetdir))
										throw e;
								}
								return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
									throws IOException
							{
								Files.copy(file, target.resolve(source.relativize(file)),
										StandardCopyOption.REPLACE_EXISTING);
								return FileVisitResult.CONTINUE;
							}
						});
			}
			catch(IOException e)
			{
				logger.warn("Could copy source folder " + sourceFile.toString() + "to "
						+ destFolder.toString());
			}
		}
		else
		{
			Path liveFolder = (new File(livePath)).toPath();
			Path srcFile = sourceFile.subpath(liveFolder.getNameCount(), sourceFile.getNameCount());
			Path dstFile = new File(new File(backupPath), srcFile.toString()).toPath();
			try
			{
				Files.copy(sourceFile, dstFile, StandardCopyOption.REPLACE_EXISTING);
			}
			catch(IOException e)
			{
				logger.warn("Could copy " + sourceFile.toString() + "to " + destFolder.toString());
			}
		}


	}

	public static void copy(List<Path> sourceFiles)
	{
		File rootPath = new File(backupPath);
		for(Path path : sourceFiles)
		{
			File dstFile = new File(rootPath, path.toString());
			try
			{
				dstFile.mkdirs();
				Files.copy(path, dstFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			catch(IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	 * Delete the specific Path. If the Path given is directory, delete the directory represented by
	 * Path.
	 * 
	 * @param file
	 *            the Path to delete.
	 */
	public static void delete(Path file)
	{
		File targetFile = file.toFile();

		// If a bad path is given...
		if(!targetFile.exists())
		{
			logger.warn(targetFile.toString() + " is not existed. Unable to delete.");
			return;
		}

		// If a folder is given, recursively delete its sub-directories first.
		if(targetFile.isDirectory())
		{
			for(File f : targetFile.listFiles())
			{
				FileOp.delete(f.toPath());
			}
		}

		if(!targetFile.delete())
		{
			logger.warn(file.toString() + " cannot be deleted. Operation aborted.");
			return;
		}

	}

	public static long fileSize(Path file)
	{
		File targetFile = file.toFile();

		return targetFile.length();
	}

	public static List<String> fileToList(Path file)
	{
		return null;
	}

	public static boolean fileValid(Path file)
	{
		String fileTypeString = null;
		try
		{
			fileTypeString = Files.probeContentType(file);
		}
		catch(Exception e)
		{}
		if(fileTypeString == null)
		{
			return false;
		}
		return fileTypeString.startsWith("text");
	}

	protected static void setBackupPath(String path)
	{
		backupPath = path;
	}

	public static void setLivePath(String livePath)
	{
		FileOp.livePath = livePath;
	}

}
