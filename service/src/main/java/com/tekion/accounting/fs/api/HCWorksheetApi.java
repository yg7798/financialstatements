package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.beans.memo.HCWorksheetTemplate;
import com.tekion.accounting.fs.dto.memo.HCBulkUpdateDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.worksheet.HCWorksheetService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import com.tekion.core.validation.TValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hcWorksheet")
public class HCWorksheetApi {
	private final TValidator validator;
	private final HCWorksheetService hcWorksheetService;

	@PostMapping("/templates")
	public ResponseEntity saveHCTemplate(@RequestBody HCWorksheetTemplate hcWorksheetTemplate){
		validator.validate(hcWorksheetTemplate);
		return TResponseEntityBuilder.okResponseEntity(hcWorksheetService.save(hcWorksheetTemplate));
	}

	@PutMapping("/templates")
	public ResponseEntity upsertHCTemplate(@RequestBody List<HCWorksheetTemplate> hcWorksheetTemplates){
		hcWorksheetService.upsertBulk(hcWorksheetTemplates);
		return TResponseEntityBuilder.okResponseEntity("done");
	}

	@GetMapping("/templates/{oemId}/{year}/v/{version}")
	public ResponseEntity getHCTemplate(
			@PathVariable OEM oemId,
			@PathVariable @NotBlank Integer year,
			@PathVariable @NotBlank Integer version) {
		return TResponseEntityBuilder.okResponseEntity(hcWorksheetService.getHCWorksheetTemplate(oemId, year, version));
	}

	@GetMapping("/{fsId}")
	public ResponseEntity getHCWorksheets(@PathVariable @NotBlank String fsId){
		return TResponseEntityBuilder.okResponseEntity(hcWorksheetService.getHCWorksheets(fsId));
	}

	@PutMapping("/")
	public ResponseEntity updateHCWorksheets(@RequestBody HCBulkUpdateDto hcBulkUpdateDto){
		validator.validate(hcBulkUpdateDto);
		return TResponseEntityBuilder.okResponseEntity(hcWorksheetService.bulkUpdate(hcBulkUpdateDto));
	}

	@PostMapping("/templates/migrate/{oemId}/{year}/v/{version}")
	public ResponseEntity migrateToWorksheet( @PathVariable OEM oemId,
											  @PathVariable @NotBlank Integer year,
											  @PathVariable @NotBlank Integer version){
		return TResponseEntityBuilder.okResponseEntity(hcWorksheetService.migrateFromTemplate(oemId,year,version));
	}

	@PostMapping("/templates/migrate/{oemId}/{year}/v/{version}/{fsId}")
	public ResponseEntity migrateToWorksheetForFsId( @PathVariable OEM oemId,
													 @PathVariable @NotBlank Integer year,
													 @PathVariable @NotBlank Integer version,
													 @PathVariable @NotNull String fsId){
		return TResponseEntityBuilder.okResponseEntity(hcWorksheetService.migrateFromTemplateWithFsId(oemId,year,version, fsId));
	}
}
