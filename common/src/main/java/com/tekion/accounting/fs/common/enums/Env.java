package com.tekion.accounting.fs.common.enums;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public enum Env {
	LOCAL("local"),
	TST("tst"),
	STAGE("stage"),
	PRE_PROD("preprod"),
	PERF("perf"),
	UAT("uat"),
	PROD("prod");


	private final String clusterPrefix;

	public static final List<Env> noEnvList = Lists.newArrayList();
}

