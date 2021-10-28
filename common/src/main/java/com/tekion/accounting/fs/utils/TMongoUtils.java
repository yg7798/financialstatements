package com.tekion.accounting.fs.utils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tekion.accounting.fs.TConstants;
import com.tekion.core.es.common.i.ITekSearchFilter;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.tekion.accounting.fs.TConstants.DELETED;

@Slf4j
public class TMongoUtils {

	//return Update object
	public static Update fromDBObjectExcludeNullFields(DBObject object) {
		return updateFromObject(object, null);
	}

	public static Update fromDBObjectExcludeNullFields(DBObject object, Set<String> ignoreFields) {
		return updateFromObject(object, ignoreFields);
	}

	public static BasicDBObject dbObjectFromCustomObject(MongoTemplate mongoTemplate, Object objToConvert) {
		BasicDBObject object = new BasicDBObject();
		mongoTemplate.getConverter().write(objToConvert, object);
		return object;
	}

	public static Update updateFromCustomObject(MongoTemplate mongoTemplate, Object objToUpdate) {
		BasicDBObject object = dbObjectFromCustomObject(mongoTemplate, objToUpdate);
		return fromDBObjectExcludeNullFields(object);
	}


	public static Update updateForMarkAsDeleted() {
		return new Update().set(DELETED, true);
	}

	/**
	 * This method takes up a custom tekion filter and creates a criteria out of it
	 *
	 * @param filters  The map of filters from which to create criteria
	 * @param criteria An already instantiated criteria, that you need to add the filters too
	 *                 //todo: may be return a Query from this
	 */
//	public static Query createFilterCriteria(Map<String, Filter> filters, Criteria criteria) {
//		for (Map.Entry<String, Filter> entry : filters.entrySet()) {
//			Filter filter = entry.getValue();
//			if (filter == null) continue;
//			String key = filter.getKey();
//			List<Object> values = filter.getValues();
//			criteria.and(key).in(values);
//		}
//		return Query.query(criteria);
//	}

	public static Query createFilterCriteria(List<? extends ITekSearchFilter> filters, Criteria criteria) {
		for (ITekSearchFilter f : filters) {
			if (f == null) continue;

			String field = f.field();
			List<Object> values = f.values();
			String operator = f.operator().name();

			switch (operator) {

				case "IN":
					criteria.and(field).in(values);
					break;

				case "NIN":
					criteria.and(field).nin(values);
					break;

				case "GT":
					criteria.and(field).gt(values.get(0));
					break;

				case "GTE":
					criteria.and(field).gte(values.get(0));
					break;

				case "LT":
					criteria.and(field).lt(values.get(0));
					break;

				case "LTE":
					criteria.and(field).lte(values.get(0));
					break;

				case "BTW_INCLUSIVE":
				case "BTW":
					criteria.and(field).gte(values.get(0)).lte(values.get(1));
					break;

				case "BOOL":
					criteria.and(field).is(values.get(0));
					break;
			}

		}
		return Query.query(criteria);
	}


//	public static Sort createSort(List<TekSort> sorts){
//		List<TekSort> tekSorts = TESQueryUtils.defaultSort(sorts);
//		TekSort tekSort = tekSorts.get(0);
//		return new Sort(Direction.fromString(tekSort.getOrder().name()), tekSort.getField());
//	}
	//modify Indexed Key Name For Delete
	public static String modifyKeyNameForDelete(String indexedKeyToBeDeleted){
		return "deleted_" + indexedKeyToBeDeleted + "_" + System.currentTimeMillis();
	}

