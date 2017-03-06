# xj

Music Audio Composite Fabrication Platform

## Chain Work

This term refers (in the **xj** universe) to a layer of work performed on the Links (sequentially, by their offset) in a Chain.

# Usage

## Getting Started

Setup workflow, build and package Java server-side application and build Web front-end user interface:

    make

The preceding command will also create a blank environment variables file called **runtime.env** which is never checked in to version control or released with the distribution. It is up to you, the developer, to obtain keys and fill in the values of your own environment variables. Because the application only has **one single common bootstrap** (located at bin/common/bootstrap) the use of environment variables is federated across development and production deployments, while all actual configurations are kept outside the scope of the code.

We use [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/) for local development with uncanny parity to production. Once your **runtime.env** file is configured, it's time to bring up the `hub01xj1` server and its supporting resources such as `mysql01xj1` and `redis01xj1`:

    docker-compose up -d
    
In the above example, `-d` tells Docker to start the containers in the background (as Daemons).

You'll want to have hostnames defined to point at the docker-compose network. To automatically update /etc/hosts:

    sudo bin/update-hosts

Your **/etc/hosts** file should now contain these lines at the end:

    172.16.217.10 hub01xj1
    172.16.217.10 xj.outright.dev
    172.16.217.50 mysql01xj1
    172.16.217.60 redis01xj1

Assuming the docker containers are up and the hosts configured, you'll be able to open the main UI in a browser at [http://xj.outright.dev/](http://xj.outright.dev/)

