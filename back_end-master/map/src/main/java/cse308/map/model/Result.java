package cse308.map.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Entity
public class Result {
    @Transient
    private static final long serialVersionUID = 4L;

    @Id
    @GeneratedValue
    private Long id;
    private String email;
    @Lob
    private Serializable stateJSON;
    @Lob
    private String summary;
    public Result(){

    }
    public Result(String email,Serializable s,String summary){
        this.email = email;
        this.stateJSON = s;
        this.summary = summary;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Serializable getStateJSON() {
        return stateJSON;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setStateJSON(Serializable stateJSON) {
        this.stateJSON = stateJSON;
    }
}
