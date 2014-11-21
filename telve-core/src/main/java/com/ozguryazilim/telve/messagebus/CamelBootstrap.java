/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.telve.messagebus;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.cdi.ContextName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Camel Context Bootstrap
 * @author Hakan Uygun
 */
@Singleton
@Startup
public class CamelBootstrap implements Serializable{
    
    private static final Logger LOG = LoggerFactory.getLogger(CamelBootstrap.class);
    
    @Inject @ContextName("telve")
    private CdiCamelContext camelContext;
    
    @PostConstruct
    public void init(){
        LOG.info("Camel CDI Context Init");
        
        try {
            camelContext.start();
        } catch (Exception ex) {
            LOG.error("Camel cannot started!", ex);
        }
        LOG.info("Camel CDI Context Started");
    }
    
    
    @PreDestroy
    public void shutdown(){
        try {
            camelContext.stop();
        } catch (Exception ex) {
            LOG.error("Camel stop error!", ex);
        }
        LOG.info("Camel CDI Context Stoped");
    }
}