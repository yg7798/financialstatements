package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.service.common.template.TemplateService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/template")
public class TemplateApi {
    private final TemplateService templateService;

    @GetMapping("/{template}/url")
    public ResponseEntity getTemplateS3Url(@PathVariable("template") String templateName){
        return TResponseEntityBuilder.okResponseEntity(templateService.getMediaPathForTemplate(templateName));
    }
}
