package com.tekion.accounting.fs.dealerMigration.decisionTaking;

import com.tekion.migration.v3.taskexecutor.MigrationContext;

import java.util.Map;

public interface MigrationRuleEngine {
    /**
     * method only accounts for true flags in migrationSpecificTask and basically returns true if any of flags are TRUE (ONLY VALUE READ).
     * Prefers flags over dmsSource for decision making
     * has to be improved later if complex flag become part of the process
     * @param migrationLogicSpecificTask
     * @param context
     * @return
     */
    boolean performFlagAndDmsSpecificTask(MigrationLogicSpecificTask migrationLogicSpecificTask, MigrationContext context);

    /**
     * method only accounts for true flags in migrationSpecificTask and basically returns true if any of flags are TRUE (ONLY VALUE READ).
     * Prefers flags over dmsSource for decision making
     * has to be improved later if complex flag become part of the process
     * @param migrationLogicSpecificTask
     * @param taskId
     * @return
     */
    boolean performFlagAndDmsSpecificTask(MigrationLogicSpecificTask migrationLogicSpecificTask, String taskId);

    /**
     * method only accounts for true flags in migrationSpecificTask and basically returns true if any of flags are TRUE (ONLY VALUE READ).
     * Prefers flags over dmsSource for decision making
     * has to be improved later if complex flag become part of the process
     *
     *
     * @return
     */

    boolean performFlagAndDmsSpecificTask(MigrationLogicSpecificTask allowClearAccGlAccounts, String tenantId, String dealerId, Map<String, String> additionalInfo);
}
