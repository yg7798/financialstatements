package com.tekion.accounting.fs.dealerMigration.decisionTaking;

import com.tekion.core.utils.TCollectionUtils;
import com.tekion.migration.v3.task.MigrationSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

import static com.tekion.migration.v3.task.MigrationSource.*;
import static java.util.Arrays.asList;

@Getter
@AllArgsConstructor
public enum MigrationLogicSpecificTask {




    //Pre Migration Athena validation
    VALIDATE_ACCOUNT_SUBTYPE_ON_ATHENA(exceptFollowingDms(asList(DEALERTRACK)),null),
    VALIDATE_TRANSACTIONS_AMOUNT_ON_ATHENA(exceptFollowingDms(asList(DOMINION)),null),
    ALLOW_CLEARING_FS_OEM_MAPPING(null,asList("ACCT_CLEAR_OEM_FS_MAPPING")),
    ;

    private List<MigrationSource> migrationSourceList;
    private List<String> flagsToConsiderInOrder;



    private static List<MigrationSource> exceptFollowingDms(List<MigrationSource> migrationSourcesToExclude){
        List<MigrationSource> toReturn = Lists.newArrayList();
        List<MigrationSource> nullSafeListToExclude = TCollectionUtils.nullSafeList(migrationSourcesToExclude);
        for (MigrationSource source : asList(MigrationSource.values())) {
            if(nullSafeListToExclude.contains(source)){
                continue;
            }
            toReturn.add(source);
        }
        return toReturn;
    }
}
