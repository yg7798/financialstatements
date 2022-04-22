package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.service.fsCellGroup.FSCellGroupService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/cellGroup")
public class CellGroupApi {


	private final FSCellGroupService fsCellGroupService;

	@PostMapping("/{oemId}/{year}/v/{version}")
	public ResponseEntity getCellGroups(@PathVariable String oemId, @PathVariable Integer year, @PathVariable Integer version, @NotNull @RequestBody List<String> groupCodes) {
		return TResponseEntityBuilder.okResponseEntity(fsCellGroupService.findGroupCodes(groupCodes, oemId, year, version));
	}

	@PostMapping("/migrate/values/{country}/fromYear/{fromYear}/toYear/{toYear}")
	public ResponseEntity migrateCellGroupValuesForOem(@PathVariable String country, @PathVariable Integer fromYear, @PathVariable Integer toYear,  @RequestBody Set<String> oemIds) {
		fsCellGroupService.migrateCellGroupValuesForOem(country, fromYear, toYear, oemIds);
		return TResponseEntityBuilder.okResponseEntity("pcl values copied from prev year to current year");
	}

	@GetMapping("/validate/{oemId}/{year}/{country}")
	public ResponseEntity getInValidCellGroups(@PathVariable String oemId, @PathVariable Integer year, @PathVariable String country) {
		return TResponseEntityBuilder.okResponseEntity(fsCellGroupService.findInvalidAndMissingGroupCodes(oemId, year, country));
	}


}
