package com.tekion.accounting.fs.client.dtos;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CellGroupDto {
	private String oemId;
	private String displayName;
	private String code;
	private Integer year;
	private Integer version;
	private String country;
	private boolean derived;
	private String subType;
	private String valueType;
	private String durationType;
	private String expression;
	private String oemDescription;
	private String groupCode;
	private String oemCode; // TBIN string
	private String oemValueType;

	private List<String> dependentFsCellCodes = Lists.newArrayList();
	private Map<String, String> additionalInfo = new HashMap<>();
	private Map<String, String> tags = new HashMap<>();
}
