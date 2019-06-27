package cse308.map.algorithm;

import com.corundumstudio.socketio.SocketIOClient;
import cse308.map.model.*;
import cse308.map.server.PrecinctService;
import cse308.map.server.ResultService;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

public class Algorithm implements Runnable{
    private volatile boolean running = true;
    private final Object pauseLock = new Object();
    private static final HashMap<Measure, String> measures;
    private StringBuilder logFile=new StringBuilder();
    private boolean isBatch = false;
    String[] colorList={"#e6194b", "#3cb44b", "#ffe119", "#4363d8", "#f58231", "#911eb4", "#46f0f0", "#f032e6", "#bcf60c", "#fabebe", "#008080", "#e6beff", "#9a6324", "#fffac8", "#800000", "#aaffc3", "#808000", "#ffd8b1", "#000075", "#808080", "#ffffff", "#000000"};
    public boolean isBatch() {
        return isBatch;
    }

    public void setBatch(boolean batch) {
        isBatch = batch;
    }

    static {
        measures = new HashMap<>();
        measures.put(Measure.POPULATION_EQUALITY, "ratePopequality");
        measures.put(Measure.EFFICIENCY_GAP, "rateStatewideEfficiencyGap");
        measures.put(Measure.COMPACTNESS, "rateCompactness");
        measures.put(Measure.PARTISAN_FAIRNESS, "ratePartisanFairness");
        measures.put(Measure.COMPETITIVENESS, "rateCompetitiveness");
    }

    private StringBuilder msg = new StringBuilder();
    private ArrayList<String> coloring = new ArrayList<>();
    private PrecinctService precinctService;
    private ResultService resultService;
    private Map<Integer, State> states = new HashMap<>();
    private State currentState;
    private SocketIOClient client;
    private HashMap<Measure, Double> weights = new HashMap<>();
    private HashMap<District, Double> currentScores = new HashMap<>();
    private Map<String, District> mmDistricts = new HashMap<>();

    public StringBuilder getLogFile(){
        return logFile;
    }
    //pass the precinctService to the algorithm object because we can't autowired precinctService for each object it is not working.
    public Algorithm(String stateName, Configuration configuration, PrecinctService precinctService, ResultService resultService, SocketIOClient client) {
        configuration.initWeights();
        if (configuration.getNumOfRun() == 1) {
            this.currentState = new State(configuration);
        } else {
            for (int i = 0; i < configuration.getNumOfRun(); i++) {
                states.put(i, new State(i, stateName, configuration));
            }
        }
        this.precinctService = precinctService;
        this.resultService=resultService;
        this.client = client;
//        setWeights(configuration.getWeights());
    }

    private void init() {
        sendMessage("fetching precinct'state data...");
//        Iterable<Precinct> allPrecincts = precinctService.getAllPrecincts("35");
        Iterable<Precinct> allPrecincts = precinctService.getAllPrecincts(currentState.getConfiguration().getState());

        for (Precinct p : allPrecincts) {
            currentState.addPrecinct(p);
        }
        sendMessage("Constructing Clusters...");
        for (Precinct p : currentState.getPrecincts().values()) {
            currentState.addCluster(new Cluster(p));
        }
        sendMessage("Initializing State...");
        currentState.initState();
        for (Cluster c : currentState.getClusters().values()) {
            c.setColor(randomColor());
        }
    }

    private void graphPartition() {
        sendMessage("Phase 1 Graph partition...");
        while (currentState.getClusters().size() > currentState.getConfiguration().getTargetDistrictNumber()) {
            Set<String> mergedCluster = new HashSet<>();
            Iterator<Map.Entry<String, Cluster>> clusterIterator = currentState.getClusters().entrySet().iterator();
            while (clusterIterator.hasNext()) {
                Map.Entry<String, Cluster> clusterEntry = clusterIterator.next();
                Cluster currentCluster = clusterEntry.getValue();
                ArrayList<ClusterEdge> bestClusterEdges = currentCluster.getBestClusterEdges();
                for (ClusterEdge edge : bestClusterEdges) {
                    if (isValidCombine(currentCluster, mergedCluster)) {
                        Cluster neighborCluster = edge.getNeighborCluster(currentCluster);
                        if (!mergedCluster.contains(neighborCluster.getClusterID())) {
                            disconnectNeighborEdge(edge, currentCluster, neighborCluster);
                            combine(currentCluster, neighborCluster);
                            mergedCluster.add(neighborCluster.getClusterID());
                            clusterIterator.remove();
                            break;
                        }
                    }
                }
            }
            updateColor();
            if(!isBatch)
            pause();
        }
    }