	private static Update updateFromObject(DBObject object, Set<String> ignoreFields){
		Collection ignoreFieldsCol = TCollectionUtils.nullSafeCollection(ignoreFields);
		Update update = new Update();
		for (String key : object.keySet()) {
			if (ignoreFieldsCol.contains(key)) continue;
			;
			Object value = object.get(key);
			if (value != null) {
				update.set(key, value);
			}
		}
		return update;
	}

//	public static Query mongoQuery(TekSearchRequest request){
//		return mongoQuery(request, UserContextProvider.getCurrentDealerId());
//	}

//	public static Query mongoQuery(TekSearchRequest request, String dealerId){
//		List<? extends ITekSearchFilter> filters = TCollectionUtils.nullSafeList(request.getFilters());
//		Criteria criteria = Criteria.where(TConstants.DELETED).is(false);
//		criteria = criteria.and(TConstants.DEALER_ID_KEY).is(dealerId);
//		createFilterCriteria(filters, criteria);
//		PageInfo page = TESQueryUtils.defaultPageIfNull(request.getPageInfo());
//		Query q = Query.query(criteria).with(TMongoUtils.createSort(TESQueryUtils.defaultSort(request.getSort())));
//		q.collation(Collation.of("en").strength(Collation.ComparisonLevel.secondary()));
//		return q.skip(page.getStart()).limit(page.getRows());
//	}
//
//	public static Query mongoQueryExcludePageInfo(TekSearchRequest request){
//		List<? extends ITekSearchFilter> filters = TCollectionUtils.nullSafeList(request.getFilters());
//		Criteria criteria = Criteria.where(TConstants.DELETED).is(false);
//		criteria = criteria.and(TConstants.DEALER_ID_KEY).is(UserContextProvider.getCurrentDealerId());
//		createFilterCriteria(filters, criteria);
//		Query q = Query.query(criteria).with(TMongoUtils.createSort(TESQueryUtils.defaultSort(request.getSort())));
//		q.collation(Collation.of("en").strength(Collation.ComparisonLevel.secondary()));
//		return q;
//	}
//
//	public static Query totalCountQuery(TekSearchRequest request){
//		List<? extends ITekSearchFilter> filters = TCollectionUtils.nullSafeList(request.getFilters());
//		Criteria criteria = Criteria.where(TConstants.DELETED).is(false);
//		criteria = criteria.and(TConstants.DEALER_ID_KEY).is(UserContextProvider.getCurrentDealerId());
//		createFilterCriteria(filters, criteria);
//		return Query.query(criteria);
//	}
//
//	public static <T> Update updateFromCustomObjectWithUnset(
//			MongoTemplate mongoTemplate, T objToUpdate, Set<String> excludedKeys) {
//		DBObject object = dbObjectFromCustomObject(mongoTemplate, objToUpdate);
//		Update update = fromDBObjectExcludeNullFields(object, excludedKeys);
//		for (Field f : objToUpdate.getClass().getDeclaredFields()) {
//			try {
//				f.setAccessible(true);
//				if (isNull(f.get(objToUpdate)) && !excludedKeys.contains(f.getName())) {
//					update.unset(f.getName());
//				}
//			} catch (IllegalAccessException e) {
//				log.error(
//						"Failed to create update with reflection in mongo utils, {}",
//						(Object) e.getStackTrace());
//			}
//		}
//		return update;
//	}
//
	public static Criteria getDealerIdFilterCriteria() {
		return new Criteria(TConstants.DEALER_ID).is(UserContextProvider.getCurrentDealerId());
	}
//
//	public static Criteria getNonDeletedDealerIdFilterCriteria() {
//		return new Criteria(TConstants.DEALER_ID).is(UserContextProvider.getCurrentDealerId())
//				.and(TConstants.DELETED).is(false);
//	}
//
//
//	public static Sort createMultiSort(List<TekSort> sorts){
//		if(TCollectionUtils.isNotEmpty(sorts)){
//			Sort sort = null;
//			for(TekSort tekSort : sorts){
//				if(sort == null){
//					sort = new Sort(Direction.fromString(tekSort.getOrder().name()), tekSort.getField());
//				}else{
//					sort = sort.and(new Sort(Direction.fromString(tekSort.getOrder().name()), tekSort.getField()));
//				}
//			}
//			return sort;
//		}
//		return new Sort(Direction.ASC, TConstants.ID);
//	}

}
