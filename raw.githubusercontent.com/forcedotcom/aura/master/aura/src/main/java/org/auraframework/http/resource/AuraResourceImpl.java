/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.auraframework.http.resource;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.auraframework.adapter.ConfigAdapter;
import org.auraframework.adapter.ExceptionAdapter;
import org.auraframework.adapter.ServletUtilAdapter;
import org.auraframework.annotations.Annotations.ServiceComponent;
import org.auraframework.http.ManifestUtil;
import org.auraframework.http.RequestParam.StringParam;
import org.auraframework.service.ContextService;
import org.auraframework.service.DefinitionService;
import org.auraframework.service.InstanceService;
import org.auraframework.service.ServerService;
import org.auraframework.system.AuraContext;
import org.auraframework.system.AuraContext.Format;
import org.auraframework.system.AuraResource;
import org.auraframework.util.AuraTextUtil;
import org.auraframework.util.json.JsonReader;
import org.springframework.context.annotation.Lazy;

@ServiceComponent
public abstract class AuraResourceImpl implements AuraResource {
    private final String name;
    private final Format format;

    protected ContextService contextService;
    protected DefinitionService definitionService;
    protected ServletUtilAdapter servletUtilAdapter;
    protected ConfigAdapter configAdapter;
    protected ServerService serverService;
    protected InstanceService instanceService;
    protected ExceptionAdapter exceptionAdapter;
    protected ManifestUtil manifestUtil;

    public AuraResourceImpl(String name, Format format) {
        this(name, format, false);
    }

    @Deprecated
    public AuraResourceImpl(String name, Format format, boolean CSRFProtect) {
        this.name = name;
        this.format = format;
    }

    public AuraResourceImpl(String name, Format format, ManifestUtil manifestUtil) {
        this.name = name;
        this.format = format;
        this.manifestUtil = manifestUtil;
    }

    @Override
    public void setContentType(HttpServletResponse response) {
        response.setContentType(this.servletUtilAdapter.getContentType(this.format));
    }

    @Override
    public abstract void write(HttpServletRequest request, HttpServletResponse response, AuraContext context) throws IOException;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Format getFormat() {
        return format;
    }

    @Deprecated
    @Override
    public boolean isCSRFProtect() {
        return false;
    }

    /**
     * Injection override.
     *
     * @param definitionService the definitionService to set
     */
    @Inject
    public void setDefinitionService(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    /**
     * Injection override.
     *
     * @param servletUtilAdapter the servletUtilAdapter to set
     */
    @Inject
    @Lazy
    public void setServletUtilAdapter(ServletUtilAdapter servletUtilAdapter) {
        this.servletUtilAdapter = servletUtilAdapter;
    }

    /**
     * Injection override.
     *
     * @param configAdapter the ConfigAdapter to set
     */
    @Inject
    public void setConfigAdapter(ConfigAdapter configAdapter) {
        this.configAdapter = configAdapter;
    }

    /**
     * Injection Override
     *
     * @param serverService the serverService to set
     */
    @Inject
    public void setServerService(ServerService serverService) {
        this.serverService = serverService;
    }

    /**
     * Injection Override
     * 
     * @param instanceService the instanceService to set
     */
    @Inject
    public void setInstanceService(InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    /**
     * Injection override.
     *
     * @param exceptionAdapter the ExceptionAdapter to set
     */
    @Inject
    public void setExceptionAdapter(ExceptionAdapter exceptionAdapter) {
        this.exceptionAdapter = exceptionAdapter;
    }

    @Inject
    public void setContextService(ContextService contextService) {
        this.contextService = contextService;
    }

    @PostConstruct
    public void initManifest() {
        this.manifestUtil = new ManifestUtil(definitionService, contextService, configAdapter);
    }

    private static final StringParam ATTRIBUTES_PARAM = new StringParam("aura.attributes", 0, false);

    @SuppressWarnings("static-method")
    protected Map<String, Object> getComponentAttributes(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String attributesString = ATTRIBUTES_PARAM.get(request);
        if (attributesString != null) {
            try {
                if (attributesString.startsWith(AuraTextUtil.urlencode("{"))) {
                    // Decode encoded context json. Serialized AuraContext json always starts with "{"
                    attributesString = AuraTextUtil.urldecode(attributesString);
                }
                @SuppressWarnings("unchecked")
                Map<String,Object> result = (Map<String, Object>) new JsonReader().read(attributesString);
                // Strip any whitespace-keyed params
                for (String key : result.keySet()) {
                    if (StringUtils.isBlank(key)) {
                        result.remove(key);
                    }
                }
                return result;
            } catch (Exception e) {
                return null;
            }
        }
        Enumeration<String> attributeNames = request.getParameterNames();
        Map<String, Object> attributes = new HashMap<>();

        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement().trim();
            if (!name.startsWith("aura.") && !name.isEmpty()) {
                String value = new StringParam(name, 0, false).get(request);

                attributes.put(name, value);
            }
        }
        return attributes;
    }

    /**
     * Wrapper exception for the exceptions occurred while creating Aura resource, so that
     * we are able to centrally handle the exceptions from resources.
     */
    public static class AuraResourceException extends Exception {
        private static final long serialVersionUID = 4630562679200875774L;

        private final String resourceName;
        private final int statusCode;

        public AuraResourceException(String resourceName, int statusCode, String message, Throwable cause) {
            super(message, cause);
            this.resourceName = resourceName;
            this.statusCode = statusCode;
        }

        public AuraResourceException(String resourceName, int statusCode, Throwable cause) {
            super(cause);
            this.resourceName = resourceName;
            this.statusCode = statusCode;
        }

        public String getResourceName() {
            return resourceName;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}