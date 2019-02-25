/*
 * This file is part of CycloneDX Core (Java).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright (c) Steve Springett. All Rights Reserved.
 */
package org.cyclonedx.model;

import org.cyclonedx.CycloneDxSchema;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@SuppressWarnings("unused")
@XmlRootElement(name = "license", namespace = CycloneDxSchema.NS_BOM_LATEST)
public class License {

    private String id;
    private String name;
    private String url;
    private LicenseText licenseText;

    public String getId() {
        return id;
    }

    @XmlElement(name = "id", namespace = CycloneDxSchema.NS_BOM_LATEST)
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @XmlElement(name = "name", namespace = CycloneDxSchema.NS_BOM_LATEST)
    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    @XmlElement(name = "url", namespace = CycloneDxSchema.NS_BOM_LATEST)
    public void setUrl(String url) {
        this.url = url;
    }

    public LicenseText getLicenseText() {
        return licenseText;
    }

    @XmlElement(name = "text", namespace = CycloneDxSchema.NS_BOM_LATEST)
    public void setLicenseText(LicenseText licenseText) {
        this.licenseText = licenseText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        License license = (License) o;
        return Objects.equals(id, license.id) &&
                Objects.equals(name, license.name) &&
                Objects.equals(url, license.url) &&
                Objects.equals(licenseText, license.licenseText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, url, licenseText);
    }
}
