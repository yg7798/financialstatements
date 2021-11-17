package com.tekion.accounting.fs.service.common.slackAlert.enums;

/** Below enum is used to mention the module name used for slack alerting **/
public enum SlackAlertModuleName {
    ACCOUNTING_MIGRATION_SLACK_ALERT_CHANNEL("accounting-migration-alerts","Alerts for accountingMigration alerts"),
    ACCOUNTING_AP_AR_SLACK_ALERT_CHANNEL("accounting-ap-ar-alerts","Alerts for AP GlAccount and AR GlAccount balance mismatch"),
    ACCOUNTING_DATA_ALERT_CHANNEL("accounting-data-alerts", "Alerts for Trial Balance mismatch"),
    FS_SLACK_ALERT_CHANNEL("financial-statements-alerts", "Alerts for FS submission");;

    private final String channelName;
    private final String description;
    SlackAlertModuleName(String channelName, String description) {
        this.channelName = channelName;
        this.description = description;
    }
}
