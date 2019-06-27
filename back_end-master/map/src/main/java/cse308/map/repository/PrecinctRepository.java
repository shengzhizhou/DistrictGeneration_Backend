package cse308.map.repository;

import cse308.map.model.Precinct;
import cse308.map.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrecinctRepository extends CrudRepository<Precinct,String> {

}
