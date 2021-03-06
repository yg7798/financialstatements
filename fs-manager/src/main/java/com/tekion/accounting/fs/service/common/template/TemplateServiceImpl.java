package com.tekion.accounting.fs.service.common.template;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.common.enums.Template;
import com.tekion.core.serverconfig.service.ServerConfigService;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.sdk.storage.s3.S3ObjectStorageService;
import com.tekion.tekionconstant.locale.TekLocale;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@Service
@Slf4j
public class TemplateServiceImpl implements TemplateService{
	private final S3ObjectStorageService s3ObjectStorageService;
	private final ServerConfigService configService;
	private final DealerConfig dealerConfig;

	public static final String XLSX_FILE_EXTENSION = ".xlsx";

	@Override
	public String getMediaPathForTemplate(String templateName) {
		log.info("Finding path for template {}",templateName);
		Template template = Template.valueOf(templateName.toUpperCase());
		String defaultBucket  = s3ObjectStorageService.getS3ClientProvider().getDefaultBucketName();
		String mediaPath = TConstants.S3_ACCOUNTING_TEMPLATE_PATH + "/" + template.getFileName();
		String signedUrl = s3ObjectStorageService.generatePreSignedURL(mediaPath,defaultBucket);
		log.info("Default bucket {} media path {}", defaultBucket, signedUrl);
		return signedUrl;
	}

	@Override
	public String getMediaPathForFSTemplate(String oemId, Integer year) {
		String mediaPath;
		log.info("Finding path for FS Template  for oemId {} and year {}", oemId, year);
		String defaultBucket  = s3ObjectStorageService.getS3ClientProvider().getDefaultBucketName();
		if(Objects.nonNull(UserContextProvider.getCurrentLocale()))
			mediaPath = TConstants.S3_ACCOUNTING_TEMPLATE_PATH + "/OEM/" + oemId + "/" + year + "_" + UserContextProvider.getCurrentLocale() +".xlsx";
		else
			mediaPath = TConstants.S3_ACCOUNTING_TEMPLATE_PATH + "/OEM/" + oemId + "/" + year + "_" + TekLocale.en + "_" + dealerConfig.getDealerCountryCode() + ".xlsx";
		String signedUrl = s3ObjectStorageService.generatePreSignedURL(mediaPath,defaultBucket);
		log.info("Default bucket {} media path {}", defaultBucket, signedUrl);
		return signedUrl;
	}

	@Override
	public File downloadFSTemplate(String oemId, Integer year) {
		try {
			log.info("Creating FS template file for oemId {} and year {}", oemId, year);
			String uuid = UUID.randomUUID().toString();
			File file = File.createTempFile(FilenameUtils.getBaseName(uuid), XLSX_FILE_EXTENSION);
			String defaultBucket  = s3ObjectStorageService.getS3ClientProvider().getDefaultBucketName();
			String mediaPath = TConstants.S3_ACCOUNTING_TEMPLATE_PATH + "/OEM/" + oemId + "/" + year + ".xlsx";
			s3ObjectStorageService.downloadAsFile(defaultBucket, mediaPath, file);
			log.info("Returning FS template file {}", file.exists());
			return file;
		} catch (Exception e) {
			log.error("error in downloadFSTemplate {} stacktrace: {}", e.getMessage(), e.getStackTrace().toString());
			return null;
		}
	}


}
