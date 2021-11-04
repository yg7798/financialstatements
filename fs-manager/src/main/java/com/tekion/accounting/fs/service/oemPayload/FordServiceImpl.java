package com.tekion.accounting.fs.service.oemPayload;

import com.tekion.accounting.fs.beans.integration.Detail;
import com.tekion.accounting.fs.dto.oemPayload.FinancialStatementRequestDto;
import com.tekion.accounting.fs.enums.AccountingError;
import com.tekion.accounting.fs.service.integration.IntegrationClient;
import com.tekion.accounting.fs.utils.DealerConfig;
import com.tekion.accounting.fs.utils.JsonUtil;
import com.tekion.core.exceptions.TBaseRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

@Slf4j
@Component
public class FordServiceImpl extends AbstractFinancialStatementService {

    public FordServiceImpl(DealerConfig dc, IntegrationClient ic, FsXMLServiceImpl fs) {
        super(dc, ic, fs);
    }

    @Override
    public String generateXML(FinancialStatementRequestDto requestDto) {
        throw new TBaseRuntimeException(AccountingError.notSupported);
    }

    @Override
    public void downloadFile(FinancialStatementRequestDto requestDto, HttpServletResponse response) {
        throw new TBaseRuntimeException(AccountingError.notSupported);
    }

    @Override
    protected BigDecimal flipSignIfRequired(BigDecimal val, Detail detail) {
        if (val.compareTo(BigDecimal.ZERO) < 0) {
            log.info("ford, flipping sign for {}", JsonUtil.toJson(detail));
            if ("+".equals(detail.getOemCodeSign())) {
                detail.setOemCodeSign("-");
            } else if ("-".equals(detail.getOemCodeSign())) {
                detail.setOemCodeSign("+");
            }

            return val.negate();
        }
        return val;
    }
}
