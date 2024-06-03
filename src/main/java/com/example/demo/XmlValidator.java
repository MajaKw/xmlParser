package com.example.demo;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * This class contains method to validate requested xml file against XSD schema. It provides also custom error handler
 * to give more information (such as row or column number) about potential errors in the xmlFile.
 */
public class XmlValidator {
    private Path xsdSchemaFilePath;

    /**
     *
     * @param xsdSchemaFilePath Path to XSD schema against which user may validate distinct xml files.
     */
    public XmlValidator(Path xsdSchemaFilePath) {
        this.xsdSchemaFilePath = xsdSchemaFilePath;
    }

    /**
     *
     * @param xmlFilePath path to xml file to validate
     * @return true if xml file is valid, false when errors occurred during validation
     */
    public boolean validateXML(Path xmlFilePath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdSchemaFilePath.toString()));
            Validator validator = schema.newValidator();

            CustomErrorHandler errorHandler = new CustomErrorHandler();
            validator.setErrorHandler(errorHandler);

            validator.validate(new StreamSource(new File(xmlFilePath.toString())));

            if (errorHandler.hasErrors()) {
                System.out.println("Validation failed with the following errors:");
                System.out.println(errorHandler.getErrors());
                return false;
            }
            return true;
        } catch (SAXException | IOException e) {
            System.out.println("Validation error: " + e.getMessage());
        }
        return false;
    }
    static class CustomErrorHandler extends org.xml.sax.helpers.DefaultHandler {
        private StringBuilder errors = new StringBuilder();

        @Override
        public void error(SAXParseException e) {
            errors.append("Error at line ").append(e.getLineNumber())
                    .append(", column ").append(e.getColumnNumber())
                    .append(": ").append(e.getMessage()).append("\n");
        }

        @Override
        public void fatalError(SAXParseException e) {
            errors.append("Fatal error at line ").append(e.getLineNumber())
                    .append(", column ").append(e.getColumnNumber())
                    .append(": ").append(e.getMessage()).append("\n");
        }

        @Override
        public void warning(SAXParseException e) {
            errors.append("Warning at line ").append(e.getLineNumber())
                    .append(", column ").append(e.getColumnNumber())
                    .append(": ").append(e.getMessage()).append("\n");
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public String getErrors() {
            return errors.toString();
        }
    }
}
