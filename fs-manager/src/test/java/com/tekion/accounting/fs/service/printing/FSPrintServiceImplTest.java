package com.tekion.accounting.fs.service.printing;

import com.tekion.accounting.fs.common.dpProvider.DpUtils;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.service.common.pdfPrinting.PDFPrintService;
import com.tekion.accounting.fs.service.printing.models.FSViewStatementDto;
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

import java.util.HashMap;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DpUtils.class)
public class FSPrintServiceImplTest extends TestCase {

    @InjectMocks
    FSPrintServiceImpl fsPrintService;

    @Mock
    DealerConfig dealerConfig;
    @Mock
    PDFPrintService pdfPrintService;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        mockStatic(System.class);
    }

    @Test
    public void testViewStatement() {
        Mockito.when(pdfPrintService.exportBulkPdfWithResponse(Mockito.any(), Mockito.any())).thenReturn(new Object());
        assertNotNull(fsPrintService.viewStatement(getFSViewStatementDto()));
    }

    private FSViewStatementDto getFSViewStatementDto() {
        FSViewStatementDto fsViewStatementDto = new FSViewStatementDto();
        fsViewStatementDto.setParams(new HashMap<>());
        return fsViewStatementDto;
    }
}