    private boolean isValidCombine(Cluster currentCluster, Set<String> mergedCluster) {
        return !mergedCluster.contains(currentCluster.getClusterID())//if the cluster already combine
                && currentState.getTargetPopulation() > currentCluster.getDemographic().getPopulation()
                && currentState.getClusters().size() > currentState.getConfiguration().getTargetDistrictNumber();
    }

    private void updateColor() {
        StringBuilder temp = new StringBuilder();
        for (Cluster c : currentState.getClusters().values()) {
            for (Precinct ps : c.getPrecincts()) {
                temp.append(ps.getId()).append(":").append(c.getColor()).append(",");
            }
        }
        client.sendEvent("updateColor", temp.toString());
    }

    private void disconnectNeighborEdge(ClusterEdge desireClusterEdge, Cluster currentCluster, Cluster neighborCluster) {
        neighborCluster.removeEdge(desireClusterEdge);
        currentCluster.removeEdge(desireClusterEdge);
        currentCluster.removeDuplicateEdge(neighborCluster);//remove c4
    }

    private void combine(Cluster currentCluster, Cluster neighborCluster) {
        for (ClusterEdge edge : currentCluster.getAllEdges()) { //add edges(c5) from neighborCluster to currentCluster
            edge.changeNeighbor(edge.getNeighborCluster(currentCluster), neighborCluster);
            neighborCluster.addEdge(edge);
        }
        neighborCluster.combineCluster(currentCluster);//combine demo data
        for (ClusterEdge edge : neighborCluster.getAllEdges()) {//re-compute currentCluster edges join
            edge.computeJoin(currentState.getComunityOfinterest(), currentState.getConfiguration().getMajorMinorWeight());
        }
    }

    private void sendMove(Move move) {
        client.sendEvent("updateColor", move.getPrecinct().getId() + ":" + move.getTo().getCluster().getColor() + "$"
                + "{"+ move.getTo().getCluster().getProperty() + "}");
    }





    private void annealing() {
        Move move;
//        System.out.println("-12-3-1-23-1-23-1-2");
        int counter=0;
        int tempc=0;
        ArrayList<Move> moves=new ArrayList<>();
        StringBuilder moveString=new StringBuilder();
        while ((move = makeMove()) != null&&counter<200) {
            counter++;

//            moveString.append("updateColor"+move.getPrecinct().getId() + ":" + move.getTo().getCluster().getColor() + ",");
//
//            TimerTask repeatedTask = new TimerTask() {
//                public void run() {
//                    client.sendEvent(String.valueOf(moveString));
//                    moveString.setLength(0);
//                }
//            };
//            Timer timer = new Timer("Timer");
//
//            long delay = 500L;
//            long period = 500L ;
//            timer.scheduleAtFixedRate(repeatedTask, delay, period);

            sendMove(move);
            move.getFrom().setCurrentScore(rateDistrict(move.getFrom()));
            move.getTo().setCurrentScore(rateDistrict(move.getTo()));
            move.getPrecinct().setCombineNum(move.getPrecinct().getCombineNum()+1);
            for(District d:currentState.getDistricts().values()){
                System.out.print(d.getShape().getGeometryType()+ ", ");

            }
            System.out.println(counter++);
//            if(counter<20){
//                moves.add(move);
//                counter++;
//            }
//            else {
//                for(Move i:moves)
//                    sendMove(i);
//                System.out.println("send"+tempc);
//                tempc++;
//                moves=new ArrayList<>();
//                counter=0;
//            }
        }
//        for(Move i:moves)
//            sendMove(i);
//        moves=new ArrayList<>();
//        counter=0;
    }

