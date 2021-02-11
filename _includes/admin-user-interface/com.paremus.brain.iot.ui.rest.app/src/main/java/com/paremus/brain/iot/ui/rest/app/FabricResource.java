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

import com.paremus.ui.rest.api.AbstractResource;
import com.paremus.ui.rest.api.ParemusRestUI;
import com.paremus.ui.rest.dto.FabricDTO;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

@Component(service = FabricResource.class,
        configurationPid = FabricResource.PID,
        configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = FabricResource.FabricConfig.class)
@ParemusRestUI
@JaxrsResource
@JaxrsName("Paremus Fabrics")
@Path(FabricResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
@RequiresAuthentication
public class FabricResource extends AbstractResource<FabricDTO> {
    public static final String PID = "com.paremus.ui.fabric";
    public static final String PATH = "fabrics";
    private LinkedHashMap<String, FabricDTO> dtos = new LinkedHashMap<>();
    private LinkedHashMap<String, FabricCredentials> credentials = new LinkedHashMap<>();
    private File trustStoreFile;
    private String trustStorePassword;

    public FabricResource() {
        super(FabricDTO.class);
    }

    @Activate
    @SuppressWarnings("unused")
    private void activate(FabricConfig fabricConfig) throws Exception {
        System.err.println("FabricResource: activated!");
        String id = fabricConfig.fabricName();
        FabricDTO dto = dtos.get(id);
        if (dto == null) {
            dto = new FabricDTO();
            dto.id = id;
            dto.systems = Collections.emptyList();
            dtos.put(id, dto);
            credentials.put(id, new FabricCredentials());
            System.err.println("FabricResource: new fabric id=" + id);
        }
        dto.managementURIs = new LinkedList<>();
        dto.managementURIs.add(fabricConfig.url());
        dto.location = fabricConfig.location();
        dto.owner = fabricConfig.owner();
        dto.expectedFibres = fabricConfig.expectedFibres();
        FabricCredentials fabricCredentials = credentials.get(id);
        fabricCredentials.username = fabricConfig.username();
        fabricCredentials.password = fabricConfig.password();
    }

    @Override
    protected Collection<FabricDTO> getDtos() {
        dtos.forEach((id, dto) -> {
            try {
                RemoteFabricClient client = new RemoteFabricClient(trustStoreFile, trustStorePassword);
                FabricCredentials fabricCredentials = credentials.get(dto.id);
                List<Map<String, Object>> fibres = client.JSONBodyAsMap(URI.create(dto.managementURIs.toArray()[0] + "/fibres"), fabricCredentials.username, fabricCredentials.password);
                dto.actualFibres = fibres.size();
                List<Map<String, Object>> systems = client.JSONBodyAsMap(URI.create(dto.managementURIs.toArray()[0] + "/systems"), fabricCredentials.username, fabricCredentials.password);
                List<FabricDTO.SystemDTO> systemsDTO = new ArrayList<>();
                dto.systems = systemsDTO;
                systems.forEach(system -> {
                    FabricDTO.SystemDTO systemDTO = new FabricDTO.SystemDTO();
                    String systemId = (String) system.get("id");
                    String[] items = systemId.split("#");
                    systemDTO.id = items[0];
                    systemDTO.version = items[1];
                    systemDTO.running = (Boolean) system.get("deployed");
                    systemsDTO.add(systemDTO);
                });
            } catch (IOException ioe) {
                System.err.println(ioe);
            }
        });
        return dtos.values();
    }

    @Override
    protected FabricDTO getDto(String id) {
        return dtos.get(id);
    }

    @ObjectClassDefinition(
            name = "Fabric Configuration",
            description = "Federation Viewer Fabric Configuration."
    )
    @interface FabricConfig {
        @AttributeDefinition(type = AttributeType.STRING,
                name = "fabric name",
                description = "fabric name"
        )
        String fabricName();

        @AttributeDefinition(type = AttributeType.STRING,
                name = "url",
                description = "url")
        String url();

        @AttributeDefinition(type = AttributeType.STRING,
                name = "username",
                description = "username")
        String username() default "Admin";

        @AttributeDefinition(type = AttributeType.PASSWORD,
                name = "password",
                description = "password"
        )
        String password();

        @AttributeDefinition(type = AttributeType.STRING,
                name = "location",
                description = "location"
        )
        String location();

        @AttributeDefinition(type = AttributeType.STRING,
                name = "owner",
                description = "owner"
        )
        String owner();

        @AttributeDefinition(type = AttributeType.INTEGER,
                name = "expected fibres",
                description = "expected fibres"
        )
        int expectedFibres();

    }

    static class FabricCredentials {
        String username;
        String password;
    }
}
