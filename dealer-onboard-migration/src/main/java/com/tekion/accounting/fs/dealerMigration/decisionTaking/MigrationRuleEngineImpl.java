package com.tekion.accounting.fs.dealerMigration.decisionTaking;


import com.tekion.core.utils.TCollectionUtils;
import com.tekion.migration.v3.dealerdata.DealerMigrationData;
import com.tekion.migration.v3.dealerdata.DealerMigrationDataRepository;
import com.tekion.migration.v3.metadata.beans.MigrationSession;
import com.tekion.migration.v3.session.repo.MigrationSessionRepo;
import com.tekion.migration.v3.task.MigrationSource;
import com.tekion.migration.v3.task.MigrationTask;
import com.tekion.migration.v3.task.MigrationTaskRepoV3;
import com.tekion.migration.v3.taskexecutor.MigrationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class MigrationRuleEngineImpl implements MigrationRuleEngine {
    private final MigrationTaskRepoV3 taskRepo;
    private final DealerMigrationDataRepository dealerMigrationDataRepository;
    private final MigrationSessionRepo migrationSessionRepo;

    @Override
    public boolean performFlagAndDmsSpecificTask(MigrationLogicSpecificTask migrationLogicSpecificTask, MigrationContext context) {
        return performFlagAndDmsSpecificTask(migrationLogicSpecificTask,context.getTask().getTenantId(),context.getTask().getDealerId(),context.getSessionAdditionalInfo());

    }

    @Override
    public boolean performFlagAndDmsSpecificTask(MigrationLogicSpecificTask migrationLogicSpecificTask, String taskId) {
        MigrationTask migrationTask = taskRepo.findById(taskId);
        MigrationSession session = migrationSessionRepo.getSessionById(migrationTask.getSessionId());

        return performFlagAndDmsSpecificTask(migrationLogicSpecificTask,migrationTask.getTenantId(),migrationTask.getDealerId(),session.getAdditionalInfo());
    }

    @Override
    public boolean performFlagAndDmsSpecificTask(MigrationLogicSpecificTask migrationLogicSpecificTask, String tenantId, String dealerId, Map<String, String> additionalInfo) {
        Optional<DealerMigrationData> dealerMigrationDataOptional = this.dealerMigrationDataRepository.findByDealerAndTenantId(tenantId,dealerId);
        DealerMigrationData dealerMigrationData = null;

        if (dealerMigrationDataOptional.isPresent()) {
            dealerMigrationData = dealerMigrationDataOptional.get();
        }

        String migrationSourceString = dealerMigrationData.getMigrationSource();
        MigrationSource migrationSource = MigrationSource.valueOf(migrationSourceString);

        boolean performTaskBasedOnDMS = false;
        if (TCollectionUtils.nullSafeList(migrationLogicSpecificTask.getMigrationSourceList()).contains(migrationSource)) {
            performTaskBasedOnDMS = true;
        }


        boolean performTaskBasedOnSessionFlag = performTaskBasedOnSessionFlag
                (additionalInfo
                        , migrationLogicSpecificTask.getFlagsToConsiderInOrder());

        return (performTaskBasedOnSessionFlag || performTaskBasedOnDMS);
    }

    private boolean performTaskBasedOnSessionFlag(Map<String, String> sessionInfoMap, List<String> flagsToConsiderInOrder) {
        boolean performTaskBasedOnSessionFlag = false;
        sessionInfoMap = TCollectionUtils.nullSafeMap(sessionInfoMap);

        for (String currSessionInfoMapKey : TCollectionUtils.nullSafeList(flagsToConsiderInOrder)) {
            try {
                if (performTaskBasedOnSessionFlag) {
                    break;
                }
                String flagBooleanValue = sessionInfoMap.get(currSessionInfoMapKey);
                Boolean currBoolVal = Boolean.valueOf(flagBooleanValue);
                performTaskBasedOnSessionFlag = currBoolVal;

            } catch (Exception e) {

            }
        }
        return performTaskBasedOnSessionFlag;
    }
}
