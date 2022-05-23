package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.accounting.fs.dto.cellGrouop.FSCellGroupCodeCreateDto;
import com.tekion.accounting.fs.dto.cellGrouop.FSCellGroupCodesCreateDto;
import com.tekion.accounting.fs.dto.cellcode.*;
import com.tekion.accounting.fs.dto.fsEntry.FsEntryCreateDto;
import com.tekion.accounting.fs.dto.mappings.MappingSnapshotDto;
import com.tekion.accounting.fs.dto.mappings.OemFsMappingUpdateDto;
import com.tekion.accounting.fs.dto.mappings.OemMappingRequestDto;
import com.tekion.accounting.fs.dto.oemTemplate.OemTemplateReqDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.accounting.fs.service.fsEntry.FsEntryService;
import com.tekion.accounting.fs.service.fsMapping.FsMappingService;
import com.tekion.accounting.fs.service.printing.FSPrintService;
import com.tekion.accounting.fs.service.printing.models.FSViewStatementDto;
import com.tekion.core.service.api.TResponseEntityBuilder;
import com.tekion.core.validation.TValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/oemMapping")
public class  OEMMappingApi {

	private final FsComputeService oemFSMappingService;
	private final TValidator validator;
	private final FsEntryService fsEntryService;
	private final FSPrintService fsPrintService;
	private final FsMappingService fsMappingService;

	/********************* OEM Mapping Apis ***************************/

	@PostMapping("/save")
	public ResponseEntity saveMapping(@RequestBody OemMappingRequestDto oemMappingRequestDto){
		oemFSMappingService.saveMapping(oemMappingRequestDto);
		return TResponseEntityBuilder.okResponseEntity(true);
	}

