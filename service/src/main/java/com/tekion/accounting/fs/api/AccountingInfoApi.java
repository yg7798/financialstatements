package com.tekion.accounting.fs.api;

import com.tekion.accounting.commons.annotations.DataUpdateApiForFeature;
import com.tekion.accounting.commons.annotations.PodGlobalApi;
import com.tekion.accounting.fs.common.GlobalService;
import com.tekion.accounting.fs.dto.accountingInfo.AccountingInfoDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoService;
import com.tekion.accounting.fs.util.ApiHelperUtils;
import com.tekion.core.service.api.TResponseEntityBuilder;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/accountingInfo")
@RequiredArgsConstructor
public class AccountingInfoApi {

	private final AccountingInfoService service;
	private final GlobalService globalService;

	@GetMapping("/")
	public ResponseEntity getAccountingInfo(){
		return  TResponseEntityBuilder.okResponseEntity(service.find(UserContextProvider.getCurrentDealerId()));
	}

	@PostMapping("/createOrUpdate")
	public ResponseEntity createOrUpdateAccountingInfo(@RequestBody AccountingInfoDto dto) {
		return TResponseEntityBuilder.okResponseEntity(service.saveOrUpdate(dto));
	}

	@DeleteMapping("/delete")
	public ResponseEntity deleteAccountingInfo() {
		return TResponseEntityBuilder.okResponseEntity(service.delete(UserContextProvider.getCurrentDealerId()));
	}

	@PostMapping("/populateOEMFields")
	public ResponseEntity populateOEMFields() {
		return TResponseEntityBuilder.okResponseEntity(service.populateOEMFields());
	}

	@PostMapping("/addOem/{oem}")
	public ResponseEntity addOem(@PathVariable @NotBlank OEM oem) {
		return TResponseEntityBuilder.okResponseEntity(service.addOem(oem));
	}

	@PostMapping("/removeOem/{oem}")
	public ResponseEntity removeOem(@PathVariable @NotBlank OEM oem) {
		return TResponseEntityBuilder.okResponseEntity(service.removeOem(oem));
	}

	@PostMapping("/setPrimaryOem/{oem}")
	public ResponseEntity setPrimaryOem(@PathVariable @NotBlank OEM oem) {
		return TResponseEntityBuilder.okResponseEntity(service.setPrimaryOem(oem));
	}

	@PostMapping("/migrateFsRoundOffOffset")
	public ResponseEntity migrateFsRoundOffOffset() {
		service.migrateFsRoundOffOffset();
		return TResponseEntityBuilder.okResponseEntity("success");
	}


	@DataUpdateApiForFeature
	@PodGlobalApi
	@PostMapping("/populateOEMFields/all")
	public ResponseEntity populateOEMFields(@RequestParam(required = false) Integer parallelism){
		globalService.executeTaskForAllDealers(service::populateOEMFields, ApiHelperUtils.getDefaultParallelism(parallelism));
		return TResponseEntityBuilder.okResponseEntity("Success");
	}

	@PodGlobalApi
	@PostMapping("/accountingInfo/migrateFsRoundOffOffset/all")
	public ResponseEntity migrateFsRoundOffOffset(@RequestParam(required = false) Integer parallelism){
		globalService.executeTaskForAllDealers(service::migrateFsRoundOffOffset, ApiHelperUtils.getDefaultParallelism(parallelism));
		return TResponseEntityBuilder.okResponseEntity("Success");
	}

	@PutMapping("/update/siteInfoOverrideFlag/{flag}")
	public ResponseEntity updateSiteOverrideInfoFlag(@PathVariable boolean flag){
		service.updateSiteOverrideInfoFlag(flag);
		return TResponseEntityBuilder.okResponseEntity("success");
	}
}
