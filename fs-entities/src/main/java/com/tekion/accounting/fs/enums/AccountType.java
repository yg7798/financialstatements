package com.tekion.accounting.fs.enums;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tekion.as.models.beans.GLAccount;
import com.tekion.core.utils.TStringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.tekion.core.utils.TStringUtils.isBlank;

@Getter
@RequiredArgsConstructor
public enum AccountType {
	ASSET("A", "Asset", false),
	COST_OF_SALE("C", "Cost of Sale", false),
	OPERATING_EXPENSE("E", "Operating Expense", false),
	NON_OPERATING_INCOME("I", "Non Operating Income", true),
	LIABILITY("L", "Liability", true),
	MEMO("M", "Memo", false),//bookkeeping TBD
	EXPENSE_ALLOCATION("N", "Expense Allocation", false),
	EQUITY("Q", "Equity", true),//bookkeeping TBD
	SALE("S", "Sale", true),
	NON_OPERATING_EXPENSE("X", "Non Operating Expense", false);

	private final String typeIdentifier;
	private final String displayValue;
	private final boolean negativeBookKeeping;


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

}
