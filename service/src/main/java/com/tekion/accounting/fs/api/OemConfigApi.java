package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.dto.oemConfig.OemConfigRequestDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/oemConfig")
public class OemConfigApi {

	private final FsComputeService fsComputeService;

	@PostMapping("/")
	public ResponseEntity saveOemConfig(@RequestBody OemConfigRequestDto requestDto){
		return TResponseEntityBuilder.okResponseEntity(fsComputeService.saveOemConfig(requestDto));
	}

	@GetMapping("/{oemId}")
	public ResponseEntity getOemConfig(@PathVariable OEM oemId){
		return TResponseEntityBuilder.okResponseEntity(fsComputeService.getOemConfig(oemId.name()));
	}

}
