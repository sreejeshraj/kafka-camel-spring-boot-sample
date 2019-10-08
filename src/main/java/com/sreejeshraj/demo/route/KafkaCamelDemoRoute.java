package com.sreejeshraj.demo.route;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.csv.CsvDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Component
@ConfigurationProperties(prefix="camel-demo-route")
@Data
@EqualsAndHashCode(callSuper=true)

public class KafkaCamelDemoRoute extends RouteBuilder {

	// The value of this property is injected from application.properties based on the profile chosen.
	private String injectedName;
	
	@Override
	public void configure() throws Exception {

		// @formatter:off
		
		String[] headersArray = {"FirstName","LastName","Age","Id","Salary"};
		CsvDataFormat csvDataFormat = new CsvDataFormat()
											.setHeader(headersArray)
											.setUseMaps(true);
		
		errorHandler(deadLetterChannel("seda:errorQueue").maximumRedeliveries(5).redeliveryDelay(1000));

		from("file://{{inputFolder}}?delay=10s&noop=true")
		.routeId("Route1")
		.split().tokenize("\r\n")
		.log("***** body:${body} *****")
		.removeHeaders("kafka*")
		.setHeader("myHeader", constant("myHeaderValue"))
		.to("kafka:inputTopic?brokers=localhost:9092")
		.log(LoggingLevel.DEBUG, "**** Input message pushed To Kafka topic ***** :"+injectedName);

		
		from("kafka:inputTopic?brokers=localhost:9092&groupId=myGroup&maxPollRecords=1&autoOffsetReset=earliest")
		.routeId("Route2")
		.log("*****Received from topic body:${body} *****")
		.log("*****Received from topic headers:${headers} *****")
		.unmarshal(csvDataFormat)
		.setBody(simple("${body[0]}"))
		.marshal().json(JsonLibrary.Jackson)
		.log("*****After unmarshall body:${body} *****")
		.removeHeaders("kafka*")
		.log("*****After removing headers:${headers} *****")
		.to("kafka:outputTopic?brokers=localhost:9092")
		.log(LoggingLevel.DEBUG, "**** Output message pushed To Kafka topic *****");
		
		
		// @formatter:on

	}

}
