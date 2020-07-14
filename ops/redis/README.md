# Redis server

Monitor production Redis queues at **[redis01.us-east-1.xj:8080](http://redis01.us-east-1.xj:8080/)**, which is only available from within the production VPN.

The host `redis01.us-east-1.xj` is defined locally, by adding something like this to your **/etc/hosts** file:

    10.1.8.184 redis01.us-east-1.xj

XJ platform requires a Redis server:

  * to persist user sessions, and quickly check hubAccess tokens for every incoming request.
  * to persist work queues.
  
In the docker configuration, the Redis server is named `redis01xj1`.

There is a script provided in this folder called **bin/install_jesque_web** intended to be run directly on a Redis server (or other) container, in order to run the **jesque-web** web application for monitoring and administration of (resque-style) jesque work queues.
