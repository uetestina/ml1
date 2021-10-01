/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.meta.loader;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;

final class Module {

  private String name;

  private List<Module> depends = new ArrayList<>();

  private String version;

  private String installedVersion;

  private boolean application = false;

  private boolean installed = false;

  private boolean removable = false;

  private boolean pending = false;

  public Module(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public List<Module> getDepends() {
    return depends;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getInstalledVersion() {
    return installedVersion;
  }

  public void setInstalledVersion(String installedVersion) {
    this.installedVersion = installedVersion;
  }

  public boolean isApplication() {
    return application;
  }

  public void setApplication(boolean application) {
    this.application = application;
  }

  public boolean isInstalled() {
    return installed;
  }

  public void setInstalled(boolean installed) {
    this.installed = installed;
  }

  public boolean isRemovable() {
    return removable;
  }

  public void setRemovable(boolean removable) {
    this.removable = removable;
  }

  public boolean isPending() {
    return pending;
  }

  public void setPending(boolean pending) {
    this.pending = pending;
  }

  public boolean isUpgradable() {
    return installed && !Objects.equal(version, installedVersion);
  }

  public void dependsOn(Module module) {
    if (!depends.contains(module)) {
      depends.add(module);
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(Module.class.getName(), name);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null) return false;
    if (!(obj instanceof Module)) return false;
    return name.equals(((Module) obj).name);
  }

  public String pprint(int depth) {
    StringBuilder builder = new StringBuilder();
    builder.append(name).append("\n");
    for (Module dep : depends) {
      builder.append(Strings.repeat("  ", depth)).append("-> ").append(dep.pprint(depth + 1));
    }
    return builder.toString();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("name", name).add("version", version).toString();
  }
}
