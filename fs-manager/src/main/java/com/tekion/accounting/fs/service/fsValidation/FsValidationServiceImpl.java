package com.tekion.accounting.fs.service.fsValidation;

import com.poiji.bind.Poiji;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.fsValidation.FsValidationRule;
import com.tekion.accounting.fs.common.exceptions.FSError;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.common.utils.OemFSUtils;
import com.tekion.accounting.fs.dto.cellcode.FsCellCodeDetailsResponseDto;
import com.tekion.accounting.fs.dto.cellcode.FsCodeDetail;
import com.tekion.accounting.fs.dto.fsValidation.FsValidationResult;
import com.tekion.accounting.fs.dto.fsValidation.FsValidationRuleDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.fsValidation.FsValidationRepo;
import com.tekion.accounting.fs.service.common.FileCommons;
import com.tekion.accounting.fs.service.common.parsing.ScriptParser;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import static com.tekion.accounting.fs.beans.common.OemTemplate.OEM_ID;
import static com.tekion.accounting.fs.common.TConstants.COUNTRY;
import static com.tekion.accounting.fs.common.TConstants.YEAR;
import static com.tekion.accounting.fs.service.utils.ExcelUtils.validateExcelFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class FsValidationServiceImpl implements FsValidationService{

	private final FsComputeService computeService;
	private final FSEntryRepo fsEntryRepo;
	private final FsValidationRepo fsValidationRepo;
	private final DealerConfig dealerConfig;
	private final FileCommons fileCommons;
	private final ScriptParser scriptParser;


	public static int FIRST_RECORD = 0;

	@Override
	public List<FsValidationRule> getRules(OEM oem, Integer year, String country) {
		if(Objects.isNull(country)){
			country = dealerConfig.getDealerCountryCode();
			log.info("fetching rules for {}", country);
		}
		return fsValidationRepo.getValidationRules(oem.name(), Collections.singletonList(year), country);
	}

	@Override
	public List<FsValidationRule> importRules(String mediaId, String oemId, String country, Integer year) {
		File file = null;
		List<FsValidationRule> validRules;
		try{
			log.info("importRules using mediaId {}",mediaId);
			file = fileCommons.downloadFileUsingMediaId(mediaId);
			validateExcelFile(file);
			validRules = importRules(file, oemId, country, year);
		}catch (Exception e){
			log.error("import FSValidation rules failed with error: ", e);
			throw new TBaseRuntimeException(FSError.uploadValidExcelFile);
		}finally {
			if(Objects.nonNull(file)) {
				file.delete();
			}
		}
		return validRules;
	}

	@Override
	public List<FsValidationRule> save(List<FsValidationRuleDto> rulesDto) {
		if(TCollectionUtils.isEmpty(rulesDto)){
			return null;
		}

		validateRules(rulesDto);
		return saveValidRules(rulesDto);
	}

	@Override
	public void delete(List<String> ids) {
		fsValidationRepo.delete(ids);
	}

	@Override
	public void deleteAll(String oemId, Integer year, String country) {
		fsValidationRepo.remove(oemId, year, country);
	}

	@Override
	public List<FsValidationResult> validateFs(String fsId, long tillEpoch, boolean includeM13, boolean addM13BalInDecBalances) {
		FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(fsId, UserContextProvider.getCurrentDealerId());
		List<FsValidationRule> rules = fsValidationRepo.getValidationRules(fsEntry.getOemId(),
				Collections.singletonList(fsEntry.getYear()),  dealerConfig.getDealerCountryCode());
		if(TCollectionUtils.isEmpty(rules)){
			return null;
		}
		FsCellCodeDetailsResponseDto cellCodeDetails = computeService.computeFsCellCodeDetails(fsEntry, tillEpoch, includeM13, addM13BalInDecBalances);
		return applyRules(rules, cellCodeDetails);
	}

	@Override
	public Integer copyRules(String oemId, String country, Integer fromYear, Integer toYear){
		List<FsValidationRule> rules = fsValidationRepo.getValidationRules(oemId, Arrays.asList(fromYear, toYear),  country);
		List<FsValidationRule> fromRules = rules.stream().filter(x -> x.getYear().equals(fromYear)).collect(Collectors.toList());
		List<FsValidationRule> toRules = rules.stream().filter(x -> x.getYear().equals(toYear)).collect(Collectors.toList());
		if(TCollectionUtils.isEmpty(fromRules) || TCollectionUtils.isNotEmpty(toRules)){
			throw new TBaseRuntimeException("cannot copy rules");
		}
		fromRules.forEach(x -> {
			x.setYear(toYear);
			x.setId(null);
			x.setCreatedTime(System.currentTimeMillis());
			x.setModifiedTime(System.currentTimeMillis());
		});

		return fsValidationRepo.bulkUpsert(fromRules).getUpserts().size();
	}


	List<FsValidationRule> saveValidRules(List<FsValidationRuleDto> rulesDto){
		if(TCollectionUtils.isEmpty(rulesDto)){
			log.error("no rule is valid to save in db");
			return new ArrayList<>();
		}
		FsValidationRuleDto fsValidationRuleDto = rulesDto.get(FIRST_RECORD);
		String oemId = fsValidationRuleDto.getOemId();
		Integer year = fsValidationRuleDto.getYear();
		String country = fsValidationRuleDto.getCountry();
		validateMandatoryFields(OEM_ID, oemId);
		validateMandatoryFields(YEAR, year.toString());
		validateMandatoryFields(COUNTRY, country);
		List<FsValidationRule> rules = rulesDto.stream().map(x -> x.toFsValidationRule(oemId, year, country)).collect(Collectors.toList());
		fsValidationRepo.bulkUpsert(rules);
		return rules;
	}

	private void validateMandatoryFields(String field, String value){
		switch (field){
			case OEM_ID:
				if(Objects.isNull(value)){
					throw new TBaseRuntimeException(FSError.invalidOemId);
				}
				break;
			case YEAR:
				if(Objects.isNull(value)){
					throw new TBaseRuntimeException(FSError.invalidYear);
				}
				break;
			case COUNTRY:
				if(Objects.isNull(value)){
					throw new TBaseRuntimeException(FSError.invalidCountry);
				}
				break;
			default:
				log.error("invalid MandatoryField!");
		}
	}

	private List<FsValidationResult> applyRules(List<FsValidationRule> rules, FsCellCodeDetailsResponseDto cellCodeDetails) {
		Map<String, FsCodeDetail> codeVsDetailsMap = cellCodeDetails.getCodeVsDetailsMap();
		Map<String, String> cellCodeVsOemCode = cellCodeDetails.getAccountingOemFsCellCodes()
				.stream().filter(x -> Objects.nonNull(x.getOemCode())).collect(Collectors
						.toMap(AccountingOemFsCellCode::getCode, AccountingOemFsCellCode::getOemCode));
		Map<String, String> oemCodeVsCellCode = cellCodeVsOemCode.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (a, b) -> a));

		Map<String, BigDecimal> oemCodeVsValue = new HashMap<>();
		for(String cellCode: cellCodeVsOemCode.keySet()){
			FsCodeDetail detail = codeVsDetailsMap.get(cellCode);
			if(Objects.nonNull(detail) && Objects.nonNull(detail.getValue())){
				oemCodeVsValue.put(cellCodeVsOemCode.get(cellCode), detail.getValue());
			}else{
				log.warn("something wrong with {} cellCode detail {}", cellCode, detail);
			}
		}

		List<FsValidationResult> failedRules = new ArrayList<>();

		for(FsValidationRule rule: rules){
			String expression = OemFSUtils.getExpressionReplacedByValues(rule.getExpression(), oemCodeVsValue);
			Boolean rulePassed = null;
			try {
				 rulePassed = (Boolean) scriptParser.eval(expression);
			} catch ( Exception e) {
				log.error("error with this expression {}", expression);
				log.error("", e);
			}
			FsValidationResult validationResult = rule.getValidationResult(oemCodeVsCellCode.get(rule.getOemCode()), rule);
			if(Objects.nonNull(rulePassed) && !rulePassed){
				failedRules.add(validationResult);
			}
		}

		return failedRules;
	}

	private List<FsValidationRule> importRules(File file, String oemId, String country, Integer year) {
		List<FsValidationRuleDto> rulesDto = TCollectionUtils.nullSafeList(Poiji.fromExcel(file, FsValidationRuleDto.class));
		List<FsValidationRuleDto> validDtos = validateRules(rulesDto);
		fillRulesWithMissingData(validDtos, oemId, country, year);
		return saveValidRules(validDtos);
	}

	private void fillRulesWithMissingData(List<FsValidationRuleDto> validDtos, String oemId, String country, Integer year) {
		FsValidationRuleDto dto = validDtos.get(FIRST_RECORD);
		dto.setOemId(oemId);
		dto.setCountry(country);
		dto.setYear(year);
	}

	private List<FsValidationRuleDto> validateRules(List<FsValidationRuleDto> rulesDto) {
		List<FsValidationRuleDto> errorRules= new ArrayList<>();
		Map<String, BigDecimal> oemCodeVsValue = new HashMap<>();
		for(FsValidationRuleDto dto: rulesDto){
			oemCodeVsValue.put(dto.getOemCode(), BigDecimal.ZERO);
		}
		for(FsValidationRuleDto rule: rulesDto){
			String expression = OemFSUtils.getExpressionReplacedByValues(rule.getExpression(), oemCodeVsValue);
			boolean validExpression = true;
			try {
				scriptParser.eval(expression);
			} catch (Exception e) {
				log.error("error with this expression: {}", rule.getExpression());
				validExpression = false;
			}

			if(validExpression) errorRules.add(rule);
		}
		return errorRules;
	}

}