    private Move makeMove() {
        District smallestDistrict = getSmallestDistrict(currentState.getDistricts());
        int equalPopulation = currentState.getPopulation() / currentState.getConfiguration().getTargetDistrictNumber();
        if (smallestDistrict.getPopulation()>0&&smallestDistrict.getPopulation() < equalPopulation) {
            Move bestMove;
//            for(Precinct precinct:smallestDistrict.getMovelist()){
            bestMove = getMove(smallestDistrict);
            if(bestMove!=null){
                updateMoveList(bestMove);
//                District from = bestMove.getFrom();
//                Precinct p = bestMove.getPrecinct();
//                for(Precinct neighbor : p.getOtherDistrctPreicincts()){
//                    for(Precinct nn : neighbor.getOtherDistrctPreicincts()){
//                        if(p.getParentCluster() == nn.getParentCluster() && p.getId()!=nn.getId()){
//                            break;
//                        }
//                    }
//                    from.getMovelist().remove(neighbor);
//                }
//                bestMove.execute();
//                bestMove.getTo().getMovelist().remove(bestMove.getPrecinct());
//                Set<Precinct> otherDistrictPrecincts=bestMove.getPrecinct().getOtherDistrctPreicincts();
//                if(!otherDistrictPrecincts.isEmpty())
//                    for (Precinct otherDistrictPrecinct : otherDistrictPrecincts) {
//                        bestMove.getTo().getMovelist().add(otherDistrictPrecinct);
//                    }
                return bestMove;
            }
//            }
            return makeMove_secondary();

        }
        return null;
    }
    private void updateMoveList(Move move){
        District from = move.getFrom();
        Precinct p = move.getPrecinct();
        for(Precinct neighbor : p.getOtherDistrctPreicincts()){
            for(Precinct nn : neighbor.getOtherDistrctPreicincts()){
                if(p.getParentCluster() == nn.getParentCluster() && p.getId()!=nn.getId()){
                    break;
                }
            }
            from.getMovelist().remove(neighbor);
        }
        move.execute();
        move.getTo().getMovelist().remove(move.getPrecinct());
        Set<Precinct> otherDistrictPrecincts = move.getPrecinct().getOtherDistrctPreicincts();
        if(!otherDistrictPrecincts.isEmpty()){
            for (Precinct otherDistrictPrecinct : otherDistrictPrecincts) {
                move.getTo().getMovelist().add(otherDistrictPrecinct);
            }
        }
    }

    private void checkMmDistricts(){
        int desiredNumMm = currentState.getConfiguration().getDesiredNumMajorMinorDistrict();
        if(mmDistricts.size() < desiredNumMm){

        }

    }


    private void setMmDistricts(){
        int desiredNumMm = currentState.getConfiguration().getDesiredNumMajorMinorDistrict();
        if(desiredNumMm != 0){
            for(District d: currentState.getDistricts().values()){
                double districtMajorMinorValue = d.getMajorMinor(currentState.getComunityOfinterest());
                if (districtMajorMinorValue >= currentState.getConfiguration().getMinMajorMinorPercent()
                        && districtMajorMinorValue <= currentState.getConfiguration().getMaxMajorMinorPercent()) {
                    if(mmDistricts.size()< desiredNumMm){
                        mmDistricts.put(d.getdistrictID(),d);
                    }
                }
            }
        }
    }


    private Move getMove(District current) {
//        System.out.println("get move");
        Move bestMove = null;
        double bestImprovement = 0;
        int desiredNumMM = currentState.getConfiguration().getDesiredNumMajorMinorDistrict();
        for (Precinct precinct : current.getMovelist()) {
            double districtMajorMinorValue = current.getMajorMinor(currentState.getComunityOfinterest());
            double totalMajorMinorValue = precinct.getMajorMinor(currentState.getComunityOfinterest()) + districtMajorMinorValue;
            if(desiredNumMM != 0 && mmDistricts.size() <= desiredNumMM){
                if (totalMajorMinorValue >= currentState.getConfiguration().getMinMajorMinorPercent()
                        && totalMajorMinorValue <= currentState.getConfiguration().getMaxMajorMinorPercent()) {
                    District neighborDistrict = currentState.getFromDistrict(precinct);
                    Move move = new Move(current, neighborDistrict,precinct);
                    double improvement = testMove(move);
                    if (improvement > bestImprovement) {
                        bestMove = move;
                        bestImprovement = improvement;
                        return bestMove;
                    }
                }
            }else{
                if(desiredNumMM != 0 && mmDistricts.containsKey(current.getdistrictID())){
                    if (totalMajorMinorValue >= currentState.getConfiguration().getMinMajorMinorPercent()
                            && totalMajorMinorValue <= currentState.getConfiguration().getMaxMajorMinorPercent()) {
                        District neighborDistrict = currentState.getFromDistrict(precinct);
                        Move move = new Move(current, neighborDistrict, precinct);
                        double improvement = testMove(move);
                        if (improvement > bestImprovement) {
                            bestMove = move;
                            bestImprovement = improvement;
                            return bestMove;
                        }
                    }
                }else{
                    District neighborDistrict = currentState.getFromDistrict(precinct);
                    Move move = new Move(current, neighborDistrict, precinct);
                    double improvement = testMove(move);
                    if (improvement > bestImprovement) {
                        bestMove = move;
                        bestImprovement = improvement;
                        return bestMove;
                    }
                }
            }
        }
        return bestMove;
    }

