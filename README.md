# XJ Music™

Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

**Composite Music Fabrication Platform**

Also see: Documents in `site/docs` which are published to https://docs.xi.io
# Laws

  * There is only one code repository.
  * This is the ultimate README in the one and only code repository.
  * Any network connection can and will fail.
  * There are no launches, pertaining instead only to the spanning of time, and the availability of said platform and its components.
  * The platform does not implement passwords; it relies on OAuth.
  * The platform does not send or receive email; it relies on vendors for all external communications.
## Workflow

  * Features are described as the desire of a person to take an action for a particular reason, e.g. "Artist wants Sequence and Pattern to be named according to musical norms, in order to make the most sense of XJ as a musical instrument."
  * Bugs are described as expectation versus actual, e.g. "Artist expects to be able to list Audios after deleting an Audio from an Instrument," then:
    - DESCRIBE LIKE THIS: "I clicked the button labeled 'Turn;' I expected the Earth to turn; actually, it stood still."
    - NOT LIKE THIS: "I click the button and nothing happened."
  * Commits reference issues by id #.
  * Time is tracked against issues by id #.
  * Branches are named according to `git-flow` including issue id #, e.g.:
    - Features are `feature/123-do-new-thing`
    - Bug Fixes are `bugfix/4567-should-do-this`
    - Hot Fixes are `hotfix/890-should-do-that`
  * `TODO` comments are used only in working branches. Upon completion of branch work, any remaining `TODO` should be a new tracker issue.



## Chain Work

This term refers (in the **xj** universe) to a layer of work performed on the Segments (sequentially, by their offset) in a Chain.



## Excellent JavaScript testing with Jest

All front-end JavaScript-based web UI is tested at once using a single command executed from the project root:

    jest
    
Or, to launch the active development interface:

    jest --watch
   
