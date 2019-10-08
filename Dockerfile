# Pull base image.
FROM azul/zulu-openjdk-alpine:latest

ADD ./target/kafka-camel-spring-boot-sample*.jar kafka-camel-spring-boot-sample.jar

#EXPOSE 8080

CMD java -jar kafka-camel-spring-boot-sample.jar