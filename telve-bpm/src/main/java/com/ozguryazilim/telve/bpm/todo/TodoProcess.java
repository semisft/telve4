/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.telve.bpm.todo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.ozguryazilim.telve.auth.UserInfo;
import com.ozguryazilim.telve.bpm.handlers.AbstractDialogProcessHandler;
import com.ozguryazilim.telve.bpm.handlers.ProcessHandler;
import com.ozguryazilim.telve.config.TelveConfigResolver;
import com.ozguryazilim.telve.sequence.SequenceManager;

/**
 * Bir kişinin kendi kendine atadığı tek kişilik HumanTask süreci.
 * 
 * @author Hakan Uygun 
 */
@ProcessHandler(processId = "todo", hasStartDialog = true)
public class TodoProcess extends AbstractDialogProcessHandler{

    private String subject;
    private String info;
    private Date startDate = new Date();
    private Date endDate;
    
    @Inject
    private SequenceManager sequenceManager;
    
    @Inject
    private TelveConfigResolver telveConfigResolver;
    
    @Inject
    private UserInfo userInfo;
    
    @Override
    public String getDialogName() {
        return "/bpm/todoProcessPopup";
    }

    /**
     * UI üzerinden alınan değerler ile süreç başlatır.
     */
    @Override
    public void startProcess() {
        
        Map<String, Object> values = new HashMap<>();
        
        values.put("SUBJECT", subject);
        values.put("INFO", info);
        values.put("START_DATE", startDate);
        values.put("END_DATE", endDate);
        values.put("ASSIGNEE", userInfo.getLoginName());
        values.put("AppTitle", telveConfigResolver.getProperty("app.title"));
        values.put("LinkDomain", telveConfigResolver.getProperty("app.linkDomain"));
        
        String bizKey = userInfo.getLoginName() + "-" + sequenceManager.getNewNumber("todo"+ userInfo.getLoginName(), 5);
        
        startProcess("todo", bizKey, values);
        
    }
 
    
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    
}
