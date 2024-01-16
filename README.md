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

If the user wishes to use the ``PhoneticConverter`` class independently in their own projects, they need to use the ``jsoup`` library (for parsing websites). In this project, it is listed as a dependency in the ``pom.xml`` file.

References
----------

`Фонетика на съвременния български книжовен език <https://rodopskadialektologia.com/wp-content/uploads/2021/09/SBE_Fonetika.pdf>`_ (Мирослав Михайлов)

`The Sound System of Standard Bulgarian <https://www.personal.rdg.ac.uk/~llsroach/phon2/b_phon/b_phon.htm>`_ (Mitko Sabev)

`Handbook of the International Phonetic Association <https://www.google.com/books/edition/Handbook_of_the_International_Phonetic_A/33BSkFV_8PEC?q=&gbpv=1#f=false>`_ (pgs. 55-57)

`International Phonetic Alphabet (IPA) Chart With Sounds <https://www.internationalphoneticalphabet.org/ipa-sounds/ipa-chart-with-sounds/>`_

`Bulgarian Phonology - Wikipedia <https://en.wikipedia.org/wiki/Bulgarian_phonology>`_

`Appendix:Bulgarian pronunciation - Wiktionary <https://en.wiktionary.org/wiki/Appendix:Bulgarian_pronunciation>`_

`Wiktionary - the free dictionary <https://en.wiktionary.org/wiki/Wiktionary:Main_Page>`_

`Ударения - Словоред <https://slovored.com/accent/>`_

`Сричкопренасяне - Словоред <https://slovored.com/hyphenation/>`_

`Речник на българския език <https://rechnik.chitanka.info/>`_

`Тълковен речник и съновник - Думите.info <https://dumite.info/>`_

`Моята Славянска филология: Фонетични промени. Тема 16 <https://poliphilology.blogspot.com/2014/03/16.html>`_

`Анализ на българския език чрез Wikipedia - Nikolay Kostov's Blog <https://nikolay.it/Blog/2011/08/%D0%90%D0%BD%D0%B0%D0%BB%D0%B8%D0%B7-%D0%BD%D0%B0-%D0%B1%D1%8A%D0%BB%D0%B3%D0%B0%D1%80%D1%81%D0%BA%D0%B8%D1%8F-%D0%B5%D0%B7%D0%B8%D0%BA-%D1%87%D1%80%D0%B5%D0%B7-Wikipedia/3>`_
