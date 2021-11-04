package com.tekion.accounting.fs.slackAlert.utils;

import com.tekion.accounting.fs.GlobalService;
import com.tekion.accounting.fs.beans.MonthInfoDto;
import com.tekion.accounting.fs.utils.DealerConfig;
import com.tekion.accounting.fs.utils.UserContextUtils;
import com.tekion.clients.slack.beans.Field;
import com.tekion.clients.slack.beans.SlackMessageRequest;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.podclient.PodConfigService;
import com.tekion.podclient.dto.TenantPodMappingResponse;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@UtilityClass
@Slf4j
public class SlackUtil {

    private static PodConfigService podConfigService;
    private static GlobalService globalService;
    private static DealerConfig dealerConfig;

    public static void setRequiredService(PodConfigService podConfigService, GlobalService globalService, DealerConfig dealerConfig) {
        SlackUtil.podConfigService = podConfigService;
        SlackUtil.globalService = globalService;
        SlackUtil.dealerConfig = dealerConfig;
    }

    public static Field createFieldForSlackMessage(String title, String value, boolean cool) {
        Field field = new Field();
        field.setTitle(title);
        field.setValue(value);
        field.setCool(cool);
        return field;
    }

    public static String createMessageForTagging(List<String> memberIds) {
        StringBuilder taggedString = new StringBuilder();
        for(String memberId : memberIds) {
            taggedString.append("<@").append(memberId).append("> ");
        }
        return taggedString.toString();
    }

    public static void addRegularFields(SlackMessageRequest slackMessageRequest) {
        slackMessageRequest.getFields().add(createEnvironmentField());
        slackMessageRequest.getFields().add(createUUIDField());
        SlackUtil.addPodIdField(slackMessageRequest, UserContextProvider.getCurrentTenantId(),UserContextProvider.getCurrentDealerId());
    }

    public static void addPodIdField(SlackMessageRequest slackMessageRequest,String tenantId,String dealerId){
        log.info("slack dealerId {} tenantId {}", tenantId, dealerId);
        try {
            TenantPodMappingResponse podIdByTenantAndDealerId = podConfigService.getPodIdByTenantAndDealerId(tenantId, dealerId);
            log.info("pod ids : {}", podIdByTenantAndDealerId);
            Field environmentField = createFieldForSlackMessage("PodId", podIdByTenantAndDealerId.getPodId(), true);
            slackMessageRequest.getFields().add(environmentField);
        } catch (Exception e) {
            log.info("pod id not found {}",e);
            Field environmentField = createFieldForSlackMessage("PodId","podId not found", true);
            slackMessageRequest.getFields().add(environmentField);
        }

    }
    public static Field createEnvironmentField() {
        Field environmentField = createFieldForSlackMessage("Environment", System.getenv("CLUSTER_TYPE"), true);
        return environmentField;
    }

    //added for debugging the whole flow where slack message was initiated
    public static Field createUUIDField() {
        String uuid = UUID.randomUUID().toString();
        log.info("Slack message " + uuid);
        Field uuidField = createFieldForSlackMessage("UUID", uuid, true);
        return uuidField;
    }

    public static List<UserContext> userContextMapForDealers(MonthInfoDto dealers){
        return UserContextUtils.createUserContextForPayload(dealers,globalService);
    }

    public static Field createTraceIdField() {
        Field traceIdField = createFieldForSlackMessage("TraceId", UserContextProvider.getTraceId(), true);
        return traceIdField;
    }

    public static Field createDealerNameField() {
        Field dealerNameField = createFieldForSlackMessage("DealerName", dealerConfig.getDealerName(), true);
        return dealerNameField;
    }
}
