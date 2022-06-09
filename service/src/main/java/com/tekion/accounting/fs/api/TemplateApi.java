package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.service.common.template.TemplateService;
import com.tekion.accounting.fs.service.multilingual.HCDepartmentService;
import com.tekion.accounting.fs.service.multilingual.HCPositionService;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetTemplateService;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetTemplateServiceImpl;
import com.tekion.core.service.api.TResponseEntityBuilder;
import com.tekion.multilingual.dto.MultiLingualExportRequest;
import com.tekion.multilingual.dto.MultiLingualImportRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/template")
public class TemplateApi {
    private final TemplateService templateService;
    private final MemoWorksheetTemplateServiceImpl memoService;
    private final HCDepartmentService departmentService;
    private final HCPositionService positionService;

    @GetMapping("/{template}/url")
    public ResponseEntity getTemplateS3Url(@PathVariable("template") String templateName){
        return TResponseEntityBuilder.okResponseEntity(templateService.getMediaPathForTemplate(templateName));
    }
}
