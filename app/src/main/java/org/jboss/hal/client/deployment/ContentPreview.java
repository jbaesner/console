/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.deployment;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.builder.ElementsBuilder;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.deployment.Content;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.deployment.ServerGroupDeploymentColumn.SERVER_GROUP_DEPLOYMENT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeBoolean;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.marginLeft5;
import static org.jboss.hal.resources.CSS.marginRight5;
import static org.jboss.hal.resources.Icons.flag;

class ContentPreview extends PreviewContent<Content> {

    private final ContentColumn column;
    private final Places places;
    private final Resources resources;
    private final AuthorisationDecision authorisationDecision;
    private final PreviewAttributes<Content> attributes;
    private final HTMLElement deploymentsDiv;
    private final HTMLElement deploymentsUl;
    private final HTMLElement undeployedContentDiv;
    private final HTMLElement unmanagedViewDiv;

    ContentPreview(ContentColumn column, Content content, Environment environment, Places places,
            Metadata serverGroupMetadata, Resources resources) {
        super(content.getName());
        this.column = column;
        this.places = places;
        this.resources = resources;
        this.authorisationDecision = AuthorisationDecision.from(environment, serverGroupMetadata.getSecurityContext());

        LabelBuilder labelBuilder = new LabelBuilder();
        attributes = new PreviewAttributes<>(content, asList(NAME, RUNTIME_NAME));
        attributes.append(model -> {
            String label = String.join(", ", labelBuilder.label(MANAGED), labelBuilder.label(EXPLODED));
            ElementsBuilder elements = Elements.elements()
                    .add(span()
                            .title(labelBuilder.label(MANAGED))
                            .css(flag(failSafeBoolean(model, MANAGED)), marginRight5))
                    .add(span()
                            .title(labelBuilder.label(EXPLODED))
                            .css(flag(failSafeBoolean(model, EXPLODED))));
            return new PreviewAttribute(label, elements.asElements());
        });
        previewBuilder().addAll(attributes);

        HTMLElement p;
        previewBuilder()
                .add(unmanagedViewDiv = new Alert(Icons.WARNING,
                        resources.messages().cannotBrowseUnmanaged()).asElement())
                .add(h(2).textContent(resources.constants().deployments()))
                .add(deploymentsDiv = div()
                        .add(p().innerHtml(resources.messages().deployedTo(content.getName())))
                        .add(deploymentsUl = ul().asElement())
                        .asElement())
                .add(undeployedContentDiv = div()
                        .add(p = p()
                                .add(span()
                                        .innerHtml(resources.messages().undeployedContent(content.getName())))
                                .asElement())
                        .asElement());
        if (authorisationDecision.isAllowed(Constraint.executable(SERVER_GROUP_DEPLOYMENT_TEMPLATE, ADD))) {
            p.appendChild(a().css(clickable, marginLeft5).on(click, event -> column.deploy(content))
                    .textContent(resources.constants().deploy()).asElement());
        }
    }

    @Override
    public void update(Content content) {
        attributes.refresh(content);

        boolean undeployed = content.getServerGroupDeployments().isEmpty();
        boolean unmanaged = !content.isManaged();
        Elements.setVisible(deploymentsDiv, !undeployed);
        Elements.setVisible(undeployedContentDiv, undeployed);
        Elements.setVisible(unmanagedViewDiv, unmanaged);
        if (!undeployed) {
            Elements.removeChildrenFrom(deploymentsUl);
            content.getServerGroupDeployments().forEach(sgd -> {
                String serverGroup = sgd.getServerGroup();
                PlaceRequest serverGroupPlaceRequest = places.finderPlace(NameTokens.DEPLOYMENTS, new FinderPath()
                        .append(Ids.DEPLOYMENT_BROWSE_BY, Ids.asId(Names.SERVER_GROUPS))
                        .append(Ids.DEPLOYMENT_SERVER_GROUP, Ids.serverGroup(serverGroup))
                        .append(Ids.SERVER_GROUP_DEPLOYMENT, Ids.serverGroupDeployment(serverGroup, content.getName())))
                        .build();
                String serverGroupToken = places.historyToken(serverGroupPlaceRequest);
                HTMLElement li = li()
                        .add(a(serverGroupToken).textContent(serverGroup))
                        .asElement();
                if (authorisationDecision.isAllowed(Constraint.executable(SERVER_GROUP_DEPLOYMENT_TEMPLATE, ADD))) {
                    li.appendChild(span().textContent(" (").asElement());
                    li.appendChild(a().css(clickable)
                            .on(click, event -> column.undeploy(content, serverGroup))
                            .textContent(resources.constants().undeploy())
                            .asElement());
                    li.appendChild(span().textContent(")").asElement());
                }
                deploymentsUl.appendChild(li);
            });
        }
    }
}
