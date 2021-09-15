package com.example.meetfake.domain;

import java.util.ArrayList;
import java.util.Objects;

public class WebSocketMessage {
    private String from;
    private String type;
    private String data;
    private Object candidate;
    private Object sid;
    private Object offer;
    private Object micinf;
    private Object vidinf;
    private String cname;
    private Object answer;
    private String conc;

    public WebSocketMessage(){

    }

    public WebSocketMessage(String from, String type, String data, Object candidate, Object sid, Object offer, Object micinf, Object vidinf, String cname, Object answer,String conc) {
        this.from = from;
        this.type = type;
        this.data = data;
        this.candidate = candidate;
        this.sid = sid;
        this.offer = offer;
        this.micinf = micinf;
        this.vidinf = vidinf;
        this.cname = cname;
        this.answer = answer;
        this.conc = conc;
    }

    public WebSocketMessage(final String from,
                            final String type,
                            final String data) {
        this.from = from;
        this.type = type;
        this.data = data;

    }

    public WebSocketMessage(String from,
                            String type,
                            String data,
                            String conc,
                            Object micinf, Object vidinf) {
        this.from = from;
        this.type = type;
        this.data = data;
        this.conc = conc;
        this.micinf = micinf;
        this.vidinf = vidinf;
    }


    public String getFrom() {
        return from;
    }

    public String getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public Object getCandidate() {
        return candidate;
    }

    public Object getSid() {
        return sid;
    }

    public Object getOffer() {
        return offer;
    }

    public Object getMicinf() {
        return micinf;
    }

    public Object getVidinf() {
        return vidinf;
    }

    public String getCname() {
        return cname;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setCandidate(Object candidate) {
        this.candidate = candidate;
    }

    public void setSid(Object sid) {
        this.sid = sid;
    }

    public void setOffer(Object offer) {
        this.offer = offer;
    }

    public void setMicinf(Object micinf) {
        this.micinf = micinf;
    }

    public void setVidinf(Object vidinf) {
        this.vidinf = vidinf;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public Object getAnswer() {
        return answer;
    }

    public void setAnswer(Object answer) {
        this.answer = answer;
    }

    public String getConc() {
        return conc;
    }

    public void setConc(String conc) {
        this.conc = conc;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final WebSocketMessage message = (WebSocketMessage) o;
        return Objects.equals(getFrom(), message.getFrom()) &&
                Objects.equals(getType(), message.getType()) &&
                Objects.equals(getData(), message.getData()) &&
                Objects.equals(getCandidate(), message.getCandidate()) &&
                Objects.equals(getSid(), message.getSid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFrom(), getType(), getData(), getCandidate(), getSid());
    }

    @Override
    public String toString() {
        return "WebSocketMessage{" +
                "from='" + from + '\'' +
                ", type='" + type + '\'' +
                ", data='" + data + '\'' +
                ", candidate=" + candidate +
                ", sid=" + sid +
                '}';
    }
}
