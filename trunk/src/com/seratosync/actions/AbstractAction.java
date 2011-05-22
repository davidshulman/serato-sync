package com.seratosync.actions;

import com.seratosync.config.ActionExecutionException;
import com.seratosync.config.RuleFile;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAction {

    private RuleFile ruleFile;
    private Map<String, String> parameters = new HashMap<String, String>();

    public abstract void run() throws ActionExecutionException;

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public void setParameter(String name, String value) {
        parameters.put(name, value);
    }

    public RuleFile getRuleFile() {
        return ruleFile;
    }

    public void setRuleFile(RuleFile ruleFile) {
        this.ruleFile = ruleFile;
    }

}
