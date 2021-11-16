package com.tekion.accounting.fs.service.excelGeneration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tekion.accounting.fs.beans.memo.MemoWorksheet;
import com.tekion.accounting.fs.beans.memo.MemoWorksheetTemplate;
import com.tekion.accounting.fs.service.common.excelGeneration.generators.financialStatement.dto.MemoWorksheetExcelRequestDto;
import com.tekion.core.excelGeneration.models.model.v2.FetchNextBatchRequestV2;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorksheetExcelReportContext {
	private List<MemoWorksheetTemplate> memoWorksheetTemplates = Lists.newArrayList();
	private Map<String, MemoWorksheet> keyToMemoWorksheetMap = Maps.newHashMap();
	private FetchNextBatchRequestV2 nextBatchRequestV2;
	private MemoWorksheetExcelRequestDto reportRequestDto;
	private String reportType;
}