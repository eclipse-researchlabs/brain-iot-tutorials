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

import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.web.env.DefaultWebEnvironment;
import org.apache.shiro.web.env.MutableWebEnvironment;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.framework.*;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.context.ServletContextHelper;
import org.osgi.service.http.whiteboard.annotations.RequireHttpWhiteboard;
import org.osgi.service.jaxrs.whiteboard.annotations.RequireJaxrsWhiteboard;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import static com.paremus.ui.rest.api.RestUtils.ROOT_CONTEXT_FILTER;
import static com.paremus.ui.rest.api.RestUtils.ROOT_CONTEXT_NAME;
import static org.apache.shiro.web.env.EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY;
import static org.osgi.namespace.service.ServiceNamespace.SERVICE_NAMESPACE;
import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.*;
import static org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants.JAX_RS_APPLICATION_SELECT;

@RequireConfigurationAdmin
@RequireJaxrsWhiteboard
@RequireHttpWhiteboard
@Requirement(namespace = SERVICE_NAMESPACE, filter = "(osgi.jaxrs.name=aries.shiro.authz)")
@Component
@SuppressWarnings("unused")
public class AppSetup {

    private BundleContext context;

    @Reference
    @SuppressWarnings("unused")
    private ConfigurationAdmin cm;

    private ServiceRegistration<ServletContextHelper> rootContext;
    private ServiceRegistration<ServletContextHelper> exampleContext;
    private ServiceRegistration<Filter> shiroFilter;

    @Activate
    private void activate(BundleContext context) throws Exception {
        System.out.println("WebSetup activated!");

        this.context = context;

        Dictionary<String, Object> uiConfig = new Hashtable<>();
        uiConfig.put("title", "Brain IoT");
        uiConfig.put("dashHead", "Welcome to the Brain IoT dashboard");
        uiConfig.put("dashContent", "You can browse, install and configure Smart Behaviours and view events.");
        uiConfig.put("hide", new String[] {"ensembles"});

        Configuration uiCfg = cm.getConfiguration("com.paremus.ui.client", "?");
        uiCfg.update(uiConfig);

        Dictionary<String, Object> metaConfig = new Hashtable<>();
        metaConfig.put("excludes", new String[] {"UnknownConfigured"});
        Configuration metaCfg = cm.getConfiguration("com.paremus.ui.metaconfig", "?");
        metaCfg.update(metaConfig);

        Dictionary<String, Object> fabricConfig = new Hashtable<>();
        fabricConfig.put("fabricName", "local");
        fabricConfig.put("url", "https://localhost:9006");
        fabricConfig.put("username", "AdminUser");
        fabricConfig.put("password", "AdminUser");
        fabricConfig.put("location", "Local");
        fabricConfig.put("owner", "Admin");
        fabricConfig.put("expectedFibres", 1);

        uiCfg = cm.getConfiguration(FabricResource.PID, "?");
        uiCfg.update(fabricConfig);

        // BMS marketplace indexes are usually set by Fabric system.xml
        // This setting allows standalone testing.
        String bmsIndexes = System.getProperty("com.paremus.brain.iot.ui.bms.indexes");
        if (bmsIndexes != null) {
            Dictionary<String, Object> bmsConfig = new Hashtable<>();
            bmsConfig.put("indexes", bmsIndexes.split("\\s+"));
            Configuration bmsCfg = cm.getConfiguration("eu.brain.iot.BehaviourManagementService", "?");
            bmsCfg.update(bmsConfig);
        }

        // target jackson to all apps (to include default used by examples)
        setPid("org.apache.aries.jax.rs.jackson",
                // JAX_RS_APPLICATION_SELECT, "(osgi.jaxrs.name=paremus-rest-ui)");
                JAX_RS_APPLICATION_SELECT, "(osgi.jaxrs.name=*)");

        setPid("org.apache.aries.jax.rs.shiro.authorization",
                JAX_RS_APPLICATION_SELECT, "(osgi.jaxrs.name=paremus-rest-ui)",
                "paremus.security.authz", "shiro");

        setFactoryPid("org.apache.aries.jax.rs.whiteboard",
                HTTP_WHITEBOARD_CONTEXT_SELECT, ROOT_CONTEXT_FILTER,
                "paremus.whiteboard.name", "paremus-rest");

        // HTTP server is configured by Fabric system.xml
//        setPid("org.apache.felix.http",
//                "org.eclipse.jetty.servlet.SessionCookie", "Paremus-SessionId",
//                "org.osgi.service.http.port", "8080");

        rootContext = registerRootContext();
        shiroFilter = registerShiroFilter();
        exampleContext = registerExampleContext();
    }

