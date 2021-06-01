/*

    Copyright 2015-2016 Artem Stasiuk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package com.github.terma.jenkins.githubprcoveragestatus;

import javax.xml.stream.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <a href="http://cobertura.sourceforge.net/xml/coverage-04.dtd">Coverage DTD</a>
 */
class CoberturaParser implements CoverageReportParser {

    private static String findFirst(String string, String pattern) {
        String result = findFirstOrNull(string, pattern);
        if (result != null) {
            return result;
        } else {
            throw new IllegalArgumentException("Can't find " + pattern + " in " + string);
        }
    }

    private static String findFirstOrNull(String string, String pattern) {
        Matcher matcher = Pattern.compile(pattern).matcher(string);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    private int getIntegerCoverageAttributeValue(XMLStreamReader xmlStreamReader, String filePath,
                                                       String tag, String attribute) throws IllegalArgumentException {
        try {
            return Integer.parseInt(xmlStreamReader
                    .getAttributeValue(null, attribute));
        } catch (Exception ex) {
            throw new IllegalArgumentException(String.format("No attribute: %s found in tag: %s in file: %s",
                    attribute, tag, filePath));
        }
    }

    private float getFloatCoverageAttributeValue(XMLStreamReader xmlStreamReader, String filePath,
                                                       String tag, String attribute) throws IllegalArgumentException {
        try {
            return Float.parseFloat(xmlStreamReader
                    .getAttributeValue(null, attribute));
        } catch (Exception ex) {
            throw new IllegalArgumentException(String.format("No attribute: %s found in tag: %s in file: %s",
                    attribute, tag, filePath));
        }
    }

    @Override
    public ReportData get(String coberturaFilePath) {
        try {
            int linesCovered = 0, linesValid = 0, branchesCovered = 0, branchesValid = 0;
            float lineRate = 0.0f, branchRate = 0.0f;
            String source = null, tag = null;
            boolean foundCoverage = false, foundSource = false;

            // Read XML using StAX
            XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
            XMLStreamReader xmlStreamReader = xmlInputFactory
                    .createXMLStreamReader(new FileInputStream(coberturaFilePath));
            int eventType;
            while(xmlStreamReader.hasNext()) {
                eventType = xmlStreamReader.getEventType();
                switch (eventType) {
                    case XMLStreamReader.START_ELEMENT:
                        tag = xmlStreamReader.getLocalName();
                        if(tag.equals("coverage")) {
                            lineRate = getFloatCoverageAttributeValue(xmlStreamReader, coberturaFilePath, tag, "line-rate");
                            linesCovered = getIntegerCoverageAttributeValue(xmlStreamReader, coberturaFilePath, tag, "lines-covered");
                            linesValid = getIntegerCoverageAttributeValue(xmlStreamReader, coberturaFilePath, tag, "lines-valid");
                            branchRate = getFloatCoverageAttributeValue(xmlStreamReader, coberturaFilePath, tag, "branch-rate");
                            branchesCovered = getIntegerCoverageAttributeValue(xmlStreamReader, coberturaFilePath, tag, "branches-covered");
                            branchesValid = getIntegerCoverageAttributeValue(xmlStreamReader, coberturaFilePath,tag, "branches-valid");
                            foundCoverage = true;
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        break;
                }
                if(foundCoverage) break;
                xmlStreamReader.next();
            }
            xmlStreamReader.close();

            return new ReportData(linesCovered, linesValid);

//            // Read from String
//            String content = FileUtils.readFileToString(new File(coberturaFilePath));
//            lineRate = Float.parseFloat(findFirst(content, "line-rate=['\"]([0-9.]+)['\"]"));
//            linesCovered = Integer.parseInt(findFirst(content, "lines-covered=['\"]([0-9]+)['\"]"));
//            linesValid = Integer.parseInt(findFirst(content, "lines-valid=['\"]([0-9]+)['\"]"));
//
//            branchRate = Float.parseFloat(findFirst(content, "branch-rate=['\"]([0-9.]+)['\"]"));
//            branchesValid = Integer.parseInt(findFirst(content, "branches-valid=['\"]([0-9]+)['\"]"));
//            branchesCovered = Integer.parseInt(findFirst(content, "branches-covered=['\"]([0-9]+)['\"]"));
//
//            if (lineRate > 0 && branchRate == 0) {
//              return lineRate;
//            } else if (lineRate == 0 && branchRate > 0) {
//              return branchRate;
//            } else {
//              return lineRate / 2 + branchRate / 2;
//            }
        } catch (IOException | XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }


}