	@GetMapping("/{oemId}/{dealerId}/{year}")
	public ResponseEntity fetchOEMMapping(@PathVariable @NotNull OEM oemId, @PathVariable String dealerId, @PathVariable String year){
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.getOEMMappingByDealerId(oemId, dealerId, year));
	}

	@PutMapping("/glAccountMapping")
	public ResponseEntity updateMapping(@RequestBody OemFsMappingUpdateDto updateDto){
		validator.validate(updateDto);
		return TResponseEntityBuilder.okResponseEntity(fsMappingService.updateOemFsMapping(updateDto));
	}

	@GetMapping("/glAccountMapping/{fsId}")
	public ResponseEntity getAcctMapping(@PathVariable @NotBlank String fsId){
		log.info("api call received for glAccountMapping");
		return TResponseEntityBuilder.okResponseEntity(fsMappingService.getOemFsMapping(fsId));
	}

	/********************* Old FSEntry related apis ***********************/


	@PostMapping("/fsMappingInfo")
	public ResponseEntity createFsMappingInfo(@RequestBody FsEntryCreateDto reqDto){
		validator.validate(reqDto);
		return TResponseEntityBuilder.okResponseEntity(fsEntryService.createFSEntry(reqDto));
	}

	@GetMapping("/fsMappingInfo/{oemId}")
	public ResponseEntity fsMappingInfo(@PathVariable  OEM oemId, @RequestParam(required = false, name = "siteId") String siteId){
		return TResponseEntityBuilder.okResponseEntity(fsEntryService.getFSEntry(oemId.name(), siteId));
	}

	@GetMapping("/fsMappingInfos")
	public ResponseEntity getFsMappingInfos(){
		return TResponseEntityBuilder.okResponseEntity(fsEntryService.getAllFSEntries());
	}

	/**************** FS group codes and cell codes related apis ***********************/

	@PostMapping("/fsCellGroup")
	public ResponseEntity saveFsCellGroup(@RequestBody FSCellGroupCodeCreateDto reqDto){
		validator.validate(reqDto);
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.saveFsCellGroupCode(reqDto));
	}

	@PostMapping("/fsCellGroups")
	public ResponseEntity saveFsCellGroups(@RequestBody FSCellGroupCodesCreateDto reqDto){
		validator.validate(reqDto);
		oemFSMappingService.saveFsCellGroupCodes(reqDto);
		return TResponseEntityBuilder.okResponseEntity("done");
	}

	@PutMapping("/fsCellGroups")
	public ResponseEntity upsertFsCellGroups(@RequestBody FSCellGroupCodesCreateDto reqDto){
		validator.validate(reqDto);
		oemFSMappingService.upsertFsCellGroupCodes(reqDto);
		return TResponseEntityBuilder.okResponseEntity("done");
	}

	@GetMapping("/fsCellGroups/{oemId}/{year}/{version}")
	public ResponseEntity fetchFsCellGroup(@PathVariable @NotNull OEM oemId, @PathVariable @NotBlank Integer year, @PathVariable @NotBlank Integer version){
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.fetchFsCellGroupCodes(oemId.name(), year, version));
	}

	@PutMapping("/migrateFsCellCodesFromGroup/{oemId}")
	public ResponseEntity migrateFsCellCodesFromGroup(@PathVariable @NotNull OEM oemId){
		oemFSMappingService.migrateFsCellCodesFromGroup(oemId);
		return TResponseEntityBuilder.okResponseEntity("done");
	}


	@PutMapping("/populateFsCellGroupCodes/{oemId}/{year}/v/{version}")
	public ResponseEntity populateFsCellGroupCodes(@PathVariable @NotNull OEM oemId,@PathVariable @NotBlank Integer year, @PathVariable @NotBlank Integer version){
		oemFSMappingService.populateGroupCodesInFsCell(oemId,year,version);
		return TResponseEntityBuilder.okResponseEntity("done");
	}

	@PostMapping("/fsCellCode")
	public ResponseEntity  fsCellCodeObject(@RequestBody FSCellCodeCreateDto reqDto){
		validator.validate(reqDto);
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.saveFsCellCode(reqDto));
	}

	@PostMapping("/fsCellCodes")
	public ResponseEntity fsCellCodes(@RequestBody FSCellCodeListCreateDto reqDto){
		validator.validate(reqDto);
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.saveFsCellCodes(reqDto));
	}

	@GetMapping("/oemFsCellCodes/{oemId}/{year}/{version}")
	public ResponseEntity getOemTMappingList(
			@RequestParam(required = false, name = "readFromCache") boolean readFromCache,
			@RequestParam(required = false, name = "countryCode") String countryCode,
			@PathVariable @NotNull OEM oemId, @PathVariable @NotBlank Integer year, @PathVariable @NotBlank Integer version){
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.getOemTMappingList(oemId.name(), year, version, countryCode, readFromCache));
	}