    private double testMove(Move move) {

        if(move.getPrecinct().getCombineNum()>1){
            return 0;
        }
        double initial_score = move.getTo().getCurrentScore() + move.getFrom().getCurrentScore();
        move.execute();
//        if(move.getFrom().getShape().getGeometryType().equals("MultiPolygon")){
//            move.undo();
//            return 0;
            if(!searchByDepth(move)) {
                move.undo();
                return 0;
            }
//        }
        double to_score = rateDistrict(move.getTo());
        double from_score = rateDistrict(move.getFrom());
        double final_score = to_score + from_score;
        double improvement = final_score - initial_score;
        move.undo();
        return improvement <= 0 ? 0 : improvement;
    }

    private Move makeMove_secondary() {
//        System.out.println("secondery");
        List<District> districts = getSortedDistricts();
        districts.remove(0);//remove last round smallest district
        int c=1;
        while (c > 0) {
            c--;
            District startDistrict = districts.get(0);
//            for (Precinct precinct : startDistrict.getBorderPrecincts()) {
            Move m = getMove(startDistrict);//......
            setMmDistricts();
            if (m != null) {
                updateMoveList(m);
//                m.execute();
//                m.getTo().getMovelist().remove(m.getPrecinct());
//
//                Set<Precinct> otherDistrictPrecincts=m.getPrecinct().getOtherDistrctPreicincts();
//                if(!otherDistrictPrecincts.isEmpty())
//                    for (Precinct otherDistrictPrecinct : otherDistrictPrecincts) {
//                        m.getTo().getMovelist().add(otherDistrictPrecinct);
//                    }
                return m;
            }
        }
        districts.remove(0);
//        }
        return null;
    }

    private District getSmallestDistrict(Map<String, District> districts) {
        int i = Integer.MAX_VALUE;
        District smallestDistrict = null;
        for (District district : districts.values()) {
            if (district.getDemo().getPopulation() < i && district.getDemo().getPopulation() > 0) {
                smallestDistrict = district;
                i = district.getDemo().getPopulation();
            }
        }
        return smallestDistrict;
    }

    private List<District> getSortedDistricts() {
        ArrayList<District> list = new ArrayList<>(currentState.getDistricts().values());
        Collections.sort(list);
        return list;
    }

    public void setWeights(HashMap<Measure, Double> w) {
        weights = w;
        currentScores = new HashMap<District,Double>();
        for (District d : currentState.getDistricts().values()) {
            currentScores.put(d, rateDistrict(d));
        }
    }

    private double rateDistrict(District d) {
        double score = 0;
        for (Measure m : weights.keySet()) {
            if (weights.get(m) != 0) {
                try {
                    Method rate = this.getClass().getDeclaredMethod(measures.get(m), District.class);
                    double rating = ((Double) rate.invoke(this, d));
                    rating = 1 - Math.pow((1 - rating), 2);
                    score += weights.get(m) * rating;
                } catch (Exception e) {
                    System.out.println(m.name() + " - " + e.getClass().getCanonicalName());
                    System.out.println(e.getMessage());
                    return -1;
                }
            }
        }
        return score;
    }

    public void calculateObjectiveFunction() {
        double score = 0;
        for (District d : currentState.getDistricts().values()) {
            score += currentScores.get(d);

        }
        currentState.setObjectiveFunValue(score);
//        return score;
    }

    public double ratePopequality(District d) {
        int idealPopulation = currentState.getPopulation() / currentState.getDistricts().size();
        int truePopulation = d.getPopulation();
        if (idealPopulation >= truePopulation) {
            return ((double) truePopulation) / idealPopulation;
        }
        return ((double) idealPopulation / truePopulation);
    }

