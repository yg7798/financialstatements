package com.tekion.accounting.fs.service.oems;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.common.OemConfig;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import com.tekion.accounting.fs.enums.FinancialYearType;
import com.tekion.accounting.fs.integration.Detail;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

@RunWith(MockitoJUnitRunner.class)
public class FordServiceImplTest extends TestCase {

    @InjectMocks
    FordServiceImpl fordService;
    @Mock
    DealerConfig dealerConfig;
    @Mock
    AbstractFinancialStatementService financialStatementService;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
    }

    @Test(expected = TBaseRuntimeException.class)
    public void testGenerateXML() {
        assertNotNull(fordService.generateXML(getFinancialStatementRequestDto()));
    }

    @Test(expected = TBaseRuntimeException.class)
    public void testSetDownloadFile() {
        fordService.downloadFile(getFinancialStatementRequestDto(), getHttpServletResponse());
        Mockito.verify(fordService, Mockito.times(1)).downloadFile(getFinancialStatementRequestDto(), getHttpServletResponse());
    }

    @Test
    public void testFlipSignIfRequiredIfNegative() {
        assertEquals(getValue2(), fordService.flipSignIfRequired(getValue1(), getDetail()));
    }

    @Test
    public void testFlipSignIfRequiredIfNonNegative() {
        assertEquals(getValue2(), fordService.flipSignIfRequired(getValue2(), getDetail()));
    }

    @Test
    public void testIfUseDownloadFileAPi() {
        assertFalse(fordService.useDownloadApiFromIntegration(new FinancialStatementRequestDto()));
    }

    private Detail getDetail() {
        Detail detail = new Detail();
        detail.setAccountId("123");
        detail.setAccountValue("xyz");
        detail.setDescription("abc");
        return detail;
    }

    private BigDecimal getValue1() {
        return new BigDecimal(-5);
    }

    private BigDecimal getValue2() {
        return new BigDecimal(5);
    }

    private HttpServletResponse getHttpServletResponse() {
        return null;
    }

    private FinancialStatementRequestDto getFinancialStatementRequestDto() {
        FinancialStatementRequestDto financialStatementRequestDto = new FinancialStatementRequestDto();
        financialStatementRequestDto.setFsId("1234");
        financialStatementRequestDto.setFinancialYearType(FinancialYearType.FISCAL_YEAR);
        return financialStatementRequestDto;
    }
}
