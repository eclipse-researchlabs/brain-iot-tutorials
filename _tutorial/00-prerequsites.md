---
title: Prerequisites 
layout: toc-guide-page
lprev: ../tutorial
lnext: 05-quickstart.html 
summary: Requirements for running these tutorials.
---

Before you start any of the tutorials you must prepare your environment so that the right tools are installed. This page helps you to achieve this. 

## Required Tools

We need to run the following tools on your computer - without them you won't get very far at all.

* [Java][java8]{:target="_blank"}, probably already got it? If not, this is a good time to get started! BRAIN-IoT targets Java 8 as a minimum version.
* [Maven][Maven]{:target="_blank"}, a popular build tool for Java applications with an enormous repository behind it. Make sure that you're on at least 3.3.9
* [Git][git]{:target="_blank"}, IDEs usually include git support, but when it comes to git nothing really beats the good old command line.
* [Bnd][bnd]{:target="_blank"}, is the Swiss army knife of OSGi, it runs inside Maven,  Eclipse & other tools, but we also need to use it on the command line.

### Installing bnd command

If you're using MacOS, then you can install bnd using [brew][brew]{:target="_blank"}.

Otherwise, you need to download the latest [bnd.jar][bnd.jar] and run bnd command as executable jar:

    $ java -jar biz.aQute.bnd-{VERSION}.jar <command>
{: .shell }

To make this a little easier, you can create a bnd shell script:

```shell
#!/bin/sh
exec java -jar $(dirname $0)/biz.aQute.bnd-5.1.2.jar "$@"
```

Then make this script executable and move it and the bnd jar to a directory on your PATH:

    $ chmod +x bnd
    $ mv bnd biz.aQute.bnd-5.1.2.jar ~/bin
{:.shell}

Now test it works:

    $ bnd version
    5.1.2.202007211702
{:.shell}


## Project Setup for private repositories
<div class="alert alert-warning">
The BRAIN-IoT git and nexus repositories are currently private, so you need credentials to access them.
</div>

Ensure the following BRAIN-IoT nexus servers are configured in your `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>brain-iot-releases</id>
      <username>${env.NEXUS_USER}</username>
      <password>${env.NEXUS_PASSWORD}</password>
    </server>
    <server>
      <id>brain-iot-snapshots</id>
      <username>${env.NEXUS_USER}</username>
      <password>${env.NEXUS_PASSWORD}</password>
    </server>
  </servers>
</settings>
```

You also need to set your credentials in the NEXUS_USER and NEXUS_PASSWORD environment variables.
{:.note}

[java8]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[Maven]: https://maven.apache.org
[bnd]: https://bnd.bndtools.org/
[bnd.jar]: https://bndtools.jfrog.io/bndtools/libs-snapshot/biz/aQute/bnd/biz.aQute.bnd/5.1.2/biz.aQute.bnd-5.1.2.jar
[git]: http://git-scm.com/book/en/Getting-Started-Installing-Git
[brew]: https://brew.sh

