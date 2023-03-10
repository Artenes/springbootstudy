# Monitoring

## TODO
- setup Prometheus and Grafana in the project (review docker-compose)
- setup integration with CloudWatch

## Actuator

This is the spring boot solution to provide endpoints to watch for the health of the application. In pactice this might not
be that useful by itself, that's why we use Micrometer with actuator.

## Micrometer

This is a lib that works alongside Actuator to expose some data regarding the application state in a format that can be consumed
by tools that can consume and display this information such as Prometheus and CloudWatch.

## Prometheus

This one you configure with a ``prometheus-config.yml`` file by passing the address of your service and some basic auth
information to authenticate against the actuator endpoint. And no, it is not possible to add custom headers to the request
that Prometheus will make. So to make things simples, just use basic auth to protect the actuator endpoints.

## Links

- [Health Checks with Spring Boot](https://reflectoring.io/spring-boot-health-check/)
- [Production-ready Features](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [How to Monitor a Spring Boot App](https://mydeveloperplanet.com/2021/03/03/how-to-monitor-a-spring-boot-app)
- [Self-Hosted Monitoring For Spring Boot Applications](https://www.baeldung.com/spring-boot-self-hosted-monitoring)
- [How To Monitor a Spring Boot App With Prometheus and Grafana](https://betterprogramming.pub/how-to-monitor-a-spring-boot-app-with-prometheus-and-grafana-22e2338f97fc)
- [Monitoring Spring Boot Application with Prometheus and Grafana](https://refactorfirst.com/spring-boot-prometheus-grafana)
- [Application Monitoring Using Spring Boot Admin (Part 2)](https://levelup.gitconnected.com/application-monitoring-using-spring-boot-admin-part-2-ed14178c6964)
- [Publishing Metrics from Spring Boot to Amazon CloudWatch](https://reflectoring.io/spring-aws-cloudwatch/)
- [Prometheus Configuration](https://prometheus.io/docs/prometheus/latest/configuration/configuration)
- [Configure basic_auth for Prometheus Target](https://stackoverflow.com/questions/64031121/configure-basic-auth-for-prometheus-target)
- [Send arbitrary extra headers when scraping](https://github.com/prometheus/prometheus/issues/1724)