    public double ratePartisanFairness(District d) {
        int totalVote = 0;
        int totalGOPvote = 0;
        int totalDistricts = 0;
        int totalGOPDistricts = 0;
        for (District sd : currentState.getDistricts().values()) {
            totalVote += sd.getGOPVote();
            totalVote += sd.getDEMVote();
            totalGOPvote += sd.getGOPVote();
            totalDistricts += 1;
            if (sd.getGOPVote() > sd.getDEMVote()) {
                totalGOPDistricts += 1;
            }
        }
        int idealDistrictChange = ((int) Math.round(totalDistricts * ((1.0 * totalGOPvote) / totalVote))) - totalGOPDistricts;
        if (idealDistrictChange == 0) {
            return 1.0;
        }
        int gv = d.getGOPVote();
        int dv = d.getDEMVote();
        int tv = gv + dv;
        int margin = gv - dv;
        if (tv == 0) {
            return 1.0;
        }
        int win_v = Math.max(gv, dv);
        int loss_v = Math.min(gv, dv);
        int inefficient_v;
        if (idealDistrictChange * margin > 0) {
            inefficient_v = win_v - loss_v;
        } else {
            inefficient_v = loss_v;
        }
        return 1.0 - ((inefficient_v * 1.0) / tv);
    }

    private double rateCompactness(District d) {
        double allPrecincts = d.getCluster().getPrecincts().size();
        double borderPrecincts = d.getMovelist().size();
        return borderPrecincts / Math.abs(allPrecincts - borderPrecincts);
    }

    private double calPolsbyPopperCompactness(District d){
        return (4*Math.PI*d.getShape().getArea())/ Math.pow(d.getShape().getLength(),2);
        //4pi*area of geometry / parameter^2
    }

    private double calSchwartzbergCompactness(District d){
        double radius = Math.sqrt(d.getShape().getArea()/Math.PI);
        double perimeter = 2 * Math.PI * radius;
        return 1/(d.getShape().getLength()/perimeter);
        //r = sqrt(A/PI)
        //C = 2*PI*r
        //1/(P/C)
    }

    public boolean searchByDepth(Move move) {
        Cluster c = move.getFrom().getCluster();
        List<Precinct> visitedNodes = new LinkedList<>();
        List<Precinct> unvisitedNodes = new LinkedList<>();
        unvisitedNodes.add(c.getFromClusterPrecinct());
        while(!unvisitedNodes.isEmpty()) {
            Precinct currNode = unvisitedNodes.remove(0);
            if(visitedNodes.contains(currNode)){
                continue;
            }
            List<Precinct> newNodes = currNode.getSameClusterNeighbor(currNode).stream()
                    .filter(node -> !visitedNodes.contains(node))
                    .collect(Collectors.toList());;
            unvisitedNodes.addAll(0,newNodes);
            visitedNodes.add(currNode);
//            unvisitedNodes.removeIf(visitedNodes::contains);
        }
//        System.out.println("out while");
        if(visitedNodes.size() == move.getFrom().getCluster().getPrecincts().size()
               ) {
            return true;
        }
        return false;
    }

    private boolean isContiguity(Move move){
//        Geometry fromDistrict = move.getFrom().getShape();
//        // Geometry toDistrict = move.getTo().getShape();
//        Geometry precinctShape = move.getPrecinct().getShape();
//        Geometry symDifference = fromDistrict.symDifference(precinctShape);
//        return !symDifference.getGeometryType().equals("MultiPolygon");
        return searchByDepth(move);

    }

    public double rateStatewideEfficiencyGap(District d) {
        int iv_g = 0;
        int iv_d = 0;
        int tv = 0;
        for (District sd : currentState.getDistricts().values()) {
            int gv = sd.getGOPVote();
            int dv = sd.getDEMVote();
            if (gv > dv) {
                iv_d += dv;
                iv_g += gv - dv;
            } else if (dv > gv) {
                iv_g += gv;
                iv_d += dv - gv;
            }
            tv += gv;
            tv += dv;
        }
        return 1.0 - ((Math.abs(iv_g - iv_d) * 1.0) / tv);
    }

    public double rateEfficiencyGap(District d) {
        int gv = d.getGOPVote();
        int dv = d.getDEMVote();
        int tv = gv + dv;
        if (tv == 0) {
            return 1.0;
        }
        int win_v = Math.max(gv, dv);
        int loss_v = Math.min(gv, dv);
        int inefficient_v = Math.abs(loss_v - (win_v - loss_v));
        return 1.0 - ((inefficient_v * 1.0) / tv);
    }

