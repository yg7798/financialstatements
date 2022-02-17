package com.tekion.accounting.fs.dealerMigration.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryResultCount {
	public static final String MIGRATION_ID = "migrationId";
	public static final String SESSION_ID = "sessionId";

	private boolean countFetchedFromAthena;
	private List<Map<String, String>> resultSet;

	public static QueryResultCount buildQueryResultCount(List<Map<String, String>> resultSet, boolean countFetchedFromAthena) {
		return new QueryResultCount().builder()
				.resultSet(resultSet)
				.countFetchedFromAthena(countFetchedFromAthena)
				.build();
	}

}
