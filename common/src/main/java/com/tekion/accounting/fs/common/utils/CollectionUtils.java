package com.tekion.accounting.fs.common.utils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CollectionUtils {

	public static boolean isTwoSetsSame(Set<String> set1, Set<String> set2) {
		if(Objects.isNull(set1)) set1 = new HashSet<>();
		if(Objects.isNull(set2)) set2 = new HashSet<>();
		if(set1.size() != set2.size()) return false;
		Set<String> set3 = new HashSet<>();
		set3.addAll(set2);
		set3.addAll(set1);
		return set3.size() == set1.size();
	}
}
