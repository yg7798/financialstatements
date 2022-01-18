package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.dto.pclCodes.MediaRequestDto;
import com.tekion.accounting.fs.service.pclCodes.PclCodeService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/pclCodes")
public class PclCodesApi {

    private final PclCodeService pclCodeService;

    @GetMapping("/oemDetails")
    public ResponseEntity oemDetails() {
        return TResponseEntityBuilder.okResponseEntity(pclCodeService.getOemDetails());
    }

    @GetMapping("/pclDetails/{oemId}/{year}/{country}")
    public ResponseEntity pclCodesDetails(@NotEmpty @PathVariable("oemId")String oemId,
                                          @NotEmpty @PathVariable("year")Integer year,
                                          @PathVariable("country")String country) {
        return TResponseEntityBuilder.okCreatedResponseEntity(pclCodeService.getPclCodeDetails(oemId, year, country));
    }

    @PutMapping("/update")
    public ResponseEntity updatePclCodesDetails(@RequestBody AccountingOemFsCellGroup pclDetailsDto) {
        pclCodeService.updatePclCodeDetails(pclDetailsDto);
        return TResponseEntityBuilder.okCreatedResponseEntity("done");
    }

    @PutMapping("/bulkUpdate")
    public ResponseEntity updatePclCodesInBulk(@NotNull @RequestBody MediaRequestDto requestDto) {
        pclCodeService.updatePclCodesInBulk(requestDto);
        return TResponseEntityBuilder.okResponseEntity("success");
    }
}
