package cse308.map.server;

import cse308.map.model.Precinct;
import cse308.map.repository.PrecinctRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;

@Service
public class PrecinctService {

    @Autowired
    private PrecinctRepository precinctRepository;

    public Iterable<Precinct> getAllPrecincts(String stateId){
        Iterable<Precinct> temp = precinctRepository.findAll();
        ArrayList<Precinct> result = new ArrayList<>();
        Iterator<Precinct> precinctIterator = temp.iterator();
        while (precinctIterator.hasNext()){
            Precinct p = precinctIterator.next();
            if(p.getState().equals(stateId)){
                result.add(p);
            }
        }
        System.out.println(result.size());
        return result;
    }



}
