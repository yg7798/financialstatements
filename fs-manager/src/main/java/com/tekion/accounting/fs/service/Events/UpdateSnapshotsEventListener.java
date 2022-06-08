package com.tekion.accounting.fs.service.Events;

import com.tekion.accounting.events.UpdateSnapshotsEvent;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.accounting.fs.common.utils.MDCUtils;
import com.tekion.accounting.fs.service.snapshots.SnapshotService;
import com.tekion.core.messaging.kafka.events.eventListener.AbstractEventListener;
import com.tekion.core.utils.UserContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class UpdateSnapshotsEventListener extends AbstractEventListener<UpdateSnapshotsEvent> {
    @Autowired
    SnapshotService snapshotService;

    @Override
    public void processEvent(UpdateSnapshotsEvent event) {
        MDCUtils.setMDCParamsFromUserContext(UserContextProvider.getContext());
        log.info("message received for UpdateSnapshotsEvent {}", JsonUtil.toJson(event));
        try {
            snapshotService.updateSnapshots(event);
        }catch (Exception e){
            log.error("Error occurred while processing the UpdateSnapshotsEvent  ",e);
        } finally {
            MDCUtils.clearMDC();
        }
    }

    @Override
    public String[] supportedTypeNames() {
        return new String[]{UpdateSnapshotsEvent.EVENT_TYPE};
    }

    @Override
    public Class<UpdateSnapshotsEvent> eventClass() {
        return UpdateSnapshotsEvent.class;
    }
}
