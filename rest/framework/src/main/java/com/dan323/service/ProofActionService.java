package com.dan323.service;

import com.dan323.model.Action;
import com.dan323.model.Proof;
import com.dan323.model.SequenceRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author danco
 */
@Service
public class ProofActionService {

    private final Map<String, List<String>> map = new HashMap<>();

    @Autowired
    private List<ProofService> servicesByLogic;

    @PostConstruct
    private void init() {
        for (ProofService service : servicesByLogic) {
            map.put(service.getLogicName(), service.initPossibleActions());
        }
    }

    public List<String> getAllActions(String logic) {
        return map.containsKey(logic) ? map.get(logic) : new ArrayList<>();
    }

    //TODO
    public Action<SequenceRule> processFile(MultipartFile file, String logic) {
        return new Action<>();
    }

    public void addCustomRule(Action<SequenceRule> action, String logic) {
        if (map.containsKey(logic)) {
            map.get(logic).add(action.getName());
        } else {
            List<String> list = new ArrayList<>();
            list.add(action.getName());
            map.put(logic, list);
        }
    }

    public <T extends Serializable, Q extends Serializable> Proof<Q> applyActionToProof(Proof<Q> proof, Action<T> action) {
        if (map.get(proof.getLogic()).contains(action.getName())) {
            ProofService<Q,T> service = servicesByLogic.stream()
                    .filter(ser -> ser.getLogicName().equals(proof.getLogic()))
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
            return service.applyAction(proof, action);
        } else {
            return proof;
        }
    }
}