//  @GetMapping("/fsCellCodeDetails/{oemId}/{oemFsYear}/{oemFsVersion}")
//  public ResponseEntity computeFsCellCodeDetails( @RequestParam(required = false, name = "month") Integer month,
//                                                  @RequestParam(required = false, name = "year") Integer year,
//                                                  @PathVariable @NotNull OEM oemId,
//                                                  @PathVariable @NotBlank Integer oemFsYear,
//                                                  @PathVariable @NotBlank Integer oemFsVersion,
//                                                  @RequestParam (required = false, defaultValue = "false") boolean includeM13,
//                                                  @RequestParam (name = "addM13BalInDecBalances" , required = false, defaultValue = "false") boolean addM13BalInDecBalances){
//    log.info("api call received for fsCellCodeDetails");
//    return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.computeFsCellCodeDetails(oemId.name(), oemFsYear, oemFsVersion, year, month, includeM13,
//            UserContextUtils.getSiteIdFromUserContext() , addM13BalInDecBalances));
//  }

	@GetMapping("/fsCellCodeDetails/{oemId}/{oemFsYear}/version/{oemFsVersion}")
	public ResponseEntity computeFsCellCodeDetails1(@RequestParam(required = false, name = "month") Integer month,
													@RequestParam(required = false, name = "year") Integer year,
													@PathVariable @NotNull OEM oemId,
													@PathVariable @NotBlank Integer oemFsYear,
													@PathVariable @NotBlank Integer oemFsVersion,
													@RequestParam (required = false, defaultValue = "false") boolean includeM13,
													@RequestParam (name = "addM13BalInDecBalances" , required = false, defaultValue = "false") boolean addM13BalInDecBalances
	){
		log.info("api call received for fsCellCodeDetails version");
		return TResponseEntityBuilder.okResponseEntity(
				oemFSMappingService.computeFsCellCodeDetails(oemId.name(), oemFsYear, oemFsVersion, year, month, includeM13, UserContextUtils.getSiteIdFromUserContext(), addM13BalInDecBalances));
	}

	/******** API currently used by frontend **********/

	@GetMapping("/fsCellCodeDetails/{fsId}/till/{tillEpoch}")
	public ResponseEntity computeFsCellCodeDetailsTillEpoch(@PathVariable @NotNull String fsId, @PathVariable @NotBlank Long tillEpoch,
															@RequestParam (required = false, defaultValue = "true") boolean includeM13,
															@RequestParam (name = "addM13BalInDecBalances" , required = false, defaultValue = "false") boolean addM13BalInDecBalances){
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.computeFsCellCodeDetailsByFsId(fsId, tillEpoch, includeM13,addM13BalInDecBalances));
	}

//  @GetMapping("/fsCellCodeDetails/site/{siteId}/{oemId}/{oemFsYear}/version/{oemFsVersion}/till/{tillEpoch}")
//  public ResponseEntity computeFsCellCodeDetailsTillEpochBySite(@PathVariable @NotNull String siteId,
//                                                          @PathVariable @NotNull OEM oemId, @PathVariable @NotBlank Integer oemFsYear,
//                                                          @PathVariable @NotBlank Integer oemFsVersion, @PathVariable @NotBlank Long tillEpoch,
//                                                          @RequestParam (required = false, defaultValue = "true") boolean includeM13,
//                                                                @RequestParam (name = "addM13BalInDecBalances" , required = false, defaultValue = "false") boolean addM13BalInDecBalances){
//    return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.computeFsCellCodeDetails(oemId.name(), oemFsYear, oemFsVersion, tillEpoch, includeM13, siteId, addM13BalInDecBalances));
//  }

	// getting Financial report according to Fiscal Year start month
	@GetMapping("/cellCodeDetailsByFYStartMonth/{fsId}/till/{tillEpoch}")
	public ResponseEntity computeFsCellCodeDetailsTillEpochForFS(@PathVariable @NotNull String fsId, @PathVariable @NotBlank Long tillEpoch,
																 @RequestParam (required = false, defaultValue = "true") boolean includeM13 ){
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.computeFsCellCodeDetailsForFS(fsId, tillEpoch, includeM13));
	}

