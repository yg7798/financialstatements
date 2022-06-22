package com.tekion.accounting.fs.service.oems;

import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.common.utils.TimeUtils;
import com.tekion.accounting.fs.dto.request.FinancialReportRequestBody;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.dealersettings.dealermaster.beans.DealerMaster;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class FsXMLServiceImplTest extends TestCase {

    @InjectMocks
    FsXMLServiceImpl fsXMLService;
    @InjectMocks
    TimeUtils timeUtils;

    @Mock
    DealerConfig dealerConfig;
    @Mock
    RestTemplate restTemplate;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        fsXMLService.postContruct();
    }

    @Test
    public void testGetFinancialStatement(){
        Mockito.when(dealerConfig.getDealerMaster()).thenReturn(getDealerMaster());
        ResponseEntity<String> responseEntityResponseEntity = new ResponseEntity<>("xyz", HttpStatus.OK);
        Mockito.when(restTemplate.exchange(
                Mockito.anyString(),
                Mockito.any(HttpMethod.class),
                Mockito.any(),
                Mockito.<Class<String>>any()))
                .thenReturn(responseEntityResponseEntity);
        assertNotNull(fsXMLService.getFinancialStatement(getFinancialReportRequestBody()));
    }

    private FinancialReportRequestBody getFinancialReportRequestBody(){
        FinancialReportRequestBody financialReportRequestBody=new FinancialReportRequestBody();
        financialReportRequestBody.setYear("2022");
        financialReportRequestBody.setMonth("1");
        return financialReportRequestBody;
    }

    private DealerMaster getDealerMaster() {
        DealerMaster dealerMaster = new DealerMaster();
        dealerMaster.setTenantId("2");
        dealerMaster.setId("1234");
        dealerMaster.setOemDealerId("5");
        return dealerMaster;
    }
}
