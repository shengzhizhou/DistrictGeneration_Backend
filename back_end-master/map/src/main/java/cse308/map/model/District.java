package cse308.map.model;

import com.vividsolutions.jts.geom.Geometry;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class District implements Comparable<District>,Serializable {
    @Transient
    private static final long serialVersionUID = 4L;
    private String districtID;
    private Cluster cluster;
    private double compactness;
    private transient HashSet<Precinct> movelist = new HashSet<>();
    private Party p;

    public HashSet<Precinct> getMovelist() {
        return movelist;
    }

    public void setMovelist(HashSet<Precinct> movelist) {
        this.movelist = movelist;
    }

    public void initMoveList(){
        Set<Precinct> borderPrecincts = new HashSet<Precinct>();

        for (Precinct precinct : cluster.getPrecincts()) {
                Set<Precinct> otherDistrictPrecincts=precinct.getOtherDistrctPreicincts();
                if(!otherDistrictPrecincts.isEmpty())
                for (Precinct otherDistrictPrecinct : otherDistrictPrecincts) {
                    movelist.add(otherDistrictPrecinct);
                }
        }
    }

    public double getCompactness() {
        return compactness;
    }

    public void setCompactness(double compactness) {
        this.compactness = compactness;
    }

    public Geometry getShape() {
        return cluster.getShape();
    }

    public void setShape(Geometry shape) {
        cluster.setShape(shape);
    }

    public District(Cluster c) {
        cluster=c;
        districtID=cluster.getClusterID();
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public String getDistrictID() {
        return districtID;
    }

    public void setDistrictID(String districtID) {
        this.districtID = districtID;
    }

    public void setDemo(Demographic demo) {
        cluster.setDemographic(demo);
    }

    public double getCurrentScore(){
        return cluster.getCurrentScore();
    }

    public void setCurrentScore(double score){
        cluster.setCurrentScore(score);
    }


    public String getdistrictID() { return districtID; }

    public void addPrecinct(Precinct p) {
//        if(!this.cluster.getPrecincts().contains(p)) {
            this.cluster.addPrecinct(p);
            p.setParentCluster(districtID);
            Geometry newDistrict = this.getCluster().getShape().union(p.getShape());
            this.getCluster().setShape(newDistrict);
            this.getCluster().getDemographic().combineDemo(p.getDemographic());
//        }
    }

    public void removePrecinct(Precinct p) {
//        if (this.cluster.getPrecincts().contains(p)) {
            this.cluster.removePrecinct(p);
            Geometry newDistrict = this.getCluster().getShape().symDifference(p.getShape());
            this.getCluster().setShape(newDistrict);
            this.getCluster().getDemographic().removeDemo(p.getDemographic());
            if(this.getCluster().getDemographic().getPopulation()<=0){
                System.out.println("FROM:"+this.getdistrictID());
                System.exit(0);
            }
        p.setParentCluster(null);
//        }
    }

    public Demographic getDemo() {
        return cluster.getDemographic();
    }

    public double getMajorMinor(MajorMinor communityOfInterest){
        double majorMinorValue = 0;
        int totalPopulation = this.getCluster().getDemographic().getPopulation();
        int totalMmPopulation = this.getCluster().getDemographic().getMajorMinorPop(communityOfInterest);
        majorMinorValue = (double) totalMmPopulation / totalPopulation;
        return majorMinorValue;
    }

    public String toString() {
        return districtID;
    }




    public Set<Precinct> getBorderPrecincts() {

        Set<Precinct> borderPrecincts = new HashSet<Precinct>();

        for (Precinct precinct : cluster.getPrecincts()) {
                if(precinct.isBorderPrecinct()){
                    borderPrecincts.add(precinct);
                }
        }
        return borderPrecincts;
    }

    public Party getP() {
        return p;
    }

    public void setParty() {
        if(this.getGOPVote()>= this.getDEMVote()){
            p = Party.REPUBLICAN;
        }else{
            p = Party.DEMOCRATIC;
        }
    }

    public int getPopulation() {
        return cluster.getDemographic().getPopulation();
    }

    public int getGOPVote() {
        return cluster.getDemographic().getRepublicanVote();
    }

    public int getDEMVote() {
        return cluster.getDemographic().getDemocraticVote();
    }

    @Override
    public int compareTo(District o) {
        if(cluster.getDemographic().getPopulation() == o.getCluster().getDemographic().getPopulation()){
            return  0;
        }else if(cluster.getDemographic().getPopulation() > o.getCluster().getDemographic().getPopulation()){
            return 1;
        }
        return -1;
    }
    public String toGeoJsonFormat(){
        return cluster.toGeoJsonFormat();
    }

    public double rateGerrymanderingScore() {
        int rVote = this.getGOPVote();
        int dVote = this.getDEMVote();
        int totalV = rVote + dVote;
        int win_v = Math.max(rVote,dVote);
        int lost_v = Math.min(rVote,dVote);
        double win_wast = (win_v-lost_v)/2 -1;
        double gerrymanderingScore = (lost_v - win_wast)/totalV;
        return gerrymanderingScore;
    }
}
