package com.tekion.accounting.fs.service.externalService.media;

import okhttp3.MediaType;

public class ExternalServiceConstant {
    public static String MEDIA_SERVICE = "MEDIASERVICE";
    public static final MediaType MS_EXCEL = MediaType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    public static final MediaType TXT = MediaType.parse("text/plain");
}
