package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.dto.fsValidation.FsValidationRuleDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.fsValidation.FsValidationService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/validation")
public class FsValidationApi {

	private final FsValidationService fsValidationService;

	@PutMapping("/import/{oem}/{country}/{year}/{mediaId}")
	public ResponseEntity importRules(@PathVariable String mediaId, @PathVariable OEM oem, @PathVariable String country, @PathVariable Integer year){
		return TResponseEntityBuilder.okResponseEntity(fsValidationService.importRules(mediaId, oem.name(), country.toUpperCase(), year));
	}

	@PutMapping("/save")
	public ResponseEntity importRules(@RequestBody List<FsValidationRuleDto> rules){
		return TResponseEntityBuilder.okResponseEntity(fsValidationService.save(rules));
	}

	@GetMapping("/{oemId}/{year}/rules")
	public ResponseEntity getRules(@PathVariable OEM oemId, @PathVariable Integer year, @RequestParam(required = false) String country){
		return TResponseEntityBuilder.okResponseEntity(fsValidationService.getRules(oemId, year, country));
	}

	@GetMapping("/{fsId}/till/{tillEpoch}")
	public ResponseEntity validateFs(@PathVariable String fsId, @PathVariable Long tillEpoch,
									 @RequestParam(required = false, defaultValue = "true") boolean includeM13,
									 @RequestParam (name = "addM13BalInDecBalances",
											 required = false, defaultValue = "false") boolean addM13BalInDecBalances){

		return TResponseEntityBuilder.okResponseEntity(fsValidationService.validateFs(fsId, tillEpoch, includeM13, addM13BalInDecBalances));
	}


	@GetMapping("/copyRules/{oemId}/{country}/{fromYear}/to/{toYear}")
	public ResponseEntity copyRules(@PathVariable OEM oemId, @PathVariable String country, @PathVariable Integer fromYear
			, @PathVariable Integer toYear){

		return TResponseEntityBuilder.okResponseEntity(fsValidationService.copyRules(oemId.getOem(), country, fromYear, toYear));
	}

}
