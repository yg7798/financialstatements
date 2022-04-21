package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.service.analytics.AnalyticsService;
import com.tekion.accounting.fs.service.compute.models.FSReportDto;
import com.tekion.core.service.api.TResponseEntityBuilder;
import com.tekion.core.validation.TValidator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/analytics")
public class AnalyticsApi {

    @Autowired
    AnalyticsService analyticsService;
    @Autowired
    private TValidator tValidator;

    @PostMapping("/fsAverageReport/{oemId}")
    public ResponseEntity getFsCellSnapshot(@PathVariable @NotBlank OEM oemId,
                                            @RequestBody @NotBlank FSReportDto fsReportDto) {
        log.info("api call received for FSReportingAverage");
        return TResponseEntityBuilder.okResponseEntity(analyticsService.getFSCellCodeAverage(fsReportDto.getFromTimestamp(),
                fsReportDto.getToTimestamp(), fsReportDto.getCodes(), oemId.name()));
    }
}