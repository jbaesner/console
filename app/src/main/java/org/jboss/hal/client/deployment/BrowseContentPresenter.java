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

import java.util.Set;
import javax.inject.Inject;

import com.google.common.collect.Sets;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.js.util.JsArrayOf;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
public class BrowseContentPresenter
        extends ApplicationPresenter<BrowseContentPresenter.MyView, BrowseContentPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.BROWSE_CONTENT)
    public interface MyProxy extends ProxyPlace<BrowseContentPresenter> {}

    public interface MyView extends PatternFlyView, HasPresenter<BrowseContentPresenter> {
        void setContent(JsArrayOf<Node<ContentEntry>> nodes);
    }
    // @formatter:on

    @SuppressWarnings("HardCodedStringLiteral")
    static final Set<String> EDITOR_FILE_TYPES = Sets.newHashSet(
            "bash",
            "css",
            "htm",
            "html",
            "ini",
            "java",
            "js",
            "jsm",
            "jsx",
            "json",
            "jsf",
            "jsp",
            "jsx",
            "less",
            "md",
            "markdown",
            "MF",
            "php",
            "php",
            "php3",
            "php4",
            "php5",
            "phps",
            "phpt",
            "phtml",
            "properties",
            "rb",
            "ru",
            "sh",
            "sql",
            "txt",
            "ts",
            "typescript",
            "shtml",
            "xhtml",
            "xml");

    @SuppressWarnings("HardCodedStringLiteral")
    static final Set<String> IMAGE_FILE_TYPES = Sets.newHashSet(
            "bmp",
            "gif",
            "ico",
            "img",
            "jpg",
            "jpeg",
            "png",
            "svg",
            "tiff",
            "webp");

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final ContentParser contentParser;
    private String content;

    @Inject
    public BrowseContentPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final Dispatcher dispatcher) {
        super(eventBus, view, proxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.contentParser = new ContentParser();
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        content = request.getParameter(CONTENT, null);
    }

    @Override
    protected void onReset() {
        super.onReset();
        loadContent();
    }

    @Override
    protected FinderPath finderPath() {
        return finderPathFactory.content(content);
    }

    void loadContent() {
        Operation operation = new Operation.Builder(BROWSE_CONTENT, new ResourceAddress().add(DEPLOYMENT, content))
                .build();
        dispatcher.execute(operation, result -> {
            JsArrayOf<Node<ContentEntry>> nodes = JsArrayOf.create();
            Node<ContentEntry> root = new Node.Builder<>(Ids.CONTENT_TREE_ROOT, content, new ContentEntry())
                    .root()
                    .folder()
                    .open()
                    .build();
            contentParser.parse(nodes, root, result.asList());
            getView().setContent(nodes);
        });
    }

    void loadContent(ContentEntry contentEntry, Dispatcher.SuccessCallback<String> successCallback) {
        if (!contentEntry.directory) {
            Operation operation = new Operation.Builder(READ_CONTENT, new ResourceAddress().add(DEPLOYMENT, content))
                    .param(PATH, contentEntry.path)
                    .build();
            dispatcher.download(operation, successCallback);
        }
    }

    String getContent() {
        return content;
    }
}