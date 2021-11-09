package com.tekion.accounting.fs.excelGeneration.generators.financialStatement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorksheetApplicableFilter {
    String key;
    List<String> values;
}