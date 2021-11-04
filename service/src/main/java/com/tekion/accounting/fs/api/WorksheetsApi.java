package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.beans.memo.HCWorksheetTemplate;
import com.tekion.accounting.fs.beans.memo.MemoWorksheet;
import com.tekion.accounting.fs.dto.memo.*;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.worksheet.HCWorksheetService;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetService;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetTemplateService;
import com.tekion.accounting.fs.utils.UserContextUtils;
import com.tekion.core.service.api.TResponseEntityBuilder;
import com.tekion.core.validation.TValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/memoWorksheet")
public class WorksheetsApi {
	private final TValidator validator;
	private final MemoWorksheetService memoWorksheetService;
	private final MemoWorksheetTemplateService memoWorksheetTemplateService;
	private final HCWorksheetService hcWorksheetService;

	@PostMapping("/headcount/templates")
	public ResponseEntity saveHCTemplate(@RequestBody HCWorksheetTemplate hcWorksheetTemplate){
		validator.validate(hcWorksheetTemplate);
		return TResponseEntityBuilder.okResponseEntity(hcWorksheetService.save(hcWorksheetTemplate));
	}

	@PutMapping("/headcount/templates")
	public ResponseEntity upsertHCTemplate(@RequestBody List<HCWorksheetTemplate> hcWorksheetTemplates){
		hcWorksheetService.upsertBulk(hcWorksheetTemplates);
		return TResponseEntityBuilder.okResponseEntity("done");
	}

	@GetMapping("/headcount/templates/{oemId}/{year}/v/{version}")
	public ResponseEntity getHCTemplate(
			@PathVariable OEM oemId,
			@PathVariable @NotBlank Integer year,
			@PathVariable @NotBlank Integer version) {
		return TResponseEntityBuilder.okResponseEntity(hcWorksheetService.getHCWorksheetTemplate(oemId, year, version));
	}

	@GetMapping("/headcount/{fsId}")
	public ResponseEntity getHCWorksheets(@PathVariable @NotBlank String fsId){
		return TResponseEntityBuilder.okResponseEntity(hcWorksheetService.getHCWorksheets(fsId));
	}

	@PutMapping("/headcount")
	public ResponseEntity updateHCWorksheets(@RequestBody HCBulkUpdateDto hcBulkUpdateDto){
		validator.validate(hcBulkUpdateDto);
		return TResponseEntityBuilder.okResponseEntity(hcWorksheetService.bulkUpdate(hcBulkUpdateDto));
	}

	@PostMapping("/headcount/templates/migrate/{oemId}/{year}/v/{version}")
	public ResponseEntity migrateToWorksheet( @PathVariable OEM oemId,
											  @PathVariable @NotBlank Integer year,
											  @PathVariable @NotBlank Integer version){
		return TResponseEntityBuilder.okResponseEntity(hcWorksheetService.migrateFromTemplate(oemId,year,version));
	}

	@PostMapping("/headcount/templates/migrate/{oemId}/{year}/v/{version}/{fsId}")
	public ResponseEntity migrateToWorksheetForFsId( @PathVariable OEM oemId,
													 @PathVariable @NotBlank Integer year,
													 @PathVariable @NotBlank Integer version,
													 @PathVariable @NotNull String fsId){
		return TResponseEntityBuilder.okResponseEntity(hcWorksheetService.migrateFromTemplateWithFsId(oemId,year,version, fsId));
	}

	/*-------------- Memo Worksheet Apis ---------*/

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

	@PutMapping
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

//    @PutMapping("/migrateFromTemplate/site/{siteId}/{oemId}/{year}/v/{version}")
//    public ResponseEntity migrateFromTemplateBySite(@PathVariable @NotBlank String siteId, @PathVariable OEM oemId,
//                                                    @PathVariable @NotBlank Integer year, @PathVariable @NotBlank Integer version){
//        return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.remigrateFromTemplate(oemId, year, version,siteId));
//    }

	@PostMapping("/migrateFromTemplate/{fsId}")
	public ResponseEntity migrateFromTemplateSelectedKeys(@PathVariable @NotBlank String fsId, @RequestBody Set<String> keys){
		return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.migrateMemoWorksheetsForKeys(fsId, keys));
	}

//    @PostMapping("/migrateFromTemplate/site/{siteId}/{oemId}/{year}/v/{version}/keys")
//    public ResponseEntity migrateFromTemplateSelectedKeysBySite(@PathVariable @NotBlank String siteId, @PathVariable OEM oemId,
//                                                                @PathVariable @NotBlank Integer year, @PathVariable @NotBlank Integer version,
//                                                                @RequestBody Set<String> keys){
//        return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.migrateWorksheetsForSelectedTemplates(oemId,year,version,keys, siteId));
//    }

	@PostMapping("/migrateFieldTypeToMemoWorksheet/{fsId}")
	public ResponseEntity migrateFieldTypeInMemoWorkSheet(@PathVariable @NotBlank String fsId, @RequestBody WorksheetRequestDto dto){
		return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.migrateFieldTypeInMemoWorkSheet(fsId, dto));
	}

//    @PostMapping("/migrateFieldTypeToMemoWorksheet/site/{siteId}/{oemId}/{year}/v/{version}")
//    public ResponseEntity migrateFromTemplateSelectedKeysBySite(@PathVariable @NotBlank String siteId,@PathVariable OEM oemId,
//                                                                @PathVariable @NotBlank Integer year, @PathVariable @NotBlank Integer version){
//        return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.migrateFieldTypeInMemoWorkSheet(oemId, year,version, siteId));
//    }

	@PostMapping("/migrateActiveFields/{oem}/fromYear/{fromYear}/toYear/{yearToUpdate}/v/{version}")
	public ResponseEntity migrateActiveFields(@PathVariable OEM oem, @PathVariable @NotBlank Integer fromYear, @PathVariable @NotBlank Integer version,
											  @PathVariable @NotBlank Integer yearToUpdate){
		return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.updateActiveFieldsFromPreviousWorksheets(oem, fromYear, yearToUpdate, version, UserContextUtils.getSiteIdFromUserContext()));
	}

//    @PostMapping("/migrateActiveFields/site/{siteId}/{oem}/fromYear/{fromYear}/toYear/{yearToUpdate}/v/{version}")
//    public ResponseEntity migrateActiveFieldsBySite(@PathVariable @NotBlank String siteId,
//                                              @PathVariable OEM oem, @PathVariable @NotBlank Integer fromYear,
//                                              @PathVariable @NotBlank Integer version,
//                                              @PathVariable @NotBlank Integer yearToUpdate){
//        return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.updateActiveFieldsFromPreviousWorksheets(oem, fromYear, yearToUpdate, version, siteId));
//    }
}