NOTE: Hub UI still has Ember tests. See: [#158271368](https://www.pivotaltracker.com/story/show/158271368) Developer wants Hub UI to be tested from Jest for excellent testing experience.



# Usage



## Getting Started

Setup workflow, build and package Java server-side application and build Web front-end user interface:

    make

The preceding command will also create a blank environment variables file called **.env** which is never checked in to version control or released with the distribution. It is up to you, the developer, to obtain keys and fill in the values of your own environment variables. Because the application only has **one single common bootstrap** (located at bin/common/bootstrap) the use of environment variables is federated across development and production deployments, while all actual configurations are kept outside the scope of the code.

We use [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/) for local development with uncanny parity to production. Once your **.env** file is configured, it's time to bring up the `hub01xj1` server and its supporting resources such as `postgres01xj1` and `redis01xj1`:

    docker-compose up -d

In the above example, `-d` tells Docker to start the containers in the background (as Daemons).

Note that `localhost` simply points to the local loopback. Docker-compose maps maps local port 80 to the `hub01xj1` docker container port 80.

Assuming the docker containers are up and the hosts configured, you'll be able to open the main UI in a browser at [http://localhost/](http://localhost/)

The front-end UI is served by the Nginx server on `hub01xj1` via the local `ui/hub-ui/dist` volume. During development, use the [Ember CLI](https://ember-cli.com/) to keep the front-end continuously re-built in real time:

    cd ui/hub-ui
    ember build --watch

Preferably, use the script from the project root, to build and watch the UI during dev:

    bin/ui/dev

To compile the Java server-side applications and package them for deployment:

    bin/package

To build and deploy the platform during local development, we run this a lot:

    bin/package && docker restart hub01xj1 worker01xj1

For a complete rebuild, including configurations and front-end, we could run:

    docker compose up -d --build

The data on `postgres01xj1` and `redis01xj1` persists until those containers are explicitly destroyed.

Tail the docker container logs for the `hub` app while it's running (/var/log in the container is mounted from local volume ./log):

    tail -f log/hub/*

Or tail container logs for the `worker` app:

    tail -f log/worker/*

You'll need to install the Postgresql client `psql` version 12, e.g. `postgresql-client-12` (ubuntu linux)

After logging in via Google, there will be a user created for you. It will have an `id`, for example 21. To grant the `admin` user role, you'll connect directly to the database on `postgres01xj1` using the port forwarding from local port 5400 (to Docker Postgres container port 5432):

    psql -h localhost -p 5400 -u root

Even better than ^^^, there's a convenience script to easily connect to the Postgres database in the Docker container.

    bin/sql/connect

And inside psql shell, for example to impersonate user #1 (after being auto-logged-in as new user #21):

    use xj;
    update user_auth set user_id=1 where user_id=21;
    
There's a convenience script to instantly perform the above operation:

    bin/sql/user_auth

Only between major platform configuration changes (e.g. to **.nginx/locations.conf**), it may be necessary to force Docker to rebuild the container using `--build`:

    docker-compose up -d --build    

There is a Postgresql dump of a complete example database, for quickly bootstrapping a dev environment. These files are located in `/.sqldump/*`:

Load the example database into `postgres01xj1` using the port forwarding from local port 5400 (to Docker Postgres container port 5432). There's a convenience script to do this:

    bin/sql/reset/all_local

The `/.sqldump/*` files can be quickly updated from the current dev database with this script:

    bin/sql/dump/all_local

It is NOT necessary to have any local Postgres server running. The build process will use your Docker `postgres01xj1`, or more specifically (for cross-platform compatibility) it will use port 5400 which Docker maps to `postgres01xj1` port 5432, for Maven to use during the build process.

Connect to the Docker `postgres01xj1` server:

    bin/sql/connect

You will need to create two databases in your local Postgres server, `xj_dev` and `xj_test`:

    of database xj_dev;
    of database xj_test;

*note that the latest codebase may run migrations on top of that ^^^, and of course it had better pass checksum ;)*



## XJ Website

The main website is located in **ui/xj-site/** and is published to https://xj.io

Serve the ui/xj-site site locally with:

    bin/serve-web
    
## Reference

See also the **/reference** folder. By Accessing the contents of that folder, you agree to these terms:

> Please only read that file on your machine and then delete. Please do Not email that file anywhere, or upload it to any other servers. I prefer that (because it is in its 12-month non-disclosed "provisional" state with the U.S.P.T.O.) the file only be read directly by a person with access to my VPN wherein all work pertaining to it is stored. Thanks!



## Additional commands

To only setup the workflow and check dependencies:

    bin/setup



## Environment (System) properties

To list all Java system properties:

    bin/properties

The default java properties are in the file **.config/.env.default** which is copied to a new file **/.env** on project setup. Developers modify their local .env file with private keys and configuration. The .env file is never committed to the repository. The **.config/.env.default** file is kept up-to-date with all environment variables expected by **bin/common/bootstrap**.

Also note, ***by design 100% of platform Java system properties are read via Config`***



## Run local platform in Docker containers

Before running the docker container, be sure to package the latest Java build artifacts, with `make` or `bin/package`.

Bring up the `hub01xj1` docker container (with an Nginx server on port 80, which proxies backend requests to its own Hub via port 8042) and its required resource containers:
Xj-control
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

The front-end user interface (ui/hub-ui) is served via Nginx from the local ui/dist/ folder. While developing (and the docker-compose containers are up), run the Ember build in watch-mode to keep the front-end ui rebuilt as your local code changes automatically:

    cd ui/hub-ui
    ember build --watch



## Compile server-side platform

Compile the Java server-side application:

    bin/build

Compile & Install the Java server-side application:

    bin/install

Compile & Package the Java server-side application, e.g. as JAR files:

    bin/package



## Postgres database

By default, you'll need to create two Postgres databases:

  * `xj_dev` (for running services)
  * `xj_test` (for build processes, and running integration tests)



## Redis server

The docker container `redis01xj1` exposes a Redis server on local port 6300.  There's a convenience script for connecting to it:

    bin/redis_cli

For more information on Redis and production, see [the README in the .redis/ folder](.redis/README.md).



## Integration testing

We use `maven-failsafe` to kick off integration tests. There's a helper script:

    bin/verify

Also, the integration test suite is run by default during a `bin/package` or `bin/release` and will block the build if integration tests fail.

Integration uses the Docker `postgres01xj1` and `redis01xj1` databases.



## Database migration

Migrate the local database (not usually necessary; migration happens automatically on application start):

    bin/migrate



## Web user interface (UI)    

Build the Web user interface:

    bin/ui/build

Build and serve (with live updates) the Web user interface:

    bin/ui-serve



## Run local platform manually

Run a local **Hub** on its default port 8042:

    bin/hub    

Run a local **Work** on its default port 8043:

    bin/work    



## Release Java platform for deployment to AWS Elastic Beanstalk

Release as a zip file (e.g. target/xj-release-2016.12.05-UTC.00.37.07.zip) containing the Procfile and shaded .JAR files required to run the application.

    bin/release
    
Note: `bin/release` relies on `bin/release_build` to pre-build the release, in order to facilitate manual partial rebuild. Run `bin/release --no-build` to invoke without running the pre-build script.

The production deployment procedure is:

  1. Release platform as .zip file.
  2. Upload .zip file as "new application version" to `xj` application.
  3. Deploy the new version to the `prod-hub` environment, and confirm migrations run successfully.
  4. Deploy the new version to the `prod-work` environment.
  5. Monitor health checks until confirmed consistently OK.
  6. Invalidate the CloudFront cache for hub.xj.io   



## Player UI

While docker containers are up, and running the `bin/ui/develop` script, the Player UI is accessed in a browser at a URL that looks like this:

    http://localhost/player/#embedKey=coolambience&startAtMillisUTC=1528750706104
    
It's a bare-metal JavaScript app designed for speed of delivery to customer.



## Cleanup

Clean all build targets:

    bin/clean

Clean even more thoroughly "down to the distribution":

    bin/clean_distro

Clean away all IntelliJ related files:

    bin/clean_idea



## Maven

To clean, build and install all artifacts:

    mvn clean install

To clean, build and package artifacts for shipment:

    mvn clean package

To run local migrations (in the `core` submodule via the Flyway plugin):

    mvn flyway:migrate



## Google Authentication

Login to the app using Google authentication. The redirect URL for local development is http://xj.io/auth/google/callback



# Debugging

It is helpful to be able to compile and run Java components against the Docker container resources made available by Docker Compose. Assuming that the containers are running locally and addressed properly (see the 'DNS' section above) simply include the following in the Run Configuration -> Program Arguments:

    -Dapp.url.base=http://localhost:8042/
    -Dapp.url.api=
    -Dauth.google.id=<dev google oauth client id>
    -Dauth.google.secret=<dev google oauth client secret>
    -Ddb.postgresql.host=postgres01xj1
    -Ddb.redis.host=redis01xj1

Also remember, it is necessary to send an authentication cookie in the header of API requests:

    curl -b Access-Token



# Audio File Uploading

Note that after an audio file is uploaded, it can be played back (on a GNU/Linux system) like:

    curl https://s3.amazonaws.com/xj-dev-audio/62536d52-8600-4941-ac04-a72106079610-instrument-5-audio.wav | aplay

Here are the public-facing Amazon CloudFront-backed URLs for audio files, and their respective Amazon S3 backing:

  * [https://audio.xj.io](https://audio.xj.io) is the production URL, backed by [https://xj-prod-audio.s3.amazonaws.com](https://xj-prod-audio.s3.amazonaws.com)
  * [https://audio.stage.xj.io](https://audio.stage.xj.io) is the staging URL, backed by [https://xj-stage-audio.s3.amazonaws.com](https://xj-stage-audio.s3.amazonaws.com)
  * [https://audio.dev.xj.io](https://audio.dev.xj.io) is the development URL, backed by [https://xj-dev-audio.s3.amazonaws.com](https://xj-dev-audio.s3.amazonaws.com)



# Amazon S3

The `/.sqldump/*` files are generated from data in the production environment, and refer to audio files located in the dev S3 bucket (synced from the production S3 bucket), xj-dev-audio.

Therefore, it is helpful to be able to sync the audio files from production into the dev environment.

**Note that this command will become impractical if production grows to any significant size!**

    aws s3 sync s3://xj-prod-audio/ s3://xj-dev-audio/

Note that in order to use that command, the source bucket (xj-prod-audio) must grant `s3:ListBucket` and `s3:GetObject` permission, and the target bucket (xj-dev-audio) must grant `s3:ListBucket` and `s3:PutObject` to the IAM user your AWS CLI is authenticated as.



## Environment Variables

Note that two environment variables are actually [built-in to the AWS SDK for Java](http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html), `aws.accessKeyId` and `aws.secretKey`:

    -Daws.accessKeyId=AKIAKJHFG789JKKS8F73
    -Daws.secretKey=07sh86hsubkuy6ykus/sd06h7fsjkdyfuk897934

Certain environment variables must be set in order for the correct Upload Policy to be generated for a file upload to Amazon S3:

    -Daudio.file.upload.acl=bucket-owner-full-control
    -Daudio.file.upload.expire.minutes=60
    -Daudio.file.bucket=xj-audio-ENVIRONMENT      
    -Daudio.url.base=https://audio.ENVIRONMENT.xj.io/
    -Daudio.url.upload=https://xj-audio-ENVIRONMENT.s3.amazonaws.com/

The file upload ACL `bucket-owner-full-control` affords the administration of uploaded objects by the bucket owner.



# Components



## ui/hub-ui

Hub user interface web application. Built with Javascript, Ember, Node.

Requires Node.js version 7+

Currently at Ember, Ember Data, and Ember CLI **version 2.13**

Connects to:

  * xj/hub

Requires installing some NPM packages globally:

    sudo npm install -g ember-cli jest npm-upgrade gulp 
    
Also possibly helpful is `npm-upgrade` for upgrading dependencies in a package.json at once:

    sudo npm install -g npm-upgrade    



## core

Common models and utilities. Built with Java, Guice, Tomcat, Maven.



## hub

Central structured data and business logic. Built with Java, Guice, Tomcat, Maven.

Depends on:

  * xj/core

Connects to:

  * SQL Database
  * Filesystem
  
Expects a `POST /heartbeat` every 60 seconds with a `key` in order to ensure platform-wide vitals.

There's a convenience script to send cURL to Hub and trigger heartbeat in development:

    bin/heartbeat



## worker



## pulse

This app exists solely to be run in AWS Lambda, and call the Hub /heartbeat endpoint once per minute.

Read more in [the Pulse README](pulse/README.md).

**Craft** fabricates a musical audio composite from source sequences and instrument-audio. Built with Java, Guice, Tomcat, Maven.

Depends on:

  * xj/core

Connects to:

  * SQL Database
  * Filesystem

**Dub** mixes and ships finished audio data to delivery. Built with Java, Guice, Tomcat, Maven.

Depends on:

  * xj/core

Connects to:

  * SQL Database
  * Filesystem



# Mix

**Mix** is a Java implementation of the Go project [https://github.com/go-mix/mix](go-mix).

The project has its own [README](mix/README.md)



# Music

**Music** is a Java implementation of the Go project [https://github.com/go-music-theory/music-theory](go-music-theory).

The project has its own [README](music/README.md)



## Note

A Note is used to represent the relative duration and pitch of a sound.



## Key

The key of a piece is a group of pitches, or scale upon which a music composition is created in classical, Western art, and Western pop music.



## Chord

In music theory, a chord is any harmonic set of three or more notes that is heard as if sounding simultaneously.



## Scale

In music theory, a scale is any set of musical notes ordered by fundamental frequency or pitch.



# App Standards



## Port

  * hub: 8042
  * worker: 8043



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

mvn archetype:generate -DarchetypeArtifactId=jersey-quickstart-grizzly2 -DarchetypeGroupId=org.glassfish.jersey.archetypes -DinteractiveMode=false -DgroupId=io.xj -DartifactId=strap -Dpackage=io.xj.strap -DarchetypeVersion=2.17



## AWS Elastic Beanstalk

The `.ebextensions` and `.ebsettings` folder contain configurations proprietary to [AWS Elastic Beanstalk](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/java-tomcat-platform.html#java-tomcat-proxy).

Ops engineers may prefer to use the [The Elastic Beanstalk Command Line Interface](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb-cli3.html) for administration of the AWS production deployment.

***Look out for `eb` trying to modify and/or change git version control for the `.config` folder when you do a `eb init`-- revert anything it tries to change!***



## Jersey

[Latest User Guide](https://jersey.java.net/documentation/latest/user-guide.html)



# Musical debugging

This sql query confirms that all segments begin where the preceding one ended:

```
SELECT
  A.offset "prev_offset",
  B.offset "next_offset",
  TIMESTAMPDIFF(SECOND, A.end_at, B.begin_at) "gap"
  FROM segment A
  JOIN segment B ON B.offset = A.offset+1; 
```

This sql query will reveal if any of the segment lengths are wildly off, given their relative lengths and totals:

```
SELECT
  offset,
  json_extract(content, '$.type') AS "type",
  total,
  tempo,
  TIMESTAMPDIFF(SECOND, begin_at, end_at) AS "length_seconds",
  TIMESTAMPDIFF(SECOND, begin_at, end_at)/total AS "time_per_beat"
  FROM segment
  WHERE chain_id=10; 
```


# Developer Setup Gotchas



## IntelliJ IDEA

Here's the official XJ Music Inc copyright Velocity template:

    Copyright (c) 1999-${today.year}, XJ Music Inc. (https://xj.io) All Rights Reserved.



### Docker run as non-root user

 - Add the docker group if it doesn't already exist:

        sudo groupadd docker

 - Add the connected user "$USER" to the docker group. Change the user name to match your preferred user if you do not want to use your current user:

        sudo gpasswd -a $USER docker

 - log out/in to activate the changes to groups.



## OSX

On OSX, because we are unable to connect to the container from the host, we are using the following workarounds, which are built in to the cross-platform workflow:

  * Local port 80 (e.g. http://localhost) is mapped to Docker container `hub01xj1` port 80
  * Local port 5400 is mapped to Postgres container `postgres01xj1` port 5432

Docker documentation: https://docs.docker.com/docker-for-mac/networking/#per-container-ip-addressing-is-not-possible
GitHub Open Issue: https://github.com/docker/for-mac/issues/155



# Open Source Web Player on GitHub

Architect wants minimal, open-source web browser based XJ Music™ player, in order to embed XJ Music™ on any website, and ensure that the experience is as widely accessible as possible.

See [player-ui README](ui/player-ui/README.md)



# VPN

XJ Music web operations occur inside a Virtual Private Cloud (VPC) hosted by Amazon Web Services (AWS). In order to access these resources, developers must connect using an OpenVPN with RSA certificates and an OpenVPN configuration provided to them by dev ops.

See: [Configuration for **net.xj.io** and **prd1.xj.io**](https://www.pivotaltracker.com/story/show/166998845) 

## VPC
id: vpc-442c7d21
subnet-04a87525c06434da6 "xj-use1-az2-public" 10.0.48.0/20
subnet-6099e639 "xj-use1-az6-private" 10.0.32.0/20
subnet-7199e628 "xj-use1-az6-public" 10.0.16.0/20

## Generating Certificates
git clone https://github.com/OpenVPN/easy-rsa.git
cd easy-rsa/easyrsa3
./easyrsa init-pki
./easyrsa build-ca nopass
./easyrsa build-server-full server nopass
./easyrsa build-client-full charney.net.xj.io nopass

## Copying Certificates to working folder
cp pki/ca.crt /home/charney/.vpn/xj/
cp pki/issued/server.crt /home/charney/.vpn/xj/
cp pki/private/server.key /home/charney/.vpn/xj/
cp pki/issued/charney.net.xj.io.crt /home/charney/.vpn/xj
cp pki/private/charney.net.xj.io.key /home/charney/.vpn/xj/

## Import Certificates to AWS
cp /home/charney/.vpn/xj						
aws acm import-certificate --certificate file://server.crt --private-key file://server.key --certificate-chain file://ca.crt --region us-east-1
aws acm import-certificate --certificate file://charney.net.xj.io.crt --private-key file://charney.net.xj.io.key --certificate-chain file://ca.crt --region us-east-1
