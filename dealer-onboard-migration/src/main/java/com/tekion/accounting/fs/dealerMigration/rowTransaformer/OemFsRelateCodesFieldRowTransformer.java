package com.tekion.accounting.fs.dealerMigration.rowTransaformer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.common.MigrationMetaDataForFsEntry;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.accounting.fs.dealerMigration.beans.AthenaOemMapping;
import com.tekion.accounting.fs.dealerMigration.context.OemFsRelateCodesMigrationContext;
import com.tekion.accounting.fs.dealerMigration.fieldMetaData.AbstractFieldMetaData;
import com.tekion.accounting.fs.dealerMigration.fieldMetaData.OemFsRelateCodesFieldMetaData;
import com.tekion.accounting.fs.dealerMigration.utils.MigrationV3Constants;
import com.tekion.accounting.fs.dealerMigration.utils.MigrationV3Utils;
import com.tekion.accounting.fs.dto.fsEntry.FsEntryCreateDto;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.migration.v3.preview.MigrationPreviewEntity;
import com.tekion.migration.v3.task.MigrationSource;
import com.tekion.migration.v3.taskexecutor.RowTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OemFsRelateCodesFieldRowTransformer extends RowTransformer<OemFsMapping, OemFsRelateCodesMigrationContext> {

	private final OemFsCellGroupRepo oemFsCellGroupRepo;
	private final DealerConfig dealerConfig;

	private final List<AbstractFieldMetaData> fieldMetaData = Lists.newArrayList(OemFsRelateCodesFieldMetaData.values());


	@Override
	public OemFsMapping transformRow(MigrationPreviewEntity migrationPreviewEntity, OemFsRelateCodesMigrationContext context) {

		populateContext(context, migrationPreviewEntity);

		AthenaOemMapping athenaOemMapping = null;

		try {
			athenaOemMapping =
					MigrationV3Utils.transformToAthenaRow(
							migrationPreviewEntity, AthenaOemMapping.class, fieldMetaData);
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}

		if (athenaOemMapping == null) {
			throw new TBaseRuntimeException("got null object after casting to athena bean");
		}
		OemFsMapping oemFsMapping = transformFsRelateMapping(athenaOemMapping, context);

		return oemFsMapping;

	}

	private OemFsMapping transformFsRelateMapping(AthenaOemMapping athenaOemMapping, OemFsRelateCodesMigrationContext context) {

		if(!TStringUtils.nullSafeString(athenaOemMapping.getOemStatementFormat()).equalsIgnoreCase(context.getAthenaFormat())){
			return null;
		}

		if(!TStringUtils.nullSafeString(athenaOemMapping.getOemStatementVersion()).equalsIgnoreCase(context.getAthenaVersion())){
			return null;
		}

		if(context.getAthenaFsStatementNumber() !=null && !context.getAthenaFsStatementNumber().equalsIgnoreCase(athenaOemMapping.getFsStatementNumber())){
			return null;
		}


		String siteId = athenaOemMapping.getSiteId();
		if ("-1".equalsIgnoreCase(siteId)){
			siteId = UserContextUtils.getDefaultSiteId();
		}

		context.getSiteIds().add(siteId);

		OemFsMapping oemFsMapping = new OemFsMapping();
		oemFsMapping.setOemId(context.getTkOemId());
		oemFsMapping.setYear(context.getTkOemYear());
		oemFsMapping.setVersion(context.getTkOemVersion());

		String glAccountId = context.getAccountNumberToAccountIdMap().get(athenaOemMapping.getDmsAccountNumber());
		if(TStringUtils.isBlank(glAccountId)){

			return null;
		}
		oemFsMapping.setGlAccountId(glAccountId);

		String tkGroupCode = null;

		switch (MigrationSource.valueOf(context.getDealerMigrationData().getMigrationSource())){
			case DEALERTRACK :
			case DEALERBUILT :
			case DOMINION:
			case QUORUM:
			case AUTOMATE:
				tkGroupCode = context.getSourceDmsPclVsTkGroupCodeMap().get(athenaOemMapping.getOemAccountNumber());
				break;
			case RR :
				String athenaOemAccountNumber = TStringUtils.nullSafeString(athenaOemMapping.getOemAccountNumber());
				tkGroupCode = context.getSourceDmsPclVsTkGroupCodeMap().get(athenaOemAccountNumber.replace("@",""));
				break;
			case CDK :
			case AUTOSOFT:
			case PBS:
				tkGroupCode = context.getSourceDmsPclVsTkGroupCodeMap().get(athenaOemMapping.getPclCode());
				break;
			default:
				tkGroupCode = context.getSourceDmsPclVsTkGroupCodeMap().get(athenaOemMapping.getOemAccountNumber());
				break;
		}

		if(TStringUtils.isBlank(tkGroupCode)){
			return null;
		}

		oemFsMapping.setFsCellGroupCode(tkGroupCode);
		oemFsMapping.setDealerId(UserContextProvider.getCurrentDealerId());
		oemFsMapping.setCreatedByUserId("-1");
		oemFsMapping.setModifiedByUserId("-1");
		oemFsMapping.setCreatedTime(context.getMigrationTime());
		oemFsMapping.setModifiedTime(context.getMigrationTime());
		oemFsMapping.setMigrated(true);
		oemFsMapping.setMigratedTime(context.getMigrationTime());
		oemFsMapping.setSiteId(siteId);
		oemFsMapping.setTenantId(UserContextProvider.getCurrentTenantId());
		addFsEntryInContextAndSaveInDb(context,oemFsMapping);

		oemFsMapping = validate(oemFsMapping, context);

		return oemFsMapping;
	}

	private void addFsEntryInContextAndSaveInDb(OemFsRelateCodesMigrationContext context, OemFsMapping oemFsMapping) {
		String key = getKeyForFs(oemFsMapping, context.getAthenaFsStatementNumber());
		if(!context.getKeyToFsEntryId().containsKey(key)) {
			List<FSEntry> fsEntries = context.getFsEntryRepo().find(context.getTkOemId(), context.getTkOemYear(), UserContextProvider.getCurrentDealerId(), oemFsMapping.getSiteId(), context.getFsType());
			if (fsEntries.size() <= 0) {
				FSEntry fsEntry = context.getFsEntryService().createFSEntry(
						FsEntryCreateDto.builder()
								.oemId(OEM.valueOf(context.getTkOemId()))
								.year(context.getTkOemYear())
								.version(context.getTkOemVersion())
								.siteId(oemFsMapping.getSiteId())
								.fsType(FSType.valueOf(context.getFsType()))
								.migrationMetaDataForFsEntry(MigrationMetaDataForFsEntry.builder().
										tkFsStatementNumber(context.getAthenaFsStatementNumber()).build())
								.build()
				);
				context.getKeyToFsEntryId().put(key, fsEntry.getId());
			} else {
				for (FSEntry fsEntry : fsEntries) {
					String keyFromFsEntry = getKeyForFs(fsEntry, fsEntry.getMigrationMetaDataForFsEntry().getTkFsStatementNumber());
					if (!context.getKeyToFsEntryId().containsKey(keyFromFsEntry)) {
						context.getKeyToFsEntryId().put(keyFromFsEntry, fsEntry.getId());
					}
				}
			}
		}
		oemFsMapping.setFsId(context.getKeyToFsEntryId().get(getKeyForFs(oemFsMapping, context.getAthenaFsStatementNumber())));
	}


	private String getKeyForFs(FSEntry fsEntry, String athenaFsStatementNumber){
		return getKeyForFs(fsEntry.getOemId(),fsEntry.getYear(),fsEntry.getVersion(),fsEntry.getSiteId(),athenaFsStatementNumber);
	}
	private String getKeyForFs(OemFsMapping oemFsMapping,String athenaFsStatementNumber){
		return getKeyForFs(oemFsMapping.getOemId(),oemFsMapping.getYear(),oemFsMapping.getVersion(),oemFsMapping.getSiteId(),athenaFsStatementNumber);
	}

	private String getKeyForFs(String oemId, Integer year, Integer version, String siteId ,String fsType){
		return oemId+"_"+year+"_"+version+"_"+siteId+"_"+fsType;
	}

	private synchronized OemFsMapping validate(OemFsMapping oemFsMapping, OemFsRelateCodesMigrationContext context) {
		if(context.getUniversalIdToOemFsMap().containsKey(oemFsMapping.getUniversalId()) || context.getUniqueOemFsMappings().containsKey(oemFsMapping.getUniversalId())){
			return null;
		}else{
			context.getUniqueOemFsMappings().put(oemFsMapping.getUniversalId(), "");
			return oemFsMapping;
		}
	}

	private void populateContext(OemFsRelateCodesMigrationContext context, MigrationPreviewEntity migrationPreviewEntity) {
		if(TStringUtils.isBlank(context.getTkOemId())){

			if(context.getEntityMetadata().getAdditionalInfo().get(MigrationV3Constants.tkOemId) == null){
				context.saveProcessingError(migrationPreviewEntity.getExternalId(), "tkOemId not set in metadata");
				throw new TBaseRuntimeException();
			}

			String tkOemId = context.getEntityMetadata().getAdditionalInfo().get(MigrationV3Constants.tkOemId);
			context.setTkOemId(tkOemId);
		}
		if(context.getFsType() == null) {
			if (TStringUtils.isNotBlank(context.getEntityMetadata().getAdditionalInfo().get(MigrationV3Constants.tkFsType))) {
				context.setFsType(context.getEntityMetadata().getAdditionalInfo().get(MigrationV3Constants.tkFsType));
			} else {
				context.setFsType(FSType.OEM.name());
			}
		}
		if(context.getAthenaFsStatementNumber() == null){
			String athenaFsStatementNumber = context.getEntityMetadata().getAdditionalInfo().get(MigrationV3Constants.athenaFsStatementFormat);
			context.setAthenaFsStatementNumber(athenaFsStatementNumber);

		}
		if(context.getTkOemYear() == null){
			Integer tkOemYear = Integer.parseInt(context.getEntityMetadata().getAdditionalInfo().get(MigrationV3Constants.tkOemYear));
			context.setTkOemYear(tkOemYear);
		}

		if(context.getTkOemVersion() == null){
			Integer tkOemVersion = Integer.parseInt(context.getEntityMetadata().getAdditionalInfo().get(MigrationV3Constants.tkOemVersion));
			context.setTkOemVersion(tkOemVersion);
		}


		if(context.getAthenaFormat() == null) {
			Map<String, String> additionalInfo = context.getEntityMetadata().getAdditionalInfo();

			if(additionalInfo.get(MigrationV3Constants.athenaFormat) == null){
				context.saveProcessingError(migrationPreviewEntity.getExternalId(), "athenaFormat not set in metadata");
				throw new TBaseRuntimeException();
			}
			context.setAthenaFormat(additionalInfo.get(MigrationV3Constants.athenaFormat));
		}

		if(context.getAthenaVersion() == null) {
			Map<String, String> additionalInfo = context.getEntityMetadata().getAdditionalInfo();

			if(additionalInfo.get(MigrationV3Constants.athenaVersion) == null){
				context.saveProcessingError(migrationPreviewEntity.getExternalId(), "athenaVersion not set in metadata");
				throw new TBaseRuntimeException();
			}
			context.setAthenaVersion(additionalInfo.get(MigrationV3Constants.athenaVersion));
		}



		if(context.getSourceDmsPclVsTkGroupCodeMap() == null ){
			String migrationSource = context.getDealerMigrationData().getMigrationSource();
			List<AccountingOemFsCellGroup> tekGroups =
					oemFsCellGroupRepo.findNonDeletedByOemIdYearVersionAndCountry(context.getTkOemId(), context.getTkOemYear(), context.getTkOemVersion(), dealerConfig.getDealerCountryCode());

			context.setSourceDmsPclVsTkGroupCodeMap(Maps.newHashMap());

			switch (MigrationSource.valueOf(migrationSource)){
				case RR :
					tekGroups.forEach(g -> {
						context.getSourceDmsPclVsTkGroupCodeMap().put(g.getRrPcl(), g.getGroupCode());
					});
					break;
				case DEALERTRACK :
					tekGroups.forEach(g -> {
						context.getSourceDmsPclVsTkGroupCodeMap().put(g.getOemAccountNumber(), g.getGroupCode());
					});
					break;
				case CDK :
					tekGroups.forEach(g -> {
						context.getSourceDmsPclVsTkGroupCodeMap().put(g.getCdkPcl(), g.getGroupCode());
					});
					break;
				case DEALERBUILT :
					tekGroups.forEach(g -> {
						context.getSourceDmsPclVsTkGroupCodeMap().put(g.getDbPcl(), g.getGroupCode());
					});
					break;
				case DOMINION :
					tekGroups.forEach(g -> {
						context.getSourceDmsPclVsTkGroupCodeMap().put(g.getDominionPcl(), g.getGroupCode());
					});
					break;
				case AUTOMATE:
					tekGroups.forEach(g -> {
						context.getSourceDmsPclVsTkGroupCodeMap().put(g.getAutomatePcl(), g.getGroupCode());
					});
					break;
				case QUORUM :
					tekGroups.forEach(g -> {
						context.getSourceDmsPclVsTkGroupCodeMap().put(g.getQuorumPcl(), g.getGroupCode());
					});
					break;
				case AUTOSOFT:
					tekGroups.forEach(g -> {
						context.getSourceDmsPclVsTkGroupCodeMap().put(g.getAutosoftPcl(), g.getGroupCode());
					});
					break;
				case PBS:
					tekGroups.forEach(g -> {
						context.getSourceDmsPclVsTkGroupCodeMap().put(g.getPbsPcl(), g.getGroupCode());
					});
					break;
				case DIS:
					tekGroups.forEach(g -> {
						context.getSourceDmsPclVsTkGroupCodeMap().put(g.getDisPcl(), g.getGroupCode());
					});
					break;
				default:
					break;
			}

		}


	}


}

