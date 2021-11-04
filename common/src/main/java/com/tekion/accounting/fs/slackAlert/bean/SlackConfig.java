package com.tekion.accounting.fs.slackAlert.bean;

import com.google.common.collect.Maps;
import com.tekion.accounting.fs.slackAlert.enums.SlackAlertModuleName;
import com.tekion.clients.slack.SlackClient;
import com.tekion.core.serverconfig.service.ServerConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class SlackConfig {

    private Map<String, SlackClient> slackConfigMap;

    @Autowired
    private ServerConfigService serverConfigService;

    public SlackConfig() {
        slackConfigMap = Maps.newHashMap();
    }

    public SlackClient getSlackClientFromModuleName(SlackAlertModuleName slackAlertModuleName) {
        SlackClient slackClient;
        if(slackConfigMap.containsKey(slackAlertModuleName.name())) {
            return slackConfigMap.get(slackAlertModuleName.name());
        }
        synchronized (this) {
            if(slackConfigMap.containsKey(slackAlertModuleName.name())) {
                return slackConfigMap.get(slackAlertModuleName.name());
            }
            slackClient = SlackClient.createClient(null, null, slackAlertModuleName.name(), serverConfigService);
            if(Objects.isNull(slackClient)) {
                log.error("CRITICAL ERROR: unable to get slack client for " + slackAlertModuleName.name());
            }
            slackConfigMap.put(slackAlertModuleName.name(), slackClient);
        }
        return slackClient;
    }



}
