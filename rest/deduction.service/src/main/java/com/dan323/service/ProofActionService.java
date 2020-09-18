package com.dan323.service;

import com.dan323.model.Action;
import com.dan323.model.Proof;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author danco
 */
@Service
public class ProofActionService {

    private final Map<String, List<Action>> map = new HashMap<>();

    public ProofActionService() {
        init(map);
    }

    private void init(Map<String, List<Action>> map) {
    }

    public Proof getProofById(String id) {
        return new Proof();
    }

    public List<Action> getAllActions(String logic) {
        return map.containsKey(logic) ? map.get(logic) : new ArrayList<>();
    }

    public Action processFile(MultipartFile file, String logic) {
        return new Action();
    }

    public boolean addAction(Action act, String logic) {
        if (map.containsKey(logic)) {
            map.get(logic).add(act);
            return true;
        } else {
            return false;
        }
    }
}
