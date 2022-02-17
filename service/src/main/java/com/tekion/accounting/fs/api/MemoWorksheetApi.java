package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.beans.memo.MemoWorksheet;
import com.tekion.accounting.fs.common.GlobalService;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.accounting.fs.dto.memo.MemoBulkUpdateDto;
import com.tekion.accounting.fs.dto.memo.MemoWorksheetTemplateBulkRequest;
import com.tekion.accounting.fs.dto.memo.MemoWorksheetTemplateRequestDto;
import com.tekion.accounting.fs.dto.memo.WorksheetRequestDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetService;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetTemplateService;
import com.tekion.accounting.fs.util.ApiHelperUtils;
import com.tekion.core.service.api.TResponseEntityBuilder;
import com.tekion.core.validation.TValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/memoWorksheet")
public class MemoWorksheetApi {
	private final TValidator validator;
	private final MemoWorksheetService memoWorksheetService;
	private final MemoWorksheetTemplateService memoWorksheetTemplateService;
	private final GlobalService globalService;

	@PostMapping("/templates")
	public ResponseEntity saveMWTemplates(@RequestBody MemoWorksheetTemplateBulkRequest memoWorksheetTemplateBulkRequest){
		validator.validate(memoWorksheetTemplateBulkRequest);
		memoWorksheetTemplateBulkRequest.getMemoTemplates().forEach( template -> {
					template.setOem(memoWorksheetTemplateBulkRequest.getOem());
					template.setYear(memoWorksheetTemplateBulkRequest.getYear());
					template.setVersion(memoWorksheetTemplateBulkRequest.getVersion());
					template.setCountry(memoWorksheetTemplateBulkRequest.getCountry());
				}
		);
		memoWorksheetTemplateService.saveBulk(memoWorksheetTemplateBulkRequest.getMemoTemplates());
		return TResponseEntityBuilder.okResponseEntity("success");
	}

	@GetMapping("/templates/{oemId}/{year}/v/{version}")
	public ResponseEntity getTemplate(
			@PathVariable OEM oemId,
			@PathVariable @NotBlank Integer year,
			@PathVariable @NotBlank Integer version) {
		return TResponseEntityBuilder.okResponseEntity(memoWorksheetTemplateService.getMemoWorksheetTemplates(oemId, year, version));
	}

	@PostMapping("/template")
	public ResponseEntity saveMWTemplate(@RequestBody MemoWorksheetTemplateRequestDto reqDto){
		validator.validate(reqDto);
		memoWorksheetTemplateService.save(reqDto);
		return TResponseEntityBuilder.okResponseEntity("success");
	}

	@GetMapping("/{fsId}")
	public ResponseEntity getMemoWorksheet(@PathVariable @NotBlank String fsId){
		return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.getMemoWorksheet(fsId));
	}

	@PutMapping("")
	public ResponseEntity saveWorkSheet(@RequestBody MemoWorksheet memoWorksheet){
		return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.save(memoWorksheet));
	}

	@PutMapping("/batch")
	public ResponseEntity saveWorkSheet(@RequestBody MemoBulkUpdateDto memoBulkUpdateDto){
		return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.bulkUpdate(memoBulkUpdateDto));
	}

	@PutMapping("/migrateFromTemplate/{fsId}")
	public ResponseEntity migrateFromTemplate(@PathVariable @NotBlank String fsId){
		return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.remigrateFromTemplate(fsId));
	}

	@PostMapping("/migrateFromTemplate/{fsId}")
	public ResponseEntity migrateFromTemplateSelectedKeys(@PathVariable @NotBlank String fsId, @RequestBody Set<String> keys){
		return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.migrateMemoWorksheetsForKeys(fsId, keys));
	}

	@PostMapping("/migrateFieldTypeToMemoWorksheet/{fsId}")
	public ResponseEntity migrateFieldTypeInMemoWorkSheet(@PathVariable @NotBlank String fsId, @RequestBody WorksheetRequestDto dto){
		return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.migrateFieldTypeInMemoWorkSheet(fsId, dto));
	}

	@PostMapping("/migrateActiveFields/{oem}/fromYear/{fromYear}/toYear/{yearToUpdate}/v/{version}")
	public ResponseEntity migrateActiveFields(@PathVariable OEM oem, @PathVariable @NotBlank Integer fromYear, @PathVariable @NotBlank Integer version,
											  @PathVariable @NotBlank Integer yearToUpdate){
		return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.updateActiveFieldsFromPreviousWorksheets(oem, fromYear, yearToUpdate, version, UserContextUtils.getSiteIdFromUserContext()));
	}

	@DeleteMapping("/deleteMemoWorksheets/fsId/{fsId}")
	public ResponseEntity deleteMemoWorksheets(@PathVariable @NotNull String fsId,
											   @RequestParam(required = false) Integer parallelism,
											   @RequestBody Set<String> memoKeys
	){

		memoWorksheetService.deleteMemoWorksheetsByKey(memoKeys, fsId);
		return TResponseEntityBuilder.okResponseEntity("Success");
	}

	@PostMapping("/migrateForMissingKeys/{fsId}")
	public ResponseEntity migrateForMissingKeys(@PathVariable String fsId){
		return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.migrateForMissingKeys(fsId));
	}

	@PostMapping("/migrateForMissingKeys/{oem}/{year}/{country}/all")
	public ResponseEntity migrateForMissingKeys(@PathVariable OEM oem, @PathVariable Integer year,
												@PathVariable String country, @RequestParam(required = false) Integer parallelism){
		globalService.executeTaskForAllDealers(() -> memoWorksheetService.migrateForMissingKeysForAll(oem.name(), year, country),
				ApiHelperUtils.getDefaultParallelism(parallelism));
		return TResponseEntityBuilder.okResponseEntity("Success");
	}
}
