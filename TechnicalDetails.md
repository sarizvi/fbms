This page details the workings of FBMS in great detail.

## Overview ##
FBMS is an automated backup and revision program. The user specifies a folder that they want to keep backed up (the "live directory") and the location to store the backup (the "backup directory"). The program automatically searches the live directory for changes in intervals, and copies changes to the backup directory. Revisions are automatically created by creating diff ("patch") files for every change.

Thus, not only is the user's data backed up, but older versions of the data are also backed up. FBMS can be thought of as a hybrid of a local-only Dropbox and a version control system. While it's not as customizeable as a version control system like SVN or git, FBMS is easy to use and runs in the background without the need for user interaction.

## A summary of the components ##
  * **Watcher**: The Watcher component is a file system listener, using the JNotify library. It finds changes to files stores lists of which files have changed, so that the control module can examine the list of changes on each interval.
  * **FileOp**: The FileOp component performs basic file operations. It can create/apply diff files (using [java-diff-utils](http://code.google.com/p/java-diff-utils/)), copy files, rename files.
  * **FileHistory**: The FileHistory component performs actions related to file history. It can retrieve revisions for files, rename revisions, and store revisions.
  * **DbManager**: The DbManager component provides an additional level of abstraction away from the actual database manager (SQLite) by providing methods for performing routine database tasks. The DbManager makes use of the [SQLite JDBC library](https://bitbucket.org/xerial/sqlite-jdbc).
  * **Control**: The Control component manages the other components. For the backend, it runs a threaded loop that checks if the Watcher has found any changes. If changes are detected, it instructs the other components on handling the change (based on the type of change). The Control also receives commands from the GUI, such as to restore a revision. Finally, the Control can manipulate the program's configuration.
  * **Data**: The Data component provides data for the front end. It makes no changes to the data, and merely retrieves it in a format that the FrontEnd can display. Retrieved data includes file information (in a directory) and revision information (for a file).
  * **FrontEnd**: The FrontEnd component is the GUI. It merely displays the contents of the backup directory (and cannot navigate outside of this directory). The user can select a single file, which opens up a window for restoring and viewing revisions (which is done by the Control). The user can also select a folder to view the contents of that folder (go "into" the folder). An up button takes the user up a directory and a refresh button re-fetches the contents of the current directory.

## Watcher ##
The Watcher uses the JNotify library to watch for created, modified, and renamed files. It's a simple event handler. When the fileCreated handler is triggered, the file name ("name" refers to the full path of the file) is added to a list for created files.

Likewise, when the fileModified handler is triggered, the file name is added to a list for modified files (the reason separate lists are used will be mentioned in the section on the Control component).

The fileRenamed handler works similarly, but since we have to store both the old name and the new name, it is necessary that we create a plain old data class (a "struct"), so that we can store both the old and last name in a single part of the list.

Finally, the fileDeleted handler is used to create a list of files that have been deleted. We don't delete files from the backup directory, but rather the list is used so that we don't try to, say, copy a file that was, say, deleted after being created in the same interval.

## Control ##
### Watching for changes ###
The Control uses the lists which the Watcher has populated. In a separate thread, the Control runs a loop which loops through all the array lists and handles their content. After handling the content of the lists, the Control empties the lists, so that the lists always contain the files that have been changed since the last interval.

#### Created files ####
For the files in the created files list, the control must first determine if the file already exists in the backup directory. If it does, this means that a new file has been created and has overwritten an older file. If this is the case, we must treat the "creation" as though it were simply a modification. Thus, we remove that file from the created list and put it in the modified list.

Control also has to check if the "file" is a folder with FileOp. Folders are not revised the same way, but rather the copy function automatically creates folders as necessary. Thus, if it's a folder, ignore the file.

If the file doesn't already exist, the Control simply tells FileOp to copy the files to the backup directory.

#### Modified files ####
For modified files, the Control uses FileOp to check if the file is a text file of under 5 MB (?). This is necessary because binary files files do not make efficient diffs. Testing will be necessary to determine what file size limit works best.

Control must first check if there exists such a file in the backup directory already, as it's possible for a file to be flagged as modified without being created. If the file does not already exist in the backup dir, don't create a diff: just copy it over.

Otherwise, for valid text and small files, the Control uses FileOp to create a diff for the file, comparing the version in the live folder with the one in the backup folder. The diff content is then passed into FileHistory, which stores the revision in the database.

For all kinds of files, FileOp copies the files to the backup directory.

#### Renamed files ####
For renamed files, the Control uses FileHistory to rename the file name in the database table (changing instances of the old name to the new name). Then FileOp is used check if a file of the new name already exists in the backup directory. If so, delete that. Either way, use FileOp to rename the old file to the new name.

If the renamed "file" is actually a folder (FileOp can detect if it's a folder), things get a bit more complicated. Folders should be renamed with FileOp and then FileHistory is used to rename a folder in the revision database.

You'll notice that if there was a conflict, we only deleted the conflicting file and not its history. This effectively merged the history of the two files, since file history is dependent only on the file's path.

#### Deleted files ####
The deleted file list is NOT used to delete files from the backup folder. After all, we want the backup there in case we accidentally deleted something we didn't want to delete.

Instead, the deleted list is used simply to remove deleted items from the other lists (created, modified, and renamed). After all, if something appears in both, say, the modified and deleted list, that means that the file was modified and then deleted in the same interval. If we tried to create a diff of that file, it'd fail, sine the file no longer exists in the live directory.

#### Files that appear in multiple lists ####
There's three lists (created, modified, and renamed), but it's entirely possible for a file to have been created, modified, or renamed multiple times in the interval, which would result in the file being in multiple lists. Since the lists simply contain the file name, duplicates contain no value. Thus, we first remove all duplicates in the individual lists before any processing is done.

We also realize that it doesn't make sense for some files to appear in multiple different lists. For example, if a file is created and immediately modified, we don't need multiple revisions, since the modifications are already done. Recall that the lists are simply names of the files, not their content. Thus, for all files that appear in the created list, we must search for any instances of the same file being in the modified list, so we just have a single instance of the file being created.

Similarly, we must search for instances of the newly created file being in the renamed list. If it appears in this list, change the name of the file in the created list (since it's now under a new name).

Otherwise, a file can appear in multiple lists as long as the created list is fully processed first, followed by the modification list, and finally the rename list.

**Example**: A file is modified and renamed within the interval. Thus, it exists in both the modified and renamed list. The lists require no modifications, we simply have to perform the modification as normal and then the rename as normal. The rename will affect all _past_ modifications as well as the one we just made (the intended behavior).

**Example**: A file is created and modified and renamed within the interval. So it exists in three lists. We first notice that the file exists in both the created and modified list. This is redundant, so we remove it from the modified list (leaving it in the created list). We then notice that the file is in the rename list, so we change the name of the file in the created list to the new name (from the rename list). The file now exists in both the created and rename lists. We keep it in the rename list in case the file was an overwrite, in which case we need to rename the older revisions so they are still accessible.

#### Error handling ####
It's entirely possible that there will be a file in the live directory that cannot be accessed. For example, if the user does not have read access to the file. In such cases, it is crucial that we don't get "stuck". If the program cannot perform an operation to a file, log it as an error (more on logging later) and skip the file.

### Handling GUI commands ###
There are three circumstances in which the control must handle events triggered by the GUI:

#### Displaying a revision ####
This occurs when the user selects a revision for a file and chooses to view the revision. The Control uses the FileHistory component to revert a chain of files until a temporary file for the given revision is obtained. The Control then displays that in the user's default program (for that type of file).

#### Displaying the changes in a revision ####
This occurs when the user selects a revision for a file and chooses to display the changes. It simply needs to show the diff for the revision. It uses FileHistory to get the specified diff and opens it in the user's default program for diff files.

#### Revert to a revision ####
This occurs when the user selects a revision for a file and chooses to revert to it. It uses the FileHistory component to create a temporary file of the revision. Then the FileOp component is used to create a diff of the new revision against the file in the backup folder. We need to do this because we have to be able to revert the reversion (for example, if the user reverts a file to a revision they had yesterday, then decides they want the version from today, again).

Once the diff is created, it's stored with FileHistory (akin to when a file is modified) and finally the revision is copied over to the live and backup folders.

The revision temporary file should then be deleted.

#### Restore all backup files ####
This occurs when the user chooses to restore everything from the file menu of the FrontEnd's menu bar. It recurses through the backup folder and uses FileHistory to copy all the files to the corresponding location in the live directory.

#### Copy-to ####
This occurs when the user chooses the copy-to option from the file menu of the FrontEnd's menu bar. It takes in a source file and destination path, then calls FileOp's copy function to copy the file.

## FileOp ##
The FileOp component is an object which can perform some operations on files. Meant to be used as a static object, so we can simply call `FileOp.copy("foo", "bar")`.

### Copying ###
Obviously it's necessary to be able to copy files. FileOp should have two different functions of the same name (an overloaded function) to perform copying. The first is straightforward: the function is given two paths: the source and the destination.

The second is more complicated and used for copying modified/created files. Recall that those files are simply lists of file names. The function can assume the copying is always done from the live directory to the backup directory. Thus, we remove the live directory path from the file names and prepend the backup directory path before calling the other copy method to copy each individual file. So in other words, this function takes in a list and copies ALL the files to the backup directory.

Both copy functions must create any necessary folders and should overwrite the destination.

The list based function can assume all paths are files, but the function that takes in a single path should check if the path is a folder. If so, create any necessary folders and call itself recursively on the contents of the folder.

### Creating a diff ###
FileOP can also create a diff of a file. The function uses the [Java-diff-utils library](https://code.google.com/p/java-diff-utils/) to create the diff as a temporary file (note: place temporary files in a subfolder of the application directory, as the system temp folder can sometimes be emptied by the OS at the wrong time). Returns a path to this temporary file.

Note that the temporary file will continue to exist until manually deleted. So functions that create a diff should delete it (using the supplied function) when they're done with the file.

When diffs are created, the should show the changes from the old file (in the backup folder) to the new file (in the live folder).

### Applying a diff ###
Applying diffs work the same way, using the Java-diff-tools library. They take in a path to the diff file (which should be a temporary file) and the file to apply the diff to. After applying the diff, it returns another path to a temporary file and deletes the temporary diff file that was passed into the function as a parameter.

We return another path to a temporary file because it is often necessary to revert changes in a chain (more on that later). Also note that we deleted the temporary file that was passed as a parameter, to remove the need to delete it manually.

Diffs must be applied backwards. Since the diff shows the changes from the old file to the new one, we need to reverse apply the diff to revert to an older version.

### Rename a file ###
Simply takes in an old path and a new name. Finds the file that matches the old path and renames it to the new name. Note that the old path includes the full path and name, while the new name is JUST the file name (no path). This ensures renaming always changes just the file name.

The function first checks if a file of the new name already exists in the directory that the old file is in. If so, we delete that first. Finally, the function renames the old file to the new name.

It may also be necessary to use a different technique to rename folders. If that is the case, the path should be checked with FileOp to check if it's a folder.

### Delete a file ###
Simplifies the deletion of a file by taking in a path and deleting the file located at that path.

### Get file size ###
Simply takes in a path and returns the size of the file.

### List-ify a file ###
The Java-diff-util library uses lists of strings. This method simply reads in a file and converts it into a list of strings (which is returned).

### Check file validity ###
Large files and binary files are not versioned. This function returns true if the file is valid (a small text file) and false otherwise (binary or large files).

To determine if a file is a text file, use [probeContentType](http://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html#probeContentType%28java.nio.file.Path%29) to get the MIME type. Then use string functions to check if the first word of the MIME type is "text". We should probably also look for "multipart/mixed" and "multipart/alternative", since these are usually at least part text and should diff fine.

Finally, check that the file isn't too large. At the time, it's unknown how large is "too large", so let's use the arbitrary number of 5 megabytes. Testing is necessary to figure out just how much is really "too large".

### Check if a "file" is a folder ###
JNotify detects not just file changes, but folder changes (creation, renaming, and deletion). Because folders have to be handled differently by the Control, it is necessary to be able to detect if a path is a folder. This returns true if a given path is a folder and false if it's a regular file.

## FileHistory ##
Manages the revision history of files.

### Get a specific revision ###
Takes in a file name and a time stamp, and copies the diff of that time stamp into a temporary file, returning the path to that diff.

### Store a new revision ###
Takes in a path to a temporary diff file, a path to the actual file, file size of the old file, and the change in the file size ("delta"). Uses the DbManager functions to store the diff, path, time stamp (of the current time), and delta in the database.

### Obtaining a specific revision ###
Takes in a path to a file in the backup directory and the time stamp of the desired revision. The file is copied into a temporary file. Then uses the DbManager to get the database entries up to (and including) the desired revision. With each entry, the FileOp component is used to apply the diff. The new temporary file created by FileOp is then used to apply the next diff, all the way until we get to the diff with the time stamp desired.

Returns a path to a temporary file with the contents of the specified revision.

### Rename revision ###
Takes in two paths: the old name and new name. Searches for a specific path (the old name) and replaces occurrences with the new name in the database.

## DbManager ##
Simplifies working with the database by providing a series of functions to access and modify the database. There should be only one at a time,, and static. It's essentially a wrapper class for accessing the database. Returns the lists created from the result set output by the database driver for select functions. **ALL** DbManager functions _must_ set a lock before doing anything, and check the value of the lock before trying to do anything. If the lock is still active, wait a milisecond (?). Be sure to remove the lock when the function ends _or_ if an exception occurs!

### Create database and tables ###
This is used for the first time the program is run, as the database must be created and the table structure defined. The tables should be empty. If the tables cannot be created for some reason, throw an exception: there's no getting this program to work.

### Get revision data ###
Takes in a path and runs a simple select operation to get all rows that contain that path (in other words, all revisions of that file). The result set of rows is copied into a list of RevisionInfo objects.

### Get a specific revision ###
Takes in a path and a time stamp, and runs a select operation that get the row that contain the path and have the matching time stamp.

### Insert a revision ###
Takes in a path (to the file that the revision belongs to), a string containing the diff content, and the difference in bytes the diff introduced. The time stamp should be then calculated (seconds since unix epoch) and the row should be stored in the table with an insert operation.

### Change revision names ###
Takes in a revision path for the new name and a path for the old name. Searches for rows that match the old name and updates the rows to have the new name as the path.

### Get configuration setting ###
Takes in a string corresponding to the setting name. Searches the configuration table for that name, and returns the string value. Throw an exception if there is no such row.

### Set configuration setting ###
Takes in a string corresponding to the setting name and another to its value. Checks if the row with the corresponding setting already exist. If so, update that row. If not, insert a new row.

## Data ##
The Data component queries the DbManager to get the info that the GUI needs.

### Get folder contents ###
Takes in a path and returns a list of FileInfo objects (which is yet another plain old data class that groups information). The FileInfo objects store: folder (a boolean value that is true if the item is a folder and false if it's a regular file), fileSize (in bytes), lastAccessedDate, lastModifiedDate, createdDate, numberOfRevisions, revisionSizes, and fileName.

It actually takes quite a bit of work to calculate the information such as the number of revisions and revision size, which require cycling through all revisions of a file.

The list should be ordered with all folders listed alphabetically first, followed by all regular files listed alphabetically.

Note that folders do not have a file size or revisions.

### Get revision info ###
Similar to the above section, it takes in a path and copies the result set from the database query into a list of RevisionInfo objects.

## FrontEnd ##
The FrontEnd is the GUI of the program. It performs no computation and depends on the Data component to get the information to display and the Control component to perform operations for it.

The FrontEnd is used to display the contents of the backup directory. It can never leave the backup directory, but can descend into sub-directories.

It should use the Data component to get the contents of the current working directory and display these contents appropriately. The FrontEnd should display files with all provided information. Folders should not display anything for the file size or revision information. The FrontEnd should store which entries are folders. Double clicking a folder "goes into" the folder, making that the working directory and getting the folder contents from the Data component.

Double clicking a file, on the other hand, opens up a window which displays the available revisions (also obtained from the Data component), if any. On this window, there should be four buttons: view revision, view changes, revert to revision, and recover most recent all which call the corresponding Control functions.

On the main window, the top of the window has a status bar. The file menu will have options that are currently in limbo (such as reverting all files to a certain time, cleaning out older revisions, etc). In the mean time, it should provide a "View revisions" option (same as double clicking a file), a copy-to option, a restore all option, and an exit option.

The restore-all option should first prompt the user for confirmation. After confirmation, uses the Control to copy the entire backup directory to the live directory, overwriting when necessary.

The copy-to option open a save dialogue for the current selected file, allowing the user to save a copy of the file in another location. The default folder should be the live directory. The source file and destination folder are then passed to Control to perform the copy. Note that both copy-to and view revisions need a file to be selected, and should be greyed out if a file is not selected (including when a folder is selected).

The help menu will have an option for opening the web documentation (to-do in the future).

Also on the main window should be two buttons: up a folder and refresh. Up a folder simply changes the working directory to the previous folder and reloads the folder contents. It should be greyed out when in the backup directory (since we cannot leave that directory). The refresh button simply re-fetches the folder contents (in case the contents have changed).

Beside those buttons is a text field containing the current path. It should display the path relative to the backup directory. For now, the text field should be disabled, so the program can change its contents (and should update that whenever we go up a folder or go into a folder) without the user being able to. In the future, we may allow this to be edited so the user can type a path manually.

The GUI should not cause the program to terminate when the window is closed. Rather, closing the window should create an icon in the system tray. Clicking this icon should recreate the GUI window.

The exit option in the file menu, however, should bring up a prompt asking the user if they're sure they want to close the program, and warning them that closing the program will prevent changes from being backed up. It should have three buttons: "Close the program" (commits suicide), "Minimize the program to the taskbar" (just closes the JFrame), and "Cancel" (does nothing).

### Time parsing ###
In the database, time stamps are stored in unix time: integer number of seconds since the unix epoch (1970-01-01 00:00:00 UTC). For ease of use, when displaying time and date values to the user, they should be displayed in [ISO 8601 format](http://en.wikipedia.org/wiki/ISO_8601). For example, `1:51:11 PM on 24 September 2013` would be displayed as `2013-09-24 13:51:11`.

Displaying the date in this format ensures that it is possible to sort the rows by date in the GUI. Care should be taken that all single digit numbers are padded with a zero (for example, September is `09`, not simply `9`).

### File size parsing ###
Likewise, file sizes (and the "delta", or difference in file sizes) are stored in bytes, which are not very human readable for larger files. Thus, for display in the GUI, all file sizes should be converted into a human readable format with units such as KB (kilobytes). In this context, prefixes refer to powers of 2. For example, a KB is 2<sup>10 (1024), not 10</sup>3 (1000). The conversions should be as follows:

  * For numbers below 1024 bytes, just display the number of bytes.
  * For numbers between 1 KB and 1024 KB, display the number of kilobytes.
  * For numbers between 1 MB and 1 GB, display the number of megabytes.
  * For numbers greater than or equal to 1 GB, just display the number of gigabytes (it's unknown if the program would even be able to handle files larger than a few gigabytes).

When division takes place, round to 2 digits. So a file that has 5000 bytes has 5000 / 1024 = 4.8828125 KB, which rounds to 4.88 KB.

## The database ##
The database has two tables, one for the revisions and one for the settings (configuration).

The revisions table contains columns for:
  * **ID**: [INTEGER](INTEGER.md) Incremented automatically by database. Serves as a means of providing uniqueness to each row.
  * **Path**: [TEXT](TEXT.md) The _full_ path to the file (due to the way file systems work, guaranteed to be unique).
  * **Diff**: [BLOB](BLOB.md) The contents of the diff file.
  * **Delta**: [INTEGER](INTEGER.md) The byte difference that the diff introduced.
  * **Time**: [INTEGER](INTEGER.md) The date as stored in Unix time (seconds since 1970-01-01 00:00:00 UTC)

The configuration table contains columns for:
  * **Name**: [TEXT](TEXT.md) The name of the setting (must be unique).
  * **Setting**: [TEXT](TEXT.md) The value for the setting.

For the time being, the settings are constrained to the backup and live directory paths. They can be expanded to other settings in the future.

## First time run ##
The first time the program is run, we need an alternative startup. There must be some way to specify the backup and live directories. The easiest way to do this is to create the database file _after_ the program has been setup. Thus, if there is no database file in the program's folder, it must be the first run.

If that is indeed the case, an alternative portion of the program should be run: create a simple GUI for obtaining the backup and live directory. Once entered, validate their paths and ensure that we can read from the live directory and can write to the backup directory.

If all is good, create the database file, initialize the settings, and then restart the program (which will cause the program to start as though it were not the first time).

The regular portions of the program should not run during this setup stage.

## On normal startups ##
Load the backup and live directory paths into static, global, constants. Initialize the logger and log levels.

Create the threaded loop that will check for changes in the watched array lists.

Create the system tray icon that will draw the GUI when clicked.

## Use cases ##
### User edits a (text) file ###
  * Watcher notices the file has been modified and places it in the modified files list.
  * Control, on its interval, notices the modified files list is not empty.
  * Control notices the file is a small text file by using FileOp.
  * Control makes a diff using FileOp.
  * Control tells FileHistory to insert the diff into the revision table.
  * FileHistory tells DbManager to insert a row with the revision information.
  * Control copies the file to the backup using FileOp.
  * Control empties the modified files array list for the next iteration before sleeping its thread for a short period of time (when the next iteration begins).

### User edits a (binary) file ###
  * Watcher notices the file has been modified and places it in the modified files list.
  * Control, on its interval, notices the modified files list is not empty.
  * Control notices the file is not a small text file by using FileOp
  * Control copies the file to the backup using FileOp.
  * Control empties the modified files array list for the next iteration before sleeping its thread for a short period of time (when the next iteration begins).

### User renames a file ###
  * Watcher notices the file has been renamed and places it in the renamed files list.
  * Control, on its interval, notices the renamed files list is not empty.
  * Control tells FileHistory to rename the revisions of the renamed file.
  * FileHistory tells DbManager to update the rows for new names.
  * Control empties the renamed files array list for the next iteration before sleeping its thread for a short period of time (when the next iteration begins).

### User wants to revert a file ###
  * User opens FrontEnd by clicking the program's icon in the system tray.
  * FrontEnd populates the window with information about folders and files in the backup directory, which it obtained from the Data component.
  * The user chooses the file that they want, which causes revision window to open.
  * The FrontEnd populates this window with information about revisions received from the Data component.
  * The user chooses the revision they want and click the "revert" button.
  * FrontEnd tells Control the file name and time stamp.
  * The Control tells the FileHistory component it wants the file as it was at the specified revision
  * FileHistory reverts the files in reverse order (using the diffs stored in the database) until it has the desired revision.
  * The Control tells the FileOp component to make a diff of this revision (versus the version in the backup directory).
  * The Control assigns this diff to the FileHistory component, which stores it as a revision.
  * The Control tells the FileOp component to copy the file to the live and backup directories.
  * The Control removes the temporary file that was created by FileHistory in the process.

### User wants to recover a file ###
  * User opens FrontEnd by clicking the program's icon in the system tray.
  * FrontEnd populates the window with information about folders and files in the backup directory, which it obtained from the Data component.
  * The user chooses the file that they want, which causes revision window to open.
  * The FrontEnd populates this window with information about revisions received from the Data component.
  * The user chooses the revision they want and click the "recover most recent" button.
  * FrontEnd tells Control the file name.
  * Control copies file from backup directory into live directory.

### User wants to restore the entire backup ###
  * User opens FrontEnd by clicking the program's icon in the system tray.
  * FrontEnd populates the window with information about folders and files in the backup directory, which it obtained from the Data component.
  * The user goes to the file menu of the menu bar and choses the "restore backup" option.
  * FrontEnd opens prompt to ask the user for confirmation.
  * If the user confirmed the action, the FrontEnd tells the Control to restore the backup.
  * Control iterates through all files in the backup folder, using FileOp to copy each one to appropriate subfolder(s) in the live directory (FileOp's copy automatically creates folders as necessary).

## Notes ##
  * The time to sleep on an interval has not been determined, but since SQLite stores time with an accuracy of a second, we should probably sleep for at least one second. Testing will hopefully find an optimal value.
  * It remains to be seen how effective the use of diff files will be on binary files. It may be necessary to store the entire file instead of a diff. If that is the case, we will need to modify the revisions table (or use a separate table for binary files). The change detection would also have to be modified to take into account whether or not the file is a text file.
  * The program has the limitation in that JNotify only noticed changes while the program is running. It will not notice changes that were made when the program was not running. To counter this, we should iterate through all files in the live directory, comparing their CRCs with the backups, and making revisions when necessary. This only has to be done each time the program is started. For the mean time, this is a reasonable limitation.
  * The program currently does not facilitate changing the live or backup directories. Changing either directory would be best done with the presumption that the old backup will no longer match the correct live directory. Thus, we could simply delete the database file to make the program think the program is on its first run.
  * All components should make heavy use of the logger (using the [log4j library](https://logging.apache.org/log4j/1.2/manual.html)). All log messages should have appropriate levels set (eg, info, warning, error) so that we can easily toggle between the level of verbosity we need (presumably the final program will only log errors and maybe warnings).
  * This is not a concrete guide. For example, feel free to implement helper functions where you feel necessary. Changes to the input or output of a function, however, need to be discussed. What's important is that the function works, not how it works.

## See also ##
  * [Early Project Proposal](https://code.google.com/p/fbms/wiki/EarlyProjectProposal)
  * [Final Project Proposal](https://code.google.com/p/fbms/wiki/FinalProjectProposal)