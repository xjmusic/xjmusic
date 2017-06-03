# AWS Elastic Beanstalk

This folder contains configuration proprietary to [AWS Elastic Beanstalk](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/java-tomcat-platform.html#java-tomcat-proxy).

## Procfile

Elastic Beanstalk assumes that all entries in the Procfile should run at all times and automatically restarts any application defined in the Procfile that terminates. See [AWS Elastic Beanstalk Developer Guide](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/java-se-platform.html)

## Environment

Any E.B. environment must have variables set in order to run the xj platform. See `production.env` to infer the expected environment variables.
 
Most importantly, there is a variable APPS which is a space-separated list of apps to run. For example, `APPS=hub` would operate only the Hub frontend, while `APPS=craftworker dubworker eraseworker` would operate all the Craftworker, Dubworker, and Eraseworker apps.
