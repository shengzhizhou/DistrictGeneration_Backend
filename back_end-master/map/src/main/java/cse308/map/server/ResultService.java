package cse308.map.server;

import cse308.map.model.Result;
import cse308.map.repository.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cse308.map.model.Result;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class ResultService{
    @Autowired
    private ResultRepository resultRepository;

    public Optional<Result> findById(long stateId){
        return  resultRepository.findById(stateId);
    }

    public Result saveState(Result state){
        return resultRepository.save(state);
    }

    public List<Long> findALLByEmail(String email) {
        Iterable<Result> temp =resultRepository.findAll();
        ArrayList<Long> result = new ArrayList<>();
        Iterator<Result> resultIterator = temp.iterator();
        while (resultIterator.hasNext()){
            Result p = resultIterator.next();
            if(p.getEmail().equals(email)){
                result.add(p.getId());
            }
        }
        return result;
    }

    public void deleteState(long id){
        resultRepository.deleteById(id);
    }
}
