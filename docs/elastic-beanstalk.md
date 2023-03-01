# Elastic Beanstalk

This service is an abstraction layer over other AWS services. 

So, instead of creating a EC2 instance, setting up a RDS database and doing any other setup, you will be guided by a step-by-step
process where you provide some information to the EB console and it will configure all the previous services for you.

In the end you can access each one of those services to individually see your instance running or your database information.

# Notes

- use Java Correto 17 to compile your code, since this is the version used in the EC2 instance.
- you need to setup inbound access rules to your ip to be able to access the database from DBeaver and create the database
for the application to use
- you can [use a cli](https://github.com/aws/aws-elastic-beanstalk-cli-setup) to perform a deploy from a CI pipeline

# Links

- [How to pause or stop AWS Elastic Beanstalk environment from running?](https://jun711.github.io/aws/how-to-pause-or-stop-elastic-beanstalk-environment-from-running/)
- [How to Deploy a Spring Boot App to AWS Elastic Beanstalk](https://mydeveloperplanet.com/2020/10/21/how-to-deploy-a-spring-boot-app-to-aws-elastic-beanstalk/)
- [Setting configuration options before environment creation](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/environment-configuration-methods-before.html#configuration-options-before-configyml)