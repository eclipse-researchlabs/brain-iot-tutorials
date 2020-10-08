---
title: Develop Smart Behaviour
layout: toc-guide-page
lprev: 05-quickstart.html  
lnext: 20-distributed.html
summary: Create and package a Smart Behaviour.
---

## Summary

This tutorial will show how to create and package a simple BRAIN-IoT Smart Behaviour. It's This tutorial is Maven and command-line based.

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

We’ll now recreate the Security Light example locally using the [enRoute Archetypes](https://enroute.osgi.org/about/112-enRoute-Archetypes.html){:target="_blank"} this tutorial to walk through the creation of a REST Microservice comprised of the following structural elements:

*  An API module (light.api)
*  A Implementation module, also including the REST Service Implementation of the light blub (light.impl)

with each module having a POM that describes its dependencies.

### Project Setup
First change into a new directory, since we are recreating the project, here we go to the `HOME` directory:
```bash
~/SmartBehaviourEventBus/eventing-example $ cd ~
```
Use the [bare-project Archetype](https://enroute.osgi.org/about/112-enRoute-Archetypes.html#the-project-archetype){:target="_blank"} to create a microservice project:
```bash
~ $ mvn archetype:generate \
     -DarchetypeGroupId=org.osgi.enroute.archetype \
     -DarchetypeArtifactId=project-bare \
     -DarchetypeVersion=7.0.0
```

Filling the project details with appropriate values:
```bash
Define value for property 'groupId': com.paremus.brain.iot.example
Define value for property 'artifactId': eventing-example
Define value for property 'version' 1.0-SNAPSHOT: : 0.0.1-SNAPSHOT
Define value for property 'package' com.paremus.brain.iot.example: : 
Confirm properties configuration:
groupId: com.paremus.brain.iot.example
artifactId: eventing-example
version: 0.0.1-SNAPSHOT
package: com.paremus.brain.iot.example
 Y: :
```
If you’re using an IDE then this would be a good time to import the generated maven project and then import the submodules when they're created.
{:.note}

This is a maven parent project, which will contain multiple modules for the Security Light example.

Open POM and change the default `<bnd.version>` property to avoid build failure:
```xml
  <bnd.version>4.2.0</bnd.version>
```
Before implementing the module, append the following BRAIN-IoT runtime dependencies in the `<dependencyManagement>` section in parent POM, as they are the dependencies of EventBus.

Here assume that you have run through the [Prerequisites](00-prerequisites.html) tutorial and the credentials setup for BRAIN-IoT private Nexus repositories has been done. 
{:.warning}

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
   <dependency>
       <groupId>org.apache.aries.spec</groupId>
       <artifactId>org.apache.aries.javax.jax.rs-api</artifactId>
       <version>1.0.1</version>
       <scope>compile</scope>
   </dependency>
```
Append the following repositories in `<repositories>` section for downloading the BRAIN-IoT runtime dependencies:
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
and append the following plugin repositories in `<pluginRepositories>` section for downloading the BRAIN-IoT `smart-behaviour-maven-plugin`:
```xml
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
```
### Create Light API Module

Change directory into the newly created `eventing-example` project directory, then create an API module `light.api` using the [api Archetype](https://enroute.osgi.org/about/112-enRoute-Archetypes.html#the-api-archetype){:target="_blank"} as shown:
```bash
~ $ cd eventing-example
~/eventing-example $ mvn archetype:generate \
     -DarchetypeGroupId=org.osgi.enroute.archetype \
     -DarchetypeArtifactId=api \
     -DarchetypeVersion=7.0.0
```
with the following values:
```bash
Define value for property 'groupId': com.paremus.brain.iot.example
Define value for property 'artifactId': light.api
Define value for property 'version' 1.0-SNAPSHOT: : 0.0.1-SNAPSHOT
Define value for property 'package' com.paremus.brain.iot.example.light.api: : 
Confirm properties configuration:
groupId: com.paremus.brain.iot.example
artifactId: light.api
version: 0.0.1-SNAPSHOT
package: com.paremus.brain.iot.example.light.api
 Y: :
```
This API module defines a sort of BRAIN-IoT Events via the use of [Data Transfer Objects (DTOs)](https://enroute.osgi.org/FAQ/420-dtos.html){:target="_blank"} transfered between `light` smart behaviour and others. Each event has to extend the common super-type `BrainIoTEvent` shown in the following `BrainIoTEvent.java` which contains information that must exist for all events.

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

#### Dependencies

Open pom.xml of `light.api` and add following dependency of the BRAIN-IoT Events and EventBus:

```xml
<dependency>
    <groupId>com.paremus.brain.iot</groupId>
    <artifactId>eventing.api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
which provides the `BrainIoTEvent`, `EventBus`, `SmartBehaviour` and `UntypedSmartBehaviour` interfaces and others.

Note that a Smart Behaviour must consume typed events and cannot be used to consume unknown event types. If unknown event types are to be consumed then an `UntypedSmartBehaviour` must be used.
{:.note}

#### Visibility
`light.api` is an API package which is imported by other implementation modules, hence it must must be exported. This is indicated by the automatically generated file `~/eventing-example/light.api/src/main/java/com/paremus/brain/iot/example/light/api/package-info.java`:
<p>
  <a class="btn btn-primary" data-toggle="collapse" href="#package-info" aria-expanded="false" aria-controls="package-info.java">package-info.java</a>
</p>

<div class="collapse" id="package-info">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/package-info.java %}
{% endhighlight %}
  </div>
</div>

#### Defining the BRAIN-IoT Events
The customized events should be defined as following:

```java
public class XXX extends BrainIoTEvent {
    // TODO: add other fields if need
}
```
Remove original auto-generated java classes in src package:

`~/eventing-example/light.api/src/main/java/com/paremus/brain/iot/example/light/api/ProviderInterface.java`

`~/eventing-example/light.api/src/main/java/com/paremus/brain/iot/example/light/api/ConsumerInterface.java`

and create the following four files:

`~/eventing-example/light.api/src/main/java/com/paremus/brain/iot/example/light/api/AbstractLightDTO.java`

`~/eventing-example/light.api/src/main/java/com/paremus/brain/iot/example/light/api/LightCommand.java`

`~/eventing-example/light.api/src/main/java/com/paremus/brain/iot/example/light/api/LightQuery.java`

`~/eventing-example/light.api/src/main/java/com/paremus/brain/iot/example/light/api/LightQueryResponse.java`

<p>
  <a class="btn btn-primary" data-toggle="collapse" href="#AbstractLightDTO" aria-expanded="false" aria-controls="AbstractLightDTO.java">AbstractLightDTO.java</a>
  <a class="btn btn-primary" data-toggle="collapse" href="#LightCommand" aria-expanded="false" aria-controls="LightCommand.java">LightCommand.java</a>
  <a class="btn btn-primary" data-toggle="collapse" href="#LightQuery" aria-expanded="false" aria-controls="LightQuery.java">LightQuery.java</a>
  <a class="btn btn-primary" data-toggle="collapse" href="#LightQueryResponse" aria-expanded="false" aria-controls="LightQueryResponse.java">LightQueryResponse.java</a>
</p>

<div class="collapse" id="AbstractLightDTO">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/AbstractLightDTO.java %}
{% endhighlight %}
  </div>
