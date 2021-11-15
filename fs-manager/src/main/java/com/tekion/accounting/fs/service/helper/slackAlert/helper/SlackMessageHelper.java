package com.tekion.accounting.fs.service.helper.slackAlert.helper;

import com.google.common.collect.Lists;
import com.tekion.accounting.fs.service.helper.slackAlert.FsSlackMessageDto;
import com.tekion.accounting.fs.common.utils.TimeUtils;
import com.tekion.clients.slack.beans.Field;
import com.tekion.clients.slack.beans.SlackMessageRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.tekion.accounting.fs.service.helper.slackAlert.utils.SlackUtil.*;

@UtilityClass
@Slf4j
public class SlackMessageHelper {

    public static final String dealerIdText = "DEALER ID";
    public static final String TENANT_ID_TEXT = "TENANT ID";
    public static final String red = "#D00000";
    private final String blue = "#0000ff";
    public static final String tagChannel = "<!channel>";
    public static final String tagHere = "<!here>";
    public static final String tagDevops = "<!subteam^S014V396CRX|devops>";

//    public static SlackMessageRequest buildMessageForAPAndARGlBalanceMismatch(String misMatchDetails, long arGlAccountBalance, long arAgingBalance) {
//        String dealerId = UserContextProvider.getCurrentDealerId();
//        long mismatchAmount = arGlAccountBalance - arAgingBalance;
//        SlackMessageRequest slackMessageRequest = new SlackMessageRequest();
//        List<Field> slackMessageFields = Lists.newArrayList();
//        slackMessageRequest.setFields(slackMessageFields);
//        addRegularFields(slackMessageRequest);
//        Field dealerIDField = createFieldForSlackMessage(dealerIdText, dealerId, true);
//        Field amountDiffField = createFieldForSlackMessage("Mis-Match Amount", String.valueOf(mismatchAmount/100.0), true);
//        Field arGlAccountBalanceField = createFieldForSlackMessage("Ar-GlAccount Balance", String.valueOf(arGlAccountBalance/100.0), true);
//        Field arAgingBalanceField = createFieldForSlackMessage("Ar-Aging Balance", String.valueOf(arAgingBalance/100.0), true);
//
//        Field misMatchResponseField = createFieldForSlackMessage("MisMatchApi response", misMatchDetails, false);
//
//        slackMessageFields.add(dealerIDField);
//        slackMessageFields.add(amountDiffField);
//        slackMessageFields.add(arGlAccountBalanceField);
//        slackMessageFields.add(arAgingBalanceField);
//        slackMessageFields.add(misMatchResponseField);
//
//        slackMessageRequest.setColor(red);
//        return slackMessageRequest;
//    }

//    public static SlackMessageRequest buildMessageForAgingBalanceMismatchForAllDealers(List<AgingReportBalanceMismatchAlertDto> agingBalanceMismatchAlertDtoList) {
//        SlackMessageRequest slackMessageRequest = new SlackMessageRequest();
//        List<Field> slackMessageFields = Lists.newArrayList();
//        slackMessageRequest.setFields(slackMessageFields);
//        slackMessageRequest.getFields().add(createFieldForSlackMessage("AgingReportType", agingBalanceMismatchAlertDtoList.get(0).getType().name(),false));
//        addRegularFields(slackMessageRequest);
//        Field misMatchResponseField = null;
//        String mismatchAmountTitle = "Amount mismatch per dealer";
//        if(TCollectionUtils.isNotEmpty(agingBalanceMismatchAlertDtoList)) {
//            misMatchResponseField = createFieldForSlackMessage(mismatchAmountTitle, buildMessageForArGlBalanceAlertsAllDealers(agingBalanceMismatchAlertDtoList), false);
//        } else {
//            misMatchResponseField = new Field();
//            misMatchResponseField.setTitle(mismatchAmountTitle);
//            misMatchResponseField.setValue("No mismatch found!");
//            misMatchResponseField.setCool(false);
//        }
//        slackMessageFields.add(misMatchResponseField);
//
//        if(AgingReportType.AP.name().equalsIgnoreCase( agingBalanceMismatchAlertDtoList.get(0).getType().name())){
//            slackMessageRequest.setColor(blue);
//        }else {
//            slackMessageRequest.setColor(red);
//        }
//        return slackMessageRequest;
//    }

//    public static SlackMessageRequest buildOutboundUnProcessedSlackRequest(Map<String, List<Outbound>> outboundsPerDealer, String tenantId){
//        SlackMessageRequest slackMessageRequest = new SlackMessageRequest();
//        List<Field> slackMessageFields = Lists.newArrayList();
//        slackMessageRequest.setFields(slackMessageFields);
//        slackMessageRequest.getFields().add(createFieldForSlackMessage("Failed Outbound Messages Alert", "",false));
//        addRegularFields(slackMessageRequest);
//        slackMessageRequest.getFields().add(createFieldForSlackMessage("Tenant", tenantId, false));
//        slackMessageRequest.getFields().add(createFieldForSlackMessage("Dealer and Count", buildOutboundDealerStatusCountTable(outboundsPerDealer),false));
//
//        return slackMessageRequest;
//    }

//    private static String buildOutboundDealerStatusCountTable(Map<String, List<Outbound>> outboundsPerDealer) {
//        List<String> headers = Arrays.asList("Dealer","Count");
//        List<List<String>> rows = Lists.newArrayList();
//        StringBuilder outboundStatusCounts = new StringBuilder("```");
//        rows.add(headers);
//        for (Map.Entry<String, List<Outbound>> dealerEntry : outboundsPerDealer.entrySet()) {
//                rows.add(Arrays.asList(dealerEntry.getKey(), Integer.toString(dealerEntry.getValue().size())));
//        }
//        outboundStatusCounts.append(formatAsTable(rows, 5));
//        return outboundStatusCounts.append("```").toString();
//    }


//    public static SlackMessageRequest buildMessageForControlMisMatchForAllDealers(DealerLevelInfoForScheduleSlackAlert controlBookMismatchDealerLevelInfoDtos, String mediaUrl ) {
//        SlackMessageRequest slackMessageRequest = new SlackMessageRequest();
//        List<Field> slackMessageFields = Lists.newArrayList();
//        slackMessageRequest.setFields(slackMessageFields);
//        Set<String> dealerInWhichMismatchFound = Sets.newHashSet();
//        Set<String> dealersHavingNewlyCreatedSchedule = Sets.newHashSet();
//        Set<String> dealersHavingRecentlyModifiedSchedule = Sets.newHashSet();
//        Set<String> dealersHavingMismatchInPostAheadMonth = Sets.newHashSet();
//        Set<String> dealersHavingMismatchInActiveMonth = Sets.newHashSet();
//
//        int newlyCreatedSchedule = 0;
//        int recentlyModifiedSchedule = 0;
//        for (ControlBookMismatchDealerLevelInfoDto controlBookMismatchDealerLevelInfoDto : controlBookMismatchDealerLevelInfoDtos.getDealerLevelInfoDtos()) {
//            dealerInWhichMismatchFound.add(controlBookMismatchDealerLevelInfoDto.getDealerId());
//            if(controlBookMismatchDealerLevelInfoDto.getNumberOfScheduleCreateWithIn24Hour()!=0){
//                dealersHavingNewlyCreatedSchedule.add(controlBookMismatchDealerLevelInfoDto.getDealerId());
//            }
//            if(controlBookMismatchDealerLevelInfoDto.getNumberOfScheduleModifiedRecently()!=0){
//                dealersHavingRecentlyModifiedSchedule.add(controlBookMismatchDealerLevelInfoDto.getDealerId());
//            }
//            if(controlBookMismatchDealerLevelInfoDto.getNumberOfScheduleMismatchInPostAheadMonth()!=0){
//                dealersHavingMismatchInPostAheadMonth.add(controlBookMismatchDealerLevelInfoDto.getDealerId());
//            }
//            if(controlBookMismatchDealerLevelInfoDto.getNumberOfScheduleMismatchInActiveMonth()!=0 ){
//                dealersHavingMismatchInActiveMonth.add(controlBookMismatchDealerLevelInfoDto.getDealerId());
//            }
//            newlyCreatedSchedule = newlyCreatedSchedule+ controlBookMismatchDealerLevelInfoDto.getNumberOfScheduleCreateWithIn24Hour();
//            recentlyModifiedSchedule = recentlyModifiedSchedule + controlBookMismatchDealerLevelInfoDto.getNumberOfScheduleModifiedRecently();
//
//        }
//
//        Field titleField = createFieldForSlackMessage(
//                "Schedule Vs trialBalance Mismatch Detail ",
//                "Found mismatch in " + controlBookMismatchDealerLevelInfoDtos.getUniquesScheduleIds().size() + " schedules(s) for " + dealerInWhichMismatchFound.size() + "dealer",
//                false
//        );
//        Field titleFieldForActiveMonth = createFieldForSlackMessage(
//                " ",
//                "Found mismatch in " + controlBookMismatchDealerLevelInfoDtos.getUniqueScheduleIdsActiveMonth().size() + " active month schedules(s) for "+ dealersHavingMismatchInActiveMonth.size() + "dealer",
//                false
//        );
//        Field titleFieldForPostAheadMonth = createFieldForSlackMessage(
//                " ",
//                "Found mismatch in " + controlBookMismatchDealerLevelInfoDtos.getUniqueScheduleIdsPostAhead().size() + " postAhead month schedules(s) for "+ dealersHavingMismatchInPostAheadMonth.size() + "dealer",
//                false
//        );
//        Field titleSubField = createFieldForSlackMessage(
//                "",
//                "Found "+ newlyCreatedSchedule +" mismatches in newly created schedules in "+ dealersHavingNewlyCreatedSchedule.size() +" dealers",
//                false
//        );
//        Field titleModifiedField = createFieldForSlackMessage(
//                "",
//                "Found "+ recentlyModifiedSchedule +" mismatches in recently modified schedules "+ dealersHavingRecentlyModifiedSchedule.size() +" dealers",
//                false
//        );
//        slackMessageFields.add(titleField);
//        slackMessageFields.add(titleFieldForActiveMonth);
//        slackMessageFields.add(titleFieldForPostAheadMonth);
//        slackMessageFields.add(titleSubField);
//        slackMessageFields.add(titleModifiedField);
//
//        addRegularFields(slackMessageRequest);
//        Field misMatchResponseField = null;
//        String mismatchAmountTitle = "Schedule mismatch per dealer detail";
//        if (TCollectionUtils.isNotEmpty(controlBookMismatchDealerLevelInfoDtos.getDealerLevelInfoDtos())) {
//            misMatchResponseField = createFieldForSlackMessage(mismatchAmountTitle, buildControlMismatchDataTable(controlBookMismatchDealerLevelInfoDtos.getDealerLevelInfoDtos().stream().limit(10).collect(Collectors.toList())), false);
//        } else {
//            misMatchResponseField = new Field();
//            misMatchResponseField.setTitle(mismatchAmountTitle);
//            misMatchResponseField.setValue("No mismatch found!");
//            misMatchResponseField.setCool(false);
//        }
//        slackMessageFields.add(misMatchResponseField);
//        slackMessageRequest.setColor(blue);
//        slackMessageFields.add(createFieldForSlackMessage("Schedule mismatch Media Url : ",TStringUtils.nullSafeString(mediaUrl),false));
//
//
//        return slackMessageRequest;
//    }

//    public static SlackMessageRequest buildMessageForSyncStatusPause(List<SyncStatusPauseAlertDto> syncStatusPauseAlertDtos, String mediaUrl){
//        SlackMessageRequest slackMessageRequest = new SlackMessageRequest();
//        List<Field> slackMessageFields = Lists.newArrayList();
//        slackMessageRequest.setFields(slackMessageFields);
//        Field titleField = createFieldForSlackMessage(
//                "Dealer Sync Pause Alerts",
//                "Found " + syncStatusPauseAlertDtos.size() + " Dealer sync pauses"
//                ,
//                false
//        );
//        slackMessageFields.add(titleField);
//        addRegularFields(slackMessageRequest);
//        Field misMatchResponseField=null;
//        String mismatchAmountTitle = "Dealer Sync Status Alert";
//
//        if (TCollectionUtils.isNotEmpty(syncStatusPauseAlertDtos)) {
//            misMatchResponseField = createFieldForSlackMessage(mismatchAmountTitle, buildSyncStatusDataTable(syncStatusPauseAlertDtos), false);
//        }
//        else{
//            misMatchResponseField = new Field();
//            misMatchResponseField.setTitle(mismatchAmountTitle);
//            misMatchResponseField.setValue("No mismatch found!");
//            misMatchResponseField.setCool(false);
//        }
//
//        slackMessageFields.add(misMatchResponseField);
//        slackMessageRequest.setColor(blue);
//        slackMessageFields.add(createFieldForSlackMessage("Sync Status Media Url : ",TStringUtils.nullSafeString(mediaUrl),false));
//        return slackMessageRequest;
//    }

//    public static SlackMessageRequest buildMessageForRepoMismatchForAllDealers(List<RepoCountMismatchAlertDto> repoCountMismatchAlertDtos, String mediaUrl){
//        SlackMessageRequest slackMessageRequest = new SlackMessageRequest();
//        List<Field> slackMessageFields = Lists.newArrayList();
//        slackMessageRequest.setFields(slackMessageFields);
//        Set<String> dealerInWhichMismatchFound = Sets.newHashSet();
//        int totalMismatch = repoCountMismatchAlertDtos.size();
//        Field titleField = createFieldForSlackMessage(
//                "Repository Vs ES Count Mismatch Detail",
//                "Found " + totalMismatch + " number of repository mismatches"
//                ,
//                false
//        );
//        slackMessageFields.add(titleField);
//        Field misMatchResponseField=null;
//        String mismatchAmountTitle = "Repository vs ES Count tracker";
//
//        if (TCollectionUtils.isNotEmpty(repoCountMismatchAlertDtos)) {
//            misMatchResponseField = createFieldForSlackMessage(mismatchAmountTitle, buildCountMismatchDataTable(repoCountMismatchAlertDtos), false);
//        }
//        else{
//            misMatchResponseField = new Field();
//            misMatchResponseField.setTitle(mismatchAmountTitle);
//            misMatchResponseField.setValue("No mismatch found!");
//            misMatchResponseField.setCool(false);
//        }
//
//        slackMessageFields.add(misMatchResponseField);
//        slackMessageRequest.setColor(blue);
//        slackMessageFields.add(createFieldForSlackMessage("Count mismatch Media Url : ",TStringUtils.nullSafeString(mediaUrl),false));
//        return slackMessageRequest;
//    }

//    public static SlackMessageRequest buildMessageForMonthCloseMismatch(List<MonthCloseMismatchAlertDto> monthCloseMismatchAlertDtos, String mediaUrl ){
//        SlackMessageRequest slackMessageRequest = new SlackMessageRequest();
//        List<Field> slackMessageFields = Lists.newArrayList();
//        slackMessageRequest.setFields(slackMessageFields);
//        int totalMismatch = monthCloseMismatchAlertDtos.size();
//        Field titleField = createFieldForSlackMessage(
//                "MonthClose vs. AccountSync vs. CustomerStatment",
//                "Found " + totalMismatch + " month sync mismatches"
//                ,
//                false
//        );
//        slackMessageFields.add(titleField);
//        addRegularFields(slackMessageRequest);
//        Field misMatchResponseField=null;
//        String mismatchAmountTitle = "MonthClose sync mismatch Tracker";
//        if (TCollectionUtils.isNotEmpty(monthCloseMismatchAlertDtos)) {
//            misMatchResponseField = createFieldForSlackMessage(mismatchAmountTitle, buildMonthSyncMismatchDataTable(monthCloseMismatchAlertDtos), false);
//        }
//        else{
//            misMatchResponseField = new Field();
//            misMatchResponseField.setTitle(mismatchAmountTitle);
//            misMatchResponseField.setValue("No mismatch found!");
//            misMatchResponseField.setCool(false);
//        }
//        slackMessageFields.add(misMatchResponseField);
//        slackMessageRequest.setColor(blue);
//        slackMessageFields.add(createFieldForSlackMessage("Month mismatch Media Url : ",TStringUtils.nullSafeString(mediaUrl),false));
//        return slackMessageRequest;
//    }

//    public static SlackMessageRequest buildMessageForARAccountFields(List<ARAccountConfigAlertDto> arAccountAlertDtos, String mediaUrl){
//        SlackMessageRequest slackMessageRequest = new SlackMessageRequest();
//        List<Field> slackMessageFields = Lists.newArrayList();
//        slackMessageRequest.setFields(slackMessageFields);
//        Field titleField = createFieldForSlackMessage(
//                "Ar Account Control Type and flag Alert",
//                "Found " + arAccountAlertDtos.size() + "accounts"
//                ,
//                false
//        );
//        slackMessageFields.add(titleField);
//        addRegularFields(slackMessageRequest);
//        Field misMatchResponseField=null;
//        String mismatchAmountTitle = "ar Account control types per dealer";
//        if(TCollectionUtils.isNotEmpty(arAccountAlertDtos)){
//            misMatchResponseField = createFieldForSlackMessage(mismatchAmountTitle,buildArAcctFieldCheckDataTable(arAccountAlertDtos), false );
//        }else{
//            misMatchResponseField = new Field();
//            misMatchResponseField.setTitle(mismatchAmountTitle);
//            misMatchResponseField.setValue("No mismatch found!");
//            misMatchResponseField.setCool(false);
//        }
//        slackMessageFields.add(misMatchResponseField);
//        slackMessageRequest.setColor(blue);
//        return slackMessageRequest;
//    }

//    public static SlackMessageRequest buildMessageForTrialBalanceMismatch(List<BalanceMismatchSlackDataDto> dealerWiseDataList, int numAccounts) {
//        SlackMessageRequest slackMessageRequest = new SlackMessageRequest();
//        List<Field> slackMessageFields = Lists.newArrayList();
//        slackMessageRequest.setFields(slackMessageFields);
//        Field titleField = createFieldForSlackMessage(
//                "FinancialReport (Active Month) vs InquiryTool Mismatch Details",
//                "Found mismatch in " +numAccounts + " account(s) for " + dealerWiseDataList.size() + " dealer(s)",
//                false
//        );
//        slackMessageFields.add(titleField);
//        addRegularFields(slackMessageRequest);
//        Field misMatchResponseField = null;
//        String mismatchAmountTitle = "Amount mismatch per dealer";
//        if (TCollectionUtils.isNotEmpty(dealerWiseDataList)) {
//            misMatchResponseField = createFieldForSlackMessage(mismatchAmountTitle, buildTrialMismatchDataTable(dealerWiseDataList), false);
//        } else {
//            misMatchResponseField = new Field();
//            misMatchResponseField.setTitle(mismatchAmountTitle);
//            misMatchResponseField.setValue("No mismatch found!");
//            misMatchResponseField.setCool(false);
//        }
//        slackMessageFields.add(misMatchResponseField);
//        slackMessageRequest.setColor(blue);
//        return slackMessageRequest;
//    }

//    public static SlackMessageRequest buildMessageForRecurringTemplateAlert(RecurringTemplateAlertDto recurringTemplateAlertDto) {
//        SlackMessageRequest slackMessageRequest = new SlackMessageRequest();
//        List<Field> slackMessageFields = Lists.newArrayList();
//        slackMessageFields.add(createFieldForSlackMessage("", "Recurring entries failed for below templates : ", false));
//        slackMessageFields.add(createFieldForSlackMessage("TenantId , DealerId " , recurringTemplateAlertDto.getTenantId() +" - "+ recurringTemplateAlertDto.getDealerId(),true));
//        slackMessageFields.add(SlackUtil.createEnvironmentField());
//        Field templateIdsInfo = createFieldForSlackMessage("Template Ids ",buildMessageForTemplateIdsSlackResponse(recurringTemplateAlertDto) , false);
//        slackMessageFields.add(templateIdsInfo);
//        slackMessageRequest.setColor(red);
//        return slackMessageRequest;
//    }
//
//    private String buildMessageForArGlBalanceAlertsAllDealers(List<AgingReportBalanceMismatchAlertDto> agingReportBalanceMismatchAlertDtoList) {
//        List<String> headers = Arrays.asList("TenantId", "DealerId", "MisMatchAmount");
//        List<List<String>> rows = Lists.newArrayList();
//        StringBuilder agingMisMatchResponse = new StringBuilder("```");
//        rows.add(headers);
//        for(AgingReportBalanceMismatchAlertDto agingReportBalanceMismatchAlertDto : agingReportBalanceMismatchAlertDtoList) {
//            rows.add(Arrays.asList(agingReportBalanceMismatchAlertDto.getTenantId(), agingReportBalanceMismatchAlertDto.getDealerId(), String.valueOf(agingReportBalanceMismatchAlertDto.getMisMatchAmount()/100.0)));
//        }
//        agingMisMatchResponse.append(formatAsTable(rows, 5));
//        return agingMisMatchResponse.append("```").toString();
//    }
//    private String buildControlMismatchDataTable(List<ControlBookMismatchDealerLevelInfoDto> controlBookMismatchDealerLevelInfoDtos) {
//        List<String> headers = Arrays.asList("TenantId", "DealerId", "month", "year", "isPostAhead","No.OfScheduleMismatch");
//        List<List<String>> rows = Lists.newArrayList();
//        StringBuilder balanceMismatch = new StringBuilder("```");
//        rows.add(headers);
//        for(ControlBookMismatchDealerLevelInfoDto alertData : controlBookMismatchDealerLevelInfoDtos) {
//            rows.add(Arrays.asList(
//                    alertData.getTenantId(),
//                    alertData.getDealerId(),
//                    alertData.getMonth()+"",
//                    alertData.getYear()+"",
//                    alertData.isPostAhead()+"",
//                    alertData.getNumberOfScheduleMismatch()+""
//            ));
//        }
//        balanceMismatch.append(formatAsTable(rows, 3));
//        return balanceMismatch.append("```").toString();
//    }

//    private String buildSyncStatusDataTable(List<SyncStatusPauseAlertDto> syncStatusPauseAlertDtos){
//        List<String> headers = Arrays.asList("TenantId","DealerId", "SyncPause", "Reason" );
//        List<List<String>> rows = Lists.newArrayList();
//        StringBuilder Mismatch = new StringBuilder("```");
//        rows.add(headers);
//        for(SyncStatusPauseAlertDto dto : syncStatusPauseAlertDtos){
//            rows.add(Arrays.asList(
//                    dto.getTenantId(),
//                    dto.getDealerId(),
//                    dto.getSyncPaused(),
//                    dto.getReason()
//            ));
//        }
//        Mismatch.append(formatAsTable(rows,3));
//        return Mismatch.append("```").toString();
//    }


//    private String buildCountMismatchDataTable(List<RepoCountMismatchAlertDto> countMismatchAlertDtos){
//        List<String> headers = Arrays.asList("DealerId", "TenantId","Table", "Repo", "ES", "Mismatch");
//        List<List<String>> rows = Lists.newArrayList();
//        StringBuilder balanceMismatch = new StringBuilder("```");
//        rows.add(headers);
//        int ctr = 0;
//       for( RepoCountMismatchAlertDto dto : countMismatchAlertDtos){
//           if(ctr >= 20){
//               break;
//           }
//           rows.add(Arrays.asList(
//                   dto.getDealerId(),
//                   dto.getTenantId(),
//                   dto.getTableName(),
//                   dto.getRepoCount(),
//                   dto.getEsCount(),
//                   dto.getMismatchedCount()
//           ));
//           ctr++;
//       }
//        balanceMismatch.append(formatAsTable(rows,3));
//        return balanceMismatch.append("```").toString();
//    }

//    private String buildMonthSyncMismatchDataTable(List<MonthCloseMismatchAlertDto> monthCloseMismatchAlertDtos) {
//        List<String> headers = Arrays.asList("DealerId", "TenantId","MonthClose", "AccountSync", "CustomerMonth");
//        List<List<String>> rows = Lists.newArrayList();
//        StringBuilder balanceMismatch = new StringBuilder("```");
//        rows.add(headers);
//        for (MonthCloseMismatchAlertDto dto : monthCloseMismatchAlertDtos) {
//
//            rows.add(Arrays.asList(
//                    dto.getDealerId(),
//                    dto.getTenantId(),
//                    dto.getMonthClose(),
//                    dto.getAccountSync(),
//                    dto.getCustomerActiveMonthSync()
//            ));
//        }
//        balanceMismatch.append(formatAsTable(rows, 5));
//        return balanceMismatch.append("```").toString();
//    }

//    private String buildArAcctFieldCheckDataTable(List<ARAccountConfigAlertDto> arAccountAlertDtos){
//        List<String> headers = Arrays.asList("DealerId", "TenantId","glAccountNumber", "Control Type", "control Mandatory");
//        List<List<String>> rows = Lists.newArrayList();
//        StringBuilder balanceMismatch = new StringBuilder("```");
//        rows.add(headers);
//
//        for (ARAccountConfigAlertDto dto : arAccountAlertDtos){
//            rows.add(Arrays.asList(
//                    dto.getDealerId(),
//                    dto.getTenantId(),
//                    dto.getGlAccountNumber(),
//                    dto.getControlType(),
//                    Boolean.toString(dto.isControlMandatory())
//            ));
//        }
//        balanceMismatch.append(formatAsTable(rows, 2));
//        return balanceMismatch.append("```").toString();
//    }

//    private String buildTrialMismatchDataTable(List<BalanceMismatchSlackDataDto> dealerWiseDataList) {
//        List<String> headers = Arrays.asList("Tenant Id", "Dealer Id", "Debit Mismatch", "Credit Mismatch", "No. of Accounts");
//        List<List<String>> rows = Lists.newArrayList();
//        StringBuilder balanceMismatch = new StringBuilder("```");
//        rows.add(headers);
//        for(BalanceMismatchSlackDataDto alertData : dealerWiseDataList) {
//            rows.add(Arrays.asList(
//                    alertData.getTenantId(),
//                    alertData.getDealerId(),
//                    new BigDecimal(alertData.getDebitMismatchAmount()).setScale(2, BigDecimal.ROUND_HALF_UP).toString(),
//                    new BigDecimal(alertData.getCreditMismatchAmount()).setScale(2, BigDecimal.ROUND_HALF_UP).toString(),
//                    String.valueOf(alertData.getMismatchAcctNum())
//            ));
//        }
//        balanceMismatch.append(formatAsTable(rows, 5));
//        return balanceMismatch.append("```").toString();
//    }


