package com.tekion.accounting.fs.api_restricted;

import com.tekion.accounting.fs.service.fsEntry.FsEntryService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
