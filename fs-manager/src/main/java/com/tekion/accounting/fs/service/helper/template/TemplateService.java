package com.tekion.accounting.fs.service.helper.template;

import java.io.File;

public interface TemplateService {
	String getMediaPathForTemplate(String templateName);
	String getMediaPathForFSTemplate(String oemId, Integer year);
	File downloadFSTemplate(String oemId, Integer year);
}
