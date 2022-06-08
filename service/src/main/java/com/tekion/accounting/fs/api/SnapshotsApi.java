package com.tekion.accounting.fs.api;

import com.tekion.accounting.events.UpdateSnapshotsEvent;
import com.tekion.accounting.fs.service.snapshots.SnapshotService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/snapshots")
public class SnapshotsApi {

	private final SnapshotService snapshotService;

	@PostMapping("/{year}/{month_1_12}")
	public ResponseEntity createMappingAndCellCodeSnapshot(@PathVariable Integer year, @PathVariable Integer month_1_12){
		snapshotService.createSnapshotsForMappingsAndCellCodes(year, month_1_12);
		return TResponseEntityBuilder.okResponseEntity("success");
	}

	@PostMapping("/mapping/{fsId}/{month_1_12}")
	public ResponseEntity createMappingSnapshot(@PathVariable String fsId, @PathVariable Integer month_1_12){
		snapshotService.createSnapshotsForMappings(fsId, month_1_12);
		return TResponseEntityBuilder.okResponseEntity("success");
	}

	@PostMapping("/cellCode/{fsId}/{month_1_12}")
	public ResponseEntity createCellCodeSnapshot(@PathVariable String fsId, @PathVariable Integer month_1_12){
		snapshotService.createSnapshotsForCellCodes(fsId, month_1_12);
		return TResponseEntityBuilder.okResponseEntity("success");
	}

	@GetMapping("/mapping/{fsId}/{month_1_12}")
	public ResponseEntity getFsMappingSnapshots(@PathVariable String fsId, @PathVariable Integer month_1_12){
		return TResponseEntityBuilder.okResponseEntity(snapshotService.getMappingSnapshots(fsId, month_1_12));
	}

}
