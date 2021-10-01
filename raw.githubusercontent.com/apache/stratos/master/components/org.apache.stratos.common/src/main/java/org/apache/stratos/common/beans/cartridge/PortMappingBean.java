/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.common.beans.cartridge;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "portMapping")
public class PortMappingBean {

    private String name;
    private String protocol;
    private int port;
    private int proxyPort;
    private String kubernetesPortType;

    public String getKubernetesPortType() {
        return kubernetesPortType;
    }

    public void setKubernetesPortType(String kubernetesPortType) {
        this.kubernetesPortType = kubernetesPortType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String toString() {
        return " [ Name: " + getName() + ", " +
                "Protocol: " + getProtocol() + ", " +
                "Port: " + getPort() + ", " +
                "Proxy port: " + getProxyPort() + "] ";
    }
}
