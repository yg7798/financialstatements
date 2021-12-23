package com.tekion.accounting.fs.api_restricted;

import com.tekion.accounting.fs.service.fsMapping.FsMappingService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mapping")
@RequiredArgsConstructor
@Slf4j
public class MappingResApi {

	private final FsMappingService oemFSMappingService;

	@DeleteMapping("/hardDelete/fsId/{fsId}")
	public ResponseEntity deleteMappings(@PathVariable String fsId){
		oemFSMappingService.hardDeleteMappings(fsId);
		return TResponseEntityBuilder.okResponseEntity("success");
	}

}
