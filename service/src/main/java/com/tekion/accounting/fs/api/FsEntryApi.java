package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.service.FsEntryService;
import com.tekion.as.client.AccountingClient;
import com.tekion.core.service.api.TResponseEntityBuilder;
import com.tekion.core.validation.TValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/fsEntry")
public class FsEntryApi {
	private final FsEntryService fsEntryService;
	private final TValidator validator;

	@GetMapping("/{id}")
	public ResponseEntity fsMappingInfo(@PathVariable @NotNull String id) {
		return TResponseEntityBuilder.okResponseEntity(fsEntryService.getFSEntryById(id));
	}
}
