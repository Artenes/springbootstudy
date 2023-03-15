# Monitoring

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

## Logs

You need to send the application logs to some aggregation system, so you can evaluate them. Don't forget to use tracing tools
to keep track of logs spanning multiple services.

Essentially the idea is to use some stack such as ELK (elasticsearch, logstash, kibana) to handle this. Sometimes we can
replace logstash with FileBeats. But this is a whole beast on its own, each of these tools have its fundamental ideas and
patterns that needs to be studied individually.

[7.4. Logging](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.logging)
[Logging In Spring Boot](https://reflectoring.io/springboot-logging/)
[Tracing with Spring Boot, OpenTelemetry, and Jaeger](https://reflectoring.io/spring-boot-tracing/)
[Per-Environment Logging with Plain Java and Spring Boot](https://reflectoring.io/profile-specific-logging-spring-boot/#per-environment-logging-with-spring-boot0)
[Spring Boot Logs Aggregation and Monitoring Using ELK Stack](https://auth0.com/blog/spring-boot-logs-aggregation-and-monitoring-using-elk-stack/)
[Elastic Search, Logstash and Kibana via docker-compose](https://gist.github.com/mjul/fa222838e94d72560c5cce6b50db3346)
[elasticsearch-kibana-docker-compose](https://github.com/self-tuts/awesome-docker-compose/blob/master/ecosystem/elasticsearch-kibana-docker-compose.yml)
[Configurando o Elasticsearch e Kibana no Docker](https://hgmauri.medium.com/configurando-o-elasticsearch-e-kibana-no-docker-3f4679eb5feb)

## Links

- [Health Checks with Spring Boot](https://reflectoring.io/spring-boot-health-check/)
- [Production-ready Features](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [How to Monitor a Spring Boot App](https://mydeveloperplanet.com/2021/03/03/how-to-monitor-a-spring-boot-app)
- [Self-Hosted Monitoring For Spring Boot Applications](https://www.baeldung.com/spring-boot-self-hosted-monitoring)
- [Monitoring Spring Boot Application with Prometheus and Grafana](https://refactorfirst.com/spring-boot-prometheus-grafana)
- [Application Monitoring Using Spring Boot Admin (Part 2)](https://levelup.gitconnected.com/application-monitoring-using-spring-boot-admin-part-2-ed14178c6964)
- [Publishing Metrics from Spring Boot to Amazon CloudWatch](https://reflectoring.io/spring-aws-cloudwatch/)
- [Prometheus Configuration](https://prometheus.io/docs/prometheus/latest/configuration/configuration)
- [Configure basic_auth for Prometheus Target](https://stackoverflow.com/questions/64031121/configure-basic-auth-for-prometheus-target)
- [Send arbitrary extra headers when scraping](https://github.com/prometheus/prometheus/issues/1724)