    @Deactivate
    private void deactivate(BundleContext context) throws Exception {
        shiroFilter.unregister();
        rootContext.unregister();
        exampleContext.unregister();

        setPid("org.apache.felix.http");
        setFactoryPid("org.apache.aries.jax.rs.whiteboard");
        setPid("org.apache.aries.jax.rs.shiro.authorization");
        setPid("org.apache.aries.jax.rs.jackson");
    }

    private ServiceRegistration<ServletContextHelper> registerRootContext() {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(Constants.SERVICE_RANKING, -1); // so ClientServlet doesn't steal webconsole
        props.put(HTTP_WHITEBOARD_CONTEXT_NAME, ROOT_CONTEXT_NAME);
        props.put(HTTP_WHITEBOARD_CONTEXT_PATH, "/");
        ServletContextHelper helper = new ServletContextHelper() {
        };
        return context.registerService(ServletContextHelper.class, helper, props);
    }

    // override the default context to support running the security light example
    // in same framework as UI
    private ServiceRegistration<ServletContextHelper> registerExampleContext() {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(HTTP_WHITEBOARD_CONTEXT_NAME, "default");
        props.put(HTTP_WHITEBOARD_CONTEXT_PATH, "/example");

        ServiceFactory<ServletContextHelper> factory = new ServiceFactory<ServletContextHelper>() {
            @Override
            public ServletContextHelper getService(Bundle bundle, ServiceRegistration<ServletContextHelper> registration) {
                return new ServletContextHelper(bundle) {
                };
            }

            @Override
            public void ungetService(Bundle bundle, ServiceRegistration<ServletContextHelper> registration, ServletContextHelper service) {
            }
        };

        return context.registerService(ServletContextHelper.class, factory, props);
    }

    private Realm setupRealm() {
        SimpleAccountRealm realm = new SimpleAccountRealm();
        realm.addAccount("admin", "admin");
        return realm;
    }

    private ServiceRegistration<Filter> registerShiroFilter() {
        Realm realm = setupRealm();

        Filter filter = new ShiroFilter() {
            @Override
            public void init() throws Exception {
                WebEnvironment env = getShiroWebEnvironment(filterConfig, realm);
                getServletContext().setAttribute(ENVIRONMENT_ATTRIBUTE_KEY, env);
                super.init();
            }
        };

        Dictionary<String, Object> props = new Hashtable<>();
        props.put(HTTP_WHITEBOARD_CONTEXT_SELECT, ROOT_CONTEXT_FILTER);
        props.put(HTTP_WHITEBOARD_FILTER_NAME, "ShiroAuthenticationFilter");
        props.put(HTTP_WHITEBOARD_FILTER_PATTERN, "/*");
        props.put(HTTP_WHITEBOARD_FILTER_DISPATCHER, new String[]{DISPATCHER_ASYNC, DISPATCHER_ERROR,
                DISPATCHER_FORWARD, DISPATCHER_INCLUDE, DISPATCHER_REQUEST});

        return context.registerService(Filter.class, filter, props);
    }

    private WebEnvironment getShiroWebEnvironment(FilterConfig config, Realm realm) {
        MutableWebEnvironment env = new DefaultWebEnvironment();

        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(realm);
        CookieRememberMeManager rememberMeManager = (CookieRememberMeManager) securityManager.getRememberMeManager();
        rememberMeManager.getCookie().setName("Paremus-RememberMe");

        env.setWebSecurityManager(securityManager);
        return env;
    }

    private void configure(String pid, boolean factory, String... kv) throws IOException {
        if (kv.length % 2 != 0)
            throw new IllegalArgumentException("odd number of key/value pairs");
        Configuration config = factory ? cm.getFactoryConfiguration(pid, "?")
                : cm.getConfiguration(pid, "?");

        if (kv.length == 0) {
            config.delete();
        } else {
            Dictionary<String, Object> props = new Hashtable<>();
            for (int i = 0; i < kv.length; i += 2) {
                props.put(kv[i], kv[i + 1]);
            }
            config.update(props);
        }
    }

    private void setFactoryPid(String factoryPid, String... kv) throws IOException {
        configure(factoryPid, true, kv);
    }

    private void setPid(String pid, String... kv) throws IOException {
        configure(pid, false, kv);
    }
}