</div>

<div class="collapse" id="LightCommand">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/LightCommand.java %}
{% endhighlight %}
  </div>
</div>

<div class="collapse" id="LightQuery">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/LightQuery.java %}
{% endhighlight %}
  </div>
</div>

<div class="collapse" id="LightQueryResponse">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/LightQueryResponse.java %}
{% endhighlight %}
  </div>
</div>

You can copy all source java files from the `light.api` sub-project of downloaded [SmartBehaviourEventBus](https://git.repository-pert.ismb.it/BRAIN-IoT/SmartBehaviourEventBus.git){:target="_blank"} Git repository to the src package.
{:.note}

### Create Light Smart Behaviour Implementation Module

A Smart Behavior is implemented as Declarative Service using the `@SmartBehaviourDefinition` annotation provided by above `eventing.api` dependency, and its relevant Capabilities (specified by the types of the consumed BRAIN-IoT events) are configured through the properties of the annotation.

Now, in the `eventing-example` project directory, create the `light.impl` module using the [rest-component Archetype](https://enroute.osgi.org/about/112-enRoute-Archetypes.html#the-rest-component-archetype){:target="_blank"}:

```bash
~/eventing-example $ mvn archetype:generate \
     -DarchetypeGroupId=org.osgi.enroute.archetype \
     -DarchetypeArtifactId=rest-component \
     -DarchetypeVersion=7.0.0
```
with the following values:
```bash
Define value for property 'groupId': com.paremus.brain.iot.example
Define value for property 'artifactId': light.impl
Define value for property 'version' 1.0-SNAPSHOT: : 0.0.1-SNAPSHOT
Define value for property 'package' com.paremus.brain.iot.example.light.impl: : 
Confirm properties configuration:
groupId: com.paremus.brain.iot.example
artifactId: light.impl
version: 0.0.1-SNAPSHOT
package: com.paremus.brain.iot.example.light.impl
 Y: :
```
#### Dependencies
Append the following dependencies in the `<dependencies>` section of POM:

```xml
    <dependencies>
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

<p>
  <a class="btn btn-primary" data-toggle="collapse" href="#lightImpl-pom" aria-expanded="false" aria-controls="lightImpl-pom">pom.xml</a>
</p>

<div class="collapse" id="lightImpl-pom">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/lightImpl-pom.xml %}
{% endhighlight %}
  </div>
</div>

#### Implement the `light` smart behaviour
Remove original auto-generated java classes in src package:

`~/eventing-example/light.impl/src/main/java/com/paremus/brain/iot/example/light/impl/RestComponentImpl.java`

And create the following two files:

`~/eventing-example/light.impl/src/main/java/com/paremus/brain/iot/example/light/impl/LightDTO.java`

`~/eventing-example/light.impl/src/main/java/com/paremus/brain/iot/example/light/impl/RestComponentImpl.java`

<p>
  <a class="btn btn-primary" data-toggle="collapse" href="#LightDTO" aria-expanded="false" aria-controls="LightDTO.java">LightDTO.java</a>
  <a class="btn btn-primary" data-toggle="collapse" href="#RestComponentImpl" aria-expanded="false" aria-controls="RestComponentImpl.java">RestComponentImpl.java</a>
</p>

<div class="collapse" id="LightDTO">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/LightDTO.java %}
{% endhighlight %}
  </div>
</div>

<div class="collapse" id="RestComponentImpl">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/RestComponentImpl.java %}
{% endhighlight %}
  </div>
</div>

You can copy the `RestComponentImpl.java`, `LightDTO.java` source files from the `light.impl` sub-project of downloaded [SmartBehaviourEventBus](https://git.repository-pert.ismb.it/BRAIN-IoT/SmartBehaviourEventBus.git){:target="_blank"} Git repository to this new module.
{:.note}

A part of the implementation of the `light` Smart Behavior in `RestComponentImpl.java` is:

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

#### Integration with BRAIN-IoT EventBus
A smart behaviour has to integrate with BRAIN-IoT EventBus which is a referenced service in implementation as shown in above code:
```java
@Reference
private EventBus eventBus;
```

In addition, two important methods must be used:
*  The `notify(T extends BrainIoTEvent)` method provided by the SmartBehaviour API, for asynchronously receiving an event
*  The `deliver(T extends BrainIoTEvent)` method provided by the EventBus, for asynchronously sending an event

#### Light Bulb REST Service Implementation 
The web page of the light bulb will be implemented also in this `light.impl` module.

Create the directory `~/eventing-example/light.impl/src/main/resources` and copy the downloaded `light.impl/src/main/resources/static` folder which is the implementation of the light bulb page to the same place of current `light.impl` module (i.e. `~/eventing-example/light.impl/src/main/resources/static`).
```bash
~/eventing-example $ ls light.impl/src/main/resources/static
css  img  index.html  js  lib
```

#### Visibility
Implementations should NOT be shared; hence no package-info.java file.
{:.note}

## Build

It’s now time to build the `light` API and implementation modules and package it as a smart behaviour using the `smart-behaviour-maven-plugin` provided by BRAIN-IoT project. This plugin provides the `smart-behaviour` goal bound to the `package` phase for gathering all dependencies specified in the project pom file, as well as the project output jar by default and building a smart behaviour jar. The resulting smart behaviour jar file has the name in the form of `${project.artifactId}-${project.version}-brain-iot-smart-behaviour.jar`. And the smart-behaviour goal is not executed by default, therefore at least one explicit execution needs to be configured.

This plugin is optionally configurable using a bndrun file in its plugin’s configuration in the POM. The bndrun file is located in the base directory of the project. And this can be configured to specify an alternate path which can be absolute or relative to the base directory of the project. The bndrun file is used as a source of initial requirements or a list of bundles to be deployed on a BRAIN-IoT Fabric Fibre when Behaviour Management Service (BMS) searches in a marketplace. So, in general, this plugin can be used together with the `bnd-resolver-maven-plugin` for resolving the bndrun file in advance.

#### Build Setup
Add the following `<build>` section in `light.impl` POM:
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
        </plugins>
    </build>
```
Create a `bndrun` file:
```bash
~/eventing-example $ touch light.impl/light.bndrun
```
Then add the following content in `light.bndrun`:
```bash
-runrequires: \
   bnd.identity;id='com.paremus.brain.iot.example.light.impl',\
	bnd.identity;id='org.apache.aries.jax.rs.jackson'

-runfw: org.eclipse.osgi
-runee: JavaSE-1.8

-resolve.effective: active
```

As shown above, the bndrun contains a `runrequires` statement that specifies the required bundles to be deployed at runtime. here we put the `light.impl` module and its JAX-RS whiteboard dependency.

Note that your runee may be different if you chose to use a higher version of Java
{:.note}

However, no `runbundles` is currently listed(i.e. the actual bundles needed at runtime to run the `light` smart behaviour). But the bndrun will be automatically resolved using the `bnd-resolver-maven-plugin` to generate the runtime dependencies. Now we use the `package` goal to check that the code compiles and can be successfully packaged into a bundle.

Build the `eventing-example` project:
```bash
~/eventing-example $ mvn package

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
If we had tests or other post-packaging checks then we could have used the `verify` goal instead.
{:.note}
If the package fails then check your code and try again. Once you can package it cleanly then continue to the next stage.
{:.warning}

You can see each module has been packaged as an OSGi bundle in their `target` directories. Check the output of the `light.impl` module:

```bash
~/eventing-example $ cd light.impl
~/eventing-example/light.impl $ ls target
classes                                                  maven-status
generated-sources                                        smart-behaviour
generated-test-sources                                   surefire-reports
light.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar  test-classes
light.impl-0.0.1-SNAPSHOT.jar                            tmp
maven-archiver
```
In the `light.impl/target` folder, there is the `light.impl-0.0.1-SNAPSHOT.jar` module output bundle, but also the `light.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar` smart behaviour jar, and a `smart-behaviour` folder with all the gathered dependencies for the `light` smart behaviour and a an OSGi repository index file is generated from the dependencies. The `index.xml` can be used for resolving the smart behaviour at runtime. You can also use the `mvn install` to deploy the jars in maven local repository for creating a marketplace later.

Check the content of the output bundle, run:

```bash
~/eventing-example/light.impl $ bnd print target/light.impl-0.0.1-SNAPSHOT.jar
[MANIFEST light.impl-0.0.1-SNAPSHOT]
......                                    
Bundle-Description                       "The light.impl DS component - built using OSGi enRoute R7"
Bundle-ManifestVersion                   2                                       
Bundle-Name                              light.impl                              
Bundle-SymbolicName                      com.paremus.brain.iot.example.light.impl
Bundle-Version                           0.0.1.202009251653                      
Created-By                               1.8.0_221 (Oracle Corporation)
Import-Package: com.paremus.brain.iot.example.light.api;version="[1.0,2)"....
Require-Capability:....
Service-Component: OSGI-INF/com.paremus.brain.iot.example.light.impl.RestComponentImpl.xml
Provide-Capability: osgi.service;objectClass:List<String>="com.paremus.brain.iot.example.light.impl.RestComponentImpl,
        eu.brain.iot.eventing.api.SmartBehaviour",
        eu.brain.iot.behaviour;consumed:List<String>="com.paremus.brain.iot.example.light.api.LightCommand,
        com.paremus.brain.iot.example.light.api.LightQuery";author=Paremus;name="Example Smart Light Bulb";
        description="Implements a Smart Light Bulb and UI to display it."
Service-Component: OSGI-INF/com.paremus.brain.iot.example.light.impl.RestComponentImpl.xml
```
As shown, the event types to be consumed by the bundle are listed in the `consumed` property.

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

### Smart Behaviour
The `light.impl` module is also packaged as a smart behaviour jar using the `smart-behaviour-maven-plugin`.

Check the content of the `light` smart behaviour jar:

```bash
~/eventing-example/light.impl $ bnd print target/light.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar

[MANIFEST light.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour]
BRAIN-IoT-Deploy-Requirement     osgi.identity;filter:="(osgi.identity=com.paremus.brain.iot.example.light.impl)",
                                 osgi.identity;filter:="(osgi.identity=org.apache.aries.jax.rs.jackson)"
BRAIN-IoT-Smart-Behaviour-SymbolicName   com.paremus.brain.iot.example.light.impl
BRAIN-IoT-Smart-Behaviour-Version        0.0.1.SNAPSHOT                          
Build-Jdk                                1.8.0_221                                                                    
Created-By                               Apache Maven 3.3.9                      
Manifest-Version                         1.0
```
Each smart behaviour will have some OSGi headers. The value of `BRAIN-IoT-Deploy-Requirement` header is drived from the `-runrequires` instruction in the bndrun file (e.g. `light.bndrun` here), and the bundles specified in this header will be resolved and installed together with their dependencies at runtime. If the `smart-behaviour-maven-plugin` is not configured with any `bndrun` file, then the default bundle must be deployed is the project output jar.

## Create the Sensor and Controller Smart Behaviours
Let's create sub-modules `sensor.api`, `sensor.impl` and `behaviour.impl` in `eventing-example` parent project. The controller Smart Behaviour is named as `behaviour.impl`.

### Create Sensor API Module

Change directory into the parent `eventing-example` project directory, then create an API module `sensor.api` using the [api Archetype](https://enroute.osgi.org/about/112-enRoute-Archetypes.html#the-api-archetype){:target="_blank"} as shown:
```bash
~ $ cd eventing-example
~/eventing-example $ mvn archetype:generate \
     -DarchetypeGroupId=org.osgi.enroute.archetype \
     -DarchetypeArtifactId=api \
     -DarchetypeVersion=7.0.0
```
with the following values:
```bash
Define value for property 'groupId': com.paremus.brain.iot.example
Define value for property 'artifactId': sensor.api
Define value for property 'version' 1.0-SNAPSHOT: : 0.0.1-SNAPSHOT
Define value for property 'package' com.paremus.brain.iot.example.sensor.api: : 
Confirm properties configuration:
groupId: com.paremus.brain.iot.example
artifactId: sensor.api
version: 0.0.1-SNAPSHOT
package: com.paremus.brain.iot.example.sensor.api
 Y: :
```
This API module defines the Events via the use of [Data Transfer Objects (DTOs)](https://enroute.osgi.org/FAQ/420-dtos.html){:target="_blank"} transfered between `sensor` smart behaviour and others.

#### Dependencies

Similar with the Light API module, open pom.xml of `sensor.api` and add following `eventing.api` dependency:

```xml
<dependency>
    <groupId>com.paremus.brain.iot</groupId>
    <artifactId>eventing.api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
#### Visibility
The `sensor.api` package is also exported by the automatically generated file `~/eventing-example/sensor.api/src/main/java/com/paremus/brain/iot/example/sensor/api/package-info.java`.

#### Sensor Events
Remove original auto-generated java classes in src package:

`~/eventing-example/sensor.api/src/main/java/com/paremus/brain/iot/example/sensor/api/ProviderInterface.java`

`~/eventing-example/sensor.api/src/main/java/com/paremus/brain/iot/example/sensor/api/ConsumerInterface.java`

and create the following file:

`~/eventing-example/sensor.api/src/main/java/com/paremus/brain/iot/example/sensor/api/AbstractLightDTO.java`

<p>
  <a class="btn btn-primary" data-toggle="collapse" href="#SensorReadingDTO" aria-expanded="false" aria-controls="SensorReadingDTO.java">SensorReadingDTO.java</a>
</p>

<div class="collapse" id="SensorReadingDTO">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/SensorReadingDTO.java %}
{% endhighlight %}
  </div>
</div>

### Create Sensor Implementation Module
Go to `eventing-example` project directory, create the `sensor.impl` module using the [rest-component Archetype](https://enroute.osgi.org/about/112-enRoute-Archetypes.html#the-rest-component-archetype){:target="_blank"}:

```bash
~/eventing-example $ mvn archetype:generate \
     -DarchetypeGroupId=org.osgi.enroute.archetype \
     -DarchetypeArtifactId=rest-component \
     -DarchetypeVersion=7.0.0
```
with the following values:
```bash
Define value for property 'groupId': com.paremus.brain.iot.example
Define value for property 'artifactId': sensor.impl
Define value for property 'version' 1.0-SNAPSHOT: : 0.0.1-SNAPSHOT
Define value for property 'package' com.paremus.brain.iot.example.sensor.impl: : 
Confirm properties configuration:
groupId: com.paremus.brain.iot.example
artifactId: sensor.impl
version: 0.0.1-SNAPSHOT
package: com.paremus.brain.iot.example.sensor.impl
 Y: :
```
#### Dependencies
Replace the auto-genearted POM in this module with the following, the content is similar with the `light.impl` POM with the build configuartion:
<p>
  <a class="btn btn-primary" data-toggle="collapse" href="#sensor-pom" aria-expanded="false" aria-controls="sensor-pom">pom.xml</a>
</p>

<div class="collapse" id="sensor-pom">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/sensor-pom.xml %}
{% endhighlight %}
  </div>
</div>

#### Implement the `sensor` smart behaviour
Overwrite original auto-generated java class in src package for the sensor smart behaviour implementation:

`~/eventing-example/sensor.impl/src/main/java/com/paremus/brain/iot/example/sensor/impl/RestComponentImpl.java`

<p>
  <a class="btn btn-primary" data-toggle="collapse" href="#sensor-RestComponentImpl" aria-expanded="false" aria-controls="sensor-RestComponentImpl.java">RestComponentImpl.java</a>
</p>

<div class="collapse" id="sensor-RestComponentImpl">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/sensor-RestComponentImpl.java %}
{% endhighlight %}
  </div>
