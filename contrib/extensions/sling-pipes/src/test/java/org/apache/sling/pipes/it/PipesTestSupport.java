/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.pipes.it;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.pipes.Plumber;
import org.apache.sling.testing.paxexam.TestSupport;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

import javax.inject.Inject;

import static org.apache.sling.testing.paxexam.SlingOptions.sling;
import static org.apache.sling.testing.paxexam.SlingOptions.slingExtensionQuery;
import static org.apache.sling.testing.paxexam.SlingOptions.slingLaunchpadOakTar;
import static org.apache.sling.testing.paxexam.SlingOptions.versionResolver;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;


public abstract class PipesTestSupport extends TestSupport {


    @Inject
    protected Plumber plumber;

    @Inject
    ResourceResolverFactory factory;

    protected Option baseConfiguration() {
        return composite(
                super.baseConfiguration(),
                launchpad(),
                logging(),
                sling(),
                slingExtensionQuery(),
                mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.pipes").version(versionResolver)
        );
    }

    protected Option launchpad() {
        final int httpPort = findFreePort();
        final String workingDirectory = workingDirectory();
        return slingLaunchpadOakTar(workingDirectory, httpPort);
    }

    protected Option logging() {
        final String filename = String.format("file:%s/src/test/resources/logback.xml", PathUtils.getBaseDir());
        return composite(
                systemProperty("logback.configurationFile").value(filename),
                mavenBundle().groupId("org.slf4j").artifactId("slf4j-api").version("1.7.21"),
                mavenBundle().groupId("org.slf4j").artifactId("jcl-over-slf4j").version("1.7.21"),
                mavenBundle().groupId("ch.qos.logback").artifactId("logback-core").version("1.1.7"),
                mavenBundle().groupId("ch.qos.logback").artifactId("logback-classic").version("1.1.7")
        );
    }

    ResourceResolver resolver() throws LoginException {
        return factory.getAdministrativeResourceResolver(null);
    }
}
