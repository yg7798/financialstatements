package com.tekion.accounting.fs.api_restricted;

import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.fsValidation.FsValidationService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/validation")
public class FsValidationResApi {

	private final FsValidationService fsValidationService;

	@DeleteMapping("/rules")
	public ResponseEntity deleteRules(@RequestBody List<String> ruleIds){
		fsValidationService.delete(ruleIds);
		return TResponseEntityBuilder.okResponseEntity("success");
	}

	@DeleteMapping("/hardDelete/{oemId}/{year}/{country}")
	public ResponseEntity deleteRules(@PathVariable OEM oemId, @PathVariable String country, @PathVariable Integer year){
		fsValidationService.deleteAll(oemId.getOem(), year, country);
		return TResponseEntityBuilder.okResponseEntity("success");
	}
}