</div>
The `sensor` smart behaviour is designed to just send the `SensorReadingDTO` trigger event, so its `consumed` property of the `@SmartBehaviourDefinition` annotation is empty.
{:.note}

In addition, place the following `sensor.bndrun` file in `~/eventing-example/sensor.impl/` for configuration of `smart-behaviour-maven-plugin`.
<p>
  <a class="btn btn-primary" data-toggle="collapse" href="#sensor.bndrun" aria-expanded="false" aria-controls="sensor.bndrun">sensor.bndrun</a>
</p>

<div class="collapse" id="sensor.bndrun">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/sensor.bndrun %}
{% endhighlight %}
  </div>
</div>

#### Sensor REST Service Implementation
Create the directory `~/eventing-example/sensor.impl/src/main/resources` and copy the downloaded `sensor.impl/src/main/resources/static` folder which is the implementation of the sensor web page to the same place of current `sensor.impl` module (i.e. `~/eventing-example/sensor.impl/src/main/resources/static`).
```bash
~/eventing-example $ ls light.impl/src/main/resources/static
css  img  index.html  js  lib
```

Now the sensor smart behaviour is ready to be built using `mvn package` command as shown before.

### Create Controller Implementation Module
Go to `eventing-example` project directory, create the `behaviour.impl` module using the [ds-component Archetype](https://enroute.osgi.org/about/112-enRoute-Archetypes.html#the-ds-component-archetype){:target="_blank"}:

```bash
~/eventing-example $ mvn archetype:generate \
     -DarchetypeGroupId=org.osgi.enroute.archetype \
     -DarchetypeArtifactId=ds-component \
     -DarchetypeVersion=7.0.0
```
with the following values:
```bash
Define value for property 'groupId': com.paremus.brain.iot.example
Define value for property 'artifactId': behaviour.impl
Define value for property 'version' 1.0-SNAPSHOT: : 0.0.1-SNAPSHOT
Define value for property 'package' com.paremus.brain.iot.example.behaviour.impl: : 
Confirm properties configuration:
groupId: com.paremus.brain.iot.example
artifactId: behaviour.impl
version: 0.0.1-SNAPSHOT
package: com.paremus.brain.iot.example.behaviour.impl
 Y: :
```

#### Dependencies

Replace the auto-genearted POM in this module with the following, specifially, the `light.api` and `sensor.api` bundles are its dependencies:
<p>
  <a class="btn btn-primary" data-toggle="collapse" href="#behavior-pom" aria-expanded="false" aria-controls="behavior-pom">pom.xml</a>
</p>

<div class="collapse" id="behavior-pom">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/behavior-pom.xml %}
{% endhighlight %}
  </div>
