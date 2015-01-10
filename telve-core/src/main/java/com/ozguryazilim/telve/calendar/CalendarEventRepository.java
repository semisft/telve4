/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.telve.calendar;

import com.ozguryazilim.mutfak.kahve.annotations.UserAware;
import com.ozguryazilim.telve.auth.ActiveUserRoles;
import com.ozguryazilim.telve.data.RepositoryBase;
import com.ozguryazilim.telve.entities.CalendarEvent;
import com.ozguryazilim.telve.entities.CalendarEvent_;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.Criteria;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

/**
 * Takvim olayları veri deposu
 * @author Hakan Uygun
 */
@Repository
@Dependent
public abstract class CalendarEventRepository extends RepositoryBase<CalendarEvent, CalendarEvent> implements CriteriaSupport<CalendarEvent>{
    
    
    /**
     * Context üzerinde tanımlı filtre
     */
    @Inject
    private CalendarFilterModel filterModel;
    
    @Inject @ActiveUserRoles
    private List<String> userRoles;
    
    @Inject @UserAware
    private String userId;
    
    /**
     * Verilen iki tarih arasındaki filtrelenmiş eventlerin listesini döndürür.
     * 
     * @param startDate
     * @param endDate
     * @return 
     */
    public List<CalendarEvent> findFilteredEvents( Date startDate, Date endDate ){

        //Hangi kaynaklar taranacak bulunuyor
        //IN hata vermesin diye liste boşsa bir string ekliyoruz.
        List<String> sources = filterModel.getCalendarSources();
        if( sources.isEmpty() ){
            sources.add("NONE");
        }
        
        //Actör filtresi hazırlanıyor
        List<String> actors = new ArrayList<>();
        
        actors.add(userId);
        
        if( filterModel.getShowPersonalEvents() ){
            actors.addAll(userRoles);
        }
        
        
        Criteria<CalendarEvent,CalendarEvent> crit = criteria()
                .gtOrEq(CalendarEvent_.startDate, startDate)
                .ltOrEq(CalendarEvent_.endDate, endDate)
                .in(CalendarEvent_.sourceName, sources.toArray(new String[]{}) )
                .in(CalendarEvent_.actor, actors.toArray(new String[]{}) );
            
        //Kapıları gösterecek miyiz?
        if( !filterModel.getShowClosedEvents() ){
            crit.eq(CalendarEvent_.done, Boolean.FALSE);
        }
        
        return crit.getResultList();
    }
    
    /**
     * Verilen kaynak için tamamlanmamış eventleri döndürür.
     * @param source
     * @param startDate
     * @param endDate
     * @return 
     */
    public List<CalendarEvent> findUnDoneEvents( String source, Date startDate, Date endDate ){
        
        Criteria<CalendarEvent,CalendarEvent> crit = criteria()
                .gtOrEq(CalendarEvent_.startDate, startDate )
                .ltOrEq(CalendarEvent_.endDate, endDate )
                .eq(CalendarEvent_.sourceName, source )
                .eq(CalendarEvent_.done, Boolean.FALSE );
        
        return crit.getResultList();
    }
    
    /**
     * Verilen source ve sourcekey'leri kullanarak event listesi döndürür. 
     * 
     * sourceKey'i like ile arar. Sadece done=false olanları döndürür.
     * 
     * @param sourceName kaynak ismi
     * @param sourceKey anahtar like için gerekli kuralları içermeli
     * @param done tamamlanıp tamamlanmadığı
     * @return 
     */
    public abstract List<CalendarEvent> findBySourceNameAndSourceKeyLikeAndDone( String sourceName, String sourceKey, Boolean done );
    
    /**
     * Bir kaynaktan gelen eventleri toplu silmek için kullanılır.
     * 
     * @param sourceName
     * @param sourceKey like ile kullanılır.
     * @return 
     */
    @Modifying
    @Query("delete CalendarEvent as p where p.sourceName = ?1 and p.sourceKey like ?2")
    public abstract int deleteEvents(String sourceName, String sourceKey );
    
    
    /**
     * Eventleri toplu olarak tamalandı işaretlemek için kullanılır.
     * 
     * @param sourceName
     * @param sourceKey like ile kullanılır.
     * @return 
     */
    @Modifying
    @Query("update CalendarEvent as p set p.done = true where p.sourceName = ?1 and p.sourceKey like ?2")
    public abstract int doneEvents(String sourceName, String sourceKey );
}
