package com.tekion.accounting.fs.common.dpProvider;

import com.google.common.collect.Maps;
import com.tekion.accounting.fs.common.dpProvider.enums.DpLevel;
import com.tekion.accounting.fs.common.dpProvider.enums.RegisteredDp;
import com.tekion.beans.DynamicProperty;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.propertyclient.DPClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class DpProvider {

    private Map<String, DynamicProperty<Integer>> integerDpMap = Maps.newHashMap();
    private Map<String, DynamicProperty<Boolean>> booleanDpMap = Maps.newHashMap();
    private Map<String, DynamicProperty<String>> stringDpMap = Maps.newHashMap();
    private Map<String, DynamicProperty<Long>> longDpMap = Maps.newHashMap();
    private final DPClient dpClient;


    @PostConstruct
    public void postConstruct(){
        for (RegisteredDp value : RegisteredDp.values()) {
            registerDp(value);
        }
    }

    // the reason for taking type from outside is it ensure type safety and so no one has to cast back.
    public <T> T getValForDp(RegisteredDp registeredDp, Class<T> clazz, DpLevel level, T defaultVal){

        //so no one can send unsupported class types.
        checkIfWillBeTypeSafe(registeredDp, (Class<T>) clazz);

        return doGetValForDp(registeredDp, clazz, level, defaultVal);
    }

    // the reason for taking type from outside is it ensure type safety and so no one has to cast back.
    public <T> T getValForDp(RegisteredDp registeredDp, Class<T> clazz, T defaultVal){
        //so no one can send unsupported class types.
        checkIfWillBeTypeSafe(registeredDp, clazz);
        if(Objects.isNull(registeredDp.getDefaultDpLevel())){
            log.error("error : trying to fetch dp whose level is not known : {}",registeredDp.name());
            throw new TBaseRuntimeException();
        }
        return doGetValForDp(registeredDp, clazz, registeredDp.getDefaultDpLevel(), defaultVal);
    }

    private <T> T doGetValForDp(RegisteredDp registeredDp, Class<T> clazz, DpLevel level, T defaultVal) {
        if (Integer.class.equals(clazz)) {
            DynamicProperty<Integer> integerDynamicProperty = integerDpMap.get(registeredDp.getDpName());
            return (T) doGetDpVal(integerDynamicProperty,Integer.class,level,(Integer)defaultVal);
        }
        else if (String.class.equals(clazz)) {
            DynamicProperty<String> integerDynamicProperty = stringDpMap.get(registeredDp.getDpName());
            return (T) doGetDpVal(integerDynamicProperty,String.class,level,(String)defaultVal);
        }
        else if (Boolean.class.equals(clazz)) {
            DynamicProperty<Boolean> integerDynamicProperty = booleanDpMap.get(registeredDp.getDpName());
            return (T) doGetDpVal(integerDynamicProperty,Boolean.class,level,(Boolean) defaultVal);
        }
        else if (Long.class.equals(clazz)) {
            DynamicProperty<Long> integerDynamicProperty = longDpMap.get(registeredDp.getDpName());
            return (T) doGetDpVal(integerDynamicProperty,Long.class,level,(Long) defaultVal);
        }
        throw new TBaseRuntimeException();
    }

    private <T> T doGetDpVal(DynamicProperty<T> integerDynamicProperty, Class<T> integerClass, DpLevel level, T defaultVal) {
        if(Objects.isNull(level)){
            throw new TBaseRuntimeException();
        }
        switch (level){
            case GLOBAL:
                return integerDynamicProperty.getSafeGlobalValue(defaultVal);
            case DEALER:
                return integerDynamicProperty.getSafeValueWithUserContext(defaultVal);
            case TENANT:
                return  integerDynamicProperty.getSafeValue(UserContextProvider.getCurrentTenantId(),"0",defaultVal);
        }
        throw new TBaseRuntimeException();
    }


    private void registerDp(RegisteredDp registeredDp) {
        doRegisterDp(registeredDp,registeredDp.getDpSupportedClass().getClazz());
    }

    private <T> void doRegisterDp(RegisteredDp registeredDp, Class<T> clazz) {
        if (Integer.class.equals(clazz)) {
            if(!integerDpMap.containsKey(registeredDp.getDpName())){
                integerDpMap.put(registeredDp.getDpName(),dpClient.getIntegerProperty(registeredDp.getModuleName(), registeredDp.getDpName()));
            }
        }
        else if (String.class.equals(clazz)) {
            if(!stringDpMap.containsKey(registeredDp.getDpName())){
                stringDpMap.put(registeredDp.getDpName(),dpClient.getStringProperty(registeredDp.getModuleName(), registeredDp.getDpName()));
            }
        }
        else if (Boolean.class.equals(clazz)) {
            if(!booleanDpMap.containsKey(registeredDp.getDpName())){
                booleanDpMap.put(registeredDp.getDpName(),dpClient.getBooleanProperty(registeredDp.getModuleName(), registeredDp.getDpName()));
            }
        }
        else if (Long.class.equals(clazz)) {
            if(!longDpMap.containsKey(registeredDp.getDpName())){
                longDpMap.put(registeredDp.getDpName(),dpClient.getLongProperty(registeredDp.getModuleName(), registeredDp.getDpName()));
            }
        }
    }


    private <T> void checkIfWillBeTypeSafe(RegisteredDp registeredDp, Class<T> clazz) {
        if (!registeredDp.getDpSupportedClass().getClazz().equals(clazz)) {
            log.error("error : sending class which is not the registered class for dp");
            throw new TBaseRuntimeException();
        }
    }
}
