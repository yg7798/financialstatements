package com.tekion.accounting.fs.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tekion.accounting.fs.TConstants;
import com.tekion.accounting.fs.core.metadata.AccountingBeanMetaDataHolder;
import com.tekion.core.es.common.PageInfo;
import com.tekion.core.es.common.i.*;
import com.tekion.core.es.common.impl.TekFilterRequest;
import com.tekion.core.es.common.impl.TekGroupRequest;
import com.tekion.core.es.common.impl.TekProjectionRequest;
import com.tekion.core.es.common.impl.TekSearchRequest;
import com.tekion.core.es.request.Range;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtilsBean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class TESQueryUtils {
	public static List<TekSort> defaultSort(List<TekSort> sorts) {
		if (TCollectionUtils.isEmpty(sorts)) {
			TekSort tekSort = new TekSort();
			tekSort.setKey(TConstants.MODIFIED_TIME);
			tekSort.setField(TConstants.MODIFIED_TIME);
			tekSort.setOrder(TekSort.Order.DESC);
			sorts = Lists.newArrayList();
			sorts.add(tekSort);
		}
		return sorts;
	}

	public static PageInfo defaultPageIfNull(PageInfo page) {
		if (page == null) {
			page = new PageInfo();
			page.setRows(20);
			page.setStart(0);
		}
		return page;
	}

	public static void lowerCaseSearchText(ITekSearchRequest searchRequest) {
		if (TStringUtils.isNotBlank(searchRequest.getSearchText())) {
			((TekSearchRequest) searchRequest).setSearchText(searchRequest.getSearchText().toLowerCase());
		}
	}

	public static TekFilterRequest createInFilter(String field, List<Object> value) {
		TekFilterRequest tekFilterRequest = createFilter(field, value, ITekFilterOperator.IN);
		return tekFilterRequest;
	}

	public static TekFilterRequest createNinFilter(String field, List<Object> value) {
		TekFilterRequest tekFilterRequest = createFilter(field, value, ITekFilterOperator.NIN);
		return tekFilterRequest;
	}

	public static TekFilterRequest createFilter(String field, List<Object> value, ITekFilterOperator operator) {
		TekFilterRequest tekFilterRequest = new TekFilterRequest();
		tekFilterRequest.setOperator(operator);
		tekFilterRequest.setValues(value);
		tekFilterRequest.setField(field);
		return tekFilterRequest;
	}

	public static TekSort createSort(String field, TekSort.Order order) {
		TekSort tekSort = new TekSort();
		tekSort.setField(field);
		tekSort.setOrder(order);
		return tekSort;
	}

	public static void addDealerIdFilterIfNotPresent(ITekSearchRequest iRequest) {
		TekSearchRequest request = (TekSearchRequest) iRequest;
		List<TekFilterRequest> filters = TCollectionUtils.nullSafeList((List<TekFilterRequest>) request.getFilters());

		if (!isDealerIdFilterPresent(filters)) {
			List<Object> values = new ArrayList<>();
			values.add(UserContextProvider.getCurrentDealerId());
			TekFilterRequest filter = createInFilter(TConstants.DEALER_ID, values);

			try {
				filters.add(filter);
			}
			// means singleton list or Arrays.asList was used to create filterList
			catch (UnsupportedOperationException e) {
				List<TekFilterRequest> modifiedFilterList = Lists.newArrayList();
				if (filters.size() != 0) {
					modifiedFilterList.addAll(filters);
				}
				modifiedFilterList.add(filter);
				filters = modifiedFilterList;
			}
			request.setFilters(filters);
		}

	}

	public static void addDealerIdFilterIfNotPresent(ITekSearchRequest iRequest, List<String> dealerIds) {
		TekSearchRequest request = (TekSearchRequest) iRequest;
		List<TekFilterRequest> filters = TCollectionUtils.nullSafeList((List<TekFilterRequest>) request.getFilters());
		if (!isDealerIdFilterPresent(filters)) {
			List<Object> values = dealerIds.stream().collect(Collectors.toList());
			TekFilterRequest filter = createInFilter(TConstants.DEALER_ID, values);
			try {
				filters.add(filter);
			}
			// means singleton list or Arrays.asList was used to create filterList
			catch (UnsupportedOperationException e) {
				List<TekFilterRequest> modifiedFilterList = Lists.newArrayList();
				if (filters.size() != 0) {
					modifiedFilterList.addAll(filters);
				}
				modifiedFilterList.add(filter);
				filters = modifiedFilterList;
			}
			request.setFilters(filters);
		}
	}

	public static void addIdSortIfNotPresent(TekSearchRequest tekSearchRequest) {
		addCustomSort(tekSearchRequest, TConstants.ID);
	}

	public static void addCustomSort(TekSearchRequest tekSearchRequest, String sortField) {
		List<TekSort> sortList = TCollectionUtils.nullSafeList(tekSearchRequest.getSort());
		boolean idSortPresent = false;
		for (TekSort tekSort : sortList) {
			if (TStringUtils.isNotBlank(tekSort.getField())) {
				if (tekSort.getField().equalsIgnoreCase(sortField)) {
					idSortPresent = true;
				}
			}
		}

		if (!idSortPresent) {
			TekSort idSort = new TekSort();
			idSort.setField(sortField);
			idSort.setOrder(TekSort.Order.ASC);
			try {
				sortList.add(idSort);
			}
			// means singleton list was used to create sortList :/
			catch (UnsupportedOperationException e) {
				List<TekSort> sortList1 = Lists.newArrayList();
				if (sortList.size() != 0) {
					sortList1.addAll(sortList);
				}
				sortList1.add(idSort);
				sortList = sortList1;
			}
		}

		tekSearchRequest.setSort(sortList);
	}


	private static boolean isDealerIdFilterPresent(List<TekFilterRequest> filters) {
		for (TekFilterRequest filter : filters) {
			if (TConstants.DEALER_ID.equals(filter.getField())) {
				return true;
			}
		}
		return false;
	}

	private static boolean isFilterPresentForGivenField(List<TekFilterRequest> filters, String fieldName) {
		for (TekFilterRequest filter : filters) {
			if (fieldName.equals(filter.getField())) {
				return true;
			}
		}
		return false;
	}

	public static List<String> getDealerIdsFromFilter(List<TekFilterRequest> filters) {
		List<String> dealerIds = new ArrayList<>();
		for (TekFilterRequest filterRequest : filters) {
			if (TConstants.DEALER_ID.equals(filterRequest.getField())) {
				filterRequest.getValues().stream().forEach(val -> {
					dealerIds.add(val.toString());
				});
			}
		}
		return dealerIds;
	}

	public static void addNonDeletedFilterIfNotPresent(ITekSearchRequest iRequest) {
		TekSearchRequest request = (TekSearchRequest) iRequest;
		List<TekFilterRequest> filters = TCollectionUtils.nullSafeList((List<TekFilterRequest>) request.getFilters());

		if (!isDeletedFilterPresent(filters)) {
			List<Object> values = new ArrayList<>();
			values.add(false);
			TekFilterRequest filter = createInFilter(TConstants.DELETED, values);
			filters.add(filter);

			request.setFilters(filters);
		}
	}

	private static boolean isDeletedFilterPresent(List<TekFilterRequest> filters) {
		for (TekFilterRequest filter : filters) {
			if (TConstants.DELETED.equals(filter.getField())) {
				return true;
			}
		}
		return false;
	}


	/**
	 * This method uses the list of sorts to directly create the filter that should be able to fetch data for next batch
	 * This should satisfy 90% of use cases. A sort is mandatory whenever fetch data in batches
	 * This also breaks if sortKeyToBeanKeyMapping and UI sends sort like refText.original :(
	 * <p>
	 * Ex:
	 * <p>
	 * Consider the following list of sorts (scheduledTime DESC , accountNumber ASC, Id ASC) (order matters in sort
	 * a filter equivalent to below condition will be created)
	 * {
	 * accountingDate < lastObject.getScheduledTime()
	 * or
	 * accountingDate = lastObject.getScheduledTime() and accountNumber > lastObject.getAccountNumber()
	 * or
	 * accountingDate = lastObject.getScheduledTime() and accountNumber = lastObject.getAccountNumber() and id> lastObj.getId()
	 * }
	 *
	 * @param lastObjectFromLastBatch
	 * @param request
	 * @param sortKeyToBeanKeyMapping
	 * @return
	 */
	public static final <T> TekFilterRequest determineUsingSortUsedInRequest(T lastObjectFromLastBatch, TekSearchRequest request, Map<String, String> sortKeyToBeanKeyMapping) {

		sortKeyToBeanKeyMapping = TCollectionUtils.nullSafeMap(sortKeyToBeanKeyMapping);

		List<TekSort> sortList = request.getSort();
		if (TCollectionUtils.isEmpty(sortList)) {
			log.error("error UsingBatchIterator : how can a batchIterator work without telling how to fetch more results ");
			throw new TBaseRuntimeException();
		}


		TekFilterRequest finalFilterToReturn = new TekFilterRequest();

		List<TekFilterRequest> listOfOrFilterToBeSetInFinalFilterToReturn = Lists.newArrayList();

		Map<String, Object> fieldToValMapForNextFilter = Maps.newHashMap();
		for (TekSort tekSort : sortList) {
			List<TekFilterRequest> existingTekFilterToAddForCurrentFilter = Lists.newArrayList();
			for (Map.Entry<String, Object> stringObjectEntry : fieldToValMapForNextFilter.entrySet()) {
				TekFilterRequest tekFilterRequest = new TekFilterRequest();
				tekFilterRequest.setField(stringObjectEntry.getKey());
				tekFilterRequest.setValues(Collections.singletonList(stringObjectEntry.getValue()));
				existingTekFilterToAddForCurrentFilter.add(tekFilterRequest);
			}

			String field = tekSort.getField();
			String fieldOfBeanToFetchFrom = field;
			if (sortKeyToBeanKeyMapping.containsKey(field)) {
				fieldOfBeanToFetchFrom = sortKeyToBeanKeyMapping.get(field);
			}

			Object fieldValFromBean = getFieldValFromBean(lastObjectFromLastBatch, fieldOfBeanToFetchFrom);
			TekFilterRequest filterForCurrentSort = TESQueryUtils.createFilter(field,
					Collections.singletonList(fieldValFromBean),
					determineFilterTypeToUse(tekSort));

			existingTekFilterToAddForCurrentFilter.add(filterForCurrentSort);

			TekFilterRequest filterToBeCreatedTilCurrentSort = new TekFilterRequest();
			filterToBeCreatedTilCurrentSort.setOperator(ITekFilterOperator.BOOL);
			filterToBeCreatedTilCurrentSort.setAndFilters(existingTekFilterToAddForCurrentFilter);
			listOfOrFilterToBeSetInFinalFilterToReturn.add(filterToBeCreatedTilCurrentSort);

			fieldToValMapForNextFilter.put(field, fieldValFromBean);
		}

		finalFilterToReturn.setOperator(ITekFilterOperator.BOOL);
		finalFilterToReturn.setOrFilters(listOfOrFilterToBeSetInFinalFilterToReturn);

		return finalFilterToReturn;
	}

	private static ITekFilterOperator determineFilterTypeToUse(TekSort tekSort) {
		switch (tekSort.getOrder()) {
			case ASC:
				return ITekFilterOperator.GT;
			case DESC:
				return ITekFilterOperator.LT;
		}
		throw new TBaseRuntimeException();
	}

	@SneakyThrows
	private static <T> Object getFieldValFromBean(T lastObjectFromLastBatch, String fieldNameForWhichValToFetch) {
		String[] arr = fieldNameForWhichValToFetch.split("\\.");
		Object finalFieldValue = BeanUtilsBean.getInstance().cloneBean(lastObjectFromLastBatch);

		for (int i = 0; i < arr.length; i++) {
			finalFieldValue = getNestedFieldValue(finalFieldValue, arr[i]);
		}
		return finalFieldValue;
	}

	private static <T> Object getNestedFieldValue(T objectToExtractFieldFrom, String fieldNameForWhichValToFetch) throws InvocationTargetException, IllegalAccessException {
		if (objectToExtractFieldFrom instanceof Iterable) {
			objectToExtractFieldFrom = (T) getIterableFieldValue(objectToExtractFieldFrom);
		}
		AccountingBeanMetaDataHolder.registerFields(objectToExtractFieldFrom.getClass());
		Method method = AccountingBeanMetaDataHolder.getMethod(objectToExtractFieldFrom.getClass().getName(), fieldNameForWhichValToFetch);
		Object fieldVal = method.invoke(objectToExtractFieldFrom);
		return fieldVal;
	}

	//TODO: need to add support for Map
	private static <T> Object getIterableFieldValue(T objectToExtractFieldFrom) {
		List<T> iterableField = new ArrayList<>();
		iterableField.addAll((Collection<? extends T>) objectToExtractFieldFrom);
		return iterableField.get(0);
	}

//	public static void addFilterToExcludeVirtualTypes(ITekSearchRequest iRequest) {
//		TekSearchRequest request = (TekSearchRequest) iRequest;
//		List<TekFilterRequest> filters = TCollectionUtils.nullSafeList((List<TekFilterRequest>) request.getFilters());
//
//		TekFilterRequest filter = createFilter(GLPostingES.SOURCE_TYPE, PostingSourceType.getListOfVirtualTypesAsObjects(), ITekFilterOperator.NIN);
//		try {
//			filters.add(filter);
//		}
//		// means singleton list or Arrays.asList was used to create filterList
//		catch (UnsupportedOperationException e) {
//			List<TekFilterRequest> modifiedFilterList = Lists.newArrayList();
//			if (filters.size() != 0) {
//				modifiedFilterList.addAll(filters);
//			}
//			modifiedFilterList.add(filter);
//			filters = modifiedFilterList;
//		}
//		request.setFilters(filters);
//	}

	public static PageInfo getPageInfo(int start, int rows) {
		PageInfo pageInfo = new PageInfo();
		pageInfo.setStart(start);
		pageInfo.setRows(rows);
		return pageInfo;
	}

	public static TekProjectionRequest createProjection(String field, String key, TekMetricFunction metricFunction) {
		TekProjectionRequest projectionRequest = new TekProjectionRequest();
		projectionRequest.setField(field);
		projectionRequest.setKey(key);
		projectionRequest.setMetricFunction(metricFunction);
		return projectionRequest;
	}

	public static TekGroupRequest createGroup(String key, String field, Range range, List<TekProjectionRequest> projectionRequests) {
		TekGroupRequest groupRequest = new TekGroupRequest();
		groupRequest.setKey(key);
		groupRequest.setField(field);

		if (Objects.nonNull(range)) {
			groupRequest.setGroupType(TekAggregationFunction.RANGE);
			groupRequest.setRanges(Collections.singletonList(range));
		}

		if (TCollectionUtils.isNotEmpty(projectionRequests)) {
			groupRequest.setProjections(projectionRequests);
		}
		return groupRequest;
	}

	public static TekGroupRequest createGroupByType(String key, String field, TekAggregationFunction groupType, List<TekProjectionRequest> projectionRequests) {
		TekGroupRequest groupRequest = new TekGroupRequest();
		groupRequest.setKey(key);
		groupRequest.setField(field);
		groupRequest.setGroupType(groupType);

		if (TCollectionUtils.isNotEmpty(projectionRequests)) {
			groupRequest.setProjections(projectionRequests);
		}
		return groupRequest;
	}


	public static List<Object> getFilterValuesForKey(List<TekFilterRequest> filters, String fieldName) {
		if (TCollectionUtils.isEmpty(filters)) {
			return Lists.newArrayList();
		}

		List<Object> values = Lists.newArrayList();
		for (TekFilterRequest filter : filters) {
			if (fieldName.equalsIgnoreCase(filter.getField())) {
				values.addAll(filter.getValues());
			}
		}
		return values;
	}

	public static void updateToMaxAllowedGroups(TekGroupRequest groupRequest) {
		groupRequest.setRows(TConstants.MAX_ES_SUPPORTED_ROWS_LONG);
	}

//	public static WorkspaceSearchRequest getSearchRequestCopy(WorkspaceSearchRequest searchRequest) {
//		List<TekFilterRequest> filters = (List<TekFilterRequest>) searchRequest.getFilters();
//		if (Objects.isNull(filters)) {
//			return null;
//		}
//		return JsonUtil.fromJson(JsonUtil.toJson(searchRequest), WorkspaceSearchRequest.class).orElse(null);
//	}
//
//	public static void addFilterIfNotPresentForGivenField(ITekSearchRequest iRequest, String fieldName, TekFilterRequest filterToAdd) {
//		TekSearchRequest request = (TekSearchRequest) iRequest;
//		List<TekFilterRequest> filters = TCollectionUtils.nullSafeList((List<TekFilterRequest>) request.getFilters());
//
//		if (!isFilterPresentForGivenField(filters, fieldName)) {
//			filters.add(filterToAdd);
//			request.setFilters(filters);
//		}
//	}

	public static List<? extends ITekSearchFilter>  removeInFilterForGivenField(List<? extends ITekSearchFilter> filters, String fieldName) {
		if(TCollectionUtils.isEmpty(filters)) {
			return filters;
		}
		for (int i=0; i< filters.size();i++) {
			ITekSearchFilter filter = filters.get(i);
			if (filter != null && TStringUtils.isNotBlank(filter.field()) && filter.field().equalsIgnoreCase(fieldName)) {
				Set<String> values = TCollectionUtils.nullSafeList(filter.values()).stream().map(v -> (String) v).collect(Collectors.toSet());
				if (filter.operator() != null) {
					if (TCollectionUtils.isNotEmpty(values)) {
						if (filter.operator() == ITekFilterOperator.IN) {
							filters.remove(i);
							return filters;
						}
					}
				}
			}
		}
		return filters;
	}

}
