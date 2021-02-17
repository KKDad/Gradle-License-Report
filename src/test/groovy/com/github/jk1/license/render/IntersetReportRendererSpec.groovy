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
package com.github.jk1.license.render

import com.github.jk1.license.LicenseReportExtension
import com.github.jk1.license.ProjectBuilder
import com.github.jk1.license.ProjectData
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.github.jk1.license.ProjectDataFixture.*

class IntersetReportRendererSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()
    File outputCsv

    ProjectBuilder builder = new ProjectBuilder()
    ProjectData projectData

    def setup() {
        testProjectDir.create()
        outputCsv = new File(testProjectDir.root, "licenses.csv")
        outputCsv.delete()

        LicenseReportExtension extension = GRADLE_PROJECT().licenseReport
        extension.outputDir = testProjectDir.root

        // copy apache2 license file
        def apache2LicenseFile = new File(getClass().getResource('/apache2.license').toURI())
        new File(testProjectDir.root, "apache2.license") << apache2LicenseFile.text

        projectData = builder.project {
            configurations(["runtime", "test"]) { configName ->
                configuration(configName) {
                    module("mod1") {
                        pom("pom1") {
                            license(APACHE2_LICENSE(), url: "https://www.apache.org/licenses/LICENSE-2.0")
                        }
                        licenseFiles {
                            licenseFileDetails(file: "apache2.license", license: "Apache License, Version 2.0", licenseUrl: "https://www.apache.org/licenses/LICENSE-2.0")
                        }
                        manifest("mani1") {
                            license("Apache 2.0")
                        }
                    }
                    module("mod2") {
                        pom("pom2") {
                            license(APACHE2_LICENSE())
                        }
                        pom("pom3") {
                            license(APACHE2_LICENSE())
                            license(MIT_LICENSE())
                        }
                        licenseFiles {
                            licenseFileDetails(file: "apache2.license", license: "Apache License, Version 2.0", licenseUrl: "https://www.apache.org/licenses/LICENSE-2.0")
                        }
                        manifest("mani1") {
                            license("Apache 2.0")
                        }
                    }
                }
            }
            importedModulesBundle("bundle1") {
                importedModule(name: "mod1", license: "Apache  2", licenseUrl: "apache-url")
                importedModule(name: "mod2", license: "Apache  2", licenseUrl: "apache-url")
            }
        }
    }

    def "writes a one-license-per-module"() {
        def intersetReportRenderer = new IntersetReportRenderer()

        when:
        intersetReportRenderer.render(projectData)

        then:
        outputCsv.exists()
        outputCsv.text == """moduleLicense,group,module,version,licenseUrl,
Apache 2.0,dummy-group,mod1,0.0.1,,
Apache License Version 2.0,dummy-group,mod1,0.0.1,https://www.apache.org/licenses/LICENSE-2.0,
Apache 2.0,dummy-group,mod2,0.0.1,,
Apache License Version 2.0,dummy-group,mod2,0.0.1,https://www.apache.org/licenses/LICENSE-2.0,
MIT License,dummy-group,mod2,0.0.1,https://opensource.org/licenses/MIT,
"""
    }

}
