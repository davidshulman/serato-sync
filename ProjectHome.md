

# Versions / History #
  * **0.2 (the latest)**, released on May ??, 2011. Download: http://serato-sync.googlecode.com/files/serato-sync-0_2.jar
    * the tool is renamed to "serato sync"
    * ability to sync multiple serato databases (i.e. sync music on internal drive with serato db on internal, and sync music on external drive with serato db on external). useful when you use multiple drives
    * flexible action-based configuration, allowing to specify which folders should be mapped to which crates
    * ability to exclude certain folders from the sync process
    * folder renames are handled properly
    * ability to backup Serato database
    * simplified configuration, Serato database path auto-detection
    * improved GUI
    * loads configuration files that end with ".txt" as well, to help Windows users
    * crate file parser rewritten from scratch, should be bullet-proof now
    * tested with Serato Scratch Live 2.2.0 and Serato ITCH 1.7.1
    * tested on Windows XP and Mac OS X 10.6.7

# Introduction #
serato-sync is a helpful utility which allows to map your music collection to Serato library. It's an improved version of http://code.google.com/p/serato-itch-sync/

It is very easy to use - you just need to specify the location of your media library, and the tool will map your directory structure to crates and put all tracks inside -- so at the end you will end up with having one-to-one hierarchical mapping, one crate (or subcrate) for each directory with your music.

If you want, you can also specify more advanced folder-to-crate mappings.

# Why the tool was created and why should I use it? #
Well, I personally don't let iTunes to organize and manage my music library. Just because I want a different layout of my music on the file system. Serato doesn't really support "sync" with the file system, so you have to create the initial crate structure manually. And even worse - once you download and put your new media files into the existing directories, Serato will not pick up your new tracks automatically and you will have to manually add them to Serato.

# How do I run it ? #

## Installation ##
The installation process is very simple. You download the latest version of the program and put it into any directory/folder on your computer. E.g. you can create a folder called "serato-sync" on your desktop.

## Configuration ##
Download the rule file template which better fits your needs and put it into the same directory where the program is:
  * Mac OS, external drive
  * Mac OS, internal drive
  * Windows, external drive
  * Windows, internal drive

Double click your downloaded rule file. When asked about the program to open, choose "Notepad" for Windows and "Text Edit" for Mac OS. Edit the file, replace the path to your music library with your own path, save it and close the file.

If you are creating the rule file manually, make sure to:
  * make sure to use forward slash in the library paths
  * for Mac OS users: the easiest way to create a configuration file is to open a "Text Edit", go to "Format" menu, select "Make Plain Text", then enter the contents, and finally "File" and "Save As" giving it "`<your-rule-file>.rules`" name.
  * the Windows users: the easiest way to create a configuration file is to open "Notepad", enter the contents, and then "File" and "Save As" giving it "`<your-rule-file>.rules`"  name. Make sure you saving the file as type "All﻿ Files", so that "Notepad" doesn't add ".txt" extension to the file name

## Execution ##
Double click the "serato-sync-(version).jar" file. It will automatically read and execute all your configuration files that are located in the same directory and have ".rules" extension

# Screenshots #
## Before sync ##
![http://serato-itch-sync.googlecode.com/svn/trunk/images/01-before.png](http://serato-itch-sync.googlecode.com/svn/trunk/images/01-before.png)

## Running the tool ##
![http://serato-itch-sync.googlecode.com/svn/trunk/images/02-run.png](http://serato-itch-sync.googlecode.com/svn/trunk/images/02-run.png)

## Original music collection ##
![http://serato-itch-sync.googlecode.com/svn/trunk/images/03-files.png](http://serato-itch-sync.googlecode.com/svn/trunk/images/03-files.png)

## Imported into Serato ITCH ##
![http://serato-itch-sync.googlecode.com/svn/trunk/images/04-after.png](http://serato-itch-sync.googlecode.com/svn/trunk/images/04-after.png)

**Warning:** please keep Serato ITCH closed before running the tool

# FAQ #

## I don't have Java installed on my computer. Will the tool work? ##
No. You need to have Java >= 1.5 installed on your computer to run the tool.

## What versions of Serato Scratch Live and Serato ITCH are supported? ##
Tested with Serato Scratch LIVE 2.2.0 and Serato ITCH 1.7

## Will the tool delete any data from my Serato? ##
No. The tool works only with crates/subcrates and tracks within them. So, Serato settings (e.g. global settings, play history, track color coding, id3 tags, beat grids, etc) are preserved. The tool modifies the following files/directories in Serato database:
  * "database V2" file - all tracks view
  * "Crates" and "Subcrates" directories - individual crates and subcrates
  * "neworder.pref" file - crate sorting

# Community & Support #
Don't hesitate to report bugs and enhancements using the "Issues" tab. I will try to address them if/when I have time.