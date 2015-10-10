## First run ##

When the program is started for the first time, a wizard will guide you through selecting your live and backup directories.
![https://fbms.googlecode.com/svn/trunk/doc/milestone_6/images/1-firstRunIntro.png](https://fbms.googlecode.com/svn/trunk/doc/milestone_6/images/1-firstRunIntro.png)

You can opt to start from scratch (specifying a backup and live directory), or import an existing backup directory (which will determine the live directory from previously saved settings).

  * **Live directory**: The folder that you want to keep backed up and revisioned.
  * **Backup directory**: The location to store the backup. Don’t use an existing folder, or you risk losing your files.

![https://fbms.googlecode.com/svn/trunk/doc/milestone_6/images/2-newBackup.png](https://fbms.googlecode.com/svn/trunk/doc/milestone_6/images/2-newBackup.png)

![https://fbms.googlecode.com/svn/trunk/doc/milestone_6/images/3-oldBackup.png](https://fbms.googlecode.com/svn/trunk/doc/milestone_6/images/3-oldBackup.png)

Once folders are chosen and the wizard is completed, the program will run quietly in the background. An icon will be created on the system tray. Double clicking this icon will open the program’s interface.

![https://fbms.googlecode.com/svn/trunk/doc/milestone_6/images/4-systemTray.png](https://fbms.googlecode.com/svn/trunk/doc/milestone_6/images/4-systemTray.png)

The interface shows a special file browser, which displays the contents of the backup directory along with information such as the number of revisions stored. This file browser can be navigated with the keyboard or a mouse, and has functionality similar to other file browsers, such as Windows Explorer or Dolphin. It is, however, a specialized browser
intended only to be able to restore files and view revisions.

![https://fbms.googlecode.com/svn/trunk/doc/milestone_6/images/5-mainWindow.png](https://fbms.googlecode.com/svn/trunk/doc/milestone_6/images/5-mainWindow.png)

Double clicking a file will open the revisions dialog, displaying a list of past revisions (if there are any). These revisions can be viewed (which opens the revision in your default program) or reverted (which sets that file in your live directory to the specified revision).
You can also view the changes introduced by a revision, which will open in your default web browser.

![https://fbms.googlecode.com/svn/trunk/doc/milestone_6/images/6-revisionDialog.png](https://fbms.googlecode.com/svn/trunk/doc/milestone_6/images/6-revisionDialog.png)

The file browser is also able to:

  * Copy the selected file to a chosen folder: From the “File” menu, choose “copy-to” while selecting a file. This will open a prompt for choosing the location you wish to copy the selected file to.

  * Restore all files: From the “File” menu, choose “restore all”. This will open a prompt for choosing the location you wish to restore all the files to.

  * Change the live or backup directory: From the “File” menu, choose either “Change live directory” or ‘Change backup directory”. This will open a prompt for choosing the directory. The live and backup  directories cannot be children of each other.

FBMS is also able to detect when files have changed since the last time you ran the program. When starting the program up, FBMS will perform a one time scan for file changes. This scan is slow, and can be disabled in the settings if FBMS is always running.

The settings dialog is accessed under the File menu of the GUI. It provides a few choices such as:

  * Whether or not to display non-fatal error messages (which don’t stop the program from running, but notify the user of issues).

  * Whether or not to run the previously mentioned first run scan

  * Whether or not to enable the trim feature, which removes revisions older than a specified date.

In terms of file operations, there’s several operations that can occur:

  * A file is created: if the file doesn’t already exist, it is merely backed up. If the file does exist, this is treated like a modification.

  * A file is modified: if the file already exists, it is backed up and revisioned. If it doesn’t already exist, this is treated like a creation.

  * A file is renamed: if the old name already exists, that is renamed, otherwise this is treated like a creation (under the new name). If the new name doesn’t already exist, we rename the file. If the new name already exists, we treat this like a modification, backing up and revisioning the file. The current behavior of “renaming” a file is to make a copy and move the revisions to the new name. To allow the old name to be restored, it is not deleted. Therefore, we end up with copies (this will be improved in future versions of the software, which will keep track of name history).

  * A file is deleted: The file is not removed from the backup directory, but files that no longer exist in the live directory will be marked with an exclamation mark in the file browser.