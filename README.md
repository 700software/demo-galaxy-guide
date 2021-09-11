demo-galaxy-guide
=================


### Dependencies: ###

 - JDK >= 11 (I used Corretto JVM)

### Usage as a library ###

Ignore the `Main` class and import the `DemoGalaxyGuide` class.

```java
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

