demo-galaxy-guide
=================

Interprets sentences providing information and asking for calculations of the intergalactic currency.

Provides CLI and library interfaces. Relies on in-memory data storage. Provides some extra features beyond the original scope.

### Dependencies: ###

 - JDK >= 11 (I used Corretto JVM)

### Usage as a library ###

Ignore the `Main` class and import the `DemoGalaxyGuide` class.

```java
DemoGalaxyGuide guide = new DemoGalaxyGuide();
guide.query("glob is I");
guide.query("prok is V");
guide.query("prok silver is 34 credits");
System.out.println(guide.query("how many credits is glob silver?"));
```

### Build and run (Linux or Git Bash) ###

I usually build and test with IDE, or production build scripts like Maven.  
However, this will allow you to run on CLI with no IDE:

##### build: #####
    cd demo-galaxy-guide
    rm -r out
    cd src
    javac -d ../out/production/demo-galaxy-guide s700/demo/galaxy_guide/Main.java
    cd ../out/production/demo-galaxy-guide
    mkdir ../../artifacts ../../artifacts/demo-galaxy-guide.jar
    jar cfe ../../artifacts/demo-galaxy-guide.jar/demo-galaxy-guide.jar s700.demo.galaxy_guide.Main *
    cd ../../..

##### run: #####

Run with any of these:

    java -jar out/artifacts/demo-galaxy-guide.jar/demo-galaxy-guide.jar # for usage statement
    java -jar out/artifacts/demo-galaxy-guide.jar/demo-galaxy-guide.jar test-input.txt
    java -jar out/artifacts/demo-galaxy-guide.jar/demo-galaxy-guide.jar - # stdin method


Sample input and output
-----------------------

### [`test-input.txt`]: ###

    glob glob Silver is 34 Credits
    glob is I
    prok is V
    pish is X
    tegj is L
    glob prok Gold is 57800 Credits
    pish pish Iron is 3910 Credits
    how much is pish tegj glob glob ?
    how many Credits is glob prok Silver ?
    how many Credits is glob prok Gold ?
    how many Credits is glob prok Iron ?
    tegj glob wood is 1 credits
    how much wood could a woodchuck chuck if a woodchuck could chuck wood ?
    Who's on first ?

### outputs: ###

    Thank you. Acknowledged, but anticipating more information about the numerals.
    Thank you. Acknowledged.
    Thank you. Acknowledged.
    Thank you. Acknowledged.
    Thank you. Acknowledged.
    Thank you. Acknowledged.
    Thank you. Acknowledged.
    pish tegj glob glob is 42
    glob prok silver is 68.00 Credits
    glob prok gold is 57800.00 Credits
    glob prok iron is 782.00 Credits
    Thank you. Acknowledged.
    A woodchuck can chuck 700 wood. That's 13.73 credits.
    I have no idea what you're talking about.


Special Features
----------------

 * If enough information has been provided about the value of wood,
   can provide answer as well as credits value to this question:
   > How much wood could a woodchuck chuck if a woodchuck could chuck wood?

 * Information can be input in reverse order of the original spec. Instead of:

   > glob is I  
   > glob glob Silver is 34 Credits

   The code supports this as well:

   > glob glob Silver is 34 Credits  
   > glob is I

   which — being in the wrong order — obviously adds some complexity to the application, but a merchant cannot choose the order in which he gathers intel about the galaxy. 🙂

 * There's only one unit test so far. Probably the most important one, but more could be added.

### Notes ###

 * There are many `TODO` comments.
 * `.idea` and `.iml` files are useful for IntelliJ IDE developers, but not required. 
 * Package name `s700` is not following convention of domain name,
   partly because my domain name starts with a number.
   (`com.700software` is not a valid package name)


  [`test-input.txt`]: test-input.txt
