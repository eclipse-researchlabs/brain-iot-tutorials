---
title: Develop Smart Behaviour
layout: toc-guide-page
lprev: 05-quickstart.html  
lnext: 20-distributed.html
summary: Create and package a Smart Behaviour.
---

## Summary

This tutorial will show how to create and package a simple BRAIN-IoT Smart Behaviour. 

Here we will create a Security Light System which is composed of three smart behaviours: a `light`, a `sensor` and a `controller`. The sensor and light are ‘virtual’ and implemented as web pages.

In this tutorial we’ll first run, and then re-create and re-run, Security Light System.

A Smart Behaviour is a functional unit deployable in an OSGi container, and it's built using the BRAIN-IoT `smart-behaviour-maven-plugin` plugin, different smart behaviours communicate through the `BRAIN-IoT Events` deliveried in the distributed `BRAIN-IoT EventBus`.

## Build and Run

Download the [SmartBehaviourEventBus](https://git.repository-pert.ismb.it/BRAIN-IoT/SmartBehaviourEventBus.git){:target="_blank"} Git repository, and change directory into `SmartBehaviourEventBus/eventing-example`.
```bash
~ $ git clone https://git.repository-pert.ismb.it/BRAIN-IoT/SmartBehaviourEventBus.git
~ $ cd SmartBehaviourEventBus/eventing-example
```

### Building the example
Build the Application with the following command:
```bash
~/SmartBehaviourEventBus/eventing-example $ mvn verify
```
### Running the example

```bash
~/SmartBehaviourEventBus/eventing-example $ java -jar single-framework-example/target/single-framework-example.jar
```
To test that the application is running, visit:

Sensor UI: [http://localhost:8080/sensor-ui/index.html](http://localhost:8080/sensor-ui/index.html){:target="_blank"}

Light UI: [http://localhost:8080/light-ui/index.html](http://localhost:8080/light-ui/index.html){:target="_blank"}

Click the `Trigger the sensor` button in sensor page, you will see the virtual lightbulb is switched on, then slowly dims in response to a sensor event.

## Recreate a Smart Behaviour

We’ll now recreate the Security Light example locally.

### Project Setup
First change into a new directory, since we are recreating the project:
```bash
~/SmartBehaviourEventBus/eventing-example $ cd ~
```
Then issue the command to create the project template:
```bash
~ $ mvn org.apache.maven.plugins:maven-archetype-plugin:3.0.1:generate \
>     -DarchetypeGroupId=org.osgi.enroute.archetype \
>     -DarchetypeArtifactId=project \
>     -DarchetypeVersion=7.0.0
```
We declare to use the version 3.0.1 of the maven-archetype-plugin because if the version is not fixed, Maven chooses the version and it does not work with e.g. version 2.4.

Filling the project details with appropriate values:
```bash
Define value for property 'groupId': com.paremus.brain.iot.example
Define value for property 'artifactId': eventing-example
Define value for property 'version' 1.0-SNAPSHOT: : 0.0.1-SNAPSHOT
Define value for property 'package' com.paremus.brain.iot.example.eventing.example: : eventing-example
[INFO] Using property: app-artifactId = app
[INFO] Using property: app-target-java-version = 8
[INFO] Using property: impl-artifactId = impl
Confirm properties configuration:
groupId: com.paremus.brain.iot.example
artifactId: eventing-example
version: 0.0.1-SNAPSHOT
package: eventing-example
app-artifactId: app
app-target-java-version: 8
impl-artifactId: impl
 Y: :
```
If you’re using an IDE then this would be a good time to import the generated maven projects.
{:.note}

We will make this template as a maven parent project, which will contain multiple modules for the Security Light example. Rename the auto-created `impl` module folder to `light.api`, and `app` to `light.impl` to compliant with our example, then open the eventing-example parent pom.xml, and change the module names:
```xml
  <modules>
    <module>light.api</module>
    <module>light.impl</module>
  </modules>
```
and change the default `bnd.version` property to 
```xml
  <bnd.version>4.2.0</bnd.version>
```
Before implementing the module, append the following dependencies in the `dependencyManagement` section in parent pom.xml, as they are the dependencies of EventBus.

```xml
  <dependency>
      <groupId>com.paremus.brain.iot</groupId>
      <artifactId>eventing.api</artifactId>
      <version>0.0.1-SNAPSHOT</version>
  </dependency>
  <dependency>
       <groupId>org.apache.aries.jax.rs</groupId>
       <artifactId>org.apache.aries.jax.rs.jackson</artifactId>
       <version>1.0.2</version>
       <scope>runtime</scope>
  </dependency>
  <dependency>
       <groupId>com.paremus.brain.iot</groupId>
       <artifactId>eventing.impl</artifactId>
       <version>0.0.1-SNAPSHOT</version>
       <scope>runtime</scope>
  </dependency>
       <dependency>
       <groupId>com.paremus.brain.iot</groupId>
       <artifactId>message.integrity.insecure.impl</artifactId>
       <version>0.0.1-SNAPSHOT</version>
       <scope>runtime</scope>
  </dependency>
```
Add folowing repositories in the `<repositories>` section:

```xml
      <repository>
            <id>brain-iot-releases</id>
            <name>BRAIN-IoT Releases</name>
            <url>https://nexus.repository-pert.ismb.it/repository/maven-releases/</url>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
        <repository>
            <id>brain-iot-snapshots</id>
            <name>BRAIN-IoT Snapshots</name>
            <url>https://nexus.repository-pert.ismb.it/repository/maven-snapshots/</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
       </repository>
```
And create a new section `<pluginRepositories>` for using BRAIN-IoT plugins
```xml
    <pluginRepositories>
        <pluginRepository>
            <id>brain-iot-releases</id>
            <name>BRAIN-IoT Releases</name>
            <url>https://nexus.repository-pert.ismb.it/repository/maven-releases/</url>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </pluginRepository>
        <pluginRepository>
            <id>brain-iot-snapshots</id>
            <name>BRAIN-IoT Snapshots</name>
            <url>https://nexus.repository-pert.ismb.it/repository/maven-snapshots/</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
```
### Define BRAIN-IoT Events

In the `light.api` API module, we'll define a sort of events consumed by the `light` smart behaviour. Each cunstomized event has to extend the common super-type `BrainIoTEvent` shown in the following which contains information that must exist for all events.
```java
package eu.brain.iot.eventing.api;

import java.time.Instant;

import org.osgi.annotation.versioning.ConsumerType;
import org.osgi.dto.DTO;

/**
 * An common super-type for all Brain IoT events containing 
 * information that must exist for all events.
 */
@ConsumerType
public abstract class BrainIoTEvent extends DTO {

	/**
	 * The identifier of the node creating the event   
	 */
    public String sourceNode;
 
    /**   
     * The time at which this event was created  
     */
    public Instant timestamp;
 
    /**   
     * The security token that can be used to authenticate/validate the event   
     */
    public byte[] securityToken;
}
```

<p>
  <a class="btn btn-primary" data-toggle="collapse" href="#BrainIoTEvent" aria-expanded="false" aria-controls="BrainIoTEvent.java">BrainIoTEvent.java</a>
</p>

<div class="collapse" id="BrainIoTEvent">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/BrainIoTEvent.java %}
{% endhighlight %}
  </div>
</div>

Open pom.xml of `light.api` and change the artifactId to ‘light.api’.
```xml
<artifactId>light.api</artifactId>
```

Add following dependency of the BRAIN-IoT Events and EventBus in the `light.api` pom.xml:

```xml
<dependency>
    <groupId>com.paremus.brain.iot</groupId>
    <artifactId>eventing.api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
which provides the `BrainIoTEvent`, `EventBus`, `SmartBehaviour` and `UntypedSmartBehaviour` interfaces and others. You can simlplly replace the auto-created pom.xml by the template with the following simplified one to remove the unnecessary dependencies:

<p>
  <a class="btn btn-primary" data-toggle="collapse" href="#lightAPI-pom" aria-expanded="false" aria-controls="pom.xml">pom.xml</a>
</p>

<div class="collapse" id="lightAPI-pom">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/lightAPI-pom.xml %}
{% endhighlight %}
  </div>
</div>

Note that a Smart Behaviour must consume typed events and cannot be used to consume unknown event types. If unknown event types are to be consumed then an `UntypedSmartBehaviour` must be used.
{:.note}

Remove original src package, and create a new src package `com/paremus/brain/iot/example/light/api`
```bash
~ $ cd eventing-example/light.api
~/eventing-example/light.api $ rm -rf src/main/java/*
~/eventing-example/light.api $ mkdir -p src/main/java/com/paremus/brain/iot/example/light/api
```

The customized events should be defined as following:

```java
public class XXX extends BrainIoTEvent {
    // TODO: add other fields if need
}
```

Copy all source java files from the `light.api` sub-project of downloaded [SmartBehaviourEventBus](https://git.repository-pert.ismb.it/BRAIN-IoT/SmartBehaviourEventBus.git){:target="_blank"} Git repository to the new src package. Here ia a sequence of events used by the light, and the controller including the `AbstractLightDTO.java`, `LightCommand.java`, `LightQuery.java`, `LightQueryResponse.java`, `package-info.java`.


### Implement A Smart Behavior

A Smart Behavior is implemented using the `@SmartBehaviourDefinition` annotation provided by above event dependency, and its relevant Capabilities (specified by the types of the consumed BRAIN-IoT events) are configured through the properties of the annotation.

Go to `light.impl` module, remove original src package, and create a new src package `com/paremus/brain/iot/example/light/impl`
```bash
~ $ cd ../eventing-example/light.impl
~/eventing-example/light.impl $ rm -rf src/main/java/config
~/eventing-example/light.impl $ mkdir -p src/main/java/com/paremus/brain/iot/example/light/impl
```
Open pom.xml of `light.impl` and change the artifactId to ‘light.impl’.
```xml
<artifactId>light.impl</artifactId>
```
Replace original `<dependencies>` section with the following:

```xml
    <dependencies>
        <dependency>
            <groupId>org.osgi.enroute</groupId>
            <artifactId>osgi-api</artifactId>
            <type>pom</type>
        </dependency>
        <dependency>
            <groupId>org.osgi.enroute</groupId>
            <artifactId>enterprise-api</artifactId>
            <type>pom</type>
        </dependency>
        <dependency>
            <groupId>org.osgi.enroute</groupId>
            <artifactId>test-bundles</artifactId>
            <type>pom</type>
        </dependency>
        <dependency>
            <groupId>com.paremus.brain.iot</groupId>
            <artifactId>eventing.api</artifactId>
        </dependency>
        <dependency>
            <groupId>com.paremus.brain.iot.example</groupId>
            <artifactId>light.api</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.osgi.enroute</groupId>
            <artifactId>impl-index</artifactId>
            <type>pom</type>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.aries.jax.rs</groupId>
            <artifactId>org.apache.aries.jax.rs.jackson</artifactId>
        </dependency>
        <dependency>
            <groupId>com.paremus.brain.iot</groupId>
            <artifactId>eventing.impl</artifactId>
        </dependency>
        <dependency>
            <groupId>com.paremus.brain.iot</groupId>
            <artifactId>message.integrity.insecure.impl</artifactId>
        </dependency>
     </dependencies>
```

A part of the implementation of the `light` Smart Behavior is:
<p>
  <a class="btn btn-primary" data-toggle="collapse" href="#RestComponentImpl" aria-expanded="false" aria-controls="RestComponentImpl.java">RestComponentImpl.java</a>
</p>

<div class="collapse" id="RestComponentImpl">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/RestComponentImpl.java %}
{% endhighlight %}
  </div>
</div>

```java
/**
 * This component represents two things:
 *  1. A Smart Light Bulb controlled using events
 *  2. A set of REST resources used by a web ui to display the light bulb
 */
import com.paremus.brain.iot.example.light.api.LightCommand;
import com.paremus.brain.iot.example.light.api.LightQuery;
import com.paremus.brain.iot.example.light.api.LightQueryResponse;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.EventBus;
import eu.brain.iot.eventing.api.SmartBehaviour;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service= {SmartBehaviour.class, RestComponentImpl.class})
@JaxrsResource
@HttpWhiteboardResource(pattern="/light-ui/*", prefix="/static")
@JSONRequired
@SmartBehaviourDefinition(consumed = {LightCommand.class, LightQuery.class}, filter = "(brightness=*)",
        author = "Paremus", name = "Example Smart Light Bulb",
        description = "Implements a Smart Light Bulb and UI to display it.")
public class RestComponentImpl implements SmartBehaviour<BrainIoTEvent>{

	@Reference
	private EventBus eventBus;
   //**
	@Override
	public void notify(BrainIoTEvent event) {
		if(event instanceof LightQuery) {
			LightQueryResponse response = new LightQueryResponse();
			response.requestor = event.sourceNode;
			//**  The internal process for consuming the event
			eventBus.deliver(response);

		} else if (event instanceof LightCommand) {
			LightCommand command = (LightCommand) event;	
         //**  The internal process for consuming the event 
		}
         //**  other code
	}
}
```
The important annotations include:

*  The @Component is used to register this component as an OSGi service.
*  The @JaxrsResource annotation is used to mark this as a JAX-RS whiteboard resource
*  The @JSONRequired is used to declare its JSON support requirement
*  The @SmartBehaviourDefinition is used to define the `Provide-Capability` of the smart behaviour using the following properties:
   1.  'consumed': Specifies the typed events to be consumed by the smart behaviour. Here they are the `LightCommand` and the `LightCommand` events.
   2.  'filter': A filter used to select the properties of the events that should be passed to this smart behaviour (only `LightCommand` here) from EventBus
   3.  'author': The author of this smart behaviour
   4.  'name': The name of this smart behaviour
   5.  'description': The description of this smart behaviour

### Integrate with BRAIN-IoT EventBus
A smart behaviour has to integrate with BRAIN-IoT EventBus which is a referenced service in implementation as shown in above code:
```java
@Reference
private EventBus eventBus;
```

In addition, two important methods must be used:
*  The `notify(T extends BrainIoTEvent)` method provided by the SmartBehaviour API, for asynchronously receiving an event
*  The `deliver(T extends BrainIoTEvent)` method provided by the EventBus, for asynchronously sending an event

Copy all source files from the `light.impl` sub-project of downloaded [SmartBehaviourEventBus](https://git.repository-pert.ismb.it/BRAIN-IoT/SmartBehaviourEventBus.git){:target="_blank"} Git repository to the new src package including the `RestComponentImpl.java`, `LightDTO.java`.
A part of the implementation of the `light` Smart Behavior is:
<p>
  <a class="btn btn-primary" data-toggle="collapse" href="#LightDTO" aria-expanded="false" aria-controls="LightDTO.java">LightDTO.java</a>
</p>

<div class="collapse" id="LightDTO">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/LightDTO.java %}
{% endhighlight %}
  </div>
</div>

And copy the downloaded `light.impl/src/main/resources/static` folder which is the implementation of the light bulb page to the same place of current `light.impl` module (i.e. `light.impl/src/main/resources/static`).


### Build as OSGi bundle

It’s now time to build the implementation project. Let's firstly resolving the `light` smart behaviour. 

Rename `~/eventing-example/light.impl/app.bndrun` file to `light.bndrun`, and open it

```bash
index: target/index.xml;name="app"

-standalone: ${index}

-runrequires: osgi.identity;filter:='(osgi.identity=com.paremus.brain.iot.example.impl)'
-runfw: org.eclipse.osgi
-runee: JavaSE-1.8

-resolve.effective: active
```
Note that your runee may be different if you chose to use a higher version of Java
{:.note}

As shown, the bndrun contains a `runrequires` statement that specifies a capability, here we should change it to the implementation for `light.impl` and its JAX-RS whiteboard dependency. And we will not make it as a standalone application:

```bash
-runrequires: \
   bnd.identity;id='com.paremus.brain.iot.example.light.impl',\
	bnd.identity;id='org.apache.aries.jax.rs.jackson'

-runfw: org.eclipse.osgi
-runee: JavaSE-1.8

-resolve.effective: active
```
However, no `runbundles` a currently listed; i.e. the actual bundles needed at runtime to run the `light` smart behaviour. Let's now automatically resolve the application using the bnd-resolver-maven-plugin by replacing the `build` section in pom.xml with:
```xml
   <build>
        <plugins>
            <!-- Expected to inherit configuration from a parent enRoute 
                pom. This includes -contract definitions and maven-jar-plugin setup -->
            <plugin>
                <groupId>biz.aQute.bnd</groupId>
                <artifactId>bnd-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
            </plugin>
            <!-- Validate that the smart behaviour can resolve -->
            <plugin>
                <groupId>biz.aQute.bnd</groupId>
                <artifactId>bnd-resolver-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>resolve</goal>
                        </goals>
                        <phase>package</phase>
                        <configuration>
                            <bndruns>
                                <bndrun>light.bndrun</bndrun>
                            </bndruns>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
``` 

Build the `eventing-example` project:
```bash
~/eventing-example/light.impl $ cd ..
~/eventing-example $ mvn package
```

Here, we use the `package` goal to check that the code compiles and can be successfully packaged into a bundle, and the current `light` implementation can be resolved to generate the runtime dependencies. If we had tests or other post-packaging checks then we could have used the `verify` goal instead.

If the package fails then check your code and try again. Once you can package it cleanly then continue to the next stage.
{:.warning}

You can see each module has been packaged as an OSGi bundle in their output directories.

If you look again at the `light.bndrun` file you will now see a number of bundles required at runtime are now listed by `runbundles` instruction.
```bash
-runrequires: \
	bnd.identity;id='com.paremus.brain.iot.example.light.impl',\
	bnd.identity;id='org.apache.aries.jax.rs.jackson'
	
-runfw: org.eclipse.osgi
-runee: JavaSE-1.8

-runproperties: org.osgi.service.http.port=8082

-resolve.effective: active
-runbundles: \
	ch.qos.logback.classic;version='[1.2.3,1.2.4)',\
	ch.qos.logback.core;version='[1.2.3,1.2.4)',\
	com.paremus.brain.iot.eventing.api;version='[0.0.1,0.0.2)',\
	com.paremus.brain.iot.eventing.impl;version='[0.0.1,0.0.2)',\
	com.paremus.brain.iot.example.light.api;version='[0.0.1,0.0.2)',\
	com.paremus.brain.iot.example.light.impl;version='[0.0.1,0.0.2)',\
	org.apache.aries.jax.rs.whiteboard;version='[1.0.1,1.0.2)',\
	org.apache.felix.configadmin;version='[1.9.8,1.9.9)',\
	org.apache.felix.http.jetty;version='[4.0.6,4.0.7)',\
	org.apache.felix.http.servlet-api;version='[1.1.2,1.1.3)',\
	org.apache.felix.scr;version='[2.1.10,2.1.11)',\
	org.apache.servicemix.specs.annotation-api-1.3;version='[1.3.0,1.3.1)',\
	org.osgi.service.jaxrs;version='[1.0.0,1.0.1)',\
	org.osgi.util.function;version='[1.1.0,1.1.1)',\
	org.osgi.util.promise;version='[1.1.0,1.1.1)',\
	slf4j.api;version='[1.7.25,1.7.26)',\
	com.fasterxml.jackson.core.jackson-annotations;version='[2.9.0,2.9.1)',\
	com.fasterxml.jackson.core.jackson-core;version='[2.9.6,2.9.7)',\
	com.fasterxml.jackson.core.jackson-databind;version='[2.9.6,2.9.7)',\
	com.fasterxml.jackson.jaxrs.jackson-jaxrs-base;version='[2.9.6,2.9.7)',\
	com.fasterxml.jackson.jaxrs.jackson-jaxrs-json-provider;version='[2.9.6,2.9.7)',\
	com.fasterxml.jackson.module.jackson-module-jaxb-annotations;version='[2.9.6,2.9.7)',\
	org.apache.aries.jax.rs.jackson;version='[1.0.2,1.0.3)',\
	org.osgi.util.pushstream;version='[1.0.0,1.0.1)',\
	org.apache.aries.javax.jax.rs-api;version='[1.0.1,1.0.2)',\
	com.paremus.brain.iot.message.integrity.api;version='[0.0.1,0.0.2)',\
	com.paremus.brain.iot.message.integrity.insecure.impl;version='[0.0.1,0.0.2)'
```

### Package as Smart Behaviour

Now it's time to package the `light` implementation as a smart behaviour using the `smart-behaviour-maven-plugin`, open `light.impl` pom.xml, add:
```xml
   <plugin>
       <groupId>com.paremus.brain.iot.maven</groupId>
       <artifactId>smart-behaviour-maven-plugin</artifactId>
       <version>0.0.1-SNAPSHOT</version>
       <executions>
         <execution>
           <goals>
              <goal>smart-behaviour</goal>
           </goals>
           <configuration>
              <bndrun>light.bndrun</bndrun>
           </configuration>
        </execution>
	    </executions>
   </plugin>
```
`smart-behaviour-maven-plugin` provides the `smart-behaviour` goal bound to the `package` phase for gathering all dependencies specified in the project pom file, as well as the project output jar by default and building a smart behaviour jar. The resulting smart behaviour jar file has the name in the form of `${project.artifactId}-${project.version}-brain-iot-smart-behaviour.jar`. And the smart-behaviour goal is not executed by default, therefore at least one explicit execution needs to be configured.

This plugin is optionally configurable using a bndrun file in its plugin’s configuration in the pom. The bndrun file with the suffix .bndrun is located in the base directory of the project. And this can be configured to specify an alternate path which can be absolute or relative to the base directory of the project. The bndrun file is used as a source of initial requirements or a list of bundles to be deployed on a BRAIN-IoT Fabric Fibre. The bundles specified in the “-runrequires” instruction in the bndrun file will be put in the BRAIN-IoT-Deploy-Requirement header of the generated jar and they will be resolved and installed on the fibre together with their dependencies. If the bndrun file doesn’t exist, then the default bundle must be deployed is the project output jar. So, in general, this plugin can be used together with the `bnd-resolver-maven-plugin` for resolving the bndrun file in advance. In the `light` smart behaviour, we use the `light.bndrun`.
{:.note}

To package the `light` as a smart behaviour, run:
```bash
~/eventing-example $ cd light.impl
~/eventing-example/light.impl $ mvn package
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building light.impl 0.0.1-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] --- maven-jar-plugin:3.0.2:jar (default-jar) @ light.impl ---
[INFO] Building jar: /home/rui/Documents/SmartBehaviourEventBus/eventing-example/light.impl/target/light.impl-0.0.1-SNAPSHOT.jar
[INFO] 
[INFO] --- bnd-resolver-maven-plugin:4.2.0:resolve (default) @ light.impl ---
[INFO] 
[INFO] --- smart-behaviour-maven-plugin:0.0.1-SNAPSHOT:smart-behaviour (default) @ light.impl ---
[INFO] Gathering dependencies
[INFO] Copying light.api-0.0.1-SNAPSHOT.jar to ~/eventing-example/light.impl/target/smart-behaviour/light.api-0.0.1-SNAPSHOT.jar
[INFO] Copying light.impl-0.0.1-SNAPSHOT.jar to ~/eventing-example/light.impl/target/smart-behaviour/light.impl-0.0.1-SNAPSHOT.jar
[INFO] Copying .....
[INFO] Processing ~/eventing-example/light.impl/light.bndrun for dependencies
[INFO] Building jar: ~/eventing-example/light.impl/target/light.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
In the `light.impl/target` folder, there is the `light.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar`, and a `smart-behaviour` folder with all the gathered dependencies for the `light` smart behaviour.

