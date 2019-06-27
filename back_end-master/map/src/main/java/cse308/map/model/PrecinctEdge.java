package cse308.map.model;

import javax.persistence.Transient;
import java.io.Serializable;

public class PrecinctEdge implements Serializable {
    @Transient
    private static final long serialVersionUID = 4L;
    private double compactness;
    private Precinct p1;
    private Precinct p2;
    private double communityOfInterset;
    private double countyJoinability;
    private double joinability;

    public Precinct getNeighborPrecinct(Precinct p) {
        if (p1 == p) {
            return p2;
        } else if (p2 == p) {
            return p1;
        }
        return null;
    }

    public PrecinctEdge(Precinct p1, Precinct p2,MajorMinor majorMinor,double commnunityWeight) {
        this.p1 = p1;
        this.p2 = p2;
        computeJoin(majorMinor,commnunityWeight);
    }

    public double getCompactness() {
        return compactness;
    }

    public void setCompactness(double compactness) {
        this.compactness = compactness;
    }

    public double getCommunityOfInterset() {
        return communityOfInterset;
    }

    public void setCommunityOfInterset(double communityOfInterset) {
        this.communityOfInterset = communityOfInterset;
    }

    public Precinct getP1() {
        return p1;
    }

    public void setP1(Precinct p1) {
        this.p1 = p1;
    }

    public Precinct getP2() {
        return p2;
    }

    public void setP2(Precinct p2) {
        this.p2 = p2;
    }

    public double getCountyJoinability() {
        return countyJoinability;
    }

    public void setCountyJoinability(double countyJoinability) {
        this.countyJoinability = countyJoinability;
    }

    public double getJoinability() {
        return joinability;
    }

    public void setJoinability(double joinability) {
        this.joinability = joinability;
    }

    public void computeJoin(MajorMinor comunityOfInterest,double communityWeight) {
        int totalPopulation = p1.getDemographic().getPopulation() + p2.getDemographic().getPopulation();
        int totalMmPopulation=p1.getDemographic().getMajorMinorPop(comunityOfInterest)+p2.getDemographic().getMajorMinorPop(comunityOfInterest);
        double majorMinorValue=(double) totalMmPopulation/totalPopulation;
        int countyValue = p1.getId().equals(p2.getId()) ? 1 : 0;
        this.joinability = majorMinorValue * communityWeight + countyValue-communityWeight;
        setJoinability(joinability);
    }

    public Precinct getNeighbor(Precinct p){
        return p==p1? p2:p1;// if p==p1 then return p2, else p1.
    }

}
