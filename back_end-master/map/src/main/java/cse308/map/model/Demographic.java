package cse308.map.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.io.Serializable;

@Embeddable
public class Demographic implements Serializable {
    @Transient
    private static final long serialVersionUID = 4L;
    private int population;
    @Column(name = "democraticvote")
    private int democraticVote;
    @Column(name = "republicanvote")
    private int republicanVote;
    @Column(name = "white")
    private int white;
    @Column(name = "africanamerican")
    private int africanAmerican;
    @Column(name = "asian")
    private int asian;
    @Column(name = "other")
    private int other;
    @Column(name = "hawaiian")
    private int hawaiian;
    @Column(name = "hispanic")
    private int hispanic;
    public Demographic(){

    }
    public String demoGeojson(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\"GOVDV2010\":\"").append(democraticVote).append("\",")
                .append("\"GOVRV2010\":\"").append(republicanVote).append("\",")
                .append("\"total\":\"").append(population).append("\",")
                .append("\"white\":\"").append(white).append("\",")
                .append("\"black\":\"").append(africanAmerican).append("\",")
                .append("\"asian\":\"").append(asian).append("\",")
                .append("\"other\":\"").append(other).append("\",")
                .append("\"hawaiian\":\"").append(hawaiian).append("\",")
                .append("\"hispanic\":\"").append(hispanic);
        return stringBuilder.toString();
    }
    public Demographic(Demographic demographic) {

        this.population= demographic.getPopulation();
        this.africanAmerican = demographic.getAfricanAmerican();
        this.asian = demographic.getAsian();
        this.hispanic = demographic.getHispanic();
        this.hawaiian = demographic.getHawaiian();
        this.white = demographic.getWhite();
        this.other = demographic.getOther();
        this.democraticVote = demographic.getDemocraticVote();
        this.republicanVote = demographic.getRepublicanVote();

    }

    public void combineDemo(Demographic demo) {
        population += demo.getPopulation();
        democraticVote += demo.getDemocraticVote();
        republicanVote += demo.getRepublicanVote();
        white += demo.getWHITE();
        africanAmerican += demo.getAfricanAmerican();
        asian += demo.getAsian();
        other += demo.getOther();
        hawaiian += demo.getHawaiian();
        hispanic += demo.getHispanic();
    }

    public void removeDemo(Demographic demo){
        population -= demo.getPopulation();
        democraticVote -= demo.getDemocraticVote();
        republicanVote -= demo.getRepublicanVote();
        white -= demo.getWHITE();
        africanAmerican -= demo.getAfricanAmerican();
        asian -= demo.getAsian();
        other -= demo.getOther();
        hawaiian -= demo.getHawaiian();
        hispanic -= demo.getHispanic();
    }

    public int getMajorMinorPop(MajorMinor majorMinor){
        int[] majorMinorPops=new int[MajorMinor.values().length];
        majorMinorPops[MajorMinor.WHITE.ordinal()] = white;
        majorMinorPops[MajorMinor.AFRICANAMERICAN.ordinal()] = africanAmerican;
        majorMinorPops[MajorMinor.ASIAN.ordinal()] = asian;
        majorMinorPops[MajorMinor.OTHER.ordinal()] = other;
        majorMinorPops[MajorMinor.HAWAIIAN.ordinal()] = hawaiian;
        majorMinorPops[MajorMinor.HISPANIC.ordinal()] = hispanic;
        return majorMinorPops[majorMinor.ordinal()];
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getDemocraticVote() {
        return democraticVote;
    }

    public void setDemocraticVote(int democraticVote) {
        this.democraticVote = democraticVote;
    }

    public int getRepublicanVote() {
        return republicanVote;
    }

    public void setRepublicanVote(int republicanVote) {
        this.republicanVote = republicanVote;
    }

    public int getWHITE() {
        return white;
    }

    public void setWHITE(int white) {
        this.white = white;
    }

    public int getAfricanAmerican() {
        return africanAmerican;
    }

    public void setAfricanAmerican(int africanAmerican) {
        this.africanAmerican = africanAmerican;
    }

    public int getAsian() {
        return asian;
    }

    public void setAsian(int asian) {
        this.asian = asian;
    }

    public int getWhite() { return white; }

    public void setWhite(int white) { this.white = white; }

    public int getOther() { return other; }

    public void setOther(int other) { this.other = other; }

    public int getHawaiian() { return hawaiian; }

    public void setHawaiian(int hawaiian) { this.hawaiian = hawaiian; }

    public int getHispanic() { return hispanic; }

    public void setHispanic(int hispanic) { this.hispanic = hispanic; }
}