The front-end UI is served by the Nginx server on `hub01xj1` via the local `ui/dist` volume. During development, use the [Ember CLI](https://ember-cli.com/) to keep the front-end continuously re-built in real time:
 
    cd ui
    ember build --watch

To compile the Java server-side applications and package them for deployment:

    bin/package
    
To build and deploy the platform during local development, we run this a lot:

    docker rm -f hub01xj1 && \
      bin/package && \
      docker-compose up -d

The data on `mysql01xj1` and `redis01xj1` persists until those containers are explicitly destroyed. 

Attach to the shell on the main server `hub01xj1` while it's running, and tail the logs:

    docker attach hub01xj1
    # now inside shell
    tail -f /var/log/nginx/*.log /var/log/hub/*.log
    
After logging in via Google, there will be a user created with an `id` of 1. To grant the `admin` user role, you'll connect directly to the database on `mysql01xj1`:
 
    mysql -uroot -hmysql01xj1 xj
    # now inside mysql shell 
    insert into user_role (user_id, type) values (1, "admin"); 

Only between major platform configuration changes (e.g. to **.nginx/locations.conf**), it may be necessary to force Docker to rebuild the container using `--build`:

    docker-compose up -d --build    

## Additional commands

To only setup the workflow and check dependencies:

    bin/setup

## Environment (System) properties

To see all Java `System.getProperty` references in project modules:

    bin/props
    
The default java properties are in the file **/default.env** which is copied to a new file **/runtime.env** on project setup. Developers modify their local runtime.env file with private keys and configuration. The runtime.env file is never committed to the repository. The **default.env** file is kept up-to-date with all environment variables expected by **bin/common/bootstrap**.

## Run local platform in Docker containers

Before running the docker container, be sure to package the latest Java build artifacts, with `make` or `bin/package`.

Bring up the `hub01xj1` docker container (with an Nginx server on port 80, which proxies backend requests to its own Hub via port 8042) and its required resource containers:

    docker-compose up -d

The `-d` option above runs containers as background daemons, instead of seeing all their `stdin`. Use `docker-compose` or `docker` to manage containers from there.

To see running containers:

    docker ps --format 

To attach to a container by `<name>`:

    docker attach <name>

To stop all containers:

    docker-compose down

To remove all containers:

    docker-compose rm

To bring up containers with a forced build: 

    docker-compose up --build

To run just the `hub01xj1` container, attached via tty:

    docker-compose run xj

The configuration uses volumes such that the latest build artifacts are available without having to rebuild the docker container. The container runs as user `root` by default. Project folders are available inside the container as:

    /var/app/current/

There is only one Nginx server locations configuration, shared by the local Docker configuration and the production AWS Elastic Beanstalk configuration:

    /.nginx/locations.conf

The front-end user interface (ui) is served via Nginx from the local ui/dist/ folder. While developing (and the docker-compose containers are up), run the Ember build in watch-mode to keep the front-end ui rebuilt as your local code changes automatically:

    cd ui
    ember build --watch

## Compile server-side platform

Compile the Java server-side application:

    bin/build

Compile & Install the Java server-side application:

    bin/install

Compile & Package the Java server-side application, e.g. as JAR files:

    bin/package

## MySQL database

By default, you'll need to create two local MySQL databases: 

  * `xj` (for running services locally)
  * `xj_test` (for running integration tests locally)

These databases must be accessible by user `root`@`localhost`.

## Integration testing

We use `maven-failsafe` to kick off integration tests. There's a helper script:

    bin/verify
    
Also, the integration test suite is run by default during a `bin/package` or `bin/release` and will block the build if integration tests fail.

Integration testing requires a MySQL database `xj_test` locally, as well as a Redis server.

## Database migration

Migrate the local database (not usually necessary; migration happens automatically on application start):

    bin/migrate

## Web user interface (UI)    

Build the Web user interface:

    bin/ui-build

Build and serve (with live updates) the Web user interface:

    bin/ui-serve

## Run local platform manually

Run a local **Hub** on its default port 8042:

    bin/hub    

Run a local **Craftworker** on its default port 8043:

    bin/craftworker    

Run a local **Dubworker** on its default port 8044:

    bin/dubworker    

## Release Java platform for deployment to AWS Elastic Beanstalk

Release as a zip file (e.g. target/xj-release-2016.12.05-UTC.00.37.07.zip) containing the Procfile and shaded .JAR files required to run the application.

    bin/release

The production deployment procedure is:

  1. Release platform as .zip file.
  2. Upload .zip file as "new application version" to `xj` application.
  3. Deploy the new version to the `prod-hub` environment, and confirm migrations run successfully.
  4. Deploy the new version to the `prod-work` environment.
  5. Monitor health checks until confirmed consistently OK.  

## Cleanup

Clean all build targets:

    bin/clean

Clean even more thoroughly "down to the distribution":

    bin/clean-distro

Clean away all IntelliJ IDEA related files:

    bin/clean-idea

## Maven

To clean, build and install all artifacts:

    mvn clean install

To clean, build and package artifacts for shipment:

    mvn clean package

To run local migrations (in the `core` submodule via the Flyway plugin):

    mvn flyway:migrate

## DNS

To automatically update /etc/hosts:

    sudo bin/update-hosts

For development, your local machine needs to have the domain `xj.outright.dev` pointed to `172.16.217.10` (the address set for hub01xj1 in the docker-compose.yml file) in `/etc/hosts`; it's helpful to have `redis01xj1` and `mysql01xj1` as well:

    172.16.217.50 mysql01xj1
    172.16.217.60 redis01xj1
    172.16.217.10 hub01xj1
    172.16.217.10 xj.outright.dev

## Google Authentication

Login to the app using Google authentication. The redirect URL for local development is http://xj.outright.io/auth/google/callback

# Components

## ui

User interface web application. Built with Javascript, Ember, Bower, Node.

Connects to:

  * xj/hub

## core

Common models and utilities. Built with Java, Guice, Tomcat, Maven.

## hub

Central structured data and business logic. Built with Java, Guice, Tomcat, Maven.

Depends on:

  * xj/core

Connects to:

  * SQL Database
  * Filesystem

## craftworker

Fabricates a musical audio composite from source ideas and instrument-audio. Built with Java, Guice, Tomcat, Maven.

Depends on:

  * xj/core

Connects to:

  * SQL Database
  * Filesystem

## dubworker

Ships finished audio data to any destination. Built with Java, Guice, Tomcat, Maven.

Depends on:

  * xj/core

Connects to:

  * SQL Database
  * Filesystem

## z

Workflow tooling

# Mix

**Mix** is a Java implementation of the Go project [https://github.com/go-mix/mix](mix).

The project has its own [README](mix/README.md)

# App Standards

## Port

  * hub: 8042
  * craftworker: 8043
  * dubworker: 8044

## Healthcheck Endpoint

**GET /o2**

## The Dao of Database Access Objects (D.A.O.) in the xj universe

1. ***All DAO methods require an AccessControl object!*** 

# References

## Intro to Google OAuth2

https://developers.google.com/+/web/samples/java

## Intro to Jersey and Grizzly2

See [Java SE 8: Creating a Basic REST Web Service using Grizzly, Jersey, and Maven](http://www.oracle.com/webfolder/technetwork/tutorials/obe/java/griz_jersey_intro/Grizzly-Jersey-Intro.html)

Bootstrap a Grizzly2 quickstart with:

mvn archetype:generate -DarchetypeArtifactId=jersey-quickstart-grizzly2 -DarchetypeGroupId=org.glassfish.jersey.archetypes -DinteractiveMode=false -DgroupId=io.outright.xj -DartifactId=strap -Dpackage=io.outright.xj.strap -DarchetypeVersion=2.17

## AWS Elastic Beanstalk

The `.ebextensions` and `.ebsettings` folder contain configurations proprietary to [AWS Elastic Beanstalk](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/java-tomcat-platform.html#java-tomcat-proxy).

## Jersey

[Latest User Guide](https://jersey.java.net/documentation/latest/user-guide.html)
