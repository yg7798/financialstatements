package com.tekion.accounting.fs.service.common;

import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.service.common.pdfPrinting.PDFPrintService;
import com.tekion.accounting.fs.service.common.pdfPrinting.dto.MediaItem;
import com.tekion.accounting.fs.service.common.pdfPrinting.dto.MediaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.FileNameUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileCommons {

    private final PDFPrintService pdfPrintService;

    public File downloadFileUsingMediaId(String mediaId) throws IOException {
        File file = File.createTempFile(FileNameUtils.getBaseName(UUID.randomUUID().toString()), TConstants.XLSX_FILE_EXTENSION);
        downloadFileFromUrl(getUrlFromMediaId(mediaId), file);
        return file;
    }

    public void downloadFileFromUrl(String url, File file) throws IOException {
        java.net.URL fileUrl = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(fileUrl.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
    }

    public String getUrlFromMediaId(String mediaId){
        List<MediaItem> mediaIds = new ArrayList<>();
        mediaIds.add(MediaItem.builder().id(mediaId).build());
        MediaResponse mediaResponse = pdfPrintService.requestMediaServiceForSignedUrl(mediaIds).get(0);
        return mediaResponse.getResponseMap().get("url");
    }

    public File downloadFileUsingPresignedUrl(String url) throws IOException {
        File file = File.createTempFile(FileNameUtils.getBaseName(UUID.randomUUID().toString()), TConstants.XLSX_FILE_EXTENSION);
        downloadFileFromUrl(url, file);
        return file;
    }
}
