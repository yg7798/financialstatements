package com.tekion.accounting.fs.service.excelGeneration;

import com.google.common.collect.Maps;
import com.tekion.accounting.fs.common.excelGeneration.context.ExcelReportContext;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.service.helper.cache.dtos.OptionMinimal;
import com.tekion.accounting.fs.common.enums.CustomFieldType;
import com.tekion.accounting.fs.common.excelGeneration.dto.financialStatement.OEMMappingRequestDto;
import com.tekion.as.models.beans.GLAccount;
import com.tekion.as.models.beans.TrialBalanceRow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OEMMappingExcelReportContext extends ExcelReportContext {

	private List<GLAccount> glAccountList = new ArrayList<>();
	private Map<CustomFieldType, Map<String, OptionMinimal>> keyToIdToOptionMap = Maps.newHashMap();
	private Map<String, TrialBalanceRow> trialBalanceRowMap = Maps.newHashMap();
	private Map<String,List<String>> glIdOemFsMappingsMap = Maps.newHashMap();
	private OEMMappingRequestDto oemMappingRequestDto;
	private Set<String> includedDealerIds = new HashSet<>();
	private FSEntry fsEntry;
	private Map<String, String> dealerIdToDealerNameMap = Maps.newHashMap();
}