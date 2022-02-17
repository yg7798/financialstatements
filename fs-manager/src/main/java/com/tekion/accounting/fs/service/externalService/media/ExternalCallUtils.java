package com.tekion.accounting.fs.service.externalService.media;

import com.tekion.core.utils.TRequestUtils;
import com.tekion.core.utils.TStringUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@UtilityClass
@Slf4j
public class ExternalCallUtils {

    private static OkHttpClient okHttpClient;

    static {
        okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

    }

    public static Response uploadFileToMediaService(String url, File file, String reportFileName, MediaType mediaType) throws IOException {

        MultipartBody build = new MultipartBody.Builder()

                .addFormDataPart("uploadFile", reportFileName, RequestBody.create(mediaType, file)).build();
        Headers of = Headers.of(getHeaders(TRequestUtils.userCallHeaderMap()));
        Request request = new Request.Builder()
                .headers(of)
                .post(build)
                .url(url)
                .build();
        return okHttpClient.newCall(request).execute();
    }

    public static Map<String, String> getHeaders(Map<String,String> headerMap) {
        Map<String, String> mapToReturn = new HashMap<>();
        for (Map.Entry<String, String> stringStringEntry : headerMap.entrySet()) {
            if (TStringUtils.isBlank(stringStringEntry.getValue()) || TStringUtils.isBlank(stringStringEntry.getKey())) {
                continue;
            }
            mapToReturn.put(stringStringEntry.getKey(), stringStringEntry.getValue());
        }
        return mapToReturn;
    }
}
