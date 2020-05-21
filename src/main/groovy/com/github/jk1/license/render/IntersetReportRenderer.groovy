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
import com.github.jk1.license.ModuleData
import com.github.jk1.license.ProjectData
import org.gradle.api.tasks.Input

/**
 * Simple Interset license report renderer
 * <br/>
 * myLicense,my.super,module,1.0
 *
 */
class IntersetReportRenderer implements ReportRenderer {

    String filename
    boolean includeHeaderLine = true

    String separator = ','
    String nl = '\r\n'

    IntersetReportRenderer(String filename = 'licenses.csv') {
        this.filename = filename
    }

    @Input
    private String getFileNameCache() { return this.filename }

    @Override
    void render(ProjectData data) {
        LicenseReportExtension config = data.project.licenseReport
        File output = new File(config.outputDir, filename)
        output.write('')

        if (includeHeaderLine) {
            output << "moduleLicense${separator}group${separator}module${separator}version${separator}$nl"
        }

        data.allDependencies.sort().each {
            renderDependency(output, it)
        }
    }

    void renderDependency(File output, ModuleData data) {
        def (String moduleUrl, String moduleLicense, String moduleLicenseUrl) = LicenseDataCollector.singleModuleLicenseInfo(data)

        if (moduleLicense != null) {
            moduleLicense = moduleLicense.replace(',', " ")
            moduleLicense = moduleLicense.replace('  ', " ")
            moduleLicense = moduleLicense.replace(' + ', "+")
        } else {
            moduleLicense = ''
        }

        output << "${moduleLicense}${separator}${data.group}${separator}${data.name}${separator}${data.version}${separator}$nl"
    }

}
