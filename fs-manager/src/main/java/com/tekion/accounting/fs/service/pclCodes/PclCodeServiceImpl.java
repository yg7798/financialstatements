package com.tekion.accounting.fs.service.pclCodes;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.beans.common.OemTemplate;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.dto.pclCodes.*;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import com.tekion.accounting.fs.repos.OemTemplateRepo;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Slf4j
@Service
@AllArgsConstructor
public class PclCodeServiceImpl implements PclCodeService{

    private OemTemplateRepo oemTemplateRepo;
    private OemFsCellGroupRepo oemFsCellGroupRepo;

    @Override
    public List<OemDetailsResponseDto> getOemDetails() {

        List<OemTemplate> oemTemplateList = TCollectionUtils.nullSafeList(oemTemplateRepo.findAllOemDetails());
        List<OemDetailsResponseDto> oemDetailsDtoList = new ArrayList<>();
        oemTemplateList.stream().forEach(oemTemplate -> {
            OemDetailsResponseDto oemDetailsDto = new OemDetailsResponseDto();
            oemDetailsDto.setOemId(oemTemplate.getOemId());
            oemDetailsDto.setYear(oemTemplate.getYear());
            oemDetailsDto.setCountry(oemTemplate.getCountry());
            if(!oemDetailsDtoList.contains(oemDetailsDto))
                oemDetailsDtoList.add(oemDetailsDto);
            });
        return oemDetailsDtoList;
    }

    @Override
    public List<AccountingOemFsCellGroup> getPclCodeDetails(String oemId, Integer year, String country) {
        country = defaultCountryCode(country);
        List<AccountingOemFsCellGroup> accountingOemFsCellGroupList =
                TCollectionUtils.nullSafeList(oemFsCellGroupRepo.findByOemId(oemId, year, country));
        return accountingOemFsCellGroupList;
    }

    @Override
    public void updatePclCodeDetails(AccountingOemFsCellGroup pclDetails) {
        AccountingOemFsCellGroup accountingOemFsCellGroup = oemFsCellGroupRepo
                .findByGroupCode(pclDetails.getOemId(), pclDetails.getYear(),
                        pclDetails.getGroupCode(), pclDetails.getCountry());
        if(Objects.isNull(accountingOemFsCellGroup)) {
            throw new TBaseRuntimeException("This combination of pclCode update does not exist {}, {}, {}, {}", pclDetails.getOemId(), pclDetails.getYear().toString(), pclDetails.getCountry(), pclDetails.getGroupCode());
        }
        accountingOemFsCellGroup.updateGroupCodes(pclDetails);
        oemFsCellGroupRepo.save(accountingOemFsCellGroup);
    }

    private String defaultCountryCode(String country) {
        return TStringUtils.isBlank(country) ? TConstants.COUNTRY_US : country;
    }
}
