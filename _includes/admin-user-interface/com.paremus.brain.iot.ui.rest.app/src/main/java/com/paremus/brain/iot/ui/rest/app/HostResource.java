/*-
 * #%L
 * com.paremus.ui.rest
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

import com.paremus.brain.iot.management.api.BehaviourManagement;
import com.paremus.ui.rest.api.AbstractResource;
import com.paremus.ui.rest.api.FrameworkInfo;
import com.paremus.ui.rest.api.ParemusRestUI;
import com.paremus.ui.rest.api.RestUtils;
import eu.brain.iot.installer.api.BehaviourDTO;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component(service = HostResource.class)
@ParemusRestUI
@JaxrsResource
@JaxrsName("Paremus Hosts")
@Path("hosts")
@Produces(MediaType.APPLICATION_JSON)
@RequiresAuthentication
public class HostResource extends AbstractResource<HostDTO> {

    @Reference
    private FrameworkInfo frameworkInfo;

    @Reference
    private BehaviourResource behaviourResource;

    @Reference
    private BehaviourManagement management;

    public HostResource() {
        super(HostDTO.class);
    }

    @Override
    protected Collection<HostDTO> getDtos() {
        List<HostDTO> hosts = new ArrayList<>();

        frameworkInfo.getHostMap().forEach((id, host) -> {
            HostDTO dto = new HostDTO();
            dto.id = host;
            dto.fwId = id;
            hosts.add(dto);
        });

        hosts.forEach(dto -> {
            dto.behaviours = behaviourResource.getBehaviours(dto.id);
            dto.info = frameworkInfo.getHostInfo(dto.fwId);
        });

        return hosts;
    }

    @Override
    protected HostDTO getDto(String host) {
        HostDTO dto = new HostDTO();
        dto.id = host;
        dto.fwId = frameworkInfo.getFrameworkId(host);
        if (dto.fwId == null) {

            return null;
        } else {
            dto.behaviours = behaviourResource.getBehaviours(dto.id);
            dto.info = frameworkInfo.getHostInfo(dto.fwId);
            return dto;
        }
    }

    @PUT
    @Path("{id}")
    public HostDTO uninstallBehaviour(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                      HostDTO hostDTO, @PathParam("id") String id) {
        if (hostDTO.uninstall == null || hostDTO.uninstall.isEmpty()
                || hostDTO.fwId == null || hostDTO.fwId.isEmpty()) {
            RestUtils.badRequestError(request, response, "uninstall or target missing");
        } else {
            String targetNode = hostDTO.fwId;


            if ("ALL".equals(hostDTO.uninstall)) {
                management.resetNode(targetNode);
            }
            else {
                // e.g. com.paremus.smart.security.SecurityBehaviour:1.0.0
                String[] split = hostDTO.uninstall.split(":");
                BehaviourDTO behaviourDTO = new BehaviourDTO();
                behaviourDTO.bundle = split[0];
                behaviourDTO.version = split.length > 1 ? split[1] : "0";
                management.uninstallBehaviour(behaviourDTO, targetNode);
            }
        }

        return hostDTO;
    }

}

