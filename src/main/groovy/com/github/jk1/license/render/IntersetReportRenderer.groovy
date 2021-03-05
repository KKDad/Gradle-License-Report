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
 * myLicense,my.super,module,1.0,licenseUrl
 *
 */
class IntersetReportRenderer implements ReportRenderer {

    String filename
    boolean includeHeaderLine = true
    boolean oneLinePerLicense = false;
    String multiLicenseSeparator = ";"

    String separator = ','
    String nl = '\n'

    IntersetReportRenderer(String filename = 'licenses.csv', boolean oneLinePerLicense = true, String multiLicenseSeparator = ",") {
        this.filename = filename
        this.oneLinePerLicense = oneLinePerLicense
        this.multiLicenseSeparator == multiLicenseSeparator
    }

    @Input
    private String getFileNameCache() { return this.filename }

    @Override
    void render(ProjectData data) {
        LicenseReportExtension config = data.project.licenseReport
        File output = new File(config.outputDir, filename)
        output.write('')

        if (includeHeaderLine) {
            output << "moduleLicense${separator}group${separator}module${separator}version${separator}licenseUrl${separator}$nl"
        }

        data.allDependencies.sort().each {
            renderDependency(output, it)
        }
    }

    void renderDependency(File output, ModuleData data) {

        LicenseDataCollector.MultiLicenseInfo multiLicenseInfo = LicenseDataCollector.multiModuleLicenseInfo(data)

        if (oneLinePerLicense) {
            multiLicenseInfo.licenses.forEach({
                def moduleLicense = it.name
                def moduleLicenseUrl = it.url == null ? '' : it.url

                if (moduleLicense != null) {
                    moduleLicense = moduleLicense.replace(',', " ")
                    moduleLicense = moduleLicense.replace('  ', " ")
                    moduleLicense = moduleLicense.replace(' + ', "+")
                } else {
                    moduleLicense = ''
                }

                output << "${moduleLicense}${separator}${data.group}${separator}${data.name}${separator}${data.version}${separator}${moduleLicenseUrl}${separator}$nl"
            })

        } else {
            def moduleLicense = ''
            def moduleLicenseUrl = ''
            multiLicenseInfo.licenses.forEach({
                moduleLicense += (moduleLicense.size() > 0 ? multiLicenseSeparator : "")
                def licenseName = it.name == null ? '' : it.name
                licenseName = licenseName.replace(',', " ")
                licenseName = licenseName.replace('  ', " ")
                licenseName = licenseName.replace(' + ', "+")
                moduleLicense += licenseName

                moduleLicenseUrl += (moduleLicenseUrl.size() > 0 ? multiLicenseSeparator : "")
                def licenseUrl = it.url == null ? '' : it.url
                licenseUrl = licenseUrl.replace(',', " ")
                licenseUrl = licenseUrl.replace('  ', " ")
                licenseUrl = licenseUrl.replace(' + ', "+")
                moduleLicenseUrl += licenseUrl
            })

            output << "${moduleLicense}${separator}${data.group}${separator}${data.name}${separator}${data.version}${separator}${moduleLicenseUrl}${separator}$nl"
        }


    }
}

