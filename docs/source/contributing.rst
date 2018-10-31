============
Contributing
============

------------
Introduction
------------

Thank you for considering contributing to SecureJSON.

Improving documentation, bug triaging, writing tutorials, or even simply requesting features are all examples of helpful
contributions that mean a better library for everyone.

------------
Ground Rules
------------
Be respectful and considerate. Try to be as detailed as possible in bug reports and ensure at least 75% unit test
coverage on any contributed code.

----------------
Responsibilities
----------------
* Ensure Java 7, 8, and 10 compatibility for every change. The build process must work on Linux and Mac OS.
* Create issues for any major changes and enhancements that you wish to make. Discuss things transparently and get
  community feedback. This is for both your benefit and the author's- you don't want to build a feature only to find out
  it's in progress already.
* Don't change publicly exposed interfaces unless absolutely necessary. If it is necessary, the old functionality must
  be marked deprecated and an issue created to remind the author to remove it.
* Ensure you sign all commits. If this is unfamiliar territory, see https://help.github.com/articles/signing-commits/
  for a helpful tutorial.

Here are a couple of friendly tutorials you can follow to get started: http://makeapullrequest.com/ and
http://www.firsttimersonly.com/

At this point, you're ready to make your changes! Feel free to ask for help; everyone is a beginner at first.
If a maintainer asks you to "rebase" your PR, they're saying that a lot of code has changed, and that you need to update
your branch so it's easier to merge.

---------------
Getting started
---------------
* Use `gradle check` to run all the checks needed to pass. If you're squeemish about your changes, you can additionally
  run `gradle verifyJSONTestSuite` to run the `JSONTestSuite <https://github.com/nst/JSONTestSuite>`_ tests. Note that
  all of these will run on the build server, anyway, but it's faster to run them locally.

For something that is bigger than a one or two line fix:

1. Create your own fork of the code
2. Do the changes in your fork
3. If you like the change and think the project could use it:
    * Be sure you have followed the code style for the project. Passing checkstyle is sufficient. The author will
      adjust the checkstyle rules if code we don't like makes it through, but that's on us.

As a rule of thumb, changes are obvious fixes if they do not introduce any new functionality or creative thinking. As
long as the change does not affect functionality, some likely examples include the following:

    * Spelling / grammar fixes
    * Typo correction, white space and formatting changes
    * Comment clean up
    * Bug fixes that change default return values or error codes stored in constants
    * Adding logging messages or debugging output
    * Changes to ‘metadata’ files like Gemfile, .gitignore, build scripts, etc.
    * Moving source files from one directory or package to another

-------------------
How to report a bug
-------------------
If you find a security vulnerability, do NOT open an issue. Email chelsea.urquhart@rocketsimplicity.com instead.

In order to determine whether you are dealing with a security issue, ask yourself these two questions:
    * Can I access something that's not mine, or something I shouldn't have access to?
    * Can I disable something for other people?

If the answer to either of those two questions are "yes", then you're probably dealing with a security issue. Note that
even if you answer "no" to both questions, you may still be dealing with a security issue, so if you're unsure, just
email us at chelsea.urquhart@rocketsimplicity.com.

When filing an issue, make sure to answer these five questions:

1. What version of SecureJSON are you using?
2. What operating system and processor architecture are you using?
3. What did you do?
4. What did you expect to see?
5. What did you see instead?

---------------------------------------
How to suggest a feature or enhancement
---------------------------------------
The SecureJSON philosophy is to provide a library that safely stores string values outside of the JVM, with as little
chance as possible of data exposure, even if an attacker has direct access to the memory.

It is also designed to be able to handle the widest range of "technically valid" JSON files.

If you find yourself wishing for a feature that doesn't exist in SecureJSON, you are probably not alone. There are bound
to be others out there with similar needs. Open an issue on our issues list on GitHub which describes the feature you
would like to see, why you need it, and how it should work.

-------------------
Code review process
-------------------
The author looks at Pull Requests frequently. After feedback has been given we expect responses within two weeks.
After two weeks, the pull request may be closed if it isn't showing any activity.
