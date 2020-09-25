/* Copyright 2019 Paremus, Ltd - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.paremus.brain.iot.example.sensor.impl;

import com.paremus.brain.iot.example.sensor.api.SensorReadingDTO;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.EventBus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardResource;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * This component triggers sensor readings based on web clicks
 */
@Component(service=RestComponentImpl.class)
@JaxrsResource
@HttpWhiteboardResource(pattern="/sensor-ui/*", prefix="/static")
// SmartBehaviourDefinition is just so example sensor is added to repository
@SmartBehaviourDefinition(consumed = {}, // this component does not consume events
        author = "Paremus", name = "Example Smart Security Sensor",
        description = "Implements a Smart Security Sensor and UI to display it.")
public class RestComponentImpl {

    @Reference
    private EventBus eventBus;

    @Path("sensor")
    @POST
    public void trigger() {
        eventBus.deliver(new SensorReadingDTO());
    }

}
