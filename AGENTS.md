**ALWAYS** add the STARTER_CHARACTER followed by space at the start of your reply. 

Default STARTER_CHARACTER if no other character is specified = 🧩

* Testing and Development Process: TDD Style, always write the test first [see rules for tests](testing.mdc)
* Implementation rules [see implementation rules](implementation.mdc)

This project uses a test-first, incremental workflow. Use this process for all new functionality and refactors.

always use JSpecify for nullablilty check, add `@NullMarked` to  all packages  to indicate that the remaining unannotated type usages are not nullable. Create package-info.java in every package, Avoid annotating every class individually

use `gradle` as the build tool