//  @GetMapping("/cellCodeDetailsByFYStartMonth/site/{siteId}/{oemId}/{oemFsYear}/version/{oemFsVersion}/till/{tillEpoch}")
//  public ResponseEntity computeFsCellCodeDetailsTillEpochForFSBySite(@PathVariable @NotNull String siteId,
//                                                                @PathVariable @NotNull OEM oemId,
//                                                               @PathVariable @NotBlank Integer oemFsYear,
//                                                               @PathVariable @NotBlank Integer oemFsVersion, @PathVariable @NotBlank Long tillEpoch,
//                                                               @RequestParam (required = false, defaultValue = "true") boolean includeM13){
//    return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.computeFsCellCodeDetailsForFS(oemId.name(), oemFsYear, oemFsVersion, tillEpoch, includeM13, siteId));
//  }

	@GetMapping("/external/fsDetails/{oemId}/{oemFsYear}/version/{oemFsVersion}/till/{tillEpoch}")
	public ResponseEntity computeFsGroupCodeDetailsTillEpoch(@PathVariable @NotNull OEM oemId, @PathVariable @NotBlank Integer oemFsYear,
															 @PathVariable @NotBlank Integer oemFsVersion, @PathVariable @NotBlank Long tillEpoch,
															 @RequestParam (required = false, defaultValue = "true") boolean includeM13,
															 @RequestParam (name = "addM13BalInDecBalances" , required = false, defaultValue = "false") boolean addM13BalInDecBalances){
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.computeFsGroupCodeDetails(oemId.name(), oemFsYear, oemFsVersion, tillEpoch, includeM13, addM13BalInDecBalances, UserContextUtils.getSiteIdFromUserContext()));
	}

	@GetMapping("/external/fsDetails/{oemId}/{oemFsYear}/version/{oemFsVersion}/till/{tillEpoch}/site/{siteId}")
	public ResponseEntity computeFsGroupCodeDetailsTillEpoch2(@PathVariable @NotNull OEM oemId, @PathVariable @NotBlank Integer oemFsYear,
															  @PathVariable @NotBlank Integer oemFsVersion, @PathVariable @NotBlank Long tillEpoch,
															  @PathVariable @NotBlank String siteId,
															  @RequestParam (required = false, defaultValue = "true") boolean includeM13,
															  @RequestParam (name = "addM13BalInDecBalances" , required = false, defaultValue = "false") boolean addM13BalInDecBalances){
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.computeFsGroupCodeDetails(oemId.name(), oemFsYear, oemFsVersion, tillEpoch, includeM13, addM13BalInDecBalances, siteId));
	}

	@PostMapping("/template")
	public ResponseEntity saveOemTemplates(@RequestBody OemTemplateReqDto reqDto){
		validator.validate(reqDto);
		oemFSMappingService.saveTemplate(reqDto);
		return TResponseEntityBuilder.okResponseEntity(true);
	}

	@PostMapping("/restricted/template")
	public ResponseEntity restrictedSaveOemTemplates(@RequestBody OemTemplateReqDto reqDto){
		validator.validate(reqDto);
		oemFSMappingService.saveTemplate(reqDto);
		return TResponseEntityBuilder.okResponseEntity(true);
	}

	@GetMapping("/template/{oemId}/{year}")
	public ResponseEntity getOemTemplates(@PathVariable @NotNull OEM oemId, @PathVariable @NotNull Integer year){
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.getOemTemplate(oemId.name(), year));
	}


	@PostMapping("/migration/oemCodes")
	public ResponseEntity updateOemCodes(@RequestBody OemCodeUpdateDto oemCodeUpdateDto){
		validator.validate(oemCodeUpdateDto);
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.updateOemCode(oemCodeUpdateDto));
	}

	@PostMapping("/migration/fsCellCodes/invalidate/cache")
	public ResponseEntity invalidateCache(){
		oemFSMappingService.invalidateCache();
		return TResponseEntityBuilder.okResponseEntity("Success");
	}

	/********************* FS Cell code and mapping snapshot apis ***********************/

	@PostMapping("/fsCellCodeSnapshot")
	public ResponseEntity createFsCellCodeSnapshot(@RequestBody CellCodeSnapshotCreateDto reqDto){
		oemFSMappingService.createFsCellCodeSnapshotForYearAndMonth(reqDto) ;
		return TResponseEntityBuilder.okResponseEntity("Successful");
	}

	@GetMapping("/fsCellCodeSnapshot/{fsId}/{month_1_12}")
	public ResponseEntity createFsCellCodeSnapshot(@PathVariable @NotNull String fsId, @PathVariable @NotNull Integer month_1_12){
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.getFsCellCodeSnapshots(fsId, month_1_12));
	}

	@DeleteMapping("/restricted/fsCellCodeBulkSnapshot/{oemId}")
	public ResponseEntity deleteBulkFsCellCodeSnapshot( @RequestParam(required = true, name = "fromMonth") Integer fromMonth,
														@RequestParam(required = true, name = "toMonth") Integer toMonth,
														@RequestParam(required = true, name = "year") Integer year,
														@PathVariable @NotNull OEM oemId){
		log.info("api call received for DeletefsCellCodeBulkSnapshot");
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.deleteBulkSnapshotByYearAndMonth(UserContextUtils.getSiteIdFromUserContext(),oemId.name(), year, fromMonth, toMonth));
	}

	@PostMapping("/fsMapping/{fsId}")
	public ResponseEntity createMappingSnapshot( @RequestParam(required = true, name = "month") Integer month,
												 @PathVariable @NotNull String fsId){
		log.info("api call received for fsCellCodeSnapshot");
		oemFSMappingService.createFsMappingSnapshot(fsId, month);
		return TResponseEntityBuilder.okResponseEntity("Successful");
	}

	@PostMapping("/fsmapping/{oemId}/{oemFsYear}/v/{oemFsVersion}/bulk")
	public ResponseEntity createMappingSnapshotBulk( @RequestParam(required = true, name = "fromMonth") Integer fromMonth,
													 @RequestParam(required = true, name = "toMonth") Integer toMonth,
													 @RequestParam(required = true, name = "year") Integer year,
													 @PathVariable @NotNull OEM oemId,
													 @PathVariable @NotBlank Integer oemFsYear,
													 @PathVariable @NotBlank Integer oemFsVersion){
		log.info("api call received for fsCellCodeBulkSnapshot");
		oemFSMappingService.createFsMappingSnapshotBulk(UserContextUtils.getSiteIdFromUserContext(), oemId.name(), oemFsYear, oemFsVersion, year, fromMonth, toMonth);
		return TResponseEntityBuilder.okResponseEntity("Successful");
	}

	@PostMapping("/fsmapping/snapshot")
	public ResponseEntity createMappingSnapshotBulk(@RequestParam(required = true, name = "month") Integer month,
													@RequestParam(required = true, name = "year") Integer year,
													@RequestParam (required = false, defaultValue = "false") boolean includeM13,
													@RequestParam (name = "addM13BalInDecBalances" , required = false, defaultValue = "false") boolean addM13BalInDecBalances){
		oemFSMappingService.createFsMappingAndCellCodeSnapshot(year,month, includeM13,UserContextUtils.getSiteIdFromUserContext(), addM13BalInDecBalances);
		return TResponseEntityBuilder.okResponseEntity("Successful");
	}

	@PostMapping("/fsMappingSnapshots")
	public ResponseEntity createSnapshots(@RequestBody MappingSnapshotDto snapshotdto){
		oemFSMappingService.createSnapshotsForMapping(snapshotdto);
		return TResponseEntityBuilder.okResponseEntity("Success");
	}

	@DeleteMapping("/fsMappingSnapshots")
	public ResponseEntity deleteSnapshots(@RequestBody MappingSnapshotDto snapshotdto){
		oemFSMappingService.deleteMappingSnapshots(snapshotdto);
		return TResponseEntityBuilder.okResponseEntity("Success");
	}


