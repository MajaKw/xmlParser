package com.example.demo;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;

/**
 * Wrapper class of XMLStreamReader to provide AutoCloseable for try-with-resources
 */
public class XmlStreamReaderWrapper implements AutoCloseable{
        private XMLStreamReader reader;
        public XmlStreamReaderWrapper(XMLStreamReader reader) {
            this.reader = reader;
        }
        public XMLStreamReader getReader () {
            return reader;
        }
        @Override
        public void close () throws IOException {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                throw new IOException("Error closing XMLStreamReader", e);
            }
        }
}
