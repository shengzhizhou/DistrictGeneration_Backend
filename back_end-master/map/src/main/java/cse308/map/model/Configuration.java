package cse308.map.model;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.HashMap;

public class Configuration implements Serializable {
    @Transient
    private String changeMeasures;
    private Measure changeMeasure;
    private String compactnessMethod;
    private static final long serialVersionUID = 4L;
    private int targetDistrictNumber;
    private int numOfRun;
    private double majorMinor;
    private double maxMajorMinorPercent;
    private double minMajorMinorPercent;
    private int desiredNumMajorMinorDistrict;
    private String interestCommunity;
    private MajorMinor communityOfInterest;
    private double equality;
    private double efficiencyGpa;
    private double compactness;
    private double fairness;
    private double competitiveness;
    private HashMap<Measure, Double> weights=new HashMap<>();
    private String email;
    private String state;
    public String getEmail() {
        return email;
    }

    public String getState() { return state; }

    public void setState(String state) { this.state = state; }

    public void setEmail(String email) {
        this.email = email;
    }

    public Configuration(){

    }

    public void setCommunityOfInterest(MajorMinor communityOfInterest) {
        this.communityOfInterest = communityOfInterest;
    }

    public void initWeights(){
        this.communityOfInterest= MajorMinor.valueOf(interestCommunity);
        weights.put(Measure.COMPACTNESS, compactness/10);
        weights.put(Measure.POPULATION_EQUALITY, equality/10);
        weights.put(Measure.EFFICIENCY_GAP, efficiencyGpa/10);
        weights.put(Measure.PARTISAN_FAIRNESS, fairness/10);
        weights.put(Measure.COMPETITIVENESS, competitiveness/10);
//        changeMeasure=Measure.valueOf(changeMeasures);
        majorMinor/=10;
        maxMajorMinorPercent /=100;
        minMajorMinorPercent /=100;
    }



    public int getTargetDistrictNumber() {
        return targetDistrictNumber;
    }

    public void setTargetDistrictNumber(int desireNum) {
        this.targetDistrictNumber = desireNum;
    }

    public int getNumOfRun() {
        return numOfRun;
    }

    public void setNumOfRun(int numOfRun) {
        this.numOfRun = numOfRun;
    }

    public double getMajorMinorWeight() { return majorMinor; }

    public void setMajorminor(double majorminor) { this.majorMinor = majorminor; }

    public void setWeights(HashMap<Measure, Double> weights) {
        this.weights = weights;
    }

    public HashMap<Measure, Double> getWeights() {
        return weights;
    }

    public MajorMinor getCommunityOfInterest() {
        return communityOfInterest;
    }

    public void setComunityOfinterest(MajorMinor comunityOfinterest) {
        this.communityOfInterest = comunityOfinterest;
    }

    public double getEquality() {
        return equality;
    }

    public void setEquality(double equality) {
        this.equality = equality;
    }

    public double getEfficiencyGpa() {
        return efficiencyGpa;
    }

    public void setEfficiencyGpa(double efficiencyGpa) {
        this.efficiencyGpa = efficiencyGpa;
    }

    public double getCompactness() {
        return compactness;
    }

    public void setCompactness(double compactness) {
        this.compactness = compactness;
    }

    public double getFairness() {
        return fairness;
    }

    public void setFairness(double fairness) {
        this.fairness = fairness;
    }

    public double getCompetitiveness() {
        return competitiveness;
    }

    public void setCompetitiveness(double competitiveness) {
        this.competitiveness = competitiveness;
    }

    public double getMajorMinor() {
        return majorMinor;
    }

    public void setMajorMinor(double majorMinor) {
        this.majorMinor = majorMinor;
    }

    public String getInterestCommunity() {
        return interestCommunity;
    }

    public void setInterestCommunity(String interestCommunity) {
        this.interestCommunity = interestCommunity;
    }

    public double getMaxMajorMinorPercent() {
        return maxMajorMinorPercent;
    }

    public void setMaxMajorMinorPercent(double maxMajorMinorPercent) {
        this.maxMajorMinorPercent = maxMajorMinorPercent;
    }

    public double getMinMajorMinorPercent() {
        return minMajorMinorPercent;
    }

    public void setMinMajorMinorPercent(double minMajorMinorPercent) {
        this.minMajorMinorPercent = minMajorMinorPercent;
    }

    public int getDesiredNumMajorMinorDistrict() {
        return desiredNumMajorMinorDistrict;
    }

    public void setDesiredNumMajorMinorDistrict(int desiredNumMajorMinorDistrict) {
        this.desiredNumMajorMinorDistrict = desiredNumMajorMinorDistrict;
    }

    @Override
    public String toString() {
        if(changeMeasures.equals("")){
            return "Configuration: " +
                    "\n TargetDistrictNumber= " + targetDistrictNumber +
                    ",\n TargetMajorMinorDistrict= "+ targetDistrictNumber +
                    ",\n NumOfRun= " + numOfRun +
                    ",\n CompactnessMethod= " + compactnessMethod +
//                    ",\n MajorMinorWeight= " + majorMinor +
                    ",\n CommunityOfInterest= " + interestCommunity +
                    ",\n PopulationEquality= " + equality +
                    ",\n EfficiencyGpa= " + efficiencyGpa +
                    ",\n Compactness= " + compactness +
                    ",\n PartisanFairness= " + fairness +
                    ",\n Competitiveness= " + competitiveness +
                    ", MinMajorMinorPercent= "+ minMajorMinorPercent +
                    ", MaxMajorMinorPercent= "+ maxMajorMinorPercent;
        }

        return "Configuration:" +
                "\n TargetDistrictNumber= " + targetDistrictNumber +
                ",\n TargetMajorMinorDistrict= "+ targetDistrictNumber +
                ",\n NumOfRun= " + numOfRun +
                ",\n ChangedMeasure= " + changeMeasures +
                ",\n CompactnessMethod= " + compactnessMethod +
//                ",\n MajorMinorWeight= " + majorMinor +
                ",\n CommunityOfInterest= " + interestCommunity +
                ",\n PopulationEquality= " + equality +
                ",\n EfficiencyGpa= " + efficiencyGpa +
                ",\n Compactness= " + compactness +
                ",\n PartisanFairness= " + fairness +
                ",\n Competitiveness= " + competitiveness +
                ", MinMajorMinorPercent= "+ minMajorMinorPercent +
                ", MaxMajorMinorPercent= "+ maxMajorMinorPercent;
    }

    public String getChangeMeasures() {
        return changeMeasures;
    }

    public void setChangeMeasures(String changeMeasures) {
        this.changeMeasures = changeMeasures;
    }

    public Measure getChangeMeasure() {
        return changeMeasure;
    }

    public void setChangeMeasure(Measure changeMeasure) {
        this.changeMeasure = changeMeasure;
    }


    public String getCompactnessMethod() {
        return compactnessMethod;
    }

    public void setCompactnessMethod(String compactnessMethod) {
        this.compactnessMethod = compactnessMethod;
    }
}
