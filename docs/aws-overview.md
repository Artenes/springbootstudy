# AWS Overview

A good default infra for an average web app is to use each one of the AWS services for each aspect of the application:

- EC2 - Host the application
- ElastiCache - to run Redis for cache
- RDS - to run postgres
- CloudWatch - to monitor app logs

You can also use Beankstalk, that is a simplification of the items above. The idea is that by using Beanstalk you can deploy
something really fast, but have not much control over it, while by setting up each service manually, you can control better
how things play out.

## Best practices before starting

### Root account
When you create your AWS account for the first time, you are creating a root account, from which you will be able to create other users. In there make sure to activate MFA to increase the security of your account.

### Other users
Right after creating your account:

- create a new user group under IAM > Access Management > User groups and give it the AdministratorAccess policy
- create another user under IAM > Access Management > Users and add it to the admin group you created

Now you will logout of your root account and login using this new admin account. Don't forget to add MFA to it too.

This is a [good practice according to AWS docs](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html#lock-away-credentials) because it prevents possible problems related to access keys.
If you generate an access key in your root account and somehow it leaks, an attacker will have root access to all the resources of your account. Something
that you can immediately block or prevent when having another user under the root account (with fewer permissions) generating these keys.

### Budget

Go to Billing Dashboard > Cost Management > Budgets and setup a budget and an alert so when the costs get over $0.01 you
receive an email, so you can shut down anything running, so you don't get overcharged while you are learning.

## Docker

In this case it does not make too much sense to use docker. The general idea would be to create an image that has everything:
your app, a database, a cache system, a server, etc. Then you just spin up a container and you are done. But this limit your application
when it needs to be scaled horizontally, also you would need to worry about some basic infra stuff such as making backups
of databases.

To help solve this we are better off to leave this infra stuff to AWS and setup things manually (or with code in kubernets), but 
the goal is to use what AWS has to offer to solve most common problems that we would face anyways, such as load balancing,
scale and backups.

You can still use docker to help during development, but in deployment, by using Beanstalk for instance, you just need to
build the .jar and deploy to AWS either manually or by a CI pipeline. The other components are already in AWS responsibility.

## Links
- [How to Deploy a Spring Boot App to AWS Elastic Beanstalk](https://mydeveloperplanet.com/2020/10/21/how-to-deploy-a-spring-boot-app-to-aws-elastic-beanstalk/)
- [Elastic IP address not attached to a running instance per hour (prorated)](https://repost.aws/questions/QU7RaHragDSve_eTsJe4hC4A/elastic-ip-address-not-attached-to-a-running-instance-per-hour-prorated)