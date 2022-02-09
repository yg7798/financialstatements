package com.tekion.accounting.fs.api_restricted;

import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.service.fsEntry.FsEntryService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/fsEntry")
@RequiredArgsConstructor
@Slf4j
public class FSEntryResApi {

	private final FsEntryService fsEntryService;

	@DeleteMapping("/{fsId}")
	public ResponseEntity deleteMappingInfo(@PathVariable @NotBlank String fsId){
		return TResponseEntityBuilder.okResponseEntity(fsEntryService.deleteFsEntryById(fsId));
	}

	/**
	 * This is for changing FS type of FS_Entry
	 */
	@PutMapping("/updateFsType/{fsId}/type/{type}")
	public ResponseEntity updateFsTypeOfFsEntry(@PathVariable @NotBlank String fsId, @PathVariable("type") @NotBlank FSType type) {
		return TResponseEntityBuilder.okResponseEntity(fsEntryService.updateFsTypeForFsEntry(fsId, type));
	}
}
