package com.tekion.accounting.fs.service.integration;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.common.dpProvider.DpUtils;
import com.tekion.accounting.fs.dto.integration.FSIntegrationRequest;
import com.tekion.accounting.fs.dto.integration.FSSubmitResponse;
import com.tekion.accounting.fs.service.oems.OEMInfo;
import com.tekion.core.feign.ClientBuilder;
import com.tekion.core.service.internalauth.AbstractServiceClientFactory;
import com.tekion.core.utils.TRequestUtils;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.tekion.accounting.fs.service.integration.IntegrationClient.OEM;
import static com.tekion.accounting.fs.service.integration.IntegrationClient.SITE;
import static com.tekion.admin.beans.global.GlobalMakesEntity.Constants.BRAND;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DpUtils.class)
public class IntegrationClientTest extends TestCase {

    @InjectMocks
    IntegrationClient integrationClient;

    @Mock
    DealerConfig dealerConfig;
    @Mock
    IntegrationInternal integrationInternal;
    @Mock
    AbstractServiceClientFactory abstractServiceClientFactory;
    @Mock
    ClientBuilder clientBuilder;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        mockStatic(System.class);
        when(abstractServiceClientFactory.getServiceBaseUrl(Mockito.anyString())).thenReturn("xyz");
        Mockito.when(clientBuilder.buildClient(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new IntegrationInternal() {
            @Override
            public FSSubmitResponse submitFS(Map<String, String> headerMap, FSIntegrationRequest fsRequest) {
                return getFSSubmitResponse();
            }

            @Override
            public FSSubmitResponse downloadFs(Map<String, String> headerMap, FSIntegrationRequest fsRequest) {
                return null;
            }


        });
        integrationClient.init();
    }

    @Test
    public void testSubmitFS() {
        assertEquals(getFSSubmitResponse(), integrationClient.submitFS(new FSIntegrationRequest()));
    }

    @Test
    public void testSubmitFsWithId() {
        assertEquals(getFSSubmitResponse(), integrationClient.submitFS(new FSIntegrationRequest(), new OEMInfo("Acura", "abc"), "-1_5"));
    }

    @Test
    public void testGetHeaders() {
        assertEquals(getHeaders(), integrationClient.getHeaders());
    }

    @Test
    public void testGetHeadersWithId() {
        OEMInfo oemInfo = new OEMInfo("Acura", "abc");
        Map<String, String> headers = getHeaders();
        headers.put(OEM, oemInfo.getOem());
        headers.put(BRAND, oemInfo.getBrand());
        headers.put(SITE, "-1_5");
        assertEquals(headers, integrationClient.getHeaders(oemInfo, "-1_5"));
    }

    private FSSubmitResponse getFSSubmitResponse() {
        FSSubmitResponse fsSubmitResponse = new FSSubmitResponse();
        fsSubmitResponse.setStatus("true");
        FSSubmitResponse.IntegrationResponse integrationResponse = new FSSubmitResponse.IntegrationResponse();
        integrationResponse.setStatus("true");
        integrationResponse.setErrorDetail(new ArrayList<>());
        fsSubmitResponse.setResponse(integrationResponse);
        return fsSubmitResponse;
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = TRequestUtils.userCallHeaderMap();
        return headers;
    }

    private Map<String, String> getHeaders1() {
        Map<String, String> headers1 = new HashMap<>();
        headers1.put("clientid", "console");
        headers1.put("rule-workflow-idempotent-key", null);
        headers1.put("roleid", null);
        headers1.put("dealerid", "4");
        headers1.put("tekion-api-token", null);
        headers1.put("tenantid", "ca");
        headers1.put("usertype", null);
        headers1.put("content-type", "application/json");
        headers1.put("tenantname", null);
        headers1.put("userid", "-1");
        headers1.put("oem", "Acura");
        headers1.put("brand", "abc");
        headers1.put("tek-siteid", "-1_5");
        return headers1;
    }


}
