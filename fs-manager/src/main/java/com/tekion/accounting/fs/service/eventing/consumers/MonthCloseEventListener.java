package com.tekion.accounting.fs.service.eventing.consumers;

import com.tekion.accounting.events.MonthCloseEvent;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.accounting.fs.common.utils.MDCUtils;
import com.tekion.accounting.fs.service.snapshots.SnapshotService;
import com.tekion.core.messaging.kafka.events.eventListener.AbstractEventListener;
import com.tekion.core.utils.UserContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class MonthCloseEventListener extends AbstractEventListener<MonthCloseEvent> {

	@Autowired
	private SnapshotService snapshotService;

	private final static String SUCCESS = "SUCCESS";

	@Override
	public void processEvent(MonthCloseEvent event) {
		MDCUtils.setMDCParamsFromUserContext(UserContextProvider.getContext());
		log.info("message received for MonthCloseEvent {}", JsonUtil.toJson(event));
		try{
			snapshotService.createSnapshotsForMappingsAndCellCodes(event.getYear(), event.getClosedMonth_0_11()+1);
		}catch(Exception e){
			log.error("Error occurred while processing the monthCloseEvent  ",e);
		}finally{
			MDCUtils.clearMDC();
		}
	}

	@Override
	public String[] supportedTypeNames() {
		return new String[]{ MonthCloseEvent.TYPE };
	}

	@Override
	public Class eventClass() {
		return MonthCloseEvent.class;
	}
}
