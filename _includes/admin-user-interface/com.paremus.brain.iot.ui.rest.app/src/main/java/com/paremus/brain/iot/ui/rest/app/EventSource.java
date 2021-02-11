/*-
 * #%L
 * com.paremus.ui.rest.app2
 * %%
 * Copyright (C) 2018 - 2019 Paremus Ltd
 * %%
 * Licensed under the Fair Source License, Version 0.9 (the "License");
 *
 * See the NOTICE.txt file distributed with this work for additional
 * information regarding copyright ownership. You may not use this file
 * except in compliance with the License. For usage restrictions see the
 * LICENSE.txt file distributed with this work
 * #L%
 */
package com.paremus.brain.iot.ui.rest.app;

import com.paremus.ui.rest.api.EventApi;
import com.paremus.ui.rest.api.FrameworkInfo;
import com.paremus.ui.rest.api.WatchApi;
import com.paremus.ui.rest.dto.EventDTO;
import eu.brain.iot.eventing.monitoring.api.EventMonitor;
import eu.brain.iot.eventing.monitoring.api.FilterDTO;
import eu.brain.iot.eventing.monitoring.api.MonitorEvent;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.util.pushstream.PushEventConsumer;
import org.osgi.util.pushstream.PushStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

@Component(service = EventSource.class, immediate = true)
public class EventSource {
    @Reference
    private EventApi eventApi;

    @Reference
    private WatchApi watchApi;

    @Reference
    private FrameworkInfo frameworkInfo;

    private Map<Object, EventMonitor> eventMonitors = new ConcurrentHashMap<>();
    private Map<Object, PushStream<MonitorEvent>> eventStreams = new ConcurrentHashMap<>();
    private FilterDTO[] eventFilters = {};
    private ClientFilterWatcher filterWatcher;

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    void addEventMonitor(EventMonitor monitor, Map<String, Object> props) {
        Object id = props.get(Constants.SERVICE_ID);
        eventMonitors.put(id, monitor);

        if (eventFilters.length > 0) {
            PushStream<MonitorEvent> ps = eventStreams.remove(id);
            if (ps != null) ps.close();
            PushStream<MonitorEvent> meps = monitor.monitorEvents(eventFilters);
            eventStreams.put(id, meps);
            meps.forEachEvent(eventConsumer);
        }

        System.out.println("XXX addEventMonitor: eventStreams: " + eventStreams.keySet());
    }

    void removeEventMonitor(EventMonitor monitor, Map<String, Object> props) {
        Object id = props.get(Constants.SERVICE_ID);
        eventMonitors.remove(id);
        PushStream<MonitorEvent> ps = eventStreams.remove(id);
        if (ps != null) ps.close();

        System.out.println("XXX removeEventMonitor: eventStreams: " + eventStreams.keySet());
    }

    @Activate
    void activate() {
        filterWatcher = new ClientFilterWatcher();
        watchApi.addObserver("events", filterWatcher);
    }

    @Deactivate
    void deactivate() {
        watchApi.removeObserver("events", filterWatcher);
        eventStreams.values().forEach(es -> es.close());
    }

    private PushEventConsumer<MonitorEvent> eventConsumer = (pe) -> {
        MonitorEvent me = pe.getData();
//        System.out.println("XXX EventSource add event: " + me.eventType);
        String sourceHost = frameworkInfo.getHostname(String.valueOf(me.eventData.get("sourceNode")));
        EventDTO event = new EventDTO();
        event.data = me.eventData;
        event.topic = me.eventType;
        event.source = sourceHost;
        event.timestamp = me.publicationTime;
        eventApi.add(event);
        return PushEventConsumer.CONTINUE;
    };

    class ClientFilterWatcher implements Observer {

        @Override
        public void update(Observable o, Object arg) {
            try {
                List<List<String>> filters = watchApi.getFilters("events");
//                System.out.println("XXX EventSource filters: " + filters);

                List<FilterDTO> dtoFilters = new ArrayList<>();
                final String publishLocal = "(-publishType=LOCAL)";

                for (List<String> clientFilters : filters) {
                    FilterDTO dto = new FilterDTO();
                    dtoFilters.add(dto);

                    dto.ldapExpression = publishLocal;

                    for (String filter : clientFilters) {
                        if (filter.startsWith("(")) {
                            dto.ldapExpression = String.format("(&%s%s)", publishLocal, filter);
                        } else {
                            dto.regularExpression = filter;
                        }
                    }
                }

                eventFilters = dtoFilters.toArray(new FilterDTO[0]);

                eventMonitors.forEach((id, monitor) -> {
                    PushStream<MonitorEvent> ps = eventStreams.remove(id);
                    if (ps != null) ps.close();

                    // only subscribe if we have some clients
                    if (eventFilters.length > 0) {
                        PushStream<MonitorEvent> meps = monitor.monitorEvents(eventFilters);
                        eventStreams.put(id, meps);
                        meps.forEachEvent(eventConsumer);
                    }
                });

//                System.out.println("XXX EventSource: eventStreams: " + eventStreams.keySet());
            }
            catch (Exception e) {
                System.out.println("eek! EventSource: " + e);
                e.printStackTrace();
            }
        }
    }

}
