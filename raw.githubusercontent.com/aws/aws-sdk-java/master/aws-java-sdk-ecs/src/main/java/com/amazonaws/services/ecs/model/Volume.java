/*
 * Copyright 2016-2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is located at
 * 
 * http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.amazonaws.services.ecs.model;

import java.io.Serializable;
import javax.annotation.Generated;
import com.amazonaws.protocol.StructuredPojo;
import com.amazonaws.protocol.ProtocolMarshaller;

/**
 * <p>
 * A data volume used in a task definition. For tasks that use the Amazon Elastic File System (Amazon EFS), specify an
 * <code>efsVolumeConfiguration</code>. For Windows tasks that use Amazon FSx for Windows File Server file system,
 * specify a <code>fsxWindowsFileServerVolumeConfiguration</code>. For tasks that use a Docker volume, specify a
 * <code>DockerVolumeConfiguration</code>. For tasks that use a bind mount host volume, specify a <code>host</code> and
 * optional <code>sourcePath</code>. For more information, see <a
 * href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/using_data_volumes.html">Using Data Volumes in
 * Tasks</a>.
 * </p>
 * 
 * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/ecs-2014-11-13/Volume" target="_top">AWS API Documentation</a>
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class Volume implements Serializable, Cloneable, StructuredPojo {

    /**
     * <p>
     * The name of the volume. Up to 255 letters (uppercase and lowercase), numbers, underscores, and hyphens are
     * allowed. This name is referenced in the <code>sourceVolume</code> parameter of container definition
     * <code>mountPoints</code>.
     * </p>
     */
    private String name;
    /**
     * <p>
     * This parameter is specified when you are using bind mount host volumes. The contents of the <code>host</code>
     * parameter determine whether your bind mount host volume persists on the host container instance and where it is
     * stored. If the <code>host</code> parameter is empty, then the Docker daemon assigns a host path for your data
     * volume. However, the data is not guaranteed to persist after the containers associated with it stop running.
     * </p>
     * <p>
     * Windows containers can mount whole directories on the same drive as <code>$env:ProgramData</code>. Windows
     * containers cannot mount directories on a different drive, and mount point cannot be across drives. For example,
     * you can mount <code>C:\my\path:C:\my\path</code> and <code>D:\:D:\</code>, but not
     * <code>D:\my\path:C:\my\path</code> or <code>D:\:C:\my\path</code>.
     * </p>
     */
    private HostVolumeProperties host;
    /**
     * <p>
     * This parameter is specified when you are using Docker volumes.
     * </p>
     * <p>
     * Windows containers only support the use of the <code>local</code> driver. To use bind mounts, specify the
     * <code>host</code> parameter instead.
     * </p>
     * <note>
     * <p>
     * Docker volumes are not supported by tasks run on Fargate.
     * </p>
     * </note>
     */
    private DockerVolumeConfiguration dockerVolumeConfiguration;
    /**
     * <p>
     * This parameter is specified when you are using an Amazon Elastic File System file system for task storage.
     * </p>
     */
    private EFSVolumeConfiguration efsVolumeConfiguration;
    /**
     * <p>
     * This parameter is specified when you are using Amazon FSx for Windows File Server file system for task storage.
     * </p>
     */
    private FSxWindowsFileServerVolumeConfiguration fsxWindowsFileServerVolumeConfiguration;

    /**
     * <p>
     * The name of the volume. Up to 255 letters (uppercase and lowercase), numbers, underscores, and hyphens are
     * allowed. This name is referenced in the <code>sourceVolume</code> parameter of container definition
     * <code>mountPoints</code>.
     * </p>
     * 
     * @param name
     *        The name of the volume. Up to 255 letters (uppercase and lowercase), numbers, underscores, and hyphens are
     *        allowed. This name is referenced in the <code>sourceVolume</code> parameter of container definition
     *        <code>mountPoints</code>.
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * <p>
     * The name of the volume. Up to 255 letters (uppercase and lowercase), numbers, underscores, and hyphens are
     * allowed. This name is referenced in the <code>sourceVolume</code> parameter of container definition
     * <code>mountPoints</code>.
     * </p>
     * 
     * @return The name of the volume. Up to 255 letters (uppercase and lowercase), numbers, underscores, and hyphens
     *         are allowed. This name is referenced in the <code>sourceVolume</code> parameter of container definition
     *         <code>mountPoints</code>.
     */

    public String getName() {
        return this.name;
    }

    /**
     * <p>
     * The name of the volume. Up to 255 letters (uppercase and lowercase), numbers, underscores, and hyphens are
     * allowed. This name is referenced in the <code>sourceVolume</code> parameter of container definition
     * <code>mountPoints</code>.
     * </p>
     * 
     * @param name
     *        The name of the volume. Up to 255 letters (uppercase and lowercase), numbers, underscores, and hyphens are
     *        allowed. This name is referenced in the <code>sourceVolume</code> parameter of container definition
     *        <code>mountPoints</code>.
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public Volume withName(String name) {
        setName(name);
        return this;
    }

    /**
     * <p>
     * This parameter is specified when you are using bind mount host volumes. The contents of the <code>host</code>
     * parameter determine whether your bind mount host volume persists on the host container instance and where it is
     * stored. If the <code>host</code> parameter is empty, then the Docker daemon assigns a host path for your data
     * volume. However, the data is not guaranteed to persist after the containers associated with it stop running.
     * </p>
     * <p>
     * Windows containers can mount whole directories on the same drive as <code>$env:ProgramData</code>. Windows
     * containers cannot mount directories on a different drive, and mount point cannot be across drives. For example,
     * you can mount <code>C:\my\path:C:\my\path</code> and <code>D:\:D:\</code>, but not
     * <code>D:\my\path:C:\my\path</code> or <code>D:\:C:\my\path</code>.
     * </p>
     * 
     * @param host
     *        This parameter is specified when you are using bind mount host volumes. The contents of the
     *        <code>host</code> parameter determine whether your bind mount host volume persists on the host container
     *        instance and where it is stored. If the <code>host</code> parameter is empty, then the Docker daemon
     *        assigns a host path for your data volume. However, the data is not guaranteed to persist after the
     *        containers associated with it stop running.</p>
     *        <p>
     *        Windows containers can mount whole directories on the same drive as <code>$env:ProgramData</code>. Windows
     *        containers cannot mount directories on a different drive, and mount point cannot be across drives. For
     *        example, you can mount <code>C:\my\path:C:\my\path</code> and <code>D:\:D:\</code>, but not
     *        <code>D:\my\path:C:\my\path</code> or <code>D:\:C:\my\path</code>.
     */

    public void setHost(HostVolumeProperties host) {
        this.host = host;
    }

    /**
     * <p>
     * This parameter is specified when you are using bind mount host volumes. The contents of the <code>host</code>
     * parameter determine whether your bind mount host volume persists on the host container instance and where it is
     * stored. If the <code>host</code> parameter is empty, then the Docker daemon assigns a host path for your data
     * volume. However, the data is not guaranteed to persist after the containers associated with it stop running.
     * </p>
     * <p>
     * Windows containers can mount whole directories on the same drive as <code>$env:ProgramData</code>. Windows
     * containers cannot mount directories on a different drive, and mount point cannot be across drives. For example,
     * you can mount <code>C:\my\path:C:\my\path</code> and <code>D:\:D:\</code>, but not
     * <code>D:\my\path:C:\my\path</code> or <code>D:\:C:\my\path</code>.
     * </p>
     * 
     * @return This parameter is specified when you are using bind mount host volumes. The contents of the
     *         <code>host</code> parameter determine whether your bind mount host volume persists on the host container
     *         instance and where it is stored. If the <code>host</code> parameter is empty, then the Docker daemon
     *         assigns a host path for your data volume. However, the data is not guaranteed to persist after the
     *         containers associated with it stop running.</p>
     *         <p>
     *         Windows containers can mount whole directories on the same drive as <code>$env:ProgramData</code>.
     *         Windows containers cannot mount directories on a different drive, and mount point cannot be across
     *         drives. For example, you can mount <code>C:\my\path:C:\my\path</code> and <code>D:\:D:\</code>, but not
     *         <code>D:\my\path:C:\my\path</code> or <code>D:\:C:\my\path</code>.
     */

    public HostVolumeProperties getHost() {
        return this.host;
    }

    /**
     * <p>
     * This parameter is specified when you are using bind mount host volumes. The contents of the <code>host</code>
     * parameter determine whether your bind mount host volume persists on the host container instance and where it is
     * stored. If the <code>host</code> parameter is empty, then the Docker daemon assigns a host path for your data
     * volume. However, the data is not guaranteed to persist after the containers associated with it stop running.
     * </p>
     * <p>
     * Windows containers can mount whole directories on the same drive as <code>$env:ProgramData</code>. Windows
     * containers cannot mount directories on a different drive, and mount point cannot be across drives. For example,
     * you can mount <code>C:\my\path:C:\my\path</code> and <code>D:\:D:\</code>, but not
     * <code>D:\my\path:C:\my\path</code> or <code>D:\:C:\my\path</code>.
     * </p>
     * 
     * @param host
     *        This parameter is specified when you are using bind mount host volumes. The contents of the
     *        <code>host</code> parameter determine whether your bind mount host volume persists on the host container
     *        instance and where it is stored. If the <code>host</code> parameter is empty, then the Docker daemon
     *        assigns a host path for your data volume. However, the data is not guaranteed to persist after the
     *        containers associated with it stop running.</p>
     *        <p>
     *        Windows containers can mount whole directories on the same drive as <code>$env:ProgramData</code>. Windows
     *        containers cannot mount directories on a different drive, and mount point cannot be across drives. For
     *        example, you can mount <code>C:\my\path:C:\my\path</code> and <code>D:\:D:\</code>, but not
     *        <code>D:\my\path:C:\my\path</code> or <code>D:\:C:\my\path</code>.
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public Volume withHost(HostVolumeProperties host) {
        setHost(host);
        return this;
    }

    /**
     * <p>
     * This parameter is specified when you are using Docker volumes.
     * </p>
     * <p>
     * Windows containers only support the use of the <code>local</code> driver. To use bind mounts, specify the
     * <code>host</code> parameter instead.
     * </p>
     * <note>
     * <p>
     * Docker volumes are not supported by tasks run on Fargate.
     * </p>
     * </note>
     * 
     * @param dockerVolumeConfiguration
     *        This parameter is specified when you are using Docker volumes.</p>
     *        <p>
     *        Windows containers only support the use of the <code>local</code> driver. To use bind mounts, specify the
     *        <code>host</code> parameter instead.
     *        </p>
     *        <note>
     *        <p>
     *        Docker volumes are not supported by tasks run on Fargate.
     *        </p>
     */

    public void setDockerVolumeConfiguration(DockerVolumeConfiguration dockerVolumeConfiguration) {
        this.dockerVolumeConfiguration = dockerVolumeConfiguration;
    }

    /**
     * <p>
     * This parameter is specified when you are using Docker volumes.
     * </p>
     * <p>
     * Windows containers only support the use of the <code>local</code> driver. To use bind mounts, specify the
     * <code>host</code> parameter instead.
     * </p>
     * <note>
     * <p>
     * Docker volumes are not supported by tasks run on Fargate.
     * </p>
     * </note>
     * 
     * @return This parameter is specified when you are using Docker volumes.</p>
     *         <p>
     *         Windows containers only support the use of the <code>local</code> driver. To use bind mounts, specify the
     *         <code>host</code> parameter instead.
     *         </p>
     *         <note>
     *         <p>
     *         Docker volumes are not supported by tasks run on Fargate.
     *         </p>
     */

    public DockerVolumeConfiguration getDockerVolumeConfiguration() {
        return this.dockerVolumeConfiguration;
    }

    /**
     * <p>
     * This parameter is specified when you are using Docker volumes.
     * </p>
     * <p>
     * Windows containers only support the use of the <code>local</code> driver. To use bind mounts, specify the
     * <code>host</code> parameter instead.
     * </p>
     * <note>
     * <p>
     * Docker volumes are not supported by tasks run on Fargate.
     * </p>
     * </note>
     * 
     * @param dockerVolumeConfiguration
     *        This parameter is specified when you are using Docker volumes.</p>
     *        <p>
     *        Windows containers only support the use of the <code>local</code> driver. To use bind mounts, specify the
     *        <code>host</code> parameter instead.
     *        </p>
     *        <note>
     *        <p>
     *        Docker volumes are not supported by tasks run on Fargate.
     *        </p>
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public Volume withDockerVolumeConfiguration(DockerVolumeConfiguration dockerVolumeConfiguration) {
        setDockerVolumeConfiguration(dockerVolumeConfiguration);
        return this;
    }

    /**
     * <p>
     * This parameter is specified when you are using an Amazon Elastic File System file system for task storage.
     * </p>
     * 
     * @param efsVolumeConfiguration
     *        This parameter is specified when you are using an Amazon Elastic File System file system for task storage.
     */

    public void setEfsVolumeConfiguration(EFSVolumeConfiguration efsVolumeConfiguration) {
        this.efsVolumeConfiguration = efsVolumeConfiguration;
    }

    /**
     * <p>
     * This parameter is specified when you are using an Amazon Elastic File System file system for task storage.
     * </p>
     * 
     * @return This parameter is specified when you are using an Amazon Elastic File System file system for task
     *         storage.
     */

    public EFSVolumeConfiguration getEfsVolumeConfiguration() {
        return this.efsVolumeConfiguration;
    }

    /**
     * <p>
     * This parameter is specified when you are using an Amazon Elastic File System file system for task storage.
     * </p>
     * 
     * @param efsVolumeConfiguration
     *        This parameter is specified when you are using an Amazon Elastic File System file system for task storage.
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public Volume withEfsVolumeConfiguration(EFSVolumeConfiguration efsVolumeConfiguration) {
        setEfsVolumeConfiguration(efsVolumeConfiguration);
        return this;
    }

    /**
     * <p>
     * This parameter is specified when you are using Amazon FSx for Windows File Server file system for task storage.
     * </p>
     * 
     * @param fsxWindowsFileServerVolumeConfiguration
     *        This parameter is specified when you are using Amazon FSx for Windows File Server file system for task
     *        storage.
     */

    public void setFsxWindowsFileServerVolumeConfiguration(FSxWindowsFileServerVolumeConfiguration fsxWindowsFileServerVolumeConfiguration) {
        this.fsxWindowsFileServerVolumeConfiguration = fsxWindowsFileServerVolumeConfiguration;
    }

    /**
     * <p>
     * This parameter is specified when you are using Amazon FSx for Windows File Server file system for task storage.
     * </p>
     * 
     * @return This parameter is specified when you are using Amazon FSx for Windows File Server file system for task
     *         storage.
     */

    public FSxWindowsFileServerVolumeConfiguration getFsxWindowsFileServerVolumeConfiguration() {
        return this.fsxWindowsFileServerVolumeConfiguration;
    }

    /**
     * <p>
     * This parameter is specified when you are using Amazon FSx for Windows File Server file system for task storage.
     * </p>
     * 
     * @param fsxWindowsFileServerVolumeConfiguration
     *        This parameter is specified when you are using Amazon FSx for Windows File Server file system for task
     *        storage.
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public Volume withFsxWindowsFileServerVolumeConfiguration(FSxWindowsFileServerVolumeConfiguration fsxWindowsFileServerVolumeConfiguration) {
        setFsxWindowsFileServerVolumeConfiguration(fsxWindowsFileServerVolumeConfiguration);
        return this;
    }

    /**
     * Returns a string representation of this object. This is useful for testing and debugging. Sensitive data will be
     * redacted from this string using a placeholder value.
     *
     * @return A string representation of this object.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (getName() != null)
            sb.append("Name: ").append(getName()).append(",");
        if (getHost() != null)
            sb.append("Host: ").append(getHost()).append(",");
        if (getDockerVolumeConfiguration() != null)
            sb.append("DockerVolumeConfiguration: ").append(getDockerVolumeConfiguration()).append(",");
        if (getEfsVolumeConfiguration() != null)
            sb.append("EfsVolumeConfiguration: ").append(getEfsVolumeConfiguration()).append(",");
        if (getFsxWindowsFileServerVolumeConfiguration() != null)
            sb.append("FsxWindowsFileServerVolumeConfiguration: ").append(getFsxWindowsFileServerVolumeConfiguration());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;

        if (obj instanceof Volume == false)
            return false;
        Volume other = (Volume) obj;
        if (other.getName() == null ^ this.getName() == null)
            return false;
        if (other.getName() != null && other.getName().equals(this.getName()) == false)
            return false;
        if (other.getHost() == null ^ this.getHost() == null)
            return false;
        if (other.getHost() != null && other.getHost().equals(this.getHost()) == false)
            return false;
        if (other.getDockerVolumeConfiguration() == null ^ this.getDockerVolumeConfiguration() == null)
            return false;
        if (other.getDockerVolumeConfiguration() != null && other.getDockerVolumeConfiguration().equals(this.getDockerVolumeConfiguration()) == false)
            return false;
        if (other.getEfsVolumeConfiguration() == null ^ this.getEfsVolumeConfiguration() == null)
            return false;
        if (other.getEfsVolumeConfiguration() != null && other.getEfsVolumeConfiguration().equals(this.getEfsVolumeConfiguration()) == false)
            return false;
        if (other.getFsxWindowsFileServerVolumeConfiguration() == null ^ this.getFsxWindowsFileServerVolumeConfiguration() == null)
            return false;
        if (other.getFsxWindowsFileServerVolumeConfiguration() != null
                && other.getFsxWindowsFileServerVolumeConfiguration().equals(this.getFsxWindowsFileServerVolumeConfiguration()) == false)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;

        hashCode = prime * hashCode + ((getName() == null) ? 0 : getName().hashCode());
        hashCode = prime * hashCode + ((getHost() == null) ? 0 : getHost().hashCode());
        hashCode = prime * hashCode + ((getDockerVolumeConfiguration() == null) ? 0 : getDockerVolumeConfiguration().hashCode());
        hashCode = prime * hashCode + ((getEfsVolumeConfiguration() == null) ? 0 : getEfsVolumeConfiguration().hashCode());
        hashCode = prime * hashCode + ((getFsxWindowsFileServerVolumeConfiguration() == null) ? 0 : getFsxWindowsFileServerVolumeConfiguration().hashCode());
        return hashCode;
    }

    @Override
    public Volume clone() {
        try {
            return (Volume) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Got a CloneNotSupportedException from Object.clone() " + "even though we're Cloneable!", e);
        }
    }

    @com.amazonaws.annotation.SdkInternalApi
    @Override
    public void marshall(ProtocolMarshaller protocolMarshaller) {
        com.amazonaws.services.ecs.model.transform.VolumeMarshaller.getInstance().marshall(this, protocolMarshaller);
    }
}
