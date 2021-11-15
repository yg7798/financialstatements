package com.tekion.accounting.fs.common.utils;

import com.tekion.as.models.dto.TekTextSearchAndAggregationRequest;
import com.tekion.core.es.common.i.ITekSearchRequest;
import com.tekion.core.es.common.impl.TekSearchAndAggregationRequest;
import com.tekion.core.es.common.impl.TekSearchRequest;
import com.tekion.core.es.request.BucketResponse;
import com.tekion.core.es.request.ESResponse;
import com.tekion.core.es.request.GroupResponse;
import com.tekion.core.es.request.TextSearchRequest;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@UtilityClass
public class EsUtils {

	public static List<GroupResponse> nullSafeGroupResponses(ESResponse esResponse) {
		if (esResponse.getGroups() == null) {
			return Arrays.asList(new GroupResponse());
		}
		return esResponse.getGroups();
	}

	public static GroupResponse nullSafeGroupResponse(ESResponse esResponse, int index) {
		if (esResponse.getGroups() == null || esResponse.getGroups().size() <= index) {
			return new GroupResponse();
		}
		return (GroupResponse) esResponse.getGroups().get(index);
	}

	public static List<BucketResponse> nullSafeBucketResponse(GroupResponse groupResponse) {
		if (groupResponse.getBuckets() == null) {
			return Arrays.asList(new BucketResponse());
		}
		return groupResponse.getBuckets();
	}

	public static TextSearchRequest createTextSearchForFields(ITekSearchRequest searchRequest, List<String> searchFieldLists) {
		if(TStringUtils.isNotBlank(searchRequest.getSearchText()) && TCollectionUtils.isNotEmpty(searchFieldLists)){
			return new SearchRequestOnFields(searchRequest.getSearchText(),searchFieldLists);
		}
		return null;
	}

	public static TekTextSearchAndAggregationRequest getTekTextSearchAndAggregationRequest(TekSearchAndAggregationRequest request){
		String s = JsonUtil.toJson(request);
		Optional<TekTextSearchAndAggregationRequest> tekTextSearchAndAggRequest = JsonUtil.fromJson(s, TekTextSearchAndAggregationRequest.class);
		return tekTextSearchAndAggRequest.orElse(null);

	}

	public static TekTextSearchAndAggregationRequest getTekTextSearchAndAggregationRequest(TekSearchRequest request){
		String s = JsonUtil.toJson(request);
		Optional<TekTextSearchAndAggregationRequest> tekTextSearchAndAggRequest = JsonUtil.fromJson(s, TekTextSearchAndAggregationRequest.class);
		return tekTextSearchAndAggRequest.orElse(null);

	}
}
