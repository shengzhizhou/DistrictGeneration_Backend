package cse308.map.server;

import cse308.map.model.State;
import cse308.map.repository.StateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StateService {

    @Autowired
    private StateRepository stateRepository;

    public Iterable<State> findById(String stateId){
        return  stateRepository.findAll();
    }

}
