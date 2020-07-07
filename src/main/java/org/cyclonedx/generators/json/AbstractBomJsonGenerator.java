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
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) Steve Springett. All Rights Reserved.
 */
package org.cyclonedx.generators.json;

import org.cyclonedx.CycloneDxSchema;
import org.cyclonedx.model.AttachmentText;
import org.cyclonedx.model.Commit;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.Dependency;
import org.cyclonedx.model.ExternalReference;
import org.cyclonedx.model.Hash;
import org.cyclonedx.model.IdentifiableActionType;
import org.cyclonedx.model.Metadata;
import org.cyclonedx.model.OrganizationalContact;
import org.cyclonedx.model.Pedigree;
import org.cyclonedx.model.Swid;
import org.cyclonedx.model.Tool;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

abstract class AbstractBomJsonGenerator extends CycloneDxSchema implements BomJsonGenerator {

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset

    JSONObject doc = OrderedJSONObjectFactory.create();

    void createMetadataNode(final JSONObject doc, final Metadata metadata) {
        if (metadata != null) {
            final JSONObject mdo = OrderedJSONObjectFactory.create();
            if (metadata.getTimestamp() != null) {
                mdo.put("timestamp", dateFormat.format(metadata.getTimestamp()));
            } else {
                mdo.put("timestamp", dateFormat.format(new Date()));
            }
            if (metadata.getTools() != null && metadata.getTools().size() > 0) {
                final JSONArray jsonTools = new JSONArray();
                for (final Tool tool: metadata.getTools()) {
                    final JSONObject jsonTool = OrderedJSONObjectFactory.create();
                    jsonTool.put("vendor", tool.getVendor());
                    jsonTool.put("name", tool.getName());
                    jsonTool.put("version", tool.getVersion());
                    createHashesNode(jsonTool, tool.getHashes());
                    jsonTools.put(jsonTool);
                }
                mdo.put("tools", jsonTools);
            }
            if (metadata.getAuthors() != null && metadata.getAuthors().size() > 0) {
                final JSONArray jsonAuthors = new JSONArray();
                for (final OrganizationalContact author: metadata.getAuthors()) {
                    final JSONObject jsonAuthor = OrderedJSONObjectFactory.create();
                    jsonAuthor.put("name", author.getName());
                    jsonAuthor.put("email", author.getEmail());
                    jsonAuthor.put("phone", author.getPhone());
                    jsonAuthors.put(jsonAuthor);
                }
                mdo.put("authors", jsonAuthors);
            }
            if (metadata.getComponent() != null) {
                mdo.put("component", createComponentNode(metadata.getComponent()));
            }
            doc.put("metadata", mdo);
        }
    }

    void createComponentsNode(final JSONObject json, final List<Component> components, final String nodeName) {
        if (components != null && !components.isEmpty()) {
            final JSONArray jsonComponents = new JSONArray();
            for (Component component: components) {
                jsonComponents.put(createComponentNode(component));
            }
            json.put(nodeName, jsonComponents);
        }
    }

    JSONObject createComponentNode(final Component component) {
        final JSONObject json = OrderedJSONObjectFactory.create();
        json.put("type", component.getType().getTypeName());
        json.put("bom-ref", component.getBomRef());
        //json.put("supplier", component.getSupplier());
        json.put("author", component.getAuthor());
        json.put("publisher", component.getPublisher());
        json.put("group", component.getGroup());
        json.put("name", component.getName());
        json.put("version", component.getVersion());
        json.put("description", component.getDescription());
        if (component.getScope() == null) {
            json.put("scope", Component.Scope.REQUIRED.getScopeName());
        } else {
            json.put("scope", component.getScope().getScopeName());
        }
        createHashesNode(json, component.getHashes());
        //json.put("licenses", component.getLicenseChoice());
        json.put("copyright", component.getCopyright());
        json.put("cpe", component.getCpe());
        json.put("purl", component.getPurl());
        createSwidNode(json, component.getSwid());
        if (this.getSchemaVersion().getVersion() == 1.0 || component.isModified()) {
            json.put("modified", component.isModified());
        }
        createPedigreeNode(json, component.getPedigree());
        createExternalReferencesNode(json, component.getExternalReferences());
        createComponentsNode(json, component.getComponents(), "components");
        return json;
    }

    private void createHashesNode(final JSONObject parent, final List<Hash> hashes) {
        if (hashes != null && !hashes.isEmpty()) {
            final JSONArray jsonHashes = new JSONArray();
            for (Hash hash : hashes) {
                final JSONObject jsonHash = OrderedJSONObjectFactory.create();
                jsonHash.put("alg", hash.getAlgorithm());
                jsonHash.put("content", hash.getValue());
                jsonHashes.put(jsonHash);
            }
            parent.put("hashes", jsonHashes);
        }
    }

