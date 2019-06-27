//package cse308.map.controller;
//
//import com.corundumstudio.socketio.SocketIOClient;
//import cse308.map.algorithm.Algorithm;
//import cse308.map.model.Configuration;
//import cse308.map.model.State;
//import cse308.map.server.PrecinctService;
//import cse308.map.server.StateService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@CrossOrigin
//public class AlgorithmController {
//
//    @Autowired
//    private StateService stateService;
//    @Autowired
//    private PrecinctService precinctService;
//
//    @PostMapping(value = "/run")//select the state with the specify id from the database
//    public ResponseEntity<?> runAlgorithm(@RequestBody Configuration stateConfiguration, SocketIOClient client){
//        System.out.println("xxxxx algorithm");
//        System.out.println("state id: "+ stateConfiguration.getStateId());
//        Iterable<State> opt = stateService.findById(stateConfiguration.getStateId());
//
//        System.out.println();
//
//        //hardcode
//        stateConfiguration.setDesireNum(10);
//        Algorithm algorithm = new Algorithm("pa", stateConfiguration.getDesireNum(),1,precinctService,client);
//        algorithm.run();
//
//
//        return new ResponseEntity<State>(opt.iterator().next(), HttpStatus.OK);
//    }
//
//
//
//}
