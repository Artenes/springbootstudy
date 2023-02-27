# AWS Overview

A good default infra for an average web app is to use each one of the AWS services for each aspect of the application:

- EC2 - Host the application
- ElastiCache - to run Redis for cache
- RDS - to run postgres

You can also use Beankstalk, that is a simplification of the items above. The idea is that by using Beanstalk you can deploy
something really fast, but have not much control over it, while by setting up each service manually, you can control better
ho things play out.

## Docker

In this case it does not make too much sense to use docker. The general idea would be to create an image that has everything:
your app, a dtabase, a cache system, a server, etc. Then you just spin up a container and you are done. But this limit your application
when it needs to be scaled horizontally, also you would need to worry about some basic infra stuff such as making backups
of databases.

To help solve this we are better off to leave this infra stuff to AWS and setup things manually (or with code in kubernets), but 
the goal is to use what AWS has to offer to solve most common problems that we would face anyways, such as load balancing,
scale and backups.

You can still use docker to help during development, but in deployment by using Beanstalk for instance, you just need to
build the .jar and deploy to AWS either manually or by a CI pipeline. The other components are already in AWS responsibility.