    public double rateCompetitiveness(District d) {
        int gv = d.getGOPVote();
        int dv = d.getDEMVote();
        return 1.0 - (Math.abs(gv-dv)/(gv + dv));
//        return rateCompactness(d);
    }

    private String randomColor() {
        Random random = new Random();
        // create a big random number - maximum is ffffff (hex) = 16777215 (dez)
        int nextInt = random.nextInt(0xffffff + 1);
        // format it as hexadecimal string (with hashtag and leading zeros)
        String colorCode = String.format("#%06x", nextInt);
        while (coloring.contains(colorCode)) {
            nextInt = random.nextInt(0xffffff + 1);
            // format it as hexadecimal string (with hashtag and leading zeros)
            colorCode = String.format("#%06x", nextInt);
        }
        coloring.add(colorCode);
        return colorCode;
    }

    public void singleRun(){
        sendMessage("Algorithm Start...");
        init();
        graphPartition();
        currentState.initDistrict();
        int counter=0;
        for(District d:currentState.getDistricts().values()){
            System.out.print(d.getShape().getGeometryType()+ ", ");
            d.getCluster().setColor(colorList[counter%20]);
            d.initMoveList();
            counter++;
        }
        updateDistrictBoundary();
        setMmDistricts();
        setWeights(currentState.getConfiguration().getWeights());
        System.out.println("----:"+mmDistricts.size());
        client.sendEvent("Phase two");
        if(isBatch==false)
            pause();
        annealing();
        calculateObjectiveFunction();
        calculateNumRepublican();
        updateDistrictBoundary();
        currentState.setNumMMDistrict(mmDistricts.size());
        System.out.println(mmDistricts.size());
        for(District d :mmDistricts.values()){
            System.out.println("MM:"+d.getMajorMinor(currentState.getComunityOfinterest()));
        }
        System.out.println(currentState.getSummary());
//        resultService.saveState(new Result("333@gmail.com",this.currentState));
        sendMessage("Algorithm finished!");


    }


    public void run() {
        StringBuilder str = new StringBuilder();
        if(isBatch){
            int counter = 1;
            str.append(originalGerrymandering()).append("\n");
            for (Map.Entry<Integer, State> stateEntry : states.entrySet()) {
                currentState = stateEntry.getValue();
//                Measure change=currentState.getConfiguration().getChangeMeasure();
//                currentState.getConfiguration().getWeights().put(change,currentState.getConfiguration().getWeights().get(change)+1);
                singleRun();
                Result result = resultService.saveState(new Result(currentState.getConfiguration().getEmail(),currentState,summaryOfBatch()));
                str.append("Batch run: ").append(result.getId()).append("\n").append(summaryOfBatch()).append("\n").append("\n");
                sendMessage("batch run: " + (stateEntry.getKey()+1) + " finished!");
            }
        }else {
            singleRun();
            str.append(getTotalSummarySingleRun());
        }

        client.sendEvent("summary",str);
    }

//    private void saveToDatabase() {
//        Result result=new Result();
//            HashMap<String,Object> attributes=new HashMap<>();
//            attributes.put("nm",currentState);
//            result.setCustomerAttributes(attributes);
//        try {
//            result.serializeCustomerAttributes();
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//        resultService.saveState(result);
//    }

    private void calculateNumRepublican(){
        int counter = 0;
        for(District d : currentState.getDistricts().values()){
            d.setParty();
            if(d.getP() == Party.REPUBLICAN){
                counter++;
            }
        }
        currentState.setNumRepublican(counter);
        currentState.setNumDemocratic(currentState.getDistricts().size()-counter);
    }
    private void updateDistrictBoundary() {
        StringBuilder info=new StringBuilder();
        info.append("'\n'");
        int districtNum = 1;
        for (District district : currentState.getDistricts().values()) {
            info.append("No.").append(districtNum).append(": ").append(district.getdistrictID()).append(" : precinct size ").append(district.getCluster().getPrecincts().size()).append(", population ").append(district.getCluster().getDemographic().getPopulation()).append("'\n'");
//            System.out.println(msg);
            districtNum++;
        }
        sendMessage(info.toString());
        msg.append("'\n'");
        StringBuilder districtColor = new StringBuilder();
        sendMessage("Assigning Colors...");
        StringBuilder districtJson = new StringBuilder();
        districtJson.append("{\"type\":\"FeatureCollection\", \"features\": [");
        for (Cluster c : currentState.getClusters().values()) {
            if(mmDistricts.containsKey(c.getClusterID())){
                c.setIsMajorMinorDistrict("true");
            }
            else{
                c.setIsMajorMinorDistrict("false");
            }
            districtJson.append(c.toGeoJsonFormat()).append("},\n");
            for (Precinct ps : c.getPrecincts()) {
                districtColor.append(ps.getId()).append(":").append(c.getColor()).append(",");
            }
            client.sendEvent("updateColor", districtColor.toString());
            districtColor = new StringBuilder();
        }
        districtJson.deleteCharAt(districtJson.length() - 2).append("]}");
        sendDistrictBoundary(districtJson.toString());
        sendMessage(msg.toString());
    }



