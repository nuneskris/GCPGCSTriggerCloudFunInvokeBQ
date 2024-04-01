package com.java.kfn.study.gcp.cloudfunction;

public class StudyPipelineException extends Exception { 
    public StudyPipelineException(String errorMessage) {
        super(errorMessage);
    }
}