//  @GetMapping("/oemMappingInfo/site/{siteId}/{year}")
//  public ResponseEntity getMappingInfos( @PathVariable @NotNull String siteId, @PathVariable @NotNull Integer year){
//    return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.findMappingForYear(siteId,year));
//  }

	/**
	 * This api is not getting used, just for test purpose
	 * */
	@GetMapping("/fsCellCodeDetailsWithRoundOff/{oemId}/{oemFsYear}/v/{version}/till/{tillEpoch}")
	public ResponseEntity computeFsCellCodeDetailsTillEpoch2(@PathVariable @NotNull OEM oemId, @PathVariable @NotBlank Integer oemFsYear,
															 @PathVariable @NotBlank Integer version, @PathVariable @NotBlank Long tillEpoch,
															 @RequestParam (required = false, defaultValue = "true") boolean includeM13,
															 @RequestParam (name = "addM13BalInDecBalances" , required = false, defaultValue = "false") boolean addM13BalInDecBalances){
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.computeFsDetails(oemId.name(), oemFsYear, version, tillEpoch, includeM13, addM13BalInDecBalances));
	}

	/************ Apis Used by Analytics *******************/

	@GetMapping("/oemMappingInfo/{year}")
	public ResponseEntity getMappingInfoWithDefaultSite( @PathVariable @NotNull Integer year) {
		return TResponseEntityBuilder.okResponseEntity(fsEntryService.findFsEntriesForYear(UserContextUtils.getSiteIdFromUserContext(),year));
	}

	@PostMapping("/fsCellCodeBulkSnapshot/{oemId}/{oemFsYear}/v/{oemFsVersion}")
	public ResponseEntity createFsCellCodeBulkSnapshot( @RequestParam(required = true, name = "fromMonth") Integer fromMonth,
														@RequestParam(required = true, name = "toMonth") Integer toMonth,
														@RequestParam(required = true, name = "year") Integer year,
														@PathVariable @NotNull OEM oemId,
														@PathVariable @NotBlank Integer oemFsYear,
														@PathVariable @NotBlank Integer oemFsVersion,
														@RequestParam (required = false, defaultValue = "false") boolean includeM13,
														@RequestParam (name = "addM13BalInDecBalances" , required = false, defaultValue = "false") boolean addM13BalInDecBalances){
		log.info("api call received for fsCellCodeBulkSnapshot");
		oemFSMappingService.createBulkFsCellCodeSnapshot(UserContextUtils.getSiteIdFromUserContext(),oemId.name(), oemFsYear, oemFsVersion, year, fromMonth, toMonth, includeM13,addM13BalInDecBalances);
		return TResponseEntityBuilder.okResponseEntity("Successful");
	}

	@DeleteMapping("/fsCellCodeSnapshot/{oemId}/v/{oemFsVersion}")
	public ResponseEntity deleteFsCellCodeSnapshot( @RequestParam(required = true, name = "year") Integer year,
													@RequestParam(required = true, name = "month") Integer month,
													@PathVariable @NotNull OEM oemId,
													@PathVariable @NotBlank Integer oemFsVersion){
		log.info("api call received for delete fsCellCodeSnapshot");
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.deleteSnapshotByYearAndMonth(UserContextUtils.getSiteIdFromUserContext(), oemId.name(), oemFsVersion, year, month));
	}

	@DeleteMapping("/bulk/fsCellCodeSnapshot")
	public ResponseEntity deleteFsCellCodeSnapshotForMultipleMonths(@RequestBody @NotBlank FSCellCodeSnapshotDto dto){
		log.info("api call received for delete multiple fsCellCodeSnapshots");
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.deleteSnapshotsInBulk(dto));
	}

	@PostMapping("/fsReport/{oemId}/{oemFsYear}/v/{oemFsVersion}")
	public ResponseEntity getFsCellSnapshot( @RequestParam(required = true, name = "month") Integer month,
											 @RequestParam(required = true, name = "year") Integer year,
											 @PathVariable @NotNull OEM oemId,
											 @PathVariable @NotBlank Integer oemFsVersion,
											 @PathVariable @NotBlank Integer oemFsYear,
											 @RequestBody @NotBlank Set<String> codes,
											 @RequestParam (required = false, defaultValue = "false") boolean includeM13,
											 @RequestParam (name = "addM13BalInDecBalances" , required = false, defaultValue = "false") boolean addM13BalInDecBalances){
		log.info("api call received for getFsCellSnapshot");
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.getOEMFsCellCodeSnapshot(UserContextUtils.getSiteIdFromUserContext(),oemId.name(),oemFsVersion, year, month, codes, oemFsYear, includeM13, addM13BalInDecBalances));
	}


	@GetMapping("/fsReport/{oemId}/{oemFsYear}/v/{oemFsVersion}/summary")
	public ResponseEntity getFsCellSnapshotSummary( @RequestParam(required = true, name = "month") Integer month,
													@RequestParam(required = true, name = "year") Integer year,
													@PathVariable @NotNull OEM oemId,
													@PathVariable @NotBlank Integer oemFsVersion,
													@PathVariable @NotBlank Integer oemFsYear,
													@RequestParam (required = false, defaultValue = "false") boolean includeM13,
													@RequestParam (name = "addM13BalInDecBalances" , required = false, defaultValue = "false") boolean addM13BalInDecBalances){
		log.info("api call received for fsReport Summary");
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.getAllOEMFsCellCodeSnapshotSummary(UserContextUtils.getSiteIdFromUserContext(),oemId.name(),oemFsVersion, year, month, oemFsYear, includeM13,addM13BalInDecBalances));
	}

	@PostMapping("/fsReport/{oemId}/{oemFsYear}/v/{oemFsVersion}/bulk")
	public ResponseEntity getBulkFsCellCode( @RequestParam(required = true, name = "fromTimestamp") Long fromTimestamp,
											 @RequestParam(required = true, name = "toTimestamp") Long toTimestamp,
											 @PathVariable @NotNull OEM oemId,
											 @PathVariable @NotBlank Integer oemFsVersion,
											 @PathVariable @NotBlank Integer oemFsYear,
											 @RequestBody @NotBlank Set<String> codes,
											 @RequestParam (required = false, defaultValue = "false") boolean includeM13,
											 @RequestParam (name = "addM13BalInDecBalances" , required = false, defaultValue = "false") boolean addM13BalInDecBalances){
		log.info("api call received for fsCellCodeBulk");
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.getBulkOEMFsCellCodeSnapshot(oemId.name(), codes, fromTimestamp, toTimestamp,
				oemFsVersion, oemFsYear, includeM13,UserContextUtils.getSiteIdFromUserContext(),addM13BalInDecBalances));

	}

	@PostMapping("/fsReport/{oemId}/{oemFsYear}/v/{oemFsVersion}/till/{tillEpoch}")
	public ResponseEntity getFsCellCodeDetailsTillEpoch(@PathVariable @NotNull OEM oemId,
														@PathVariable @NotBlank Integer oemFsYear,
														@PathVariable @NotBlank Integer oemFsVersion,
														@PathVariable @NotBlank Long tillEpoch,
														@RequestBody @NotBlank Set<String> codes,
														@RequestParam(required = false, defaultValue = "false")  Boolean includeM13,
														@RequestParam (name = "addM13BalInDecBalances" , required = false, defaultValue = "false") boolean addM13BalInDecBalances){
		log.info("api call received for selected fsCellCodeDetails version");
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.getFsCellCodeDetails(oemId.name(), oemFsYear, oemFsVersion,
				tillEpoch, codes, includeM13, addM13BalInDecBalances));
	}

	@PostMapping("/dependentGLAccounts/{oem}/{year}/v/{version}/till/{epoch}")
	public ResponseEntity getMappingInfos(@PathVariable @NotNull OEM oem
			,@PathVariable @NotNull Integer year,  @PathVariable @NotNull Integer version
			,@RequestBody Set<String> cellCodes, @PathVariable @NotNull Long epoch
			,@RequestParam(required = false)  String siteId){

		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.getDependentGlAccounts(oem, year, version, cellCodes, epoch, siteId));
	}

	/**
	 * Apis needed to link Financial Statement Group Codes with GL Report Generator
	 * */
	@PostMapping("/fsCellGroups/bulk/{year}")
	public ResponseEntity fetchFsCellGroupCodesInBulk(@PathVariable Integer year, @RequestBody Set<OEM> oemIds){
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.fetchFsCellGroupCodesInBulk(year, oemIds));
	}


	@PostMapping("/viewStatement")
	public ResponseEntity fetchFsCellGroupCodesInBulk(@NotNull @RequestBody FSViewStatementDto viewStatementDto){
		return TResponseEntityBuilder.okResponseEntity(fsPrintService.viewStatement(viewStatementDto));
	}

}
