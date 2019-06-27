package cse308.map.model;

import com.vividsolutions.jts.geom.Geometry;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.*;
//import javax.persistence.*;

@Entity
public class Precinct implements Serializable {
    @Transient
    private static final long serialVersionUID = 4L;
    @Id
    private String id;
    private Geometry shape;
    private String state;
    private String county;
    private String neighbors;
    @Embedded
    private Demographic demographic;
    @Transient
    private String parentCluster;
    @Transient
    private Set<PrecinctEdge> precinctEdges = new HashSet<>();
    @Transient
    private boolean iscCompute = false;

    @Transient
    private int combineNum; //for annealing

    public Set<Precinct> getOtherDistrctPreicincts(){
        Set<Precinct> otherDistrictPrecincts=new HashSet<Precinct>();
        for(PrecinctEdge e:precinctEdges){
            if(!e.getNeighbor(this).getParentCluster().equals(parentCluster))
                otherDistrictPrecincts.add(e.getNeighborPrecinct(this));
        }
        return otherDistrictPrecincts;
    }

    public boolean isNeighbor(Precinct nei) {
        for (PrecinctEdge e : precinctEdges) {
            if (e.getNeighborPrecinct(this) == nei) {
                return true;
            }
        }
        return false;
    }

    public void addEdge(PrecinctEdge e) {
        if (!precinctEdges.contains(e)) {
            precinctEdges.add(e);
        }
    }

    public void removeEdge(PrecinctEdge e) {
        if (precinctEdges.contains(e)) {
            precinctEdges.remove(e);
        }
    }

    public String toString() {
        return id;
    }

    public Boolean isBorderPrecinct(){
        for(PrecinctEdge precinctEdge : precinctEdges){
            Precinct neighbor = precinctEdge.getNeighbor(this);
            if(!neighbor.getParentCluster().equals(this.getParentCluster()))
                return true;
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Geometry getShape() {
        return shape;
    }

    public void setShape(Geometry shape) {
        this.shape = shape;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(String neighbors) {
        this.neighbors = neighbors;
    }

    public Demographic getDemographic() {
        return demographic;
    }

    public void setDemographic(Demographic demographic) {
        this.demographic = demographic;
    }

    public String getParentCluster() {
        return parentCluster;
    }

    public void setParentCluster(String parentCluster) {
        this.parentCluster = parentCluster;
    }

    public Set<PrecinctEdge> getPrecinctEdges() {
        return precinctEdges;
    }

    public void setPrecinctEdges(Set<PrecinctEdge> precinctEdges) {
        this.precinctEdges = precinctEdges;
    }

    public boolean isIscCompute() {
        return iscCompute;
    }

    public void setIscCompute(boolean iscCompute) {
        this.iscCompute = iscCompute;
    }

    public double getMajorMinor(MajorMinor communityOfInterest){
        double majorMinorValue = 0;
        int totalPopulation = this.getDemographic().getPopulation();
        int totalMmPopulation = this.getDemographic().getMajorMinorPop(communityOfInterest);
        majorMinorValue = (double) totalMmPopulation / totalPopulation;
        return majorMinorValue;
    }

    public List<Precinct> getSameClusterNeighbor(Precinct precinct){
        List<Precinct> neighbor = new LinkedList<>();
        for(PrecinctEdge precinctEdge : precinct.getPrecinctEdges()){
            Precinct neig= precinctEdge.getNeighbor(precinct);
            if(neig.getParentCluster().equals(precinct.getParentCluster())){
                neighbor.add(neig);
            }
        }

        return neighbor;
    }

    public int getCombineNum() {
        return combineNum;
    }

    public void setCombineNum(int combineNum) {
        this.combineNum = combineNum;
    }
}
