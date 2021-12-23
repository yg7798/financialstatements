package com.tekion.accounting.fs.util;

import java.util.Objects;

public class ApiHelperUtils {
	public static Integer getDefaultParallelism(Integer parallelism){
		if(Objects.isNull(parallelism)) return 1;
		return parallelism;
	}
}
