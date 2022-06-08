package com.tekion.accounting.fs.common;

//import com.fasterxml.jackson.core.JsonPointer;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.TextNode;

import com.tekion.core.properties.TekionCommonProperties;
import org.apache.poi.ss.usermodel.BuiltinFormats;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TConstants {
	public static final String SETTER_BOOLEAN_CONSTANT = "is";
	public static final String SETTER_CONSTANT = "set";
	public static final String SETTER_FIELD_DELIMITTER = "_";
	public static final String DEFAULT_DELIMITER_FOR_CLUBBING_DESCRIPTION = " - ";
	public static final String TENANT_DEFAULT = "TENANT_DEFAULT";
	public static final String ACCOUNTING_DEFAULT = "ACCOUNTING_DEFAULT";
	public static final String ACCOUNTING_FS_CELLCODE = "ACCOUNTING_FS_CELLCODE";
	public static final String ACC_MAIL_SERVICE_MODULE_NAME = "ACCOUNTING_SERVICE";

	public static final String YEAR = "year";
	public static final String MONTH = "month";
	public static final String STATUS = "status";
	public static final String POST_AHEAD = "POST_AHEAD";
	public static final String CODE = "code";

	public static final String ACCOUNTING = "ACCOUNTING";
	public static final String ACCOUNTING_MIGRATION_SERVICENAME = "ACCOUNTINGMIGRATION";
	public static final String ACCOUNTING_SMALL_CASE = "accounting";
	public static final String PAYROLL = "PAYROLL";

	public static final String KEY = "key";

	public static final String ES_MONTH_INTERVAL = "1M";
	public static final String ES_DAY_INTERVAL = "1d";
	public static final String ES_MILLI_SEC_FORMAT = "epoch_millis";

	public static final int DEFAULT_COPY_NUM = 1;

	public static final String FLOW_TYPE = "flowType";

	//for pdf export
	public static final String PDF_EXPORT_URL = "/exports/pdf-v2/";
	public static final String PDF_ASSET_TYPE = "ACCOUNTING";
	public static final String PDF_CONTENT_TYPE = "application/pdf";

	//Common Fields
	public static final String ID = "id";
	public static final String GLACCOUNT_ID = "glAccountId";
	public static final String DELETED = "deleted";
	public static final String CREATED_TIME = "createdTime";
	public static final String MODIFIED_TIME = "modifiedTime";
	public static final String CREATED_BY_USER_ID = "createdByUserId";
	public static final String MODIFIED_BY_USER_ID = "modifiedByUserId";
	public static final String MIGRATED = "migrated";
	public static final String MIGRATION_TIME = "migrationTime";
	public static final String MIGRATED_FROM = "migratedFrom";
	public static final String DEALER_ID = "dealerId";
	public static final String SITE_ID = "siteId";
	public static final String KEY_META_DATA_REF_SITE_ID = "refSiteId";
	public static final String IS_ACTIVE = "isActive";
	public static final String EMPTY = "empty";
	public static final String S3_KEY = "S3";
	public static final String DEFAULT_KEY = "DEFAULT";
	public static final String VENDOR = "VENDOR";
	public static final String NOT_AVAILABLE = "N/A";
	public static final String ALL = "ALL";
	public static final String VOIDED = "VOIDED";
	public static final String SUCCESS = "SUCCESS";
	public static final String FAILED = "FAILED";
	public static final String SYSTEM_USER = "-1";
	public static final String INACTIVE = "inactive";
	public static final int CHARACTER_LIMIT = 255;
	public static final int REF_TEXT_CHAR_LIMIT =192;
	public static final String X_API_KEY = "x-api-key";
	public static final String TEMP_URL_WHEN_CONFIG_NOT_PRESENT = "https://abc/";
	public static final String INCLUDED = "included";
	public static final String CASH_RECEIPT_NUMBER = "cashReceiptNumber";
	public static final String IN_ACTIVE_STATUS = "inactive";
	public static final String ACTIVE_STATUS  = "active";
	public static final String SOURCE_TYPE  = "sourceType";
	public static final String COUNTRY_US = "US";
	public static final String COUNTRY = "country";
	public static final String INTERNAL_PAY_TYPE = "INTERNAL";
	public static final String LOCALE = "locale";

	public static final String SERVICE_NAME_ACCOUNTING = "ACCOUNTING";
	public static final String SERVICE_NAME_FINANCIAL_STATEMENTS = "FINANCIAL-STATEMENTS";

	public static final String DEPT_TYPE_CUSTOM_FIELD_OPTION = "department_type";

	public static final String KEYWORD_CONFIG_GLOBAL_ASSET_TYPE = "GLOBAL";

	public static final String KENSTON_TENANT_NAME = "kentsoncarcompany";
	public static final String KENSTON_DEALER_ID = "601";

	public static final List<String> NON_UPDATABLE_COLUMNS = Arrays.asList(TConstants.ID,
			TConstants.CREATED_BY_USER_ID, TConstants.CREATED_TIME, TConstants.DEALER_ID);

	//Regex
	public static final String REGEX_ALPHA_NUMERIC =   "[a-zA-Z0-9]*";
	public static final String REGEX_ALPHA_NUMERIC_AND_UNDERSCORE =   "[a-zA-Z0-9_]*";

	//sync status
	public static final String FALSE =   "false";
	public static final String TRUE =   "true";
	public static final String ZERO_STRING =   "0";



	//Sequence Names
	public static final String TRANSACTION_ID_SEQ = "transaction_id_seq";
	public static final String TRANSACTION_NUM_SEQ = "transaction_num_seq";
	public static final String GLPOSTING_ID_SEQ = "glposting_id_seq";
	public static final String CASH_RECEIPT_NUM_SEQ = "cash_receipt_num_seq";
	public static final String COUNT_ADJUSTMENT_NUM_SEQ = "count_adjustment_num_seq";
	public static final String CONTROLBOOK_NUM_SEQ = "control_book_num_seq";
	public static final String CHECK_NO_PREFIX ="check_num_seq_%s";
	public static final String EFT_NO_PREFIX ="eft_num_seq_%s";

	public static final String TRANSIENT_POSTINGS = "transient_posting_id_seq";
	public static final String CASHIER_DEPOSIT_ID_SEQ = "cashier_deposit_id_seq";

	//Lookup names
	public static final String LOOKUP_FAIL_ERROR = "Failed to fetch!";
	//public static final TextNode ERROR_JSON_NODE = TextNode.valueOf(LOOKUP_FAIL_ERROR);
	public static final String TENANT_ID_KEY = "tenantId";
	//public static final ObjectMapper MAPPER = new ObjectMapper();
	public static final String DEALER_ID_KEY = "dealerId";
	public static final String ENTITIES_KEY = "entities";
	public static final String COUNT_KEY ="count";
	public static final String COMPUTED_COUNT = "computedCount";
	public static final String REQUEST_ID_LOG = " RequestId: {} ";
	public static final String REQUEST_ID_KEY = "requestId";
	public static final String CORRELATION_ID_KEY = "correlationId";
	public static final String NULL_STRING = "null";
	public static final String BLANK_STRING = "";
	public static final String HYPHEN = "-";


//
//	public static final JsonPointer SEARCH_TEXT_POINTER = JsonPointer.compile("/searchText");
//	public static final JsonPointer PAGE_INFO_START_POINTER = JsonPointer.compile("/pageInfo/start");
//	public static final JsonPointer PAGE_INFO_ROWS_POINTER = JsonPointer.compile("/pageInfo/rows");
//	public static final JsonPointer SORT_POINTER = JsonPointer.compile("/sort");
//	public static final JsonPointer SORT_ORDER_POINTER = JsonPointer.compile("/order");
//	public static final JsonPointer SORT_FIELD_POINTER = JsonPointer.compile("/field");

	//for locks
	public static final String BULK_DRAFT="BULKDRAFT";


	public static final int CLIENT_EXECUTION_TIMEOUT = 100000;
	public static final String ATHENA_DEFAULT_DATABASE = "cacargroup_tdp_models_poc";
	public static final String ATHENA_TK_NONPROD_DATABASE = "tst1_tdp_cacargroup_models";
	public static final int SLEEP_AMOUNT_IN_MS = 1000;
	public static final String ATHENA_OUTPUT_BUCKET =
			"s3://xyz-tekion-data-com.tekion.com.tekion.as.migration";

	public static final String ATHENA_OUTPUT_BUCKET_1 = "s3://aws-athena-query-results-116442029361-us-west-2/tst-accounting-migration/";

	//    public static final String ATHENA_SAMPLE_QUERY = "SELECT * FROM tekion_parts_master where
	// part_number not in
	//    (SELECT part_number FROM tekion_parts_master group by part_number having count(*) > 1) limit
	// 10;";
	public static final String ATHENA_GL_ACCOUNTS_QUERY =
			"SELECT * FROM \"cacargroup_tdp_models_poc\".\"tekion_acct_chart_of_accounts\";";
	public static final String ATHENA_JOURNALS_QUERY =
			"SELECT * FROM \"cacargroup_tdp_models_poc\".\"tekion_acct_journal_setup\" ;";
	public static final String ATHENA_TRANSACTION_QUERY =
			"SELECT * FROM \"cacargroup_tdp_models_poc\".\"tekion_acct_transaction_header\" ";
	public static final String ATHENA_TRANSACTION_QUERY_FOR_COMPANYID_10 =
			"SELECT * FROM \"cacargroup_tdp_models_poc\".\"tekion_acct_transaction_header\" WHERE \"source_company_id\" = 10 ";
	public static final String ATHENA_TRANSACTION_DETAIL_QUERY =
			"SELECT * FROM \"cacargroup_tdp_models_poc\".\"tekion_acct_transaction_detail\" ";

	public static final String ATHENA_GL_LEDGER_QUERY =
			"SELECT * FROM \"cacargroup_tdp_models_poc\".\"tekion_acct_gl_ledger\" WHERE \"fiscal_yr_end_date\" LIKE '2019%'  ";

	public static final String ATHENA_SALES_CHAIN_QUERY =
			"SELECT * FROM \"cacargroup_tdp_models_poc\".\"tekion_acct_gl_sales_chain\"   ";

	public static final String ATHENA_INVOICES_QUERY =
			"SELECT * FROM \"tst1_tdp_cacargroup_models\".\"acct_invoice\"   ";

	public static final String ATHENA_SERVICE_CUSTOMER_QUERY =
			"SELECT * FROM \"tst1_tdp_cacargroup_models\".\"service_customer\" WHERE \"dms_customer_type\" = 'AP Vendor'   ";

	public static final String ATHENA_TRANSACTION_HEADER_DETAIL_JOIN_QUERY =
			"SELECT * FROM \"cacargroup_tdp_models_poc\".\"tekion_acct_transaction_header\" JOIN \"cacargroup_tdp_models_poc\".\"tekion_acct_transaction_detail\" ON \"cacargroup_tdp_models_poc\".\"tekion_acct_transaction_header\".\"gl_transaction_header_id\" = \"cacargroup_tdp_models_poc\".\"tekion_acct_transaction_detail\".\"gl_transaction_header_id\" limit 100;";


	//S3
	public static final String S3_ACCOUNTING_TEMPLATE_PATH = "accounting-templates";

	public static final int MAX_CONTROL_BOOK = 5000;

	public static final String VERSION = "version";

	public static final String MASKED_VALUE = "***";

	public static final String OEM_CELL_MAPPING = "acOemCellMapp";
	public static final String PRECISION = "precision";


	//	public static final boolean IS_LOCAL = GeneralUtils.isLocalClusterType();
//
	//excelDataTypeFormats
	public static final String EXCEL_DATA_TYPE_GENERAL = BuiltinFormats.getBuiltinFormat(0);
	public static final String EXCEL_DATA_TYPE_PERCENT = BuiltinFormats.getBuiltinFormat(10);
	public static final String EXCEL_DATA_TYPE_0 = BuiltinFormats.getBuiltinFormat(1);
	public static final String EXCEL_DATA_TYPE_0_00 = BuiltinFormats.getBuiltinFormat(2);
	public static final String EXCEL_DATA_TYPE_M_D_YY = BuiltinFormats.getBuiltinFormat(BuiltinFormats.getBuiltinFormat("m/d/yy"));
	public static final String EXCEL_DATA_TYPE_h_m_aa = BuiltinFormats.getBuiltinFormat(BuiltinFormats.getBuiltinFormat("h:mm AM/PM"));
	public static final String EXCEL_DATA_TYPE_h_m_ss_aa = BuiltinFormats.getBuiltinFormat(BuiltinFormats.getBuiltinFormat("h:mm:ss AM/PM"));
	public static final String EXCEL_DATA_TYPE_MM_DD_YYYY = "mm/dd/yyyy";
	public static final String EXCEL_DATA_TYPE_MM_DD_YY = "mm/dd/yy";

	public static final int DEFAULT_BATCH_SIZE_FOR_EXCEL = 5000;
	public static final int DEFAULT_EXCEL_PLAIN_VIEW_LIMIT = 2_00_000;

	//date format
	public static final String DATE_FORMAT_DD_MM_YY_HH_MM_AA = "MM/dd/yy hh:mm aa";
	public static final String DATE_FORMAT_HH_MM_AA = "hh:mm aa";


	public static final String YTD_APPLICABLE = "ytdApplicable";
	public static final String MTD_APPLICABLE = "mtdApplicable";
	public static final String ACCOUNTING_MODULE = "ACCOUNTING_MODULE";
	public static final String CRITICAL_ERROR = "CRITICAL ERROR";
	public static final String AUTO_POSTING_FLOW_TYPE = "autoPostingFlowType";
	public static final String AUTO_POSTING_LINE_TYPE = "autoPostingLineType";
	public static final String SO_RETURN_TYPE = "SO_RETURN";
	public static final String TRANSACTION_TYPE = "transactionType";
	public static final String POSTINGS = "postings";
	public static final String USER_OVERRIDDEN_CONTROL = "userOverriddenControl";


	//excel
	public static final String INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME = "Inquiry Tool";
	public static final String CASHIERING= "Cashiering";
	public static final String OTHER_GL_POSTING= "Other GL Posting";
	public static final String GENERATED_DATE= "generatedDate";
	public static final String DEPARTMENT_ID_SALES = "SALES";

	//DP propertyIdentifiers
	public static final String CONTROL_BOOK_MAX_LIMIT_RESPONSE = "CONTROL_BOOK_MAX_LIMIT_RESPONSE";
	public static final String DEFAULT_EXCEL_EXPORT_MAX_LIMIT = "DEFAULT_EXCEL_EXPORT_MAX_LIMIT";
	public static final String INQUIRY_TOOL_MAX_LIMIT = "INQUIRY_TOOL_MAX_LIMIT";
	public static final String BANK_FEATURE_WORKING_STATUS = "BANK_FEATURE_WORKING_STATUS";
	public static final String SCHEDULE_AMOUNT_SORT_PROPERTY = "SCHEDULE_AMOUNT_SORT";
	public static final String SCHEDULE_AMOUNT_SORT_VALUE = "NORMAL";
	public static final String ACCOUNTING_SITE_OVERRIDE_ENABLED = "ACCOUNTING_SITE_OVERRIDE_ENABLED";
	public static final String ACCOUNTING_FORM1099 = "ACCOUNTING_FORM1099";

	//form8300
	public static final long FORM8300_THRESHOLD_AMOUNT_IN_CENTS = 10_000_00L;

	//file format
	public static final String XLSX_FILE_EXTENSION = ".xlsx";
	public static final String CSV_FILE_EXTENSION = ".csv";
	public static final List<String> TEXT_FILE_EXTENSIONS = Arrays.asList("csv","txt");
	public static final List<String> EXCEL_FILE_EXTENSIONS = Arrays.asList("xls","xlsx");
	public static final List<String> CSV_TXT_MEDIA_TYPE = Arrays.asList(".csv", "text/plain");
	public static final List<String> XL_MEDIA_TYPE = Arrays.asList("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel");

	//Audit log asset names
	public static final String ACCOUNTING_AP = "ACCOUNTING_AP";
	public static final String ACCOUNTING_AR = "ACCOUNTING_AR";
	public static final String ACCOUNTING_GL_ACCOUNT = "ACCOUNTING_GL_ACCOUNT";
	public static final String ACCOUNTING_GLOBAL_SETTING = "ACCOUNTING_GLOBAL_SETTING";
	public static final String BALANCE = "BALANCE";
	public static final String COUNT = "COUNT";
	public static final String INVOICE_BATCH_PAYMENT_LOG = "invoice-batch-payment-log";
	public static final String ASSET_GL_ACCOUNT = "GL_ACCOUNT";
	public static final String PDF_PRINT_SERVICE = "PDF_PRINT_SERVICE";
	public static final String FS_GROUP_CODE = "FS_GROUP_CODE";


	public static final String ACCOUNTS_CF_ASSET_TYPE = "accounts";
	public static final String UNDERSCORE_SEPARATOR = "_";

	/**
	 Date pattern can be derived from below class
	 *{@link java.text.DateFormatSymbols}
	 *
	 * date patterns
	 **/

	public static final String YYYY_MM_DD_hh_mm_ss_WITH_SEPARATOR = "yyyy-MM-dd'T'hh-mm-ss";
	public static final String MM_dd_yy = "MM/dd/yy";
	public static final String HH_mm_ss = "HH:mm:ss";
	public static final String MM_dd_yyyy = "MM/dd/yyyy";
	public static final String MMddyyyy = "MMddyyyy";
	public static final String MMddyy = "MMddyy";
	public static final String DATE_FORMAT_MM_DD = "MM-dd";
	public static final String M_D_YYYY = "M/d/yyyy";
	public static final String YYYY_MM_DD = "yyyy-MM-dd";
	public static final String SOURCE = "SOURCE";
	public static final String M_D_YY = "Mdyy";
	public static final String MM_DD_YYYY = "MM-dd-yyyy";
	public static final String GL_ACCOUNT_IDS = "glAccountIds";
	public static final String YY_MM_DD = "yyMMdd";
	public static final String HH_MM = "HHmm";
	public static final String HHMMSS = "HHmmss";


	//Thread pool
	public static final String ES_SYNC_ASYN_THREAD_POOL = "esSyncAsyncExecutor";
	public static final String CHECK_AND_1099_ASYNC_THREAD_POOL = "checkAnd1099AsyncExecutor";

	//SQL VARS
	public static final String BATCH_SIZE = "batchSize";
	public static final String MAX_LAST_BATCH_ID = "maxLastBatchId";
	public static final int SQL_BATCH_SIZE = 2000;

	public static final int BATCH_SIZE_3000 = 3000;

	public static final int MAX_ES_SUPPORTED_ROWS  =9999;
	public static final long MAX_ES_SUPPORTED_ROWS_LONG  =9999L;
	public static final int MIN_ES_START_VALUE = 0;
	public static final int ES_SINGLE_ROW = 1;


	//ES related
	public static final String DEFAULT_GROUP_BUCKET_NAME = "byGroupKey";
	public static final String TOP_HITS = "top_hits";
	public static final String GROUP_BY_SORT_FIELD = "_key";

	//logs
	public static final String CA_ISSUED_CHECK_LOGS = "CA_ISSUED_CHECK_LOGS";

	//TimeRange
	public static final int TIME_RANGE_SIZE = 2;
	public static final int MAX_NO_OF_DAYS_IN_MONTH = 31;

	//daily deposit
	public static final int DAILY_DEPOSIT_GL_POSTING_MAX_UNBATCHED_LIMIT = 10000;

	public static final long oneYearTime  = TimeUnit.DAYS.toMillis(365);
	public static final long oneMonthTime = TimeUnit.DAYS.toMillis(30);

	//bulk export
	public static final String URL_PARAMS = "urlParams";

	//numbers
	public static final int _0_INT = 0;
	public static final int _100_INT = 100;
	public static final int _MINUS_100_INT = -100;

	public static final int ES_BATCH_SIZE = 5000;

//	public static final Map<AssetType,String> assetTypeToSectionMappingForCashieringEvent = new HashMap<AssetType,String>() {{
//		put(AssetType.REPAIR_ORDER,CUSTOMER_PAY.name());
//		put(AssetType.PARTS_SALE,RETAIL.name());
//	}};

	public static final String ACCOUNTING_PDF_EXPORT_V2_CALLBACK = "/accounting/u/pdfExport/v2/callback/";
	//Recurring schedule
	public static final int LAST_DAY_OF_MONTH = 32;
	public static final int RECURRING_SCHEDULE_LAST_DAY_LIMIT = 28;

	public static final boolean IS_LOCAL = "local".equals(System.getenv(TekionCommonProperties.CLUSTER_TYPE));

}