    private void createSwidNode(final JSONObject jsonComponent, final Swid swid) {
        if (swid != null) {
            final JSONObject jsonSwid = OrderedJSONObjectFactory.create();
            jsonSwid.put("tagId", swid.getTagId());
            jsonSwid.put("name", swid.getName());
            jsonSwid.put("version", swid.getVersion());
            jsonSwid.put("tagVersion", swid.getTagVersion());
            jsonSwid.put("patch", swid.isPatch());
            createAttachmentTextNode(jsonSwid, swid.getAttachmentText());
            jsonComponent.put("swid", jsonSwid);
        }
    }

    private void createAttachmentTextNode(final JSONObject json, final AttachmentText attachment) {
        if (attachment != null && attachment.getText() != null) {
            JSONObject jsonAttachment = OrderedJSONObjectFactory.create();
            jsonAttachment.put("contentType", attachment.getContentType());
            jsonAttachment.put("encoding", attachment.getEncoding());
            jsonAttachment.put("content", attachment.getText());
            json.put("text", jsonAttachment);
        }
    }

    void createExternalReferencesNode(final JSONObject parent, final List<ExternalReference> references) {
        if (references != null && !references.isEmpty()) {
            final JSONArray jsonReferences = new JSONArray();
            for (ExternalReference reference : references) {
                final JSONObject jsonReference = OrderedJSONObjectFactory.create();
                jsonReference.put("type", reference.getType().getTypeName());
                jsonReference.put("url", reference.getUrl());
                jsonReference.put("comment", reference.getComment());
                jsonReferences.put(jsonReference);
            }
            parent.put("externalReferences", jsonReferences);
        }
    }

    private void createPedigreeNode(final JSONObject parent, final Pedigree pedigree) {
        if (pedigree != null) {
            final JSONObject jsonPedigree = OrderedJSONObjectFactory.create();
            if (pedigree.getAncestors() != null && !pedigree.getAncestors().isEmpty()) {
                createComponentsNode(jsonPedigree, pedigree.getAncestors(), "ancestors");
            }
            if (pedigree.getDescendants() != null && !pedigree.getDescendants().isEmpty()) {
                createComponentsNode(jsonPedigree, pedigree.getDescendants(), "descendants");
            }
            if (pedigree.getVariants() != null && !pedigree.getVariants().isEmpty()) {
                createComponentsNode(jsonPedigree, pedigree.getVariants(), "variants");
            }
            if (pedigree.getCommits() != null && !pedigree.getCommits().isEmpty()) {
                createCommitsNode(jsonPedigree, pedigree.getCommits());
            }
            jsonPedigree.put("notes", pedigree.getNotes());
            parent.put("pedigree", jsonPedigree);
        }
    }

    private void createCommitsNode(final JSONObject parent, List<Commit> commits) {
        if (commits != null && !commits.isEmpty()) {
            final JSONArray jsonCommits = new JSONArray();
            for (Commit commit: commits) {
                final JSONObject jsonCommit = OrderedJSONObjectFactory.create();
                jsonCommit.put("uid", commit.getUid());
                jsonCommit.put("url", commit.getUrl());
                createActorNode(jsonCommit, "author", commit.getAuthor());
                createActorNode(jsonCommit, "committer", commit.getCommitter());
                jsonCommit.put("message", commit.getMessage());
                jsonCommits.put(jsonCommit);
            }
            parent.put("commits", jsonCommits);
        }
    }

    private void createActorNode(final JSONObject parent, final String nodeName, final IdentifiableActionType actor) {
        if (actor != null) {
            final JSONObject json = OrderedJSONObjectFactory.create();
            json.put("timestamp", dateFormat.format(actor.getTimestamp()));
            json.put("name", actor.getName());
            json.put("email", actor.getEmail());
            parent.put(nodeName, json);
        }
    }

    void createDependenciesNode(final JSONObject parent, final List<Dependency> dependencies) {
        if (dependencies != null && !dependencies.isEmpty()) {
            final JSONArray jsonDependencies = new JSONArray();
            for (Dependency dependency : dependencies) {
                final JSONObject dependencyNode = OrderedJSONObjectFactory.create();
                dependencyNode.put("ref", dependency.getRef());
                if (dependency.getDependencies() != null && !dependency.getDependencies().isEmpty()) {
                    final JSONArray jsonDependsOn = new JSONArray();
                    for (Dependency dependsOn : dependency.getDependencies()) {
                        jsonDependsOn.put(dependsOn.getRef());
                    }
                    dependencyNode.put("dependsOn", jsonDependsOn);
                }
                jsonDependencies.put(dependencyNode);
            }
            parent.put("dependencies", jsonDependencies);
        }
    }

    public String toJsonString() {
        return doc.toString(2);
    }

    @Override
    public String toString() {
        return doc.toString(0);
    }

}