</div>

#### Implement the `behaviour.impl` smart behaviour
Overwrite auto-generated `ComponentImpl.java` class in src package:

`~/eventing-example/behaviour.impl/src/main/java/com/paremus/brain/iot/example/behaviour/impl/ComponentImpl.java`
<p>
  <a class="btn btn-primary" data-toggle="collapse" href="#ComponentImpl" aria-expanded="false" aria-controls="ComponentImpl.java">ComponentImpl.java</a>
</p>

<div class="collapse" id="ComponentImpl">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/ComponentImpl.java %}
{% endhighlight %}
  </div>
</div>

Now the `behaviour.impl` smart behaviour is ready to be built using `mvn package` command as shown before.

## Build the System

Now build and install all smart behaviours together from the parent root directory:
```bash
~/eventing-example $ mvn clean install

[INFO] Scanning for projects...
[INFO] Copying .....
[INFO] Processing ~/eventing-example/*/*.bndrun for dependencies
[INFO] Building jar: ~/eventing-example/light.impl/target/light.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar

[INFO] Installing ~/eventing-example/light.impl/target/light.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar to ~/.m2/repository/com/paremus/brain/iot/example/light.impl/0.0.1-SNAPSHOT/light.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar
......
[INFO] Building jar: ~/eventing-example/sensor.impl/target/sensor.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar

[INFO] Installing ~/eventing-example/sensor.impl/target/sensor.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar to ~/.m2/repository/com/paremus/brain/iot/example/sensor.impl/0.0.1-SNAPSHOT/sensor.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar
......
[INFO] Building jar: ~/eventing-example/behaviour.impl/target/behaviour.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar

[INFO] Installing ~/eventing-example/behaviour.impl/target/behaviour.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar to ~/.m2/repository/com/paremus/brain/iot/example/behaviour.impl/0.0.1-SNAPSHOT/behaviour.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
As shown in the logs, finally there are three smart behaviurs generated in the implementation modules.

The `install` goal will install the smart behaviors together with the OSGi bundles in Maven local repository (~/.m2), which will be used when creating the marketplace, as shown later in this page. Alternativly, the smart behaviours can be deployed to a respository server, and add this repository in the marketplace project.
{:.note}

If there is already a smart behaviour has been generated before rebuilding the project, remember to run the `clean` goal before the `package` goal to remove the existing `smart-behaviour` folder from the project `target` directory to avoid failure when copying the denpendencies.
{:.warning}

## The Composite Application
We now pull these Modules together to create the Composite Application to test the whole system work well before deploying it to BRAIN-IoT Fabric.

In the `eventing-example` project directory, create the `single-framework-example` application module using the [application Archetype](https://enroute.osgi.org/about/112-enRoute-Archetypes.html#the-application-archetype){:target="_blank"}:

```bash
~/eventing-example $ mvn archetype:generate \
     -DarchetypeGroupId=org.osgi.enroute.archetype \
     -DarchetypeArtifactId=application \
     -DarchetypeVersion=7.0.0