### Create the Sensor and Controller Smart Behaviours
Let's create sub-modules `sensor.api`, `sensor.impl` and `behaviour.impl` in `eventing-example` parent project. The controller Smart Behaviour is named as `behaviour.impl` 
```bash
~/eventing-example $ mvn archetype:generate -DgroupId=com.paremus.brain.iot.example  -DartifactId=sensor.api
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] eventing-example
[INFO] light.api
[INFO] light.impl
Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): 1674: 
......
Choose a number: 8: 5
[INFO] Using property: groupId = com.paremus.brain.iot.example
[INFO] Using property: artifactId = sensor.impl
Define value for property 'version' 1.0-SNAPSHOT: : 0.0.1-SNAPSHOT
[INFO] Using property: package = com.paremus.brain.iot.example
Confirm properties configuration:
groupId: com.paremus.brain.iot.example
artifactId: sensor.impl
version: 0.0.1-SNAPSHOT
package: com.paremus.brain.iot.example
 Y: :
```
When creating the submodules, it requires the user inputs for the plugin: `Enter` -> `5` -> `0.0.1-SNAPSHOT` -> `Enter`.

Then create the other two modules:
```bash
~/eventing-example $ mvn archetype:generate -DgroupId=com.paremus.brain.iot.example  -DartifactId=sensor.api
~/eventing-example $ mvn archetype:generate -DgroupId=com.paremus.brain.iot.example  -DartifactId=behaviour.impl
......
```

