package com.tekion.accounting.fs.dealerMigration.helpers;

import com.google.common.collect.Maps;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.dealerMigration.helpers.PreMigrationDbSetupService;
import com.tekion.accounting.fs.repos.OemFSMappingRepo;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.migration.v3.taskexecutor.MigrationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
@Slf4j
public class PreMigrationDbSetupServiceImpl implements PreMigrationDbSetupService {

	private final OemFSMappingRepo oemFSMappingRepo;

	public void clearOemFsMapping(String dealerId, MigrationContext context) {
		try {
			oemFSMappingRepo.hardDeleteMigratedOemFsMapping(dealerId);
		}
		catch (Exception e){
			log.error("error occured ",e);
			if(Objects.nonNull(context)){
				context.saveBatchProcessingError("","error occured while deleting");

			}
		}
	}

}