```
with the following values:
```bash
Define value for property 'groupId': com.paremus.brain.iot.example
Define value for property 'artifactId': single-framework-example
Define value for property 'version' 1.0-SNAPSHOT: : 0.0.1-SNAPSHOT
Define value for property 'package' com.paremus.brain.iot.example: : 
Define value for property 'impl-artifactId': light.impl
Define value for property 'impl-groupId' com.paremus.brain.iot.example: : 
Define value for property 'impl-version' 0.0.1-SNAPSHOT: : 
[INFO] Using property: target-java-version = 8
Confirm properties configuration:
groupId: com.paremus.brain.iot.example
artifactId: single-framework-example
version: 0.0.1-SNAPSHOT
package: com.paremus.brain.iot.example
impl-artifactId: light.impl
impl-groupId: com.paremus.brain.iot.example
impl-version: 0.0.1-SNAPSHOT
target-java-version: 8
 Y: :
```
### Dependencies
Replace the auto-genearted POM in this module with the following:
 the content is similar with the `light.impl` POM with the build configuartion:
<p>
  <a class="btn btn-primary" data-toggle="collapse" href="#singleFW-pom" aria-expanded="false" aria-controls="singleFW-pom">pom.xml</a>
</p>

<div class="collapse" id="singleFW-pom">
  <div class="card card-block">
{% highlight properties %}
{% include smart-behaviours/singleFW-pom.xml %}
{% endhighlight %}
  </div>
</div>
Apart from the `light.impl` bundle, the `sendor.impl` and the `behaviour.impl` bundles should also be the dependencies for the application module.
{:.note}

### Define Runtime Entity

Our Security Light System is composed of the following elements:

*  A light smart behaviour
*  A sensor smart behaviour
*  A behaviour controller smart behaviour
*  JAX-RS whiteboard resources for web pages

These dependencies are expressed as runtime Requirements in the `~/eventing-example/single-framework-example/single-framework-example.bndrun` file.

Overwrite the content of the `single-framework-example.bndrun` from:
```bash
index: target/index.xml;name="single-framework-example"