Remove all auto-created source file `App.java` in each module, and copy all source files and pom.xml from the corresponding sub-project of downloaded [SmartBehaviourEventBus](https://git.repository-pert.ismb.it/BRAIN-IoT/SmartBehaviourEventBus.git){:target="_blank"} Git repository to the same places of the `sensor.api`, `sensor.impl` and `behaviour.impl` modules. In addition, copy the `sensor.bndrun` and the `static` resource folder into the `sensor.impl` module.

The `sensor` smart behaviour is designed to just send the `SensorReadingDTO` trigger event, so its `consumed` property of the `@SmartBehaviourDefinition` annotation is empty.
{:.note}

Now build the all smart behaviours together in the parent root directory:
```bash
~/eventing-example $ mvn clean package
[INFO] Scanning for projects...
[INFO] Copying .....
[INFO] Processing ~/eventing-example/*/*.bndrun for dependencies
[INFO] Building jar: ~/eventing-example/light.impl/target/light.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar
[INFO] Building jar: ~/eventing-example/sensor.impl/target/sensor.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar
[INFO] Building jar: ~/eventing-example/behaviour.impl/target/behaviour.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
If there is already a smart behaviour has been generated before rebuilding the project, remember to run the `clean` goal before the `package` goal to remove the existing `smart-behaviour` folder from the project `target` directory to avoid failure when copying the denpendencies.
{:.warning}


Follow the same procedure to create the `single-framework-example` application module and copy the downloaded pom.xml and the `single-framework-example.bndrun` here, then build and run as done in the begining of this tutorial.
```bash
~/eventing-example $ mvn archetype:generate -DgroupId=com.paremus.brain.iot.example  -DartifactId=single-framework-example
```

## End

That completes this tutorial.