    private void sendMessage(String msg) {
        client.sendEvent("message", msg);
        logFile.append(msg).append("'\n'");
    }

    private void sendDistrictBoundary(String msg) {
        System.out.println("send");
        client.sendEvent("updateDistrictBoundary", msg);
    }
    public void stop() {
        running = false;
        // you might also want to interrupt() the Thread that is
        // running this Runnable, too, or perhaps call:
        resume();
        // to unblock
    }

    public void pause() {
        synchronized (pauseLock){
            try {
                pauseLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void resume() {
        synchronized (pauseLock) {
            pauseLock.notifyAll(); // Unblocks thread
        }
    }

    public void saveMap(){
        Result result = new Result(currentState.getConfiguration().getEmail(), currentState,summaryOfBatch());
        resultService.saveState(result);
    }

    public void loadMap(Long id){
        Optional<Result> result = resultService.findById(id);
        State state = (State)result.get().getStateJSON();
       sendDistrictBoundary(state.generateGeoJson());

    }

    public String majorMinorResult(){
        String mm ="";
        for(District d:mmDistricts.values()){
            mm += "District: "+d.getdistrictID()+" majorMinor_Result: "+d.getMajorMinor(currentState.getComunityOfinterest())+ "population of this race: "+d.getCluster().getDemographic().getMajorMinorPop(currentState.getComunityOfinterest())+"\n";
        }
        return mm;
    }

    public String originalGerrymandering(){
        String gm = "Gerrymandering scores for the actual votes by district:\n";
        gm += "District: 1"+" Score: 0.4066,\n  Republican Percentage: 0.2855,\n  Democratic Percentage: 0.2344\n";
        gm += "District: 2"+" Score: 0.4867,\n  Republican Percentage: 0.2041,\n  Democratic Percentage: 0.1967\n";
        gm += "District: 3"+" Score: 0.4789,\n  Republican Percentage: 0.2614,\n  Democratic Percentage: 0.2700\n";
        gm += "District: 4"+" Score: 0.4040,\n  Republican Percentage: 0.0249,\n  Democratic Percentage: 0.2989\n";
        return gm;
    }
    public String gerrymanderingGenerated(){
        DecimalFormat df = new DecimalFormat("0.0000");
        String gm = "Gerrymandering scores of the newly generated districts:\n";
        for(District d : currentState.getDistricts().values()){
            double rvote = d.getGOPVote()*1.0;
            double dvote = d.getDEMVote()*1.0;
            double totalRVote = currentState.getRvote()*1.0;
            double totalDVote = currentState.getDvote()*1.0;
            double r = rvote/totalRVote;
            double gd = dvote/totalDVote;
            System.out.println("totRv:"+totalRVote+"totDv:"+totalDVote);
            System.out.println("r:"+r+"rvote:"+rvote+"d:"+d+"dvote:"+dvote);
            gm += "District: "+d.getDistrictID()+",\n  Score: "+ df.format(d.rateGerrymanderingScore())+
                    ",\n  Republican Percentage: "+df.format(r)+",\n  Democratic Percentage: "+df.format(gd)+"\n";
        }
        return gm;
    }
    
    public String getTotalSummarySingleRun(){
        String total = "";
        total += originalGerrymandering()+"\n";
        total += gerrymanderingGenerated()+"\n";
        total += majorMinorResult()+"\n";
        total += currentState.getSummary();
        return total;
    }

    public String summaryOfBatch(){
        String total = "";
        total += gerrymanderingGenerated()+"\n";
        total += majorMinorResult()+"\n";
        total += currentState.getSummary();
        return total;
    }

}
