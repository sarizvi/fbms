# This is for running the program after ant has already built it with
# "ant compile". See run.bat for the Windows version.
cd build
# Extend the classpath as necessary. Note that Linux separates paths with a
# colon, different from Windows, which uses a semicolon
java -classpath ../lib/jnotify-0.94.jar:../lib/sqlite-jdbc-3.7.15-M1.jar:. DemoMain
cd ../