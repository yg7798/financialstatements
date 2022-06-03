package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.dto.oemConfig.OemConfigRequestDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.oemConfig.OemConfigService;
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

	private final OemConfigService oemConfigService;

	@PostMapping("/")
	public ResponseEntity saveOemConfig(@RequestBody OemConfigRequestDto requestDto){
		return TResponseEntityBuilder.okResponseEntity(oemConfigService.saveOemConfig(requestDto));
	}

	@GetMapping("/{oemId}")
	public ResponseEntity getOemConfig(@PathVariable OEM oemId){
		return TResponseEntityBuilder.okResponseEntity(oemConfigService.getOemConfig(oemId.name()));
	}

	@PutMapping("/enableRoundedTrialBal/{oem}/{country}")
	public ResponseEntity enableRoundedTrialBal(@PathVariable OEM oem, @PathVariable String country){
		oemConfigService.enableRoundedTrialBal(oem, country);
		return TResponseEntityBuilder.okResponseEntity("success");
	}

	@PutMapping("/disableRoundedTrialBal/{oem}/{country}")
	public ResponseEntity disableRoundedTrialBal(@PathVariable OEM oem, @PathVariable String country){
		oemConfigService.disableRoundedTrialBal(oem, country);
		return TResponseEntityBuilder.okResponseEntity("success");
	}



}
