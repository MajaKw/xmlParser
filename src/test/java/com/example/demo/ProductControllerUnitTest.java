package com.example.demo;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = DemoApplication.class)
public class ProductControllerUnitTest {
    static final Path RELATIVE_PATH_IN = Paths.get("src/test/resources/in");
    static final Path RELATIVE_PATH_OUT = Paths.get("src/test/resources/out/");
    static final Path SCHEMA_PATH = Paths.get("src/main/resources/xmlSchema.xsd");

    ProductController productController = new ProductController();
    final XmlValidator xmlValidator = new XmlValidator(SCHEMA_PATH);

    @Test
    public void testGetNumberOfRecords()  {
        productController.init(RELATIVE_PATH_IN.resolve("data.xml"), xmlValidator);

        int actualRecordCount = productController.getNumberOfRecords();
        int expectedRecordCount = 3;
        assertEquals(expectedRecordCount, actualRecordCount);
    }

    @Test
    public void testGetListOfActiveProductsWhenProductsHaveSameName() throws JSONException{
        productController.init(RELATIVE_PATH_IN.resolve("productsWitTheSameName.xml"), xmlValidator);
        String expected ="[ \"apple\"]";
        String actual= productController.getListOfProducts();
        JSONAssert.assertEquals(expected, actual, false);
    }
    @Test public void testGetProductByName() throws IOException, JSONException {
        productController.init(RELATIVE_PATH_IN.resolve("data.xml"), xmlValidator);
        Path expectedFilePath = RELATIVE_PATH_OUT.resolve("glass.json");

        String expected = Files.readString(expectedFilePath);
        String actual= productController.getProductByName("glass");
        JSONAssert.assertEquals(expected, actual, false);
    }
    @Test
    public void testGetProductByNameWhenProductsHaveSameName() throws IOException, JSONException {
        productController.init(RELATIVE_PATH_IN.resolve("productsWitTheSameName.xml"), xmlValidator);
        Path expectedFilePath = RELATIVE_PATH_OUT.resolve("sameNameApple.json");

        String expected = Files.readString(expectedFilePath);
        String actual= productController.getProductByName("apple");

        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    public void testGetListOfManyProducts() throws JSONException {
        ProductController productController = new ProductController();
        productController.init(RELATIVE_PATH_IN.resolve("manyProducts.xml"), xmlValidator);
        String actual = productController.getListOfProducts();
        String expect = "[ \"apple\", \"glass\",  \"banana\"]";
        JSONAssert.assertEquals(expect, actual, false);
    }

}

