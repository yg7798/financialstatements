package com.tekion.accounting.fs.dealerMigration.context;

import com.google.common.collect.Maps;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.service.fsEntry.FsEntryService;
import com.tekion.migration.v3.taskexecutor.MigrationContext;
import lombok.Data;
import org.apache.commons.compress.utils.Sets;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class OemFsRelateCodesMigrationContext extends MigrationContext {
	private Map<String, String> accountNumberToAccountIdMap;
	private Map<String, String> sourceDmsPclVsTkGroupCodeMap;
	private Long migrationTime;

	private ConcurrentHashMap<String, String> uniqueOemFsMappings = new ConcurrentHashMap<>();
	private Map<String, OemFsMapping> universalIdToOemFsMap = Maps.newHashMap();
	private Map<String, String> keyToFsEntryId = Maps.newHashMap();
	private Set<String> siteIds = Sets.newHashSet();

	private String tkOemId;
	private Integer tkOemYear;
	private Integer tkOemVersion;
	private String fsType;
	private String athenaFsStatementNumber;

	private String athenaFormat;
	private String athenaVersion;

	private FSEntryRepo fsEntryRepo;

	private FsEntryService fsEntryService;
}

