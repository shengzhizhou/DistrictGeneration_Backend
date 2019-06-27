package cse308.map.controller;

import cse308.map.repository.StateRepository;
import cse308.map.model.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping(value = "/rest/mapdata")
@CrossOrigin
public class MapController {
    @Autowired
    private StateRepository stateRepository;

    @GetMapping("/{state}")//select the state with the specify id from the database
    public ResponseEntity<State> getStateInfo(@PathVariable("state") String id) {
        Optional<State> opt = stateRepository.findById(id);
        if (!opt.isPresent()) {
            return new ResponseEntity("No such element ", HttpStatus.NOT_FOUND);
        } else
            return new ResponseEntity<State>(opt.get(), HttpStatus.OK);
    }

    @GetMapping("/all")
    public Iterable<State> findAll() {
        return stateRepository.findAll();
    }

    @PostMapping(value = "/load")//save the state to the database
    public ResponseEntity<?> addPTToBoard(@RequestBody State state) {
        State newState = stateRepository.save(state);//save the model into database
        return new ResponseEntity<State>(newState, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")//deleted the state by specify id from database
    public ResponseEntity<?> deletedState(@PathVariable String id) {
        stateRepository.deleteById(id);
        return new ResponseEntity<String>("State has been deleted", HttpStatus.OK);
    }
}
