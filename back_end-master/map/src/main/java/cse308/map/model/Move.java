package cse308.map.model;

import javax.persistence.Transient;
import java.io.Serializable;

public class Move implements Serializable {
    @Transient
    private static final long serialVersionUID = 4L;
    private District to;
    private District from;
    private Precinct precinct;
    public Move(District to, District from, Precinct precinct){
        this.to = to;
        this.from = from;
        this.precinct = precinct;
    }

    public void execute(){
        from.removePrecinct(precinct);
        to.addPrecinct(precinct);
    }

    public void undo(){
        to.removePrecinct(precinct);
        from.addPrecinct(precinct);
    }

    public District getTo(){
        return to;
    }

    public District getFrom(){ return from; }

    public Precinct getPrecinct(){
        return precinct;
    }

}
