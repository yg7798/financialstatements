package com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs;

import com.google.common.collect.Maps;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class ExcelColumnConfigGeneratorUtil {

    public static void registerClassInSortKeyMap(String configClassName) {
        if (AccAbstractColumnConfig.configClassNameToSortKeyToEnumMap.get(configClassName) == null) {
            doSynchronisedRegisterKeysInMap(configClassName);
        }
    }

    public static void registerClassInPreferenceKeyToEnumListMap(String configClassName) {
        if (AccAbstractColumnConfig.configClassNameToPreferenceKeyToEnumListMap.get(configClassName) == null) {
            doSynchronisedRegisterKeysInSortKeyTOEnumListMap(configClassName);
        }
    }

    public static void registerClassInConditionedColumnsMap(String configClassName) {
        if (AccAbstractColumnConfig.configClassNameToConditionedColumnsMap.get(configClassName) == null) {
            doSynchronisedConditionedColumnsinMap(configClassName);
        }
    }

    public static void registerClassInPreferenceKeyMap(String configClassName) {
        if (AccAbstractColumnConfig.configClassNameToPreferenceKeyToEnumMap.get(configClassName) == null) {
            doSynchronisedRegisterPreferenceKepMap(configClassName);
        }
    }


    public static void registerClassForColumnFreezeMapping(String configClassName) {
        if (AccAbstractColumnConfig.configClassNameToFrozenTypeToEnumListMap.get(configClassName) == null) {
            doSynchronisedRegisterColumnFreezeMapping(configClassName);
        }
    }

    public static void registerClassForEnumList(String configClassName) {
        if (AccAbstractColumnConfig.configClassNameToEnumList.get(configClassName) == null) {
            doSynchronisedRegisterEnumList(configClassName);
        }
    }

    private static Class<?> getClassForClazzName(String configClassName) {
        Class<?> aClass = null;
        try {
            aClass = Class.forName(configClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return aClass;
    }

    private synchronized static void doSynchronisedRegisterKeysInMap(String configClassName) {
        if (AccAbstractColumnConfig.configClassNameToSortKeyToEnumMap.get(configClassName) == null) {
            Class<?> clazz = getClassForClazzName(configClassName);
            
            AccAbstractColumnConfig[] enumList = (AccAbstractColumnConfig[])clazz.getEnumConstants();
            for (AccAbstractColumnConfig val : enumList) {
                AccAbstractColumnConfig.configClassNameToSortKeyToEnumMap.compute(val.getClass().getName(), (key, oldVal) -> {
                    oldVal = TCollectionUtils.nullSafeMap(oldVal);
                    oldVal.put(val.getSortKeyMapping(), val);
                    return oldVal;
                });
            }

        }
    }

    private synchronized static void doSynchronisedRegisterKeysInSortKeyTOEnumListMap(String configClassName) {
        if (AccAbstractColumnConfig.configClassNameToPreferenceKeyToEnumListMap.get(configClassName) == null) {
            Class<?> clazz = getClassForClazzName(configClassName);

            AccAbstractColumnConfig[] enumList = (AccAbstractColumnConfig[])clazz.getEnumConstants();
            for (AccAbstractColumnConfig val : enumList) {
                AccAbstractColumnConfig.configClassNameToPreferenceKeyToEnumListMap.compute(val.getClass().getName(), (key, oldVal) -> {
                    oldVal = TCollectionUtils.nullSafeMap(oldVal);

                    oldVal.compute(val.getPreferenceColumnKey(), (keyInternal,currentValue)->{
                        currentValue = TCollectionUtils.nullSafeList(currentValue);
                        currentValue.add(val);
                        return currentValue;
                    });
                    return oldVal;
                });
            }

        }
    }
    private synchronized static void doSynchronisedConditionedColumnsinMap(String configClassName) {
        if (AccAbstractColumnConfig.configClassNameToConditionedColumnsMap.get(configClassName) == null) {
            Class<?> clazz = getClassForClazzName(configClassName);

            AccAbstractColumnConfig[] enumList = (AccAbstractColumnConfig[])clazz.getEnumConstants();
            for (AccAbstractColumnConfig val : enumList) {
                if(val.isColumnConditioned()){
                    AccAbstractColumnConfig.configClassNameToConditionedColumnsMap.compute(val.getClass().getName(), (key, oldVal) -> {
                        oldVal = TCollectionUtils.nullSafeList(oldVal);
                        oldVal.add(val);
                        return oldVal;
                    });
                }
            }
        }
    }
    private synchronized static void doSynchronisedRegisterPreferenceKepMap(String configClassName) {
        if (AccAbstractColumnConfig.configClassNameToPreferenceKeyToEnumMap.get(configClassName) == null) {
            Class<?> clazz = getClassForClazzName(configClassName);
            AccAbstractColumnConfig[] enumList = (AccAbstractColumnConfig[])clazz.getEnumConstants();
            for (AccAbstractColumnConfig val : enumList) {
                if (TStringUtils.isNotBlank(val.getPreferenceColumnKey()) && !val.isColumnConditioned()) {
                    AccAbstractColumnConfig.configClassNameToPreferenceKeyToEnumMap.compute(val.getClass().getName(), (key, oldVal) -> {
                        oldVal = TCollectionUtils.nullSafeMap(oldVal);
                        oldVal.put(val.getPreferenceColumnKey(), val);
                        return oldVal;
                    });
                }
            }
            if (AccAbstractColumnConfig.configClassNameToPreferenceKeyToEnumMap.get(configClassName) == null) {
                AccAbstractColumnConfig.configClassNameToPreferenceKeyToEnumMap.put(configClassName, Maps.newHashMap());
            }
        }
    }

    private synchronized static void doSynchronisedRegisterColumnFreezeMapping(String configClassName) {
        if (AccAbstractColumnConfig.configClassNameToFrozenTypeToEnumListMap.get(configClassName) == null) {
            Class<?> clazz = getClassForClazzName(configClassName);
            AccAbstractColumnConfig[] enumList = (AccAbstractColumnConfig[])clazz.getEnumConstants();
            for (AccAbstractColumnConfig val : enumList) {
                if (Objects.nonNull(val.getColumnFreezeType()) && !val.isColumnConditioned()) {
                    AccAbstractColumnConfig.configClassNameToFrozenTypeToEnumListMap.compute(val.getClass().getName(), (key, oldVal) -> {
                        oldVal = TCollectionUtils.nullSafeMap(oldVal);
                        oldVal.compute(val.getColumnFreezeType(), (keyInternalMap, oldValInternalList) -> {
                            oldValInternalList = TCollectionUtils.nullSafeList(oldValInternalList);
                            oldValInternalList.add(val);
                            return oldValInternalList;
                        });
                        return oldVal;
                    });
                }
            }
            if (AccAbstractColumnConfig.configClassNameToFrozenTypeToEnumListMap.get(configClassName) == null) {
                AccAbstractColumnConfig.configClassNameToFrozenTypeToEnumListMap.put(configClassName, Maps.newHashMap());
            }
        }

    }

    private synchronized static void doSynchronisedRegisterEnumList(String configClassName) {
        if (AccAbstractColumnConfig.configClassNameToEnumList.get(configClassName) == null) {
            Class<?> clazz = getClassForClazzName(configClassName);
            AccAbstractColumnConfig[] enumList = (AccAbstractColumnConfig[])clazz.getEnumConstants();
            for (AccAbstractColumnConfig val : enumList) {
                    if(!val.isColumnConditioned()){
                        AccAbstractColumnConfig.configClassNameToEnumList.compute(val.getClass().getName(), (key, oldVal) -> {
                            oldVal = TCollectionUtils.nullSafeList(oldVal);
                            oldVal.add(val);
                            return oldVal;
                        });
                    }
            }
        }
    }
}
