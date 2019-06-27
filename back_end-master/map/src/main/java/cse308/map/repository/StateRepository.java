package cse308.map.repository;

import cse308.map.model.State;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateRepository extends CrudRepository<State,String> {
    //first argument is the entity class
    //second is the id type of that entity class

    //CrudRepository is a interface has all functionality like things below.
    //getALLStates()
    //getStates(String id)
    //updateState(State s)
    //deleteState(String id)

}
