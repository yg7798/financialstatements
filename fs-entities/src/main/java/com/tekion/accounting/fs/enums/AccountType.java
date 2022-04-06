package com.tekion.accounting.fs.enums;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tekion.accounting.commons.utils.LocaleUtils;
import com.tekion.as.models.beans.GLAccount;
import com.tekion.core.utils.TStringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.tekion.core.utils.TStringUtils.isBlank;

@Getter
@Slf4j
@RequiredArgsConstructor
public enum AccountType {
	ASSET("A", "Asset", false, "accountType.asset"),
	COST_OF_SALE("C", "Cost of Sale", false, "accountType.costOfSale"),
	OPERATING_EXPENSE("E", "Operating Expense", false, "accountType.operating.expense"),
	NON_OPERATING_INCOME("I", "Non Operating Income", true, "accountType.non.operating.income"),
	LIABILITY("L", "Liability", true, "accountType.liability"),
	MEMO("M", "Memo", false, "accountType.memo"),//bookkeeping TBD
	EXPENSE_ALLOCATION("N", "Expense Allocation", false, "accountType.expense.allocation"),
	EQUITY("Q", "Equity", true, "accountType.equity"),//bookkeeping TBD
	SALE("S", "Sale", true, "accountType.sale"),
	NON_OPERATING_EXPENSE("X", "Non Operating Expense", false, "accountType.non.operating.expense");

	private final String typeIdentifier;
	private final String displayValue;
	private final boolean negativeBookKeeping;
	private final String displayKey;


	public static AccountType fromTypeIdentifier(String typeIdentifier) {
		if (isBlank(typeIdentifier)) {
			return null;
		}
		return Arrays.stream(values())
				.filter(elem -> elem.typeIdentifier.equalsIgnoreCase(typeIdentifier))
				.findFirst()
				.orElse(null);
	}
	private static final AccountType[] accountTypes= AccountType.values();
	private static final Map<String,AccountType> accountTypeCIToAccountTypeMap= getMap();

	private static Set<String> incomeAccountList = Collections.unmodifiableSet(Sets.newHashSet(AccountType.COST_OF_SALE.toString(),AccountType.SALE.toString(),AccountType.OPERATING_EXPENSE.toString(),
			AccountType.NON_OPERATING_EXPENSE.toString(),AccountType.NON_OPERATING_INCOME.toString()));

	private static Set<String> aleAccountTypeList = Collections.unmodifiableSet(Sets.newHashSet(AccountType.ASSET.toString(),AccountType.LIABILITY.toString(),AccountType.EQUITY.toString()));


	public static String getDisplayValueForAccountType(GLAccount glAccount) {
		AccountType controlType=null;
		if(glAccount.getControlField()!=null) {
			controlType = AccountType.valueOf(glAccount.getAccountTypeId());
		}
		if(controlType!=null){
			return controlType.getTypeIdentifier()+" - " +controlType.getDisplayValue();
		}
		return  "";
	}

	public static AccountType getAccountType_CI(String accountType_CI){

		return accountTypeCIToAccountTypeMap.get(TStringUtils.nullSafeString(accountType_CI).toLowerCase());
	}


	public static Set<String> getIncomeAccounts(){
		return incomeAccountList;
	}
	public static Set<String> getAleAccountTypeList(){
		return aleAccountTypeList;
	}


	private static Map<String, AccountType> getMap() {
		Map<String, AccountType> mapToReturn= Maps.newHashMap();
		for (AccountType accountType : accountTypes) {
			mapToReturn.put(accountType.name().toLowerCase(),accountType);
		}
		return mapToReturn;
	}

	public static String getDisplayName(String accountType){
		if(TStringUtils.isBlank(accountType))
			return "";
		try{
			return LocaleUtils.translateLabel(AccountType.valueOf(accountType).getDisplayKey());
		}catch(Exception e){
			log.error("Exception while getting display name for accountType : {} ", accountType, e);
		}
		return "";
	}

}