    public static String formatAsTable(List<List<String>> rows, int spacingBetweenColumn)
    {
        int[] maxLengths = new int[rows.get(0).size()];
        for (List<String> row : rows)
        {
            for (int i = 0; i < row.size(); i++)
            {
                maxLengths[i] = Math.max(maxLengths[i], row.get(i).length());
            }
        }

        StringBuilder formatBuilder = new StringBuilder();
        for (int i = 0; i < maxLengths.length; i++) {
            int maxLength = maxLengths[i];
            if(i==maxLengths.length-1){
                formatBuilder.append("%-").append(maxLength ).append("s");
            }
            else{
                formatBuilder.append("%-").append(maxLength + spacingBetweenColumn).append("s");
            }
        }
        String format = formatBuilder.toString();

        StringBuilder result = new StringBuilder();
        for (List<String> row : rows)
        {
            result.append(String.format(format, row.toArray(new String[0]))).append("\n");
        }
        return result.toString();
    }
//
//    public static SlackMessageRequest buildMessageForMigrationCompletion(MigrationSlackMessageDto slackMessageDto) {
//        SlackMessageRequest slackMessageRequest = new SlackMessageRequest();
//        List<Field> slackMessageFields = Lists.newArrayList();
//        slackMessageRequest.setFields(slackMessageFields);
//        slackMessageFields.add(createFieldForSlackMessage("" , "Migration Pass Completed for The following :",false));
//        slackMessageFields.add(createFieldForSlackMessage("TenantId , DealerId " , slackMessageDto.getDealerMigrationData().getTenantId() +" - "+slackMessageDto.getDealerMigrationData().getDealerId(),true));
//        slackMessageFields.add(SlackUtil.createEnvironmentField());
//        Field misMatchResponseField = createFieldForSlackMessage("MigrationDetails ",buildMessageForSlackResponse(slackMessageDto) , false);
//        slackMessageFields.add(misMatchResponseField);
//        slackMessageFields.add(createFieldForSlackMessage("SessionLink : ",TStringUtils.nullSafeString(slackMessageDto.getSessionUrl()),false));
//
//
//        slackMessageRequest.setColor(red);
//        return slackMessageRequest;
//    }

//    private String buildMessageForSlackResponse(MigrationSlackMessageDto slackMessageDto) {
//        List<String> headers = Arrays.asList("Key" ,"| "+ "Value");
//        List<List<String>> rows = Lists.newArrayList();
//        StringBuilder sb = new StringBuilder("```");
//        rows.add(headers);
//        rows.add(Arrays.asList("DealerId","| "+ TStringUtils.nullSafeString(slackMessageDto.getDealerMigrationData().getDealerId())));
//        rows.add(Arrays.asList("TenantId","| "+ TStringUtils.nullSafeString(slackMessageDto.getDealerMigrationData().getTenantId())));
//        rows.add(Arrays.asList("IsGoLivePass","| "+ Boolean.toString(slackMessageDto.isFinalPass())));
//        rows.add(Arrays.asList("MigrationSource","| "+ TStringUtils.nullSafeString(slackMessageDto.getDealerMigrationData().getMigrationSource())));
//        rows.add(Arrays.asList("DatabaseName","| "+ TStringUtils.nullSafeString(slackMessageDto.getDealerMigrationData().getDbName())));
//        rows.add(Arrays.asList("DealershipCode","| "+ TStringUtils.nullSafeString(slackMessageDto.getDealerMigrationData().getDealershipCode())));
//        rows.add(Arrays.asList("PostValidationReports : MediaId","| "+ TStringUtils.nullSafeString(slackMessageDto.getPostValidationMediaId())));
//        rows.add(Arrays.asList("PreValidationReports : MediaId","| "+ TStringUtils.nullSafeString(slackMessageDto.getPreValidationMediaId())));
//        rows.add(Arrays.asList("SessionId","| "+ TStringUtils.nullSafeString(slackMessageDto.getSessionId())));
//
//        sb.append(formatAsTable(rows, 2));
//        return sb.append("```").toString();
//    }

//    private String buildMessageForTemplateIdsSlackResponse(RecurringTemplateAlertDto recurringTemplateAlertDto) {
//        List<List<String>> rows = Lists.newArrayList();
//        StringBuilder sb = new StringBuilder("```");
//        for (String templateId : recurringTemplateAlertDto.getTemplateIds()) {
//            rows.add(Arrays.asList(templateId));
//        }
//        sb.append(formatAsTable(rows,2));
//        return sb.append("``").toString();
//    }

    public static SlackMessageRequest buildMessageForFinancialStatementCompletion(FsSlackMessageDto slackMessageDto) {
        SlackMessageRequest slackMessageRequest = new SlackMessageRequest();
        List<Field> slackMessageFields = Lists.newArrayList();
        slackMessageRequest.setFields(slackMessageFields);
        Field titleField = createFieldForSlackMessage(
                "Financial Statement Submission Alert",
                "For "+ slackMessageDto.getOemId() +" FS submission "+ slackMessageDto.getStatus() + " at " + TimeUtils.getCurrentDateTimeInReadableFormat(),
                false
        );
        slackMessageFields.add(titleField);
        slackMessageRequest.getFields().add(createFieldForSlackMessage( "TenantId , DealerId", slackMessageDto.getTenantId() +" - "+slackMessageDto.getDealerId(), true));
        slackMessageRequest.getFields().add(createDealerNameField());
        addRegularFields(slackMessageRequest);
        slackMessageRequest.getFields().add(createTraceIdField());
        slackMessageRequest.setColor(blue);
        return slackMessageRequest;
    }
}

