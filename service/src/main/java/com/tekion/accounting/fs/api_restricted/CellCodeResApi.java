package com.tekion.accounting.fs.api_restricted;

import com.tekion.accounting.fs.dto.cellcode.FsCellCodeDeleteDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/cellCode")
@RequiredArgsConstructor
@Slf4j
public class CellCodeResApi {

	private final FsComputeService oemFSMappingService;

	@DeleteMapping("/")
	public ResponseEntity deleteCellCodes(@RequestBody FsCellCodeDeleteDto reqDto){
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.deleteCellCodes(reqDto));
	}

	@PostMapping("/migrate/country/{countryCode}/{oemId}/fromYear/{fromYear}/toYear/{toYear}")
	public ResponseEntity migrateFSCellCodes(@PathVariable @NotNull OEM oemId, @PathVariable Integer fromYear, @PathVariable Integer toYear,
											 @PathVariable @NotBlank String countryCode){
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.migrateCellCodesToYear(oemId.toString(), fromYear, toYear, countryCode));
	}
}
