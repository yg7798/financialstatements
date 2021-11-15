package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.beans.memo.MemoWorksheet;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.accounting.fs.dto.memo.MemoBulkUpdateDto;
import com.tekion.accounting.fs.dto.memo.MemoWorksheetTemplateBulkRequest;
import com.tekion.accounting.fs.dto.memo.MemoWorksheetTemplateRequestDto;
import com.tekion.accounting.fs.dto.memo.WorksheetRequestDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetService;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetTemplateService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import com.tekion.core.validation.TValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/memoWorksheet")
public class MemoWorksheetApi {
	private final TValidator validator;
	private final MemoWorksheetService memoWorksheetService;
	private final MemoWorksheetTemplateService memoWorksheetTemplateService;

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

//    @PostMapping("/migrateFieldTypeToMemoWorksheet/site/{siteId}/{oemId}/{year}/v/{version}")
//    public ResponseEntity migrateFromTemplateSelectedKeysBySite(@PathVariable @NotBlank String siteId,@PathVariable OEM oemId,
//                                                                @PathVariable @NotBlank Integer year, @PathVariable @NotBlank Integer version){
//        return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.migrateFieldTypeInMemoWorkSheet(oemId, year,version, siteId));
//    }

//    @PostMapping("/migrateActiveFields/site/{siteId}/{oem}/fromYear/{fromYear}/toYear/{yearToUpdate}/v/{version}")
//    public ResponseEntity migrateActiveFieldsBySite(@PathVariable @NotBlank String siteId,
//                                              @PathVariable OEM oem, @PathVariable @NotBlank Integer fromYear,
//                                              @PathVariable @NotBlank Integer version,
//                                              @PathVariable @NotBlank Integer yearToUpdate){
//        return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.updateActiveFieldsFromPreviousWorksheets(oem, fromYear, yearToUpdate, version, siteId));
//    }

//    @PostMapping("/migrateFromTemplate/site/{siteId}/{oemId}/{year}/v/{version}/keys")
//    public ResponseEntity migrateFromTemplateSelectedKeysBySite(@PathVariable @NotBlank String siteId, @PathVariable OEM oemId,
//                                                                @PathVariable @NotBlank Integer year, @PathVariable @NotBlank Integer version,
//                                                                @RequestBody Set<String> keys){
//        return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.migrateWorksheetsForSelectedTemplates(oemId,year,version,keys, siteId));
//    }

//    @PutMapping("/migrateFromTemplate/site/{siteId}/{oemId}/{year}/v/{version}")
//    public ResponseEntity migrateFromTemplateBySite(@PathVariable @NotBlank String siteId, @PathVariable OEM oemId,
//                                                    @PathVariable @NotBlank Integer year, @PathVariable @NotBlank Integer version){
//        return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.remigrateFromTemplate(oemId, year, version,siteId));
//    }
}
