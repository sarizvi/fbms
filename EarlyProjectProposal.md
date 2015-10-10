# File Backup and Management System #

  * Mike Hoffert - mlh374
  * Syed Ahsan Rizvi - sar457
  * Hattan Alsharif - haa775
  * Da Tao - dat293
  * Michael Butler - mdb815

## Project Description ##
Version control systems are powerful tools, but often outside of the grasp and interest of casual users. The File Backup and Management System, or FBMS, is a hybrid backup and versioning control system that's low maintenance, easy to use, and ensures the user's files are safe and sound. Akin to programs like Dropbox and Google Drive, FBMS runs in the background, automatically backing up the user's files. Unlike those web services, FBMS runs entirely locally, making it useful where internet access is sporadic or bandwidth is limited. FBMS also does not require subscriptions to a web service, being instead limited by your own hard drive space.

Unlike Google Drive and Dropbox, FBMS allows users to keep old file revisions for as long as you want. Revisions are as compact as can be, being stored as incremental diff files. Users can, at their discretion, specify limits to how long or how many older revisions they want to keep.

So easy a liberal arts major could use it!

## Work Unit Decomposition ##
We decomposed FBMS into the following components.

  1. **Graphical user interface**
    * **Assigned to**: ?
    * **Work Unit Goal**: This component creates the interface that the user can use to browse their backup folder, allowing them to restore files or revert to older versions. It is a display module only, and retrieves all data from other modules.
    * **Users**: Standard user
    * **Use Cases**:
      * User wants to revert a file to an older version
      * User wants to recover a deleted file
      * User wants to view how much storage their backup is using
  1. **Database module**
    * **Assigned to**: ?
    * **Work Unit Goal**: This component works as a wrapper for database functions. It includes methods for retrieving and saving specified data from the database.
    * **Users**: System
    * **Use Cases**:
      * All
  1. **File operation module**
    * **Assigned to**: ?
    * **Work Unit Goal**: This component is used as a wrapper for file system operations, such as copying files, and applying and creating diffs.
    * **Users**: System
    * **Use Cases**:
      * User modifies a file
      * User renames a file
      * User wants to revert a file to an older version
      * User accidentally deleted a file in their working folder and wants to recover it
  1. **File history module**
    * **Assigned to**: ?
    * **Work Unit Goal**: This component retrieves data pertaining to the diffs which make up the file revision history by interacting with the database module.
    * **Users**: System
    * **Use Cases**:
      * User modifies a file
      * User renames a file
      * User wants to revert a file to an older version
      * User wants to view how much storage their backup is using
  1. **Control module**
    * **Assigned to**: ?
    * **Work Unit Goal**: This component controls the various service modules which make up the backend of the program. It receives changes from the watching module and sends appropriate instructions to the file history and file operation modules.
    * **Users**: System
    * **Use Cases**:
      * All
  1. **Watching module**
    * **Assigned to**: ?
    * **Work Unit Goal**: This component watches the live folder for changes and alerts the control module of such changes.
    * **Users**: System
    * **Use Cases**:
      * User modified a file
      * User renames a file
  1. **Data module**
    * **Assigned to**: ?
    * **Work Unit Goal**: Due to the fact that the backend must run concurrently with the GUI, the data module is used to retrieve information that the GUI model needs, such as the files in a folder, data about these files, and the file history. It acts as a data provider and retrieves data from database module.
    * **Users**: User
    * **Use Cases**:
      * User wants to revert a file to an older version
      * User wants to recover a deleted file
      * User wants to view how much storage their backup is using

## Use cases ##
  * **User wants to revert a file to an older version**: User chooses the file in the GUI. GUI tells control module to revert the specified file to the specified diff. Control module gets diffs from file history module and tells the file operation module to use the diffs to revert the file.
  * **User wants to recover a deleted file**: User chooses the file in the GUI. GUI tells control module to restore the specified file. Control module tells file operation module to copy file into live folder.
  * **User wants to view how much storage their backup is using**: User chooses option in the GUI. GUI tells data module to calculate storage space. Data module retrieves information from database and calculate the size, and return it to GUI.
  * **User modifies a file**: Watching module detects change. Control module notices the watching module's alert and tells the file operation module to calculate a diff if an older version exists in the backup folder. Control module then tells file history module to add the diff to the history. Control module then tells file operation module to copy the file into the backup directory.
  * **User renames a file**: Watching module detects change. Control module notices the watching module's alert and tells the file operation module to rename the file in the backup directory. Control module then tells the file history module to rename the older revision's names (so they belong to the same, renamed file).