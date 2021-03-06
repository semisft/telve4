/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.telve.idm.user;

import com.google.common.base.Strings;
import com.ozguryazilim.telve.audit.AuditLogCommand;
import com.ozguryazilim.telve.audit.ChangeLogStore;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.auth.UserDataChangeEvent;
import com.ozguryazilim.telve.auth.UserModel;
import com.ozguryazilim.telve.auth.UserModelRegistery;
import com.ozguryazilim.telve.data.RepositoryBase;
import com.ozguryazilim.telve.forms.FormBase;
import com.ozguryazilim.telve.forms.FormEdit;
import com.ozguryazilim.telve.idm.IdmEvent;
import com.ozguryazilim.telve.idm.entities.User;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kullanıcı tanımlama ekranı.
 * 
 * @author Hakan Uygun
 */
@FormEdit( feature = UserFeature.class )
public class UserHome extends FormBase<User, Long>{
    
    private static final Logger LOG = LoggerFactory.getLogger(UserHome.class);

    @Inject
    private ViewConfigResolver viewConfigResolver;
    
    @Inject
    private UserRepository repository;
    
    @Inject 
    private Event<IdmEvent>  event;
    
    @Inject
    private Event<UserDataChangeEvent> userEvent;
    
    @Inject
    private Identity identity;

    private String password;
    
    private List<String> fragments;
    
    private ChangeLogStore changeLogStore = new ChangeLogStore();

    @Override
    public boolean onAfterLoad() {
        changeLogStore.clear();
        
        changeLogStore.addOldValue("general.label.FirstName", getEntity().getFirstName());
        changeLogStore.addOldValue("general.label.LastName", getEntity().getLastName());
        changeLogStore.addOldValue("user.label.UserType", getEntity().getUserType());
        changeLogStore.addOldValue("general.label.Email", getEntity().getEmail());
        changeLogStore.addOldValue("user.label.DomainGroup", getEntity().getDomainGroup() != null ? getEntity().getDomainGroup().getName() : null );
        
        return true;
    }

    
    
    @Override
    public boolean onBeforeSave() {
        
        if( !Strings.isNullOrEmpty(password)){
            DefaultPasswordService passwordService = new DefaultPasswordService();
            getEntity().setPasswordEncodedHash(passwordService.encryptPassword(password));
            changeLogStore.addNewValue("user.caption.Password", "Changed");
        }
        
        
        User ofUser = repository.hasLoginName( getEntity().getLoginName(), getEntity().getId() == null ? 0 : getEntity().getId());
        if ( ofUser != null ) {
            FacesMessages.error("Aynı login name ile kayıtlı kullanıcı mevcut. Lütfen Kullanıcı adımı değiştiriniz.");
            return false;
        }
        
        
        changeLogStore.addNewValue("general.label.FirstName", getEntity().getFirstName());
        changeLogStore.addNewValue("general.label.LastName", getEntity().getLastName());
        changeLogStore.addNewValue("user.label.UserType", getEntity().getUserType());
        changeLogStore.addNewValue("general.label.Email", getEntity().getEmail());
        changeLogStore.addNewValue("user.label.DomainGroup", getEntity().getDomainGroup() != null ? getEntity().getDomainGroup().getName() : null );
        
        
        return true;
    }

    @Override
    public boolean onAfterSave() {
        event.fire(new IdmEvent(IdmEvent.FROM_USER, IdmEvent.CREATE, getEntity().getLoginName()));
        userEvent.fire(new UserDataChangeEvent(getEntity().getLoginName()));
        return super.onAfterSave(); 
    }
    
    @Override
    public boolean onBeforeDelete() {
        event.fire(new IdmEvent(IdmEvent.FROM_USER, IdmEvent.DELETE, getEntity().getLoginName()));
        userEvent.fire(new UserDataChangeEvent(getEntity().getLoginName()));
        return super.onAfterSave(); 
    }
    
    

    /**
     * Geriye ek model UI fragmentlerinin listesi döner.
     *
     * @return
     */
    public List<String> getUIFragments() {

        if (fragments == null) {
            populateFragments();
        }

        LOG.info("UI Fragments : {}", fragments);

        return fragments;
    }

    /**
     * UI için gerekli fragment listesini hazırlar.
     */
    protected void populateFragments() {
        fragments = new ArrayList<>();

        for (UserModel m : UserModelRegistery.getUserModelMap().values()) {
            fragments.add(viewConfigResolver.getViewConfigDescriptor(m.fragment()).getViewId());
        }
    }

    
    /**
     * Geriye kullanıcı tiplerini döndürür.
     *
     * @return
     */
    public List<String> getUserTypes() {
        
        //Eğer kullanıcı SUPERADMIN değil ise başka bir kullanıcıyı SUPERADMIN yapamaz
        List<String> result = UserModelRegistery.getUserTypes();
        if( !UserModelRegistery.SUPER_ADMIN_TYPE.equals(identity.getUserInfo().getUserType())){
            result.remove(UserModelRegistery.SUPER_ADMIN_TYPE);
        }
        
        return UserModelRegistery.getUserTypes();
    }
    
    /**
     * Eğer kullanıcı SUPERADMIN değil ise kendi bilgilerinden kritik olan yerleri değiştiremez.
     * 
     * Örneğin UserType, DomainGroup v.b.
     * @return 
     */
    public Boolean canChangeCriticalData(){
        if( !identity.getLoginName().equals( getEntity().getLoginName())) return true;
        if( UserModelRegistery.SUPER_ADMIN_TYPE.equals(identity.getUserInfo().getUserType())) return true;
        return false;
    }
    
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getDomainGroupRequired(){
        return "true".equals(ConfigResolver.getPropertyValue("security.domainGroup.control", "false"));
    }
    
    @Override
    protected RepositoryBase<User, ?> getRepository() {
        return repository;
    }    

    
    @Override
    protected void auditLog(String action) {
        getAuditLogger().actionLog(getEntity().getClass().getSimpleName(), getEntity().getId(), getBizKeyValue(), AuditLogCommand.CAT_AUTH,  action, identity.getLoginName(), "", changeLogStore.getChangeValues());
    }
    
    
}
