package com.tekion.accounting.fs.service.common.template;

import java.io.File;

public interface TemplateService {
	String getMediaPathForTemplate(String templateName);
	String getMediaPathForFSTemplate(String oemId, Integer year);
	File downloadFSTemplate(String oemId, Integer year);
}
