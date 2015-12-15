[![Build Status](https://travis-ci.org/nhl/bootique.svg)](https://travis-ci.org/nhl/bootique)

Bootique is a minimally opinionated technology for building single-jar runnable Java applications of any kind. With Bootique you can create and run REST services, webapps, jobs, etc. as if they were simple commands. No JEE container required!

Bootique was inspired by two similar products - [Dropwizard](http://www.dropwizard.io) and [SpringBoot](http://projects.spring.io/spring-boot/), however its focus is different. Bootique favors modularity and clean pluggable architecture.

Bootique is built on top of [Google Guice](https://github.com/google/guice) DI container, that is the core of its modularity mechanism.

## Getting Started

Declare Bootique Maven repository in your pom.xml (unless you have your own repo proxy, in which case add this repo to the proxy):

```XML
<repositories>
    <repository>
        <id>bq-repo</id>
        <name>Bootique Repo</name>
        <url>http://maven.objectstyle.org/nexus/content/repositories/bootique</url>
    </repository>
</repositories>
```
_TODO: eventually we'll start publishing Bootique to Central, so the step above will not be needed._

Add Bootique dependency:

```XML
<dependency>
	<groupId>com.nhl.bootique</groupId>
	<artifactId>bootique</artifactId>
	<version>0.7</version>
</dependency>
<!-- Below any number of Bootique extensions -->
```
Write a main class that configures app's own DI Module, builds extensions Modules and starts Bootique app. In the example below we are setting up a JAX-RS application and the application class also serves as a JAX-RS resource:

```Java
@Path("/hello")
@Produces("text/plain")
@Singleton
public class LRApplication {

	public static void main(String[] args) throws Exception {

		Module jetty = JettyBundle.create().context("/").port(3333).module();
		Module jersey = JerseyBundle.create().packageRoot(LRApplication.class).module();
		Module logback = LogbackBundle.logbackModule();

		Bootique.app(args).modules(jetty, jersey, logback).run();
	}
	
	@Args
	@Inject
	private String[] args;

	@GET
	public String get() {

		String allArgs = Arrays.asList(args).stream().collect(joining(" "));
		return "Hello! The app args were: " + allArgs;
	}
}
```

Now you can run it in your IDE, and open [http://127.0.0.1:3333/hello/](http://127.0.0.1:3333/hello/) in the browser.

## YAML Config

## YAML Config Property Overrides
