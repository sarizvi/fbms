# Milestone 4: Updated Design and Near-Complete Implementation #
  * Mike Hoffert - mlh374
  * Syed Ahsan Rizvi - sar457
  * Hattan Alsharif - haa775
  * Da Tao - dat293
  * Michael Butler - mdb815

## 1 - System operations ##
  * Startup.resolveBackupDirectory()
  * DbManager.setConfig()
  * FileOp.createPatch()
  * FileOp.applyPatch()
  * GuiController.revertRevision()
  * GuiController.viewRevision()
  * GuiController.changeLiveDirectory()
  * GuiController.changeBackupDirectory()
  * FileChangeHandlers.handleRenamedFiles()
  * FileChangeHandlers.handleCreatedFiles()
  * FileChangeHandlers.handleModifiedFiles()
  * FileChangeHandlers.handleDeletedFiles()

## 2 - Sequence diagrams of major system operations ##
### FileChangeHandlers.handleModifiedFiles()<sup>(Buttler)</sup> ###
![https://fbms.googlecode.com/svn-history/trunk/doc/uml/handleModifiedFiles.png](https://fbms.googlecode.com/svn-history/trunk/doc/uml/handleModifiedFiles.png)

### FileChangeHandlers.handleRenamedFiles()<sup>(Hoffert)</sup> ###
![https://fbms.googlecode.com/svn-history/r357/trunk/doc/uml/fileChangeHandlerHandleRenamedFiles.png](https://fbms.googlecode.com/svn-history/r357/trunk/doc/uml/fileChangeHandlerHandleRenamedFiles.png)

### FileChangeHandlers.handleCreatedFiles()<sup>(Tao)</sup> ###
![https://fbms.googlecode.com/svn-history/r353/trunk/doc/uml/handleCreatedFiles.png](https://fbms.googlecode.com/svn-history/r353/trunk/doc/uml/handleCreatedFiles.png)

### FileChangeHandlers.handleDeletedFiles()<sup>(Alsharif)</sup> ###
![http://i.imgur.com/IFQ8MuX.png](http://i.imgur.com/IFQ8MuX.png)

### GuiController.revertRevision()<sup>(Rizvi)</sup> ###
![https://fbms.googlecode.com/svn/trunk/doc/uml/revertRevision.png](https://fbms.googlecode.com/svn/trunk/doc/uml/revertRevision.png)

## 3 - Class diagram ##
The class diagram is located in the included `class_diagram.png` file.

Used GRASP patterns include:

  * The use of a `GuiController` class as a controller to handle system functionality for the front end.
  * The `FileChangeHandlers` class also acts as a controller as it handles System events which are then delegated to other classes.
  * The `FileHistory` class performs gets and sets the file history (its revisions), acting in a high cohesion way: it's focused on dealing with file history and leaves everything else to the other classes (in particular, it uses `FileOp` to perform operations such as creating the patch files that are the heart of revisions).
  * The `FileOp` class is very low coupling. With the exception of getting the live and backup directory from `Main` (which most of the classes do), `FileOp` has no dependencies and works on its own as a low end collection of utility methods
  * Most of the classes like `FileChangeHandlers`, `GuiController`, and `DataRetriever` make use of the principle of polymorphism as they operate on interfaces, encapsulating the internal implementation of the objects.
  * `DbManager` is a class that provides high cohesion for database access while having low coupling to the rest of our program. By consolidating those functions interacting with the database into one class we avoid unnecessary coupling to the database itself while providing robust SQL interaction.

## 4 - Implementation ##
### Instruction manual ###
FBMS, or the File Backup and Management System, works akin to an offline Dropbox or Google Drive. When the program is started for the first time, a wizard will guide you through selecting your live and backup directories.

![http://i.imgur.com/lkS8hL5.png](http://i.imgur.com/lkS8hL5.png)

  * **Live directory**: The folder that you want to keep backed up and revisioned.
  * **Backup directory**: The location to store the backup. Don't use an existing folder, or you risk losing your files.

You can opt to start from scratch (specifying a backup and live directory), or import an existing backup directory (which will determine the live directory from previously saved settings).

![http://i.imgur.com/QZ2jGjR.png](http://i.imgur.com/QZ2jGjR.png)

![http://i.imgur.com/eCAmmkX.png](http://i.imgur.com/eCAmmkX.png)

Once folders are chosen and the wizard is completed, the program will run quietly in the background. An icon will be created on the system tray. Double clicking this icon will open the program's interface.

![http://i.imgur.com/ATSvU6S.png](http://i.imgur.com/ATSvU6S.png)

![http://i.imgur.com/q9oumYv.png](http://i.imgur.com/q9oumYv.png)

The interface shows a special file browser, which displays the contents of the backup directory along with information such as the number of revisions stored. This file browser can be navigated with the keyboard or a mouse, and has functionality similar to other file browsers, such as Windows Explorer or Dolphin. It is, however, a specialized browser intended only to be able to restore files and view revisions.

![http://i.imgur.com/T383die.png](http://i.imgur.com/T383die.png)

Double clicking a file will open the revisions dialog, displaying a list of past revisions (if there are any). These revisions can be viewed (which opens the revision in your default program) or reverted (which sets that file in your live directory to the specified revision). You can also view the changes introduced by a revision, which will open in your default web browser.

The file browser is also able to:

  * Copy the selected file to a chosen folder
  * Restore all files
  * Change the live or backup directory

FBMS is also able to detect when files have changed since the last time you ran the program. When starting the program up, FBMS will perform a one time scan for file changes. This scan is slow, and can be disabled in the settings if FBMS is always running.

The settings dialog is accessed under the File menu of the GUI. It provides a few choices such as:

  * Whether or not to display non-fatal error messages (which don't stop the program from running, but notify the user of issues).
  * Whether or not to run the previously mentioned first run scan
  * Whether or not to enable the trim feature, which removes revisions older than a specified date.

### Meetings ###
| **Date** | **Attendance** | **Discussion** |
|:---------|:---------------|:---------------|
| 29 October | Mike, Da       | Discussed progress of project and others' work; Handling of binary files |
| 2 November | Mike, Ahsen    | Optional meeting for implementation; GUI design; GUI file browser table; Taskbar icons |
| 4 November | Mike, Ahsen, Hattan | Personal meeting for development of GUI and file diff system |
| 5 November | All            | Minor implementation; testing; GUI revision table; Obtaining specific revisions; Handling file changes; Settings dialog |
| 9 November | Mike, Da, Ahsen | Optional meeting for implementation; GUI wrap-up; bug handling; testing; Milestone 5 plans; Milestone 4 review; Non-backwards compatible changes; Logging |
| 12 November | Mike, Da, Ahsen | Optional meeting for milestone 5; testing results |

As with before, scheduling meetings is very difficult due to the very full and very different schedules of the group members. We had optional integration meetups on Saturdays in Spinks, which were mostly a way to get everyone coding with easy access to their group members.

The majority of communication stems from email contact. We've exchanged well over 100 emails since.

### SVN log ###
The SVN log is located in the included file, `svn_log.txt`.

## 5 - Project plan listing ##
### Spent time ###
<pre>
Mike:<br>
- Planning and documentation:   5 hours<br>
- Milestones:                  14 hours<br>
- Utility demo:                 8 hours<br>
- Skeleton implemention and<br>
PoD classes:                  2 hours<br>
- Watcher class:                1 hour<br>
- DbManager.init():             3 hours<br>
- FirstRunWizard class:         6 hours<br>
- Data.getFolderContents():     2 hours<br>
- Various Control code related<br>
to startup:                   5 hours<br>
- JUnit testing code:           4 hours<br>
- Errors class:                 3 hours<br>
- MainFrame table:              5 hours<br>
- MainMenu functionality:       2 hours<br>
- MainMenu functionality:       2 hours<br>
- MainToolBar functionality:    1 hour<br>
- RevisionDialog table:         2 hours<br>
- SettingsDialog:               3 hours<br>
- Redid FileOp.createPatch and<br>
FileOp.applyPatch:            2 hours<br>
- Data.getTableData and<br>
Data.getRevisionData:         3 hours<br>
- Data number formatting:       1 hour<br>
- Trim database feature:        3 hours<br>
- FileOp utility functions:     2 hours<br>
- Miscellaneous bug fixes:      2 hours<br>
Total: 81 hours<br>
Da:<br>
- Rough design of modules:      1 hour<br>
- Rough design of functions:    1 hour<br>
- Work on previous milestones:  4 hours<br>
- Work on FileOp:               7 hours<br>
- Milestone 4(text):            5 hours<br>
- Milestone 4(graph):           2 hours<br>
- Test case:                    2 hours<br>
- Work on FileHistory:          6 hours<br>
- Miscellaneous bug fixes:      2 hours<br>
- Redo operation contracts:     1 hour<br>
- Build.xml:                    4 hours<br>
- Milestone 5(graph):           5 hours<br>
Total:  39 hours<br>
Ahsen:<br>
-Milestone 4 (documentation):   5 hours<br>
-Frontend implementation:       4 hours<br>
-GUI Brain storm(in total):     7 hours<br>
-GUI implementation:           10 hours<br>
-Testing:                       3 hours<br>
-Milestones:                    9 hours<br>
Total: 38 hours<br>
Hattan:<br>
- Milestone 4:                  9 hours<br>
- Work on Presentation slides:  3 hours<br>
- FileOp.creatDiff:             4 hours<br>
- FileOp.fileToList:            4 hours<br>
- Test FileOp.creatDiff:        2 hours<br>
- Test FileOp.fileToList:       3 hours<br>
- FileOp.applayDiff:            6 hours<br>
- Milestone 5:                  4 hours<br>
- Instruction manual pics:      2 hours<br>
<br>
Total:  37 hours<br>
Michael:<br>
- Milestones:                   8 hours<br>
- getConfig():                  3 hours<br>
- setConfig():                  3 hours<br>
- handleCreatedFiles():         6 hours<br>
- handleDeletedFiles():         2 hours<br>
- handleModifiedFiles():        4 hours<br>
- handleRenamedFiles():         3 hours<br>
- testing(general):             4 hours<br>
- debugging/optimization():     4 hours<br>
<br>
Total:  37 hours<br>
</pre>

### Future assignments ###
At this point of time, the program is largely completed in terms of functionality. There is much testing, quality assurance, and bug fixing to do, however.

We've tagged a stable version as version 1.0. We're currently working on improving MIME type detection, revisioning of binary files, and making the system more object orientated.