-standalone: ${index}

-runrequires: osgi.identity;filter:='(osgi.identity=com.paremus.brain.iot.example.light.impl)'
-runfw: org.eclipse.osgi
-runee: JavaSE-1.8

-resolve.effective: active
```
to:
```bash
index: target/index.xml;name="single-framework-example"

-standalone: ${index}

-runrequires: \
	osgi.identity;filter:='(osgi.identity=com.paremus.brain.iot.example.behaviour.impl)',\
	bnd.identity;id='com.paremus.brain.iot.example.light.impl',\
	bnd.identity;id='com.paremus.brain.iot.example.sensor.impl',\
	bnd.identity;id='org.apache.aries.jax.rs.jackson'
-runfw: org.eclipse.osgi
-runee: JavaSE-1.8

-resolve.effective: active
```

Now build to generate the index, test index, and resolve this bndrun (you should be able to see the changes in the runbundles list afterwards). Then run whole system in the `eventing-example` root directory as done in the begining of this tutorial.
```bash
~/eventing-example $ cd single-framework-example
~/eventing-example/single-framework-example $ mvn bnd-indexer:index bnd-indexer:index@test-index bnd-resolver:resolve
~/eventing-example/single-framework-example $ cd ..
~/eventing-example $ mvn clean verify
~/eventing-example $ java -jar single-framework-example/target/single-framework-example.jar
```
If the `resolve` fails then check your code and try again. Once you can package it cleanly then continue to the next stage.
{:.warning}


## Create a Marketplace
BRAIN-IoT provides the `behaviour-marketplace-maven-plugin` for creating a marketplace. A marketplace gathers and unpackages all smart behaviours jars of a system into the `marketplace` directory in project `target` folder. In addition, there are a `index.xml` will be generated for resolving the marketplace and a marketplace site `index.html` file. 

The development of the Security Light System is done, here we'll create a marketplace for it.

Create a minimal enRoute maven project `security-light-marketplace` at `HOME` folder using the [bare-project Archetype](https://enroute.osgi.org/about/112-enRoute-Archetypes.html#the-project-archetype){:target="_blank"}:
```bash
~/eventing-example $ cd
~$ mvn archetype:generate \
     -DarchetypeGroupId=org.osgi.enroute.archetype \
     -DarchetypeArtifactId=project-bare \
     -DarchetypeVersion=7.0.0
