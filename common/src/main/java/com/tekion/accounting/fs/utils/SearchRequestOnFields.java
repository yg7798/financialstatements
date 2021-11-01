package com.tekion.accounting.fs.utils;

import com.tekion.core.es.request.TextSearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.List;

public class SearchRequestOnFields implements TextSearchRequest {
	private final String text;
	private final List<String> searchFieldsList;

	public SearchRequestOnFields(String text, List<String> searchFieldLists) {
		this.text = text;
		this.searchFieldsList = searchFieldLists;
	}

	@Override
	public QueryBuilder getQueryBuilder() {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		final List<String> fields = this.searchFieldsList;
		for (String field : fields) {
			PrefixQueryBuilder prefixQueryBuilder = QueryBuilders.prefixQuery(field, this.text);
			boolQueryBuilder.should(prefixQueryBuilder);
		}
		return boolQueryBuilder;
	}
}
