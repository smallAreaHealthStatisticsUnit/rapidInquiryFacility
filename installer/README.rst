==========================================
 Installer for the Rapid Inquiry Facility
==========================================

These instructions are about how to build the RIF's installer functions. For instructions on how to install the RIF itself, see the Installation Instructions.

Overview
--------

The RIF Installer is a Python script. It is packaged into binary executables for various platforms using the ``PyInstaller`` package.

You can run the script directly, simply by entering the command:

.. code-block::

	python installer.py
	
on Windows, or just:

.. code-block::

	./install.py 
	
on Unix-based systems, including the Mac. Users are strongly encouraged to use the binary executables, though, and this document explains how to create those.

Prerequisites
-------------

Command Line and Directory
~~~~~~~~~~~~~~~~~~~~~~~~~~

All the steps below assume familiarity with working at the command line: ``cmd.exe`` on Windows, the Terminal app or similar on Mac, or a terminal on your Unix of choice.

Python 
~~~~~~

You must have Python installed, and available from the command line. We developed it with Python 3.7, so we recommend using that version. It may work with earlier 3.x versions, but we haven't tested it with those. It won't work with 2.x.

You will also need to install several Python modules using Pip. Run ``pip install <package-name>`` for each of the following packages:

* ``PyInstaller``
* ``dataclasses``
* ``pywin32`` (on Windows)

Maven
~~~~~

You will need the command-line Maven tool to build the RIF

The RIF
~~~~~~~

You must also have the RIF checked out and built. To check it out:

.. code-block::

	clone https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility.git

And build with:

.. code-block::
	
	cd rapidInquiryFacility
	mvn clean install


Building the Executables
------------------------

To create the installers, Go to the ``installer`` directory, under ``rapidInquiryFacility``:

.. code-block::

	cd installer

Then run:

.. code-block::

	PyInstaller install.spec
	
The ``.spec`` file is a configuration file that tells PyInstaller what files to include in the executable, along with the Python runtime.

The executable will be created in the ``dist`` subdirectory.

If the ``pyinstaller`` command is not found, you will have to edit your ``PATH`` environment variable to include its location, or specify the full path to it. On Windows 10 it will be somewhere like:

.. code-block::

	C:\Users\<user-name>\AppData\Local\Programs\Python\Python37\Scripts\pyinstaller.exe

while on Mac it might be at:

.. code-block::

	/usr/local/bin/pyinstaller

But you will have to identify the correct location for your own system.

Multiple Platforms
------------------

You will have to repeat the above steps on each of the platforms for which you want to create an installer. The file will be named appropriately for the platform: ``rifInstaller.exe`` on Windows and ``rifInstaller_<platform>`` on Unix-based systems.





