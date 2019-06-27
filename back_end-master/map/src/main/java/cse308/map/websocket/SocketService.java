package cse308.map.websocket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import cse308.map.algorithm.Algorithm;
import cse308.map.model.Configuration;
import cse308.map.model.Result;
import cse308.map.model.State;
import cse308.map.server.PrecinctService;
import cse308.map.server.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

@Service
public class SocketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketService.class);
    private static String[] states = {"New York", "California", "Pennsylvania"};
    private static String[] colors = {"red", "purple", "yellow", "orange", "blue"};
    private static Random r = new Random();
    private Algorithm currentAlgorithm = null;
    @Autowired
    private SocketIOServer server;
    @Autowired
    private PrecinctService precinctService;
    @Autowired
    private ResultService resultService;
    private static Map<String, SocketIOClient> clientsMap = new HashMap<String, SocketIOClient>();

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String uuid = client.getSessionId().toString();
        clientsMap.put(uuid, client);
        LOGGER.debug("IP: " + client.getRemoteAddress().toString() + " UUID: " + uuid + " connection success");
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String uuid = client.getSessionId().toString();
        clientsMap.remove(uuid);
        LOGGER.debug("IP: " + client.getRemoteAddress().toString() + " UUID: " + uuid + " disconnect from service");
    }

    @OnEvent(value = "runAlgorithm")
    public void onEvent(SocketIOClient client, AckRequest request, @RequestBody Configuration data) {
        System.out.println(data.getEmail());
        currentAlgorithm = new Algorithm("pa", data, precinctService,resultService, client);
        if(data.getNumOfRun() > 1)
            currentAlgorithm.setBatch(true);

        Thread task = new Thread(currentAlgorithm);
        task.start();
        //System.out.println("finished");
//       Optional<Result> a = resultService.findById(1);
//       Result r = a.get();
//       State s = (State)r.getStateJSON();
//       System.out.println(s.getId()+ s.getName());
    }
    @OnEvent(value = "saveMap")
    public void onSaveMap() {
        currentAlgorithm.saveMap();
    }

    @OnEvent(value = "loadMap")
    public void onLoadMap(SocketIOClient client, String id) {
        Optional<Result> result = resultService.findById(Long.valueOf(id));
        State state = (State)result.get().getStateJSON();
        client.sendEvent("updateDistrictBoundary", state.generateGeoJson());
    }

    @OnEvent(value = "deleteMap")
    public void onDeleteMap(SocketIOClient client, String id) {
        resultService.deleteState(Long.valueOf(id));
    }

    @OnEvent(value = "saveLog")
    public void onSaveLog(SocketIOClient client, String id) {
        java.util.logging.Logger logger= java.util.logging.Logger.getLogger("CSE308");
        FileHandler fh;
        try {
            fh=new FileHandler("GerryMandering.log");
            logger.addHandler(fh);
            SimpleFormatter formatter=new SimpleFormatter();
            fh.setFormatter(formatter);

            logger.info(currentAlgorithm.getLogFile().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    @OnEvent(value = "resume")
    public void onResume() {
        currentAlgorithm.resume();
    }

    @OnEvent(value = "pause")
    public void onPause() {
        currentAlgorithm.pause();
    }

    @OnEvent(value = "stop")
    public void onStop() {
        currentAlgorithm.stop();
    }


    public void sendMessageToAllClient(String eventType, String message) {
        Collection<SocketIOClient> clients = server.getAllClients();
        for (SocketIOClient client : clients) {
            client.sendEvent(eventType, message);
        }
    }
}
