package ua.moyo.rabbitmq.rabbitMQ.rabbit_other;

import com.fasterxml.jackson.annotation.*;
import jdk.nashorn.internal.runtime.regexp.joni.constants.Arguments;

import java.io.Serializable;

/**
 * Created by JLD on 30.08.2017.
 */@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "source",
        "vhost",
        "destination",
        "destination_type",
        "routing_key",
        "arguments",
        "properties_key"
})
public class ExchangesBindings implements Serializable{

    @JsonProperty("source")
    private String source;
    @JsonProperty("vhost")
    private String vhost;
    @JsonProperty("destination")
    private String destination;
    @JsonProperty("destination_type")
    private String destinationType;
    @JsonProperty("routing_key")
    private String routingKey;
    @JsonProperty("arguments")
    private Arguments arguments;
    @JsonProperty("properties_key")
    private String propertiesKey;

    @JsonProperty("source")
    public String getSource() {
        return source;
    }

    @JsonProperty("source")
    public void setSource(String source) {
        this.source = source;
    }

    @JsonProperty("vhost")
    public String getVhost() {
        return vhost;
    }

    @JsonProperty("vhost")
    public void setVhost(String vhost) {
        this.vhost = vhost;
    }

    @JsonProperty("destination")
    public String getDestination() {
        return destination;
    }

    @JsonProperty("destination")
    public void setDestination(String destination) {
        this.destination = destination;
    }

    @JsonProperty("destination_type")
    public String getDestinationType() {
        return destinationType;
    }

    @JsonProperty("destination_type")
    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    @JsonProperty("routing_key")
    public String getRoutingKey() {
        return routingKey;
    }

    @JsonProperty("routing_key")
    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    @JsonProperty("arguments")
    public Arguments getArguments() {
        return arguments;
    }

    @JsonProperty("arguments")
    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    @JsonProperty("properties_key")
    public String getPropertiesKey() {
        return propertiesKey;
    }

    @JsonProperty("properties_key")
    public void setPropertiesKey(String propertiesKey) {
        this.propertiesKey = propertiesKey;
    }



}
