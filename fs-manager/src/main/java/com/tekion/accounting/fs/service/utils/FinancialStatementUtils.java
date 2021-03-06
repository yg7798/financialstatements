package com.tekion.accounting.fs.service.utils;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.common.exceptions.FSError;
import com.tekion.accounting.fs.dto.context.FsReportContext;
import com.tekion.accounting.fs.beans.common.OEMFsCellCodeSnapshot;
import com.tekion.accounting.fs.beans.common.OemConfig;
import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.service.compute.models.OemFsCellContext;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.core.excelGeneration.models.utils.TCollectionUtils;
import com.tekion.core.exceptions.TBaseRuntimeException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import static com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode.additionInfoField_month;

@UtilityClass
@Slf4j
public class FinancialStatementUtils {

	public static void validateOemId(String oemId){
		if(!EnumUtils.isValidEnum(OEM.class, oemId)){
			throw new TBaseRuntimeException(FSError.invalidOemId, oemId);
		}
	}

	public static BigDecimal getSnapshotValue(AccountingOemFsCellCode fsCellCode, OemFsCellContext context){
		int month = Integer.parseInt(fsCellCode.getAdditionalInfo().get(additionInfoField_month));
		Map<Integer, Map<String, OEMFsCellCodeSnapshot>> monthToCellCodeSnapshots = context.getMonthToCellCodeSnapshots();
		//log.info("monthly snapshots {}", JsonUtil.toJson(monthToCellCodeSnapshots));

		if (TCollectionUtils.isNotEmpty((monthToCellCodeSnapshots.get(month)))) {
			String code = fsCellCode.getCode();
			//log.info("putting values from snapshot for {} {}", month, code);

			if (Objects.nonNull(monthToCellCodeSnapshots.get(month).get(code))) {
				log.info("adding snapshot value for cell {} month {}", fsCellCode.getCode(), month);
				return monthToCellCodeSnapshots.get(month).get(code).getValue();
			}
		}
		return null;
	}

	public static boolean useSnapshotValuesInFS(OemFsCellContext context) {
		AccountingInfo accountingInfo = context.getAccountingInfo();
		OemConfig oemConfig = context.getOemConfig();
		if (Objects.nonNull(accountingInfo) && Objects.nonNull(accountingInfo.getFsPreferences())
				&& Objects.nonNull(accountingInfo.getFsPreferences().getUseSnapshotsForMonthlyCells())) {
			return accountingInfo.getFsPreferences().getUseSnapshotsForMonthlyCells();
		}

		if (Objects.nonNull(oemConfig) && Objects.nonNull(oemConfig.getFsPreferences())
				&& Objects.nonNull(oemConfig.getFsPreferences().getUseSnapshotsForMonthlyCells())) {
			return oemConfig.getFsPreferences().getUseSnapshotsForMonthlyCells();
		}

		return false;
	}

	public static boolean isRoundOffGLBalances(FsReportContext context){
		AccountingInfo accountingInfo = context.getAccountingInfo();
		OemConfig oemConfig = context.getOemConfig();
		if (Objects.nonNull(accountingInfo) && Objects.nonNull(accountingInfo.getFsPreferences())
				&& Objects.nonNull(accountingInfo.getFsPreferences().getRoundOffGlAccountBalances())
				&& Objects.nonNull(accountingInfo.getFsPreferences().getRoundOffGlAccountBalances().get(context.getOemId()))) {
			return accountingInfo.getFsPreferences().getRoundOffGlAccountBalances().get(context.getOemId());
		}

		if (Objects.nonNull(oemConfig) && Objects.nonNull(oemConfig.getFsPreferences())
				&& Objects.nonNull(oemConfig.getFsPreferences().getRoundOffGlAccountBalances())
				&& Objects.nonNull(oemConfig.getFsPreferences().getRoundOffGlAccountBalances().get(context.getOemId()))) {
			return oemConfig.getFsPreferences().getRoundOffGlAccountBalances().get(context.getOemId());
		}

		return false;
	}


	public static boolean useNewRoundOffFlow(AccountingInfo accountingInfo, OemConfig oemConfig) {
		String oemId = oemConfig.getOemId();

		if (Objects.nonNull(accountingInfo) && Objects.nonNull(accountingInfo.getFsPreferences())
				&& Objects.nonNull(accountingInfo.getFsPreferences().getUseNewFlowRoundoff())
		&& Objects.nonNull(accountingInfo.getFsPreferences().getUseNewFlowRoundoff().get(oemId))) {
			return accountingInfo.getFsPreferences().getUseNewFlowRoundoff().get(oemId);
		}

		if (Objects.nonNull(oemConfig) && Objects.nonNull(oemConfig.getFsPreferences())
				&& Objects.nonNull(oemConfig.getFsPreferences().getUseNewFlowRoundoff())
				&& Objects.nonNull(oemConfig.getFsPreferences().getUseNewFlowRoundoff().get(oemId))) {

			return oemConfig.getFsPreferences().getUseNewFlowRoundoff().get(oemId);
		}

		return false;
	}
}

