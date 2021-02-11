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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component(service = BehaviourResource.class)
@ParemusRestUI
@JaxrsResource
@JaxrsName("Smart Behaviors")
@Path(BehaviourResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
@RequiresAuthentication
public class BehaviourResource extends AbstractResource<SmartBehaviourDTO> {

    public static final String PATH = "behaviours";

    @Reference
    private BehaviourManagement management;

    @Reference
    private FrameworkInfo frameworkInfo;

    private Map<String, SmartBehaviourDTO> dtos = new ConcurrentHashMap<>();

    public BehaviourResource() {
        super(SmartBehaviourDTO.class);
    }

    @Override
    protected synchronized Collection<SmartBehaviourDTO> getDtos() {
        dtos.clear();
        try {
            Collection<BehaviourDTO> behaviours = management.findBehaviours(null);

            List<String> nameVersions = behaviours.stream()
                    .map(b -> b.bundle + ":" + b.version)
                    .collect(Collectors.toList());

            Map<String, List<String>> hostMap = frameworkInfo.getHosts(nameVersions);

            behaviours.forEach(b -> {
                SmartBehaviourDTO dto = newBehaviour(b, hostMap);
                dtos.put(dto.id, dto);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dtos.values();
    }

    @Override
    protected SmartBehaviourDTO getDto(String id) {
        SmartBehaviourDTO dto = dtos.get(id);

        if (dto == null) {
            String[] nameVersion = id.split(":");

            if (nameVersion.length == 2) {
                Map<String, List<String>> hostMap = frameworkInfo.getHosts(Arrays.asList(id));
                String filter = String.format("(&(osgi.identity=%s)(version=%s))", nameVersion[0], nameVersion[1]);
                try {
                    dto = management.findBehaviours(filter).stream()
                            .map(b -> newBehaviour(b, hostMap))
                            .findFirst()
                            .orElse(null);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return dto;
    }

    @PUT
    @Path("{id}")
    public SmartBehaviourDTO installBehaviour(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                             SmartBehaviourDTO sbDto, @PathParam("id") String id) {
        if ((sbDto.installHost == null || sbDto.installHost.isEmpty()) ||
                (sbDto.bundle == null || sbDto.bundle.isEmpty()) ||
                (sbDto.version == null || sbDto.version.isEmpty())) {
            RestUtils.badRequestError(request, response, "bundle, version or installHost missing");
            return null;
        }

        String targetHost = frameworkInfo.getFrameworkId(sbDto.installHost);

        if (targetHost == null) {
            RestUtils.badRequestError(request, response, "installHost not valid");
            return null;
        }

        BehaviourDTO bDto = new BehaviourDTO();
        bDto.bundle = sbDto.bundle;
        bDto.version = sbDto.version;

        // eek! remote call fails to deserialize args if sbDto used
        management.installBehaviour(bDto, targetHost);

        return sbDto;
    }

    private SmartBehaviourDTO newBehaviour(BehaviourDTO dto, Map<String, List<String>> hostMap) {
        SmartBehaviourDTO sdto = new SmartBehaviourDTO();
        sdto.author = dto.author;
        sdto.consumed = dto.consumed;
        sdto.name = dto.name;
        sdto.description = dto.description;
        sdto.bundle = dto.bundle;
        sdto.version = dto.version;

        sdto.id = dto.bundle + ":" + dto.version;
        sdto.hosts = hostMap.containsKey(sdto.id) ? hostMap.get(sdto.id) : Collections.emptyList();

        return sdto;
    }

    Map<String, String> getBehaviours(String host) {
        if (dtos.isEmpty()) {
            getDtos();
        }
        return dtos.values().stream()
                .filter(d -> d.hosts.contains(host))
                .collect(Collectors.toMap(d -> d.bundle + ":" + d.version, d -> d.name));
    }

}