```
with the following values:
```bash
Define value for property 'groupId': com.paremus.brain.iot.marketplace
Define value for property 'artifactId': security-light-marketplace
Define value for property 'version' 1.0-SNAPSHOT: : 0.0.1-SNAPSHOT
Define value for property 'package' com.paremus.brain.iot.marketplace: : 
Confirm properties configuration:
groupId: com.paremus.brain.iot.marketplace
artifactId: security-light-marketplace
version: 0.0.1-SNAPSHOT
package: com.paremus.brain.iot.marketplace
 Y: :
```

### Dependencies
Open pom and change the `<bnd.version>` to
```xml
<bnd.version>4.2.0</bnd.version>
```
Similar with the POM setup of `eventing.example` project above, append the following repositories in `<repositories>` section for downloading the BRAIN-IoT runtime dependencies:

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
and append the following plugin repositories in `<pluginRepositories>` section for downloading the BRAIN-IoT `behaviour-marketplace-maven-plugin`:
```xml
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
```
Then create the new `<dependencies>` section and add the `light.iml`, `sensor.impl`, `behaviour.impl` OSGi artifacts: 
```xml
    <dependencies>
        <dependency>
            <groupId>com.paremus.brain.iot.example</groupId>
            <artifactId>behaviour.impl</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.paremus.brain.iot.example</groupId>
            <artifactId>light.impl</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.paremus.brain.iot.example</groupId>
            <artifactId>sensor.impl</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
    </dependencies>
