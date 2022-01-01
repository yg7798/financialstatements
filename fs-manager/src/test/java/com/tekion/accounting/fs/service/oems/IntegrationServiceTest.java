package com.tekion.accounting.fs.service.oems;

import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.integration.masterdata.response.ProviderDealerMappingInfoResponse;
import com.tekion.integration.masterdata.services.ProviderDealerMappingInfoService;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.TimeZone;

@RunWith(MockitoJUnitRunner.class)
public class IntegrationServiceTest extends TestCase {

    @InjectMocks
    IntegrationService integrationService;

    @Mock
    DealerConfig dealerConfig;
    @Mock
    ProviderDealerMappingInfoService providerDealerMappingInfoService;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
    }

    @Test
    public void testGetBacCodeFromIntegration() {
        Mockito.when(providerDealerMappingInfoService.getProviderDealerMappingInfo(Mockito.any())).thenReturn(getProviderDealerMappingInfoResponse1());
        assertEquals("xyz", integrationService.getBacCodeFromIntegration("Acura", "5", "abc"));
    }

    @Test
    public void testGetBacCodeFromIntegrationForNull() {
        Mockito.when(providerDealerMappingInfoService.getProviderDealerMappingInfo(Mockito.any())).thenReturn(getProviderDealerMappingInfoResponse2());
        assertNull(integrationService.getBacCodeFromIntegration("Acura", "5", "abc"));
    }

    private ProviderDealerMappingInfoResponse getProviderDealerMappingInfoResponse1() {
        ProviderDealerMappingInfoResponse providerDealerMappingInfoResponse = new ProviderDealerMappingInfoResponse();
        providerDealerMappingInfoResponse.setProviderDealerId("xyz");
        return providerDealerMappingInfoResponse;
    }

    private ProviderDealerMappingInfoResponse getProviderDealerMappingInfoResponse2() {
        return new ProviderDealerMappingInfoResponse();
    }

}
