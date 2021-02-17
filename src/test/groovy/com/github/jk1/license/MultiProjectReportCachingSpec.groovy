/*
 * Copyright 2018 Evgeny Naumenko <jk.vc@mail.ru>
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
 */
package com.github.jk1.license

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class MultiProjectReportCachingSpec extends Specification {

    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile
    File localBuildCacheDirectory

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        localBuildCacheDirectory = testProjectDir.newFolder('.local-cache')
        testProjectDir.newFile('settings.gradle') << """
        buildCache {
            local {
                directory '${localBuildCacheDirectory.toURI()}'
            }
        }

        include 'sub'
    """
    }


    def "task cache should be invalidated on configured project set change"() {
        when:
        buildFile << """
            plugins {
                id 'com.github.kkdad.dependency-license-report'
            }

            licenseReport {
                projects = [project]
            }
        """

        File subFolder = new File(testProjectDir.root, "sub")
        subFolder.mkdirs()
        File subFile = new File(subFolder, "build.gradle")
        subFile.createNewFile()
        subFile << """
            plugins {
                id 'java'
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                compile "javax.annotation:javax.annotation-api:1.3.2"
            }
        """


        BuildResult result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.getRoot())
            .withArguments('--build-cache', "generateLicenseReport")
            .build()

        then:
        result.task(':generateLicenseReport').outcome == TaskOutcome.SUCCESS

        when:
        buildFile.text = """
            plugins {
                id 'com.github.jk1.dependency-license-report'
            }
        """

        result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.getRoot())
            .withArguments('--build-cache', "generateLicenseReport")
            .build()

        then:
        result.task(':generateLicenseReport').outcome == TaskOutcome.SUCCESS
    }
}
