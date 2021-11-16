package com.tekion.accounting.fs.common.dpProvider;

import com.tekion.accounting.fs.common.dpProvider.enums.DpLevel;
import com.tekion.accounting.fs.common.dpProvider.enums.RegisteredDp;
import com.tekion.accounting.fs.common.enums.Env;
import com.tekion.core.exceptions.TConfigurationNotFoundException;
import com.tekion.core.utils.TStringUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static java.lang.System.getenv;

@Slf4j
@UtilityClass
public class DpUtils {

    private static DpProvider dpProvider;
    public static final String DEFAULT_TB_CALCULATOR_VERSION_FOR_OEM_FS = "v2";
    private static final boolean DEFAULT_M13_CLOSE_ENABLED = true ;
    private static final boolean DEFAULT_USE_ID_SORT_IN_CONTROL_BOOK_FETCH = true ;
    private static final boolean DEFAULT_MONTH_LEDGER_BASED_TB_CALCULATION = true;

    public static void autowireStaticElem(DpProvider dpProvider1) {
        DpUtils.dpProvider = dpProvider1;
    }

    public static boolean doUseTbGeneratorV2VersionForFsInOem(){
        boolean val = false;
        String valForDp = dpProvider.getValForDp(RegisteredDp.TB_CALCULATOR_VERSION_FOR_OEM_FS, String.class, DEFAULT_TB_CALCULATOR_VERSION_FOR_OEM_FS);
        if(valForDp.equalsIgnoreCase("v2")){
            val =  true;
        }
        log.info("doUseTbGeneratorV2VersionForFsInOem val: {}", val);

        return val;
    }

    public static boolean doAllowM13Close(){
        Boolean valForDp = dpProvider.getValForDp(RegisteredDp.M13_CLOSE_FEATURE_ENABLED, Boolean.class, DEFAULT_M13_CLOSE_ENABLED);
        return valForDp;
    }

    // wont have perf issues  as fetches in 1 go at service start and doesnt fetch again till modifiedTime updated for dp
    // if it doesnt work for carls black we can revert for just the tenant.

    public static boolean doUseIdSortInScheduleControlFetch(){
        String globalVal = dpProvider.getValForDp(RegisteredDp.USE_ID_SORT_IN_CONTROL_BOOK_FETCH, String.class, "");
        String tenantVal = dpProvider.getValForDp(RegisteredDp.USE_ID_SORT_IN_CONTROL_BOOK_FETCH, String.class, DpLevel.TENANT,"");
        String dealerVal = dpProvider.getValForDp(RegisteredDp.USE_ID_SORT_IN_CONTROL_BOOK_FETCH, String.class, DpLevel.DEALER,"");

        if(TStringUtils.isNotBlank(dealerVal)){
            try {
                return Boolean.valueOf(dealerVal);
            }
            catch (Exception e){

            }
        }

        if(TStringUtils.isNotBlank(tenantVal)){
            try {
                return Boolean.valueOf(tenantVal);
            }
            catch (Exception e){

            }
        }

        if(TStringUtils.isNotBlank(globalVal)){
            try {
                return Boolean.valueOf(globalVal);
            }
            catch (Exception e){

            }
        }
        return DEFAULT_USE_ID_SORT_IN_CONTROL_BOOK_FETCH;
    }
    public static long getSleepTimeInSyncInMillis() {
        Long globalVal = dpProvider.getValForDp(RegisteredDp.SLEEP_TIME_IN_SYNC_MILLIS, Long.class, 500L);
        Long tenantVal = dpProvider.getValForDp(RegisteredDp.SLEEP_TIME_IN_SYNC_MILLIS, Long.class, DpLevel.TENANT,0L);
        Long dealerVal = dpProvider.getValForDp(RegisteredDp.SLEEP_TIME_IN_SYNC_MILLIS, Long.class, DpLevel.DEALER,0L);
        if (dealerVal > 0) return dealerVal;
        if (tenantVal > 0) return tenantVal;
        return globalVal;
    }

    public static long getMaxMonthsAllowedForControlBookHistory(){
        Long globalVal = dpProvider.getValForDp(RegisteredDp.MAX_MONTHS_OF_CONTROL_BOOK_HISTORIES_ALLOWED, Long.class, 18l);
        return globalVal;
    }


    public static int getMaxControlBookFetchAllowed(){
        Integer globalVal = dpProvider.getValForDp(RegisteredDp.MAX_CONTROL_BOOK_FETCH_ALLOWED, Integer.class, 20000);
        return globalVal;
    }


    public static String getRetryableConfig(){
        String valForDp = dpProvider.getValForDp(RegisteredDp.RETRY_CONFIG, String.class, "");
        return valForDp;
    }

    public static boolean doAllowMonthLedgerBasedTbCalculation() {
        return dpProvider.getValForDp(RegisteredDp.MONTH_LEDGER_BASED_TB_CALCULATION, Boolean.class, DEFAULT_MONTH_LEDGER_BASED_TB_CALCULATION);
    }


    //TODO: update this
    public static int getExcelGenerationLAPiGatewayLambdaWarmInstanceCount(){
        Env env = getEnv();
        long defaultVal = 5l;

        if(Env.PROD.equals(env)
        || Env.PRE_PROD.equals(env)
        ){
            defaultVal = 10;
        }

        Long globalVal = dpProvider.getValForDp(RegisteredDp.EXCEL_GENERATION_LAMBDA_API_GATEWAY_WARM_INSTANCE_COUNT, Long.class, defaultVal);
        return (int) globalVal.longValue();
    }

    private static String getClusterType() {
        String clusterType = getenv("CLUSTER_TYPE");
        if (StringUtils.isBlank(clusterType))
            throw new TConfigurationNotFoundException("CLUSTER_TYPE IS NOT SET");
        return clusterType;
    }

    public static Env getEnv(){
        String clusterType = getClusterType();
        for (Env value : Env.values()) {
            if(clusterType.startsWith(value.getClusterPrefix())){
                return value;
            }
        }
        log.error("cluster type not registered in code : {} ", clusterType);
        return null;
    }
}
