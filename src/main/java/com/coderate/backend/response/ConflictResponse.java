package com.coderate.backend.response;

import com.coderate.backend.enums.State;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConflictResponse {
    private Map<String , String> fileConflictLines;
    private Map<String , List<State>> statesThatDiffer;

    public ConflictResponse() {
        this.fileConflictLines = new HashMap<>();
        this.statesThatDiffer = new HashMap<>();
    }

    public ConflictResponse(Map<String, String> fileConflictLines, Map<String , List<State>>  statesThatDiffer) {
        this.fileConflictLines = fileConflictLines;
        this.statesThatDiffer = statesThatDiffer;
    }

    public Map<String, String> getFileConflictLines() {
        return fileConflictLines;
    }

    public void setFileConflictLines(Map<String, String> fileConflictLines) {
        this.fileConflictLines = fileConflictLines;
    }

    public Map<String , List<State>> getStatesThatDiffer() {
        return statesThatDiffer;
    }

    public void setStatesThatDiffer(Map<String , List<State>>  statesThatDiffer) {
        this.statesThatDiffer = statesThatDiffer;
    }
}
