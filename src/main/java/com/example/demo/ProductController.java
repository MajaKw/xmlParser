package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.example.demo.Tag.*;

/**
 * This class provides methods for parsing XML files to JSON format and extracting desired data from them.
 */
@RestController
@RequestMapping("/")
public class ProductController {
    /**
     * Indicates whether the XML parser is currently inside a product element.
     */
    private boolean isInsideProduct;
    /**
     * The name of the currently processed XML tag.
     */
    private String currentElementName;
    /**
     * The name of the current product tag being processed.
     */
    private String currentProductName;
    /**
     * Holds all the data of the current product being processed.
     */
    private Map<String, String> productData;
    /**
     * The file path to the XML file containing products information.
     */
    private Path xmlFilePath;
    /**
     * Indicates whether the XML file being processed is valid.
     */
    private boolean isValidXml;

    public ProductController() {
        isInsideProduct = false;
    }
    /**
     *Initializes the ProductController with the provided XML file path and validator with the chosen XSD schema.
     * @param xmlFilePath The path to the XML file containing products information.
     * @param xmlValidator The XML validator used to validate the XML file against the XSD schema.
     */
    public void init(Path xmlFilePath, XmlValidator xmlValidator) {
        this.xmlFilePath = xmlFilePath;
        isValidXml = xmlValidator.validateXML(xmlFilePath);
    }

    /**
     * @return number of all products
     */
    @GetMapping("/records-count")
    public int getNumberOfRecords() {
        if (!isValidXml) return -1;

        int recordCount = 0;
        try (InputStream inputStream = Files.newInputStream(xmlFilePath); XmlStreamReaderWrapper readerWrapper = new XmlStreamReaderWrapper(XMLInputFactory.newInstance().createXMLStreamReader(inputStream))) {
            XMLStreamReader reader = readerWrapper.getReader();

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(PRODUCT.getValue())) {
                    recordCount++;
                }
            }
        } catch (XMLStreamException | IOException e) {
            System.out.println("XmlReader.getNumberOfRecords method: " + e.getMessage());
        }
        return recordCount;
    }

    /**
     * The product is included in the list if it is marked as active for at least one entry in the magazine. The list does not contain duplicates.
     *
     * @return list of products that are available in the shop magazine
     */
    @GetMapping("/products-list")
    public String getListOfProducts() {
        if (!isValidXml) return null;
        Set<String> productNames = new HashSet<>();
        XMLStreamReader reader;
        try (InputStream inputStream = Files.newInputStream(xmlFilePath); XmlStreamReaderWrapper readerWrapper = new XmlStreamReaderWrapper(XMLInputFactory.newInstance().createXMLStreamReader(inputStream))) {
            reader = readerWrapper.getReader();
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT && PRODUCT.getValue().equals(reader.getLocalName())) {
                    Product product = getProductActiveData(reader);
                    if (isActive(product)) {
                        productNames.add(product.name());
                    }
                }
            }
        } catch (XMLStreamException | IOException e) {
            System.out.println("XmlReader.getListOfProducts method: " + e.getMessage());
        }
        return toJson(productNames);
    }

    /**
     * @return returns a list of all products with given name
     */
    @GetMapping("/products-list/{productName}")
    public String getProductByName(@PathVariable String productName) {
        if (!isValidXml) return null;

        productData = new HashMap<>();
        StringBuilder listOfProductsWithGivenName = new StringBuilder("{ \"" + productName + "\": [\n");

        try (InputStream inputStream = Files.newInputStream(xmlFilePath); XmlStreamReaderWrapper readerWrapper = new XmlStreamReaderWrapper(XMLInputFactory.newInstance().createXMLStreamReader(inputStream))) {
            XMLStreamReader reader = readerWrapper.getReader();

            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT -> {
                        processStartElement(reader);
                        getProductId(reader);
                    }
                    case XMLStreamConstants.CHARACTERS -> processCharacters(reader);
                    case XMLStreamConstants.END_ELEMENT -> {
                        String result = processEndElement(reader, productName);
                        if (result != null) {
                            listOfProductsWithGivenName.append(result).append(",\n");
                        }
                    }
                }
            }
        } catch (XMLStreamException | IOException e) {
            System.out.println("XmlReader.getProductByName method: " + e.getMessage());
        }
        listOfProductsWithGivenName = new StringBuilder(listOfProductsWithGivenName.substring(0, listOfProductsWithGivenName.length() - 2)).append("\n] \n }");
        return listOfProductsWithGivenName.toString();
    }

    /**
     * Helper function for getListOfProducts. This method saves information inside Product Tag - about its Name and Active Element
     *
     * @return Product object
     */
    private Product getProductActiveData(XMLStreamReader reader) throws XMLStreamException {
        String productIsActive = null;
        String productName = null;
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT -> {
                    if (ACTIVE.getValue().equals(reader.getLocalName())) productIsActive = reader.getElementText();
                    else if (NAME.getValue().equals(reader.getLocalName())) productName = reader.getElementText();
                }
                case XMLStreamConstants.END_ELEMENT -> {
                    if (PRODUCT.getValue().equals(reader.getLocalName()))
                        return new Product(productName, productIsActive);
                }
            }
        }
        return new Product(productName, productIsActive);
    }

    /**
     * Helper function to transform object into json
     *
     * @return String in jsonFormat
     */
    private String toJson(Object object) {
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            return jsonMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            System.out.println("XmlReader.toJson method: " + e.getMessage());
        }
        return null;
    }

    /**
     * Helper function for getListOfProducts.
     *
     * @return returns true if Active tag is set to true.
     */
    private boolean isActive(Product product) {
        return "true".equalsIgnoreCase(product.active()) && product.name() != null;
    }

    /**
     * Helper function for getProductByName. This method retrieves from tag attribute id and adds this data to  productData map
     */
    private void getProductId(XMLStreamReader reader) {
        String productId = reader.getAttributeValue(null, "id");
        if (productId != null) productData.put("id", productId);
    }

    /**
     * Helper function for getProductByName. This method informs that reader is going inside Product Tag
     */
    private void processStartElement(XMLStreamReader reader) {
        currentElementName = reader.getLocalName();
        if (!PRODUCT.getValue().equals(currentElementName)) return;
        isInsideProduct = true;
    }

    /**
     * Helper function for getProductByName. This method adds all data inside Product Tag to productData map
     */
    private void processCharacters(XMLStreamReader reader) {
        String text = reader.getText().trim();
        if (!isInsideProduct || text.isEmpty()) return;

        productData.put(currentElementName, text);
        if (NAME.getValue().equals(currentElementName)) {
            currentProductName = text;
        }
    }

    /**
     * Helper function for getProductByName. This method informs that reader finished processing the Product tag
     *
     * @return data about single product with given nam in json
     */
    private String processEndElement(XMLStreamReader reader, String productName) {
        currentElementName = reader.getLocalName();
        if (!PRODUCT.getValue().equals(currentElementName)) return null;

        isInsideProduct = false;
        if (productName.equals(currentProductName)) return toJson(productData);
        return null;
    }
}
