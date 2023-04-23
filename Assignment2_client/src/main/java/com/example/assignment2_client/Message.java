package com.example.assignment2_client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Date;

public class Message implements Serializable {
    Date timestamp;
    String sendBy;
    String sendTo;
    String data;
    String method;

    @JsonCreator
    public Message(@JsonProperty("timestamp") Date timestamp,
                   @JsonProperty("sentBy") String sendBy,
                   @JsonProperty("sendTo") String sendTo,
                   @JsonProperty("data") String data,
                   @JsonProperty("method") String method) {
        this.timestamp = timestamp;
        this.sendBy = sendBy;
        this.sendTo = sendTo;
        this.data = data;
        this.method = method;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getSendBy() {
        return sendBy;
    }

    public String getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }

    public String getMethod() {
        return method;
    }

    public String serialize() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
}