```
### Build
Create the new `<build>` section and add the `behaviour-marketplace-maven-plugin` plugin to build the marketplace:
```xml
    <build>
        <plugins>
            <plugin>
            <groupId>com.paremus.brain.iot.maven</groupId>
				<artifactId>behaviour-marketplace-maven-plugin</artifactId>
				<version>0.0.1-SNAPSHOT</version>
				<configuration>
				</configuration>
				<executions>
					<execution>
						<goals>
							<goal>generate</goal>
						</goals>
					</execution>
				</executions>
            </plugin>
        </plugins>
    </build>
``` 
```bash
~ $ cd security-light-marketplace
~/security-light-marketplace $ mvn package
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building security-light-marketplace 0.0.1-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] --- behaviour-marketplace-maven-plugin:0.0.1-SNAPSHOT:generate (default) @ security-light-marketplace ---
......
[INFO] Unpacking ~/.m2/repository/com/paremus/brain/iot/example/behaviour.impl/0.0.1-SNAPSHOT/behaviour.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar to ~/security-light-marketplace/target/marketplace/behaviour.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour-jar ......
[INFO] Unpacking ~/.m2/repository/com/paremus/brain/iot/example/light.impl/0.0.1-SNAPSHOT/light.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar to ~/security-light-marketplace/target/marketplace/light.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour-jar ......
[INFO] Unpacking ~/.m2/repository/com/paremus/brain/iot/example/sensor.impl/0.0.1-SNAPSHOT/sensor.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour.jar to ~/security-light-marketplace/target/marketplace/sensor.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour-jar ......
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
The logs indicated that the smart behaviours are unpacked from the maven local Repo (~/.m2) to the `target` directory folder
{:.note}

Enter the `target` folder and check the geenrated marketplace:
```bash
~/security-light-marketplace $ ls target/marketplace
behaviour.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour-jar  light.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour-jar
index.html                                                   sensor.impl-0.0.1-SNAPSHOT-brain-iot-smart-behaviour-jar
index.xml
```
To make the marketplace index available for configuring the Behaviour Management Service from the BRAIN-IoT UI as shown in the [Quick Start](05-quickstart.html) tutorial, the marketplace site has to be deployed to a HTTP server. The marketplace index will be also used to create a `system document` as shown in next [Destributed Deployment](20-distributed.html) tutorial.

### Deploy Marketplace to a Local HTTP server

Copy the `marketplace` folder from `target` directory to the root of the HTTP server, the marketplace index is `https://localhost/marketplace/index.xml`
```bash
~/security-light-marketplace $ sudo cp -rf target/marketplace /var/www/html
~/security-light-marketplace $ ls /var/www/html
marketplace
```

### Deploy to a Nexus server
The Security Light Marketplace is now deployed on the BRAIN-IoT Nexus server. Open the `security-light-marketplace` POM, then

Add new `<distributionManagement>` section, then add the site URL:
```xml
	<distributionManagement>
		<site>
			<id>brain-iot-nexus-marketplace</id>
			<url>dav:https://nexus.repository-pert.ismb.it/repository/marketplaces/${project.groupId}/${project.artifactId}/${project.version}</url>
		</site>
	</distributionManagement>
```
Append the `org.apache.maven.plugins:maven-site-plugin` in the `<build>` section to deploy the the generated site to site URL specified in pom:
```xml
            <plugin>
                <artifactId>maven-site-plugin</artifactId>
                <version>3.4</version>
                <dependencies>
                    <dependency>
                        <groupId>org.apache.maven.wagon</groupId>
                        <artifactId>wagon-webdav-jackrabbit</artifactId>
                        <version>2.8</version>
                    </dependency>
                </dependencies>
                <configuration>
                    <skip>true</skip>
                    <inputDirectory>${project.build.directory}/marketplace</inputDirectory>
                </configuration>
            </plugin>
```
Run the following command to deploy the site:
```bash
~/security-light-marketplace $ mvn clean verify site:deploy
```
After deployment is successful, then the marketplace index `https://nexus.repository-pert.ismb.it/repository/marketplaces/com.paremus.brain.iot.example/security-light-marketplace/0.0.1-SNAPSHOT/index.xml` is avilable.


## End

That completes this tutorial.
