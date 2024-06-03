package com.example.demo;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

@SpringBootTest(classes = DemoApplication.class)
public class XmlValidatorTest {
    static final Path RELATIVE_PATH_IN = Paths.get("src/test/resources/in");
    static final Path SCHEMA_PATH = Paths.get("src/main/resources/xmlSchema.xsd");
    final XmlValidator xmlValidator = new XmlValidator(SCHEMA_PATH);
    static final String regex = "^\\d{4}-[A-Z]\\d[A-Z]-[A-Z]-[A-Z]-[A-Z]\\d{3}[A-Z]-[A-Z]{2}$";


    @Test
    public void RegexValidPartNumberNRTest() {
        String partNumber1 = "2303-E1A-G-M-W209B-VM";
        String partNumber2 = "5603-J1A-G-M-W982F-PO";
        String partNumber3 = "9999-E7R-Q-M-K287B-YH";

        assertTrue(partNumber1.matches(regex));
        assertTrue(partNumber2.matches(regex));
        assertTrue(partNumber3.matches(regex));
    }
    @Test
    public void RegexInvalidPartNumberNRTest() {
        String partNumber1 = "2303-E1A-G-M-W209B-VMaa";
        String partNumber2 = "5C03-J1A-G-M-W982F-PO";
        String partNumber3 = "9999-E7R-Qb-M-K287B-YH";
        String partNumber4 = "9999-E7R-Qb-M-K287B--";

        assertFalse(partNumber1.matches(regex));
        assertFalse(partNumber2.matches(regex));
        assertFalse(partNumber3.matches(regex));
        assertFalse(partNumber4.matches(regex));
    }

    @Test
    public void validXmlTest() {
        assertTrue(xmlValidator.validateXML(RELATIVE_PATH_IN.resolve("data.xml")));
    }
    @Test
    public void invalidXmlTooLongPartNumberNRTest() {
        assertFalse(xmlValidator.validateXML(RELATIVE_PATH_IN.resolve("tooLongPartNumber.xml")));
    }

    @Test
    public void noElementTest() {
        assertFalse(xmlValidator.validateXML(RELATIVE_PATH_IN.resolve("noElement.xml")));
    }

    @Test
    public void errorInTagTest() {
        assertFalse(xmlValidator.validateXML(RELATIVE_PATH_IN.resolve("errorTag.xml")));
    }


}
