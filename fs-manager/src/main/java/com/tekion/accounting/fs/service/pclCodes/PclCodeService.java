package com.tekion.accounting.fs.service.pclCodes;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.dto.pclCodes.OemDetailsResponseDto;

import java.util.List;

public interface PclCodeService {

    List<OemDetailsResponseDto> getOemDetails();

    List<AccountingOemFsCellGroup>  getPclCodeDetails(String oemId, Integer year, String country);

    void updatePclCodeDetails(AccountingOemFsCellGroup pclDetailsDto);
}
