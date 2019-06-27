package cse308.map.model;

import javax.persistence.Transient;
import java.io.Serializable;

public class ClusterEdge implements Comparable<ClusterEdge>,Serializable {
    @Transient
    private static final long serialVersionUID = 4L;
    private double compactness;
    private Cluster c1;
    private Cluster c2;

    private double communityifInterset;

    private double joinability;

    public double getCompactness() {
        return compactness;
    }

    public void setCompactness(double compactness) {
        this.compactness = compactness;
    }

    public Cluster getC1() {
        return c1;
    }

    public void setC1(Cluster c1) {
        this.c1 = c1;
    }

    public Cluster getC2() {
        return c2;
    }

    public void setC2(Cluster c2) {
        this.c2 = c2;
    }

    public double getCommunityifInterset() {
        return communityifInterset;
    }

    public void setCommunityifInterset(double communityifInterset) {
        this.communityifInterset = communityifInterset;
    }


    public void setJoinability(double joinability) {
        this.joinability = joinability;
    }

    public Cluster getNeighborCluster(Cluster p) {
        if (c1 == p) {
            return c2;
        } else if (c2 == p) {
            return c1;
        }
        return null;
    }

    public void changeNeighbor(Cluster p, Cluster neighbor) {
        if (c1 == p) {
            c2 = neighbor;
        } else {
            c1 = neighbor;
        }
    }

    public ClusterEdge(Cluster c1, Cluster c2, MajorMinor majorMinor,double commnunityWeight) {
        this.c1 = c1;
        this.c2 = c2;
        computeJoin(majorMinor,commnunityWeight);
    }

    public void computeJoin(MajorMinor communityOfInterest,double communityWeight) {
        int totalPopulation = c1.getDemographic().getPopulation() + c2.getDemographic().getPopulation();
        int countyValue = c1.getCountyID().equals(c2.getCountyID()) ? 1 : 0;
        int totalMmPopulation = c1.getDemographic().getMajorMinorPop(communityOfInterest)+c2.getDemographic().getMajorMinorPop(communityOfInterest);
        double majorMinorValue = (double) totalMmPopulation / totalPopulation;
        joinability = majorMinorValue * communityWeight + countyValue * (1-communityWeight);
        setJoinability(joinability);
    }


    public double getJoinability() {
        return joinability;
    }

    public String toString() {
        return c1.getClusterID() + ": " + c2.getClusterID();
    }

    @Override
    public int compareTo(ClusterEdge o) {
        if(joinability == o.joinability){
            return  0;
        }else if(joinability > o.joinability){
            return -1;
        }
        return 1;
    }
}
