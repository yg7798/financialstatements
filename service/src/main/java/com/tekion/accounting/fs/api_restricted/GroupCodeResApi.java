package com.tekion.accounting.fs.api_restricted;

import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groupCode")
@RequiredArgsConstructor
public class GroupCodeResApi {

	private final FsComputeService oemFSMappingService;

	@PostMapping("/migrate/{oemId}/fromYear/{fromYear}/to/{toYear}/country/{country}")
	public ResponseEntity migrateGroupCodes(@PathVariable OEM oemId, @PathVariable Integer fromYear,
											@PathVariable Integer toYear, @PathVariable String country){
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.migrateGroupsCodesToYear(oemId.toString(), fromYear, toYear, country));
	}

	@DeleteMapping("/country/{country}/{oemId}/{year}/v/{version}")
	public ResponseEntity deleteCellGroups(@PathVariable OEM oemId, @PathVariable Integer year, @PathVariable String country,
										   @PathVariable Integer version, @RequestBody List<String> groupDisplayNames){
		return TResponseEntityBuilder.okResponseEntity(oemFSMappingService.deleteGroupCodes(oemId.name(), year, groupDisplayNames, version, country));
	}

}
