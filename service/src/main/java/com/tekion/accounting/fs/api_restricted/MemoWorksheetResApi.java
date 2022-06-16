package com.tekion.accounting.fs.api_restricted;

import com.tekion.accounting.fs.dto.memo.CopyMemoValuesDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetService;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetTemplateService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@RestController
@RequestMapping("/memoWorksheet")
@RequiredArgsConstructor
@Slf4j
public class MemoWorksheetResApi {

	MemoWorksheetService memoWorksheetService;
	MemoWorksheetTemplateService memoWorksheetTemplateService;

	@DeleteMapping("/fsId/{fsId}")
	public ResponseEntity deleteMemoWorksheet(@PathVariable @NotNull String fsId,
											  @RequestBody Set<String> keys) {
		return TResponseEntityBuilder.okResponseEntity(memoWorksheetService.deleteMemoWorksheetsByKey(keys, null));
	}

	@PostMapping("/copyValues")
	public ResponseEntity copyMemoWorksheet(@RequestBody CopyMemoValuesDto dto){

		return TResponseEntityBuilder.okResponseEntity(
				memoWorksheetService.copyValues(dto)
		);
	}

	@DeleteMapping("/templates/country/{countryCode}/{oemId}/{year}/v/{version}")
	public ResponseEntity deleteMemoWorksheetTemplate(@PathVariable @NotNull OEM oemId,
													  @PathVariable @NotBlank Integer year,
													  @PathVariable @NotBlank Integer version,
													  @RequestBody Set<String> keys,
													  @PathVariable @NotBlank String countryCode) {
		return TResponseEntityBuilder.okResponseEntity(memoWorksheetTemplateService.deleteMemoWorksheetTemplatesByKeys(oemId, year, version, keys, countryCode));
	}

	@DeleteMapping("/templates/country/{countryCode}/{oemId}/{year}/all")
	public ResponseEntity hardDeleteMemoWorksheetTemplates(@PathVariable @NotNull OEM oemId,
													  @PathVariable @NotBlank Integer year,
													  @PathVariable @NotBlank String countryCode) {
		memoWorksheetTemplateService.deleteMWTemplatesByOemByCountryByYear(oemId, year, countryCode);
		return TResponseEntityBuilder.okResponseEntity("done");
	}

	@DeleteMapping("/templates/country/{countryCode}/{oemId}/{year}")
	public ResponseEntity hardDeleteMemoWorksheetTemplates(@PathVariable @NotNull OEM oemId,
													   @PathVariable @NotBlank Integer year,
													   @RequestBody Set<String> keys,
													   @PathVariable @NotBlank String countryCode) {
		memoWorksheetTemplateService.deleteMWTemplatesByOemByCountryByYearByKeys(oemId, year, keys, countryCode);
		return TResponseEntityBuilder.okResponseEntity("done");
	}
}
