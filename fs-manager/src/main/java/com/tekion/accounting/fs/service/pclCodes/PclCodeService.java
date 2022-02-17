package com.tekion.accounting.fs.service.pclCodes;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.dto.pclCodes.MediaRequestDto;
import com.tekion.accounting.fs.dto.pclCodes.OemDetailsResponseDto;
import com.tekion.accounting.fs.dto.pclCodes.PclDownloadRequestDto;
import com.tekion.accounting.fs.dto.pclCodes.PclFilterRequestDto;
import com.tekion.core.excelGeneration.models.model.MediaUploadResponse;

import java.util.List;
import java.util.Map;

public interface PclCodeService {

    List<OemDetailsResponseDto> getOemDetails();

    List<AccountingOemFsCellGroup>  getPclCodeDetails(String oemId, Integer year, String country);

    void updatePclCodeDetails(AccountingOemFsCellGroup pclDetailsDto);

    void updatePclCodesInBulk(MediaRequestDto requestDto);

    List<AccountingOemFsCellGroup> getOemDetailsWithFilter(PclFilterRequestDto requestDto);

    Map<String, String> downloadOemDetailsAndProvidePresignedUrl(PclDownloadRequestDto requestDto);

    String updatePclCodes(MediaRequestDto requestDto);
}
