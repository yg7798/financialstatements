package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.dto.fsEntry.FSEntryUpdateDto;
import com.tekion.accounting.fs.dto.fsEntry.FsEntryCreateDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.fsEntry.FsEntryService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import com.tekion.core.validation.TValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/fsEntry")
public class FsEntryApi {

	private final FsEntryService fsEntryService;
	private final TValidator validator;

	@PostMapping("/")
	public ResponseEntity createFSEntry(@RequestBody FsEntryCreateDto reqDto){
		validator.validate(reqDto);
		return TResponseEntityBuilder.okResponseEntity(fsEntryService.createFSEntry(reqDto));
	}

	@GetMapping("/oem/{oemId}")
	public ResponseEntity getFSEntryByOemId(@PathVariable @NotNull OEM oemId){
		return TResponseEntityBuilder.okResponseEntity(fsEntryService.getFSEntry(oemId.name()));
	}

	@GetMapping("/{fsId}")
	public ResponseEntity fetchFSEntryById(@PathVariable @NotBlank String fsId){
		return TResponseEntityBuilder.okResponseEntity(fsEntryService.getFSEntryById(fsId));
	}

	@GetMapping("/all")
	public ResponseEntity getAllFSEntries(){
		return TResponseEntityBuilder.okResponseEntity(fsEntryService.getAllFSEntries());
	}

	@PostMapping("/filter")
	public ResponseEntity getFSEntriesBySite(@RequestBody List<String> siteIds){
		return TResponseEntityBuilder.okResponseEntity(fsEntryService.getFSEntriesBySiteId(siteIds));
	}

	@PutMapping("/{fsId}")
	public ResponseEntity updateFSEntry(@PathVariable("fsId") String fsId, @RequestBody FSEntryUpdateDto FSEntryUpdateDto){
		validator.validate(FSEntryUpdateDto);
		return TResponseEntityBuilder.okResponseEntity(fsEntryService.updateFSEntry(fsId, FSEntryUpdateDto));
	}

	/**
	 * This is for checking NCT statement presence
	 * */
	@GetMapping("/year/{year}")
	public ResponseEntity getFsEntriesForYear(@PathVariable @NotNull Integer year) {
		return TResponseEntityBuilder.okResponseEntity(fsEntryService.findFsEntriesForYear(year));
	}

	/**
	 * This is for fetching dealers detail for consolidated type FS
	 * */
	@GetMapping("/{fsId}/dealers")
	public ResponseEntity getAllDealersDetails(@PathVariable @NotBlank String fsId){
		return TResponseEntityBuilder.okResponseEntity(fsEntryService.getDealersDetailForConsolidatedFS(fsId));
	}
}
