package com.tekion.accounting.fs.api_migration;

import com.tekion.accounting.fs.common.GlobalService;
import com.tekion.accounting.fs.service.fsEntry.FsEntryService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/fsEntry/mig")
public class FsEntryMigApi {

	private final FsEntryService fsEntryService;
	private final GlobalService globalService;

	@PostMapping("/migrateFsName")
	public ResponseEntity migrateFsName() {
		fsEntryService.migrateFSName();
		return TResponseEntityBuilder.okResponseEntity("success");
	}

	@PostMapping("/migrateFsName/all")
	public ResponseEntity migrateFsName(@RequestParam(required = false) Integer parallelism) {
		if (parallelism == null) {
			parallelism = 1;
		}
		globalService.executeTaskForAllDealers(fsEntryService::migrateFSName, parallelism);
		return TResponseEntityBuilder.okResponseEntity("success");
	}
}
