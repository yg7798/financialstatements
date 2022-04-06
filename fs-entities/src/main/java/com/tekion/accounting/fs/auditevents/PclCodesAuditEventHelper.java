package com.tekion.accounting.fs.auditevents;

import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.audit.client.manager.impl.AuditEventDTO;
import lombok.experimental.UtilityClass;

import java.util.UUID;

import static com.tekion.core.utils.UserContextProvider.*;

@UtilityClass
public class PclCodesAuditEventHelper {

    public static final Integer SIXTY_DAYS = 60;

    public AuditEventDTO getAuditEvent(AccountingOemFsCellGroupAuditEvent auditEvent) {
        AuditEventDTO auditEventDTO = new AuditEventDTO();
        auditEventDTO.setAssetId(auditEvent.getId());
        auditEventDTO.setAssetType(TConstants.FS_GROUP_CODE);
        auditEventDTO.setAuditId(UUID.randomUUID().toString());
        auditEventDTO.setCompleteJsonEvent(JsonUtil.toJson(auditEvent));
        auditEventDTO.setDealerId(getCurrentDealerId());
        auditEventDTO.setTenantId(getCurrentTenantId());
        auditEventDTO.setTtlInDays(SIXTY_DAYS);
        auditEventDTO.setUserId(getCurrentUserId());
        return auditEventDTO;
    }
}
