.. -*- mode: rst -*-

================================
Bulgarian Phonetic Transcription
================================

Bulgarian Cyrillic Transcription into the International Phonetic Alphabet (IPA)

This program contains several methods, all located in the ``PhoneticConverter`` class, that transcribe Bulgarian text written in the Cyrillic alphabet into the IPA. ``PhoneticConverter`` also contains a method to convert a single Cyrillic letter into its IPA equivalent, a method for splitting a Bulgarian word into syllables, and one for getting the stress(es) of Bulgarian text. For a demo of these methods and more, refer to the ``Main`` class.

Usage Notes
-----------

Usually Cyrillic and IPA characters don't display in the console initially. What fixed this issue for me in Eclipse was right clicking on main source -> Run As -> Run Configurations -> (x)= Arguments -> VM arguments -> add::

  -Dsun.stdout.encoding=UTF-8

If that doesn't work, check `here <https://stackoverflow.com/questions/9180981/how-to-support-utf-8-encoding-in-eclipse>`_ for possible solutions.

If the user wishes to use the PhoneticConverter class independently in their own projects, they need to use the ``jsoup`` library (for parsing websites). In this project, it is listed as a dependency in the ``pom.xml`` file.
