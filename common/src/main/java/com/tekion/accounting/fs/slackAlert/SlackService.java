package com.tekion.accounting.fs.slackAlert;

import com.google.common.collect.Lists;
import com.tekion.accounting.fs.slackAlert.bean.FsSlackMessageDto;
import com.tekion.accounting.fs.slackAlert.bean.SlackConfig;
import com.tekion.accounting.fs.slackAlert.enums.SlackAlertModuleName;
import com.tekion.accounting.fs.slackAlert.helper.SlackMessageHelper;
import com.tekion.accounting.fs.utils.JsonUtil;
import com.tekion.clients.slack.SlackClient;
import com.tekion.clients.slack.beans.Field;
import com.tekion.clients.slack.beans.SlackMessageRequest;
import com.tekion.core.utils.TCollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.tekion.accounting.fs.slackAlert.helper.SlackMessageHelper.tagChannel;
import static com.tekion.accounting.fs.slackAlert.utils.SlackUtil.createFieldForSlackMessage;
import static com.tekion.accounting.fs.slackAlert.utils.SlackUtil.createMessageForTagging;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackService {

    private final SlackConfig slackConfig;

    //    TODO: move business specific logic out of this service
//    public void sendAlertForArGlBalanceMismatch(String misMatchDetails, long arGlAccountBalance, long arAgingBalance) {
//        SlackMessageRequest slackMessageRequest = buildMessageForAPAndARGlBalanceMismatch(misMatchDetails, arGlAccountBalance, arAgingBalance);
//        List<String> peopleToBeTagged = Lists.newArrayList();
//        sendSlackMessage(Collections.singletonList(SlackAlertModuleName.ACCOUNTING_AP_AR_SLACK_ALERT_CHANNEL), slackMessageRequest, peopleToBeTagged);
//    }


//    public void sendAlertForMigration(MigrationSlackMessageDto slackMessageDto) {
//        SlackMessageRequest slackMessageRequest = SlackMessageHelper.buildMessageForMigrationCompletion(slackMessageDto);
//        log.info("SLACK MESSAGE FOR SESSION ID  : {} : prepared slack message request : {} and messageDto : {} ",slackMessageDto.getSessionId(), JsonUtil.toJson(slackMessageRequest),JsonUtil.toJson(slackMessageDto));
//        List<String> peopleToBeTagged = Lists.newArrayList();
//         sendSlackMessage(Collections.singletonList(SlackAlertModuleName.ACCOUNTING_MIGRATION_SLACK_ALERT_CHANNEL), slackMessageRequest, peopleToBeTagged);
//    }

//    //    TODO: move business specific logic out of this service
//    public void sendAllDealersAlertForAgingGlBalanceMismatch(List<AgingReportBalanceMismatchAlertDto> agingReportBalanceMismatchAlertDtoList, SlackAlertModuleName slackAlertModuleName) {
//        SlackMessageRequest slackMessageRequest = buildMessageForAgingBalanceMismatchForAllDealers(agingReportBalanceMismatchAlertDtoList);
//        List<String> peopleToBeTagged = Lists.newArrayList();
//        sendSlackMessage(Collections.singletonList(slackAlertModuleName), slackMessageRequest, peopleToBeTagged);
//    }

//    //    TODO: move business specific logic out of this service
//    public void sendSlackAlertForControlMismatch(MisMatchInfoDto<ControlBookAmountMismatchAlertDto> dtoSlackAlertDataDto) {
//        SlackMessageRequest slackMessageRequest = buildMessageForControlMisMatchForAllDealers((DealerLevelInfoForScheduleSlackAlert) dtoSlackAlertDataDto.getExtraInfo(),dtoSlackAlertDataDto.getMediaId());
//        List<String> peopleToBeTagged = Lists.newArrayList();
//        sendSlackMessage(Collections.singletonList(SlackAlertModuleName.ACCOUNTING_DATA_ALERT_CHANNEL), slackMessageRequest, peopleToBeTagged);
//    }

//    public void sendSlackAlertForSyncPause(MisMatchInfoDto<SyncStatusPauseAlertDto> dtoSlackAlertDataDto){
//        SlackMessageRequest slackMessageRequest = buildMessageForSyncStatusPause((List<SyncStatusPauseAlertDto>) dtoSlackAlertDataDto.getRowToPrint(),dtoSlackAlertDataDto.getMediaId());
//        List<String> peopleToBeTagged = Lists.newArrayList();
//        sendSlackMessage(Collections.singletonList(SlackAlertModuleName.ACCOUNTING_DATA_ALERT_CHANNEL), slackMessageRequest, peopleToBeTagged);
//    }

//    public void sendSlackAlertForRepoMismatch(MisMatchInfoDto<RepoCountMismatchAlertDto> dtoSlackAlertDataDto){
//        SlackMessageRequest slackMessageRequest = buildMessageForRepoMismatchForAllDealers((List<RepoCountMismatchAlertDto>) dtoSlackAlertDataDto.getRowToPrint(),dtoSlackAlertDataDto.getMediaId());
//        List<String> peopleToBeTagged = Lists.newArrayList();
//        sendSlackMessage(Collections.singletonList(SlackAlertModuleName.ACCOUNTING_DATA_ALERT_CHANNEL), slackMessageRequest, peopleToBeTagged);
//    }
//
//    public void sendSlackAlertForMonthCloseMismatch(MisMatchInfoDto<MonthCloseMismatchAlertDto> dtoSlackAlertDataDto){
//        SlackMessageRequest slackMessageRequest = buildMessageForMonthCloseMismatch(dtoSlackAlertDataDto.getRowToPrint(), dtoSlackAlertDataDto.getMediaId());
//        List<String> peopleToBeTagged = Lists.newArrayList();
//        sendSlackMessage(Collections.singletonList(SlackAlertModuleName.ACCOUNTING_DATA_ALERT_CHANNEL), slackMessageRequest, peopleToBeTagged);
//    }
//
//    public void sendSlackAlertForARAcctConfig(MisMatchInfoDto<ARAccountConfigAlertDto> dtoSlackAlertDataDto){
//        SlackMessageRequest slackMessageRequest = buildMessageForARAccountFields(dtoSlackAlertDataDto.getRowToPrint(), dtoSlackAlertDataDto.getMediaId());
//        List<String> peopleToBeTagged = Lists.newArrayList();
//        sendSlackMessage(Collections.singletonList(SlackAlertModuleName.ACCOUNTING_DATA_ALERT_CHANNEL), slackMessageRequest, peopleToBeTagged);
//    }

    public void sendSlackMessage(List<SlackAlertModuleName> slackAlertModuleNameList, SlackMessageRequest slackMessageRequest, List<String> peopleToBeTagged) {
        List<Field> messageFields = TCollectionUtils.nullSafeList(slackMessageRequest.getFields());
        Field taggedUserGroup = createUserTagField(peopleToBeTagged);
        messageFields.add(0, taggedUserGroup);
        for(SlackAlertModuleName slackAlertModuleName : slackAlertModuleNameList) {
            SlackClient slackClient = slackConfig.getSlackClientFromModuleName(slackAlertModuleName);
            if(Objects.nonNull(slackClient)) {
                slackClient.send(slackMessageRequest);
            }
        }
    }

    private Field createUserTagField(List<String> peopleToBeTagged) {
        Field taggedField = createFieldForSlackMessage("", createMessageForTagging(peopleToBeTagged) + tagChannel, false);
        return taggedField;
    }

//    //    TODO: move business specific logic out of this service
//    public void sendMonthlyTrialBalanceMismatchAlert(List<BalanceMismatchSlackDataDto> dealerWiseDataList , int numAccounts, SlackAlertModuleName moduleName) {
//        SlackMessageRequest slackMessageRequest = buildMessageForTrialBalanceMismatch(dealerWiseDataList, numAccounts);
//        log.info("trialBalanceSync: slackMessageRequest {}", slackMessageRequest.toString());
//        List<String> peopleTobeTagged = Lists.newArrayList();
//        sendSlackMessage(Collections.singletonList(moduleName), slackMessageRequest, peopleTobeTagged);
//        log.info("trialBalanceSync: sent slack message");
//    }

//    public void sendFailedRecurringTemplatesAlert(RecurringTemplateAlertDto recurringTemplateAlertDto, SlackAlertModuleName moduleName) {
//        SlackMessageRequest slackMessageRequest = buildMessageForRecurringTemplateAlert(recurringTemplateAlertDto);
//        log.info("RecurringTemplatesAlert: slackMessageRequest {}", slackMessageRequest.toString());
//        List<String> peopleTobeTagged = Lists.newArrayList();
//        sendSlackMessage(Collections.singletonList(moduleName), slackMessageRequest, peopleTobeTagged);
//        log.info("RecurringTemplatesAlert: sent slack message");
//    }

    public void sendAlertForFSSubmission(FsSlackMessageDto slackMessageDto) {
        SlackMessageRequest slackMessageRequest = SlackMessageHelper.buildMessageForFinancialStatementCompletion(slackMessageDto);
        log.info("prepared slack message request : {} and messageDto : {} ", JsonUtil.toJson(slackMessageRequest),JsonUtil.toJson(slackMessageDto));
        List<String> peopleToBeTagged = Lists.newArrayList();
        sendSlackMessage(Collections.singletonList(SlackAlertModuleName.FS_SLACK_ALERT_CHANNEL), slackMessageRequest, peopleToBeTagged);
        log.info("FS Submission Alert: sent slack message");
    }
}
