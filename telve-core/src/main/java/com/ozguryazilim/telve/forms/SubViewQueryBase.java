/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.telve.forms;

import com.ozguryazilim.telve.entities.EntityBase;
import com.ozguryazilim.telve.entities.ViewModel;
import com.ozguryazilim.telve.query.QueryControllerBase;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SubView'lar için detay soru control sınıfı.
 * 
 * @author Hakan Uygun
 * @param <E> Sorgu için kullanılacak Entity Sınıfı
 * @param <R> Sorgu sonuçları için kullanılacak ViewModel
 */
public abstract class SubViewQueryBase<E extends EntityBase,R extends ViewModel> extends QueryControllerBase<E,R>{


    private static final Logger LOG = LoggerFactory.getLogger(SubViewQueryBase.class);
    
    private E entity;

    @Inject
    private ViewConfigResolver viewConfigResolver;
    
    @PostConstruct
    @Override
    public void init(){
        super.init();
        search();
    }
    
    /**
     * Geriye edit edilecek olan entity'i döndürür.
     * @return 
     */
    public E getEntity() {
        return entity;
    }

    /**
     * Edit edilen entity2i setler
     * @param entity 
     */
    public void setEntity(E entity) {
        this.entity = entity;
    }
    
    /**
     * Yeni bir entity oluşturur.
     */
    public void addItem(){
        try {
            entity = getRepository().createNew();
        } catch (InstantiationException | IllegalAccessException ex) {
            LOG.error("Error", ex);
        }
    }
    
    /**
     * Verilen id'li entity'i edit edilmek üzere hazırlar.
     * @param id 
     */
    public void editItem( Long id ){
        entity = getRepository().findBy(id);
    }
    
    /**
     * Verilen ID'li entity'i siler.
     * @param id
     */
    @Transactional
    public void removeItem( Long id ){
        editItem(id);
        if( !onBeforeDelete() ) return;
        //FIXME: Normal remove bir şekilde silmedi
        //getRepository().remove(entity);
        getRepository().deleteById(id);
        search();
        onAfterDelete();
    }
    
    /**
     * Edit edilen entity'i saklar
     */
    @Transactional
    public void save(){
        if( !onBeforeSave() ) return;
        getRepository().save(entity);
        search();
        onAfterSave();
    }
    
    /**
     * Saklama işleminden hemen önce çağrılır.
     * 
     * Eğer geriye false dönerse işlem devam etmez
     */
    public boolean onBeforeSave(){
        //Bu method override edilmek için var.
        return true;
    }
    
    /**
     * Saklama işleminden hemen sonra çağrılır.
     */
    public boolean onAfterSave(){
        //Bu method override edilmek için var.
        return true;
    }
    
    /**
     * Silme işleminden hemen önce çağrılır.
     * Eğer geriye false dönerse işlem devam etmez
     */
    public boolean onBeforeDelete(){
        //Bu method override edilmek için var.
        return true;
    }
    
    /**
     * Silme işleminden hemen sonra çağrılır.
     */
    public boolean onAfterDelete(){
        //Bu method override edilmek için var.
        return true;
    }

    /**
     * Eğer bu subview'ın seçildiğine dair FormBase'den event geldiyse tekrar sorgu çek.
     * @param event 
     */
    public void selectionListener( @Observes(notifyObserver = Reception.IF_EXISTS) SubViewSelectEvent event ){
        String s = viewConfigResolver.getViewConfigDescriptor(getViewPage()).getViewId();
        if( s.equals(event.getSubViewId())){
            search();
        }
    }

    /**
     * Bu SubView için tanımlı viewPage'i döndürür.
     * @return 
     */
    public Class<? extends ViewConfig> getViewPage(){
        return ((SubView)(ProxyUtils.getUnproxiedClass(this.getClass()).getAnnotation(SubView.class))).viewPage();
    }
}
