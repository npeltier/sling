/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.sling.maven.projectsupport;

import static org.apache.sling.maven.projectsupport.BundleListUtils.interpolateProperties;
import static org.apache.sling.maven.projectsupport.BundleListUtils.readBundleList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.sling.maven.projectsupport.bundlelist.v1_0_0.BundleList;
import org.apache.sling.maven.projectsupport.bundlelist.v1_0_0.io.xpp3.BundleListXpp3Writer;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Attaches the bundle list as a project artifact.
 *
 * @goal attach-partial-bundle-list
 * @phase package
 * @requiresDependencyResolution test
 * @description attach the partial bundle list as a project artifact
 */
public class AttachPartialBundleListMojo extends AbstractBundleListMojo {

    public static final String CONFIG_CLASSIFIER = "bundlelistconfig";

    public static final String CONFIG_TYPE = "zip";

    public static final String SLING_COMMON_PROPS = "common.properties";

    public static final String SLING_COMMON_BOOTSTRAP = "common.bootstrap.txt";

    public static final String SLING_WEBAPP_PROPS = "webapp.properties";

    public static final String SLING_WEBAPP_BOOTSTRAP = "webapp.bootstrap.txt";

    public static final String SLING_STANDALONE_PROPS = "standalone.properties";

    public static final String SLING_STANDALONE_BOOTSTRAP = "standalone.bootstrap.txt";

    /**
     * @parameter default-value="${project.build.directory}/bundleListconfig"
     */
    private File configOutputDir;

    /**
     * @parameter default-value="${project.build.directory}/list.xml"
     */
    private File bundleListOutput;

    /**
     * The zip archiver.
     *
     * @component role="org.codehaus.plexus.archiver.Archiver" roleHint="zip"
     */
    private ZipArchiver zipArchiver;

    public void execute() throws MojoExecutionException, MojoFailureException {
        final BundleList initializedBundleList;
        if (bundleListFile.exists()) {
            try {
                initializedBundleList = readBundleList(bundleListFile);
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to read bundle list file", e);
            } catch (XmlPullParserException e) {
                throw new MojoExecutionException("Unable to read bundle list file", e);
            }
        } else {
            initializedBundleList = new BundleList();
        }

        addDependencies(initializedBundleList);

        interpolateProperties(initializedBundleList, this.project, this.mavenSession);

        final BundleListXpp3Writer writer = new BundleListXpp3Writer();
        try {
            writer.write(new FileWriter(bundleListOutput), initializedBundleList);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write bundle list", e);
        }

        project.getArtifact().setFile(bundleListOutput);

        this.getLog().info("Attaching bundle list configuration");
        try {
            this.attachConfigurations();
        } catch (final IOException ioe) {
            throw new MojoExecutionException("Unable to attach configuration.", ioe);
        } catch (final ArchiverException ioe) {
            throw new MojoExecutionException("Unable to attach configuration.", ioe);
        }
    }

    private boolean checkFile(final File f) {
        return f != null && f.exists();
    }

    private void attachConfigurations() throws MojoExecutionException, IOException, ArchiverException {
        if ( this.ignoreBundleListConfig ) {
            this.getLog().debug("ignoreBundleListConfig is set to true, therefore not attaching configurations.");
            return;
        }
        // check if we have configurations
        boolean hasConfigs = this.checkFile(this.getConfigDirectory());
        hasConfigs |= this.checkFile(this.commonSlingBootstrap);
        hasConfigs |= this.checkFile(this.commonSlingProps);
        hasConfigs |= this.checkFile(this.webappSlingBootstrap);
        hasConfigs |= this.checkFile(this.webappSlingProps);
        hasConfigs |= this.checkFile(this.standaloneSlingBootstrap);
        hasConfigs |= this.checkFile(this.standaloneSlingProps);

        if ( !hasConfigs ) {
            this.getLog().debug("No configurations to attach.");
            return;
        }
        // copy configuration, as this project might use different names we have to copy everything!
        this.configOutputDir.mkdirs();
        if ( this.checkFile(this.commonSlingBootstrap) ) {
            final File slingDir = new File(this.configOutputDir, "sling");
            slingDir.mkdirs();
            FileUtils.copyFile(this.commonSlingBootstrap, new File(slingDir, SLING_COMMON_BOOTSTRAP));
        }
        if ( this.checkFile(this.commonSlingProps) ) {
            final File slingDir = new File(this.configOutputDir, "sling");
            slingDir.mkdirs();
            FileUtils.copyFile(this.commonSlingProps, new File(slingDir, SLING_COMMON_PROPS));
        }
        if ( this.checkFile(this.webappSlingBootstrap) ) {
            final File slingDir = new File(this.configOutputDir, "sling");
            slingDir.mkdirs();
            FileUtils.copyFile(this.webappSlingBootstrap, new File(slingDir, SLING_WEBAPP_BOOTSTRAP));
        }
        if ( this.checkFile(this.webappSlingProps) ) {
            final File slingDir = new File(this.configOutputDir, "sling");
            slingDir.mkdirs();
            FileUtils.copyFile(this.webappSlingProps, new File(slingDir, SLING_WEBAPP_PROPS));
        }
        if ( this.checkFile(this.standaloneSlingBootstrap) ) {
            final File slingDir = new File(this.configOutputDir, "sling");
            slingDir.mkdirs();
            FileUtils.copyFile(this.standaloneSlingBootstrap, new File(slingDir, SLING_STANDALONE_BOOTSTRAP));
        }
        if ( this.checkFile(this.standaloneSlingProps) ) {
            final File slingDir = new File(this.configOutputDir, "sling");
            slingDir.mkdirs();
            FileUtils.copyFile(this.standaloneSlingProps, new File(slingDir, SLING_STANDALONE_PROPS));
        }
        if ( this.checkFile(this.getConfigDirectory()) ) {
            final File configDir = new File(this.configOutputDir, "config");
            configDir.mkdirs();
            FileUtils.copyDirectory(this.getConfigDirectory(), configDir,
                    null, FileUtils.getDefaultExcludesAsString());
        }
        final File destFile = new File(this.configOutputDir.getParent(), this.configOutputDir.getName() + ".zip");
        zipArchiver.setDestFile(destFile);
        zipArchiver.addDirectory(this.configOutputDir);
        zipArchiver.createArchive();

        projectHelper.attachArtifact(project, CONFIG_TYPE, CONFIG_CLASSIFIER, destFile);
    }
}
