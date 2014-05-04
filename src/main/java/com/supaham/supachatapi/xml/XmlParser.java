package com.supaham.supachatapi.xml;

import com.supaham.supachatapi.Parser;
import com.supaham.supachatapi.fanciful.FancyMessage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

/**
 * Represents an XML implementation of {@link Parser}.
 */
public class XmlParser implements Parser {

    @Override
    public FancyMessage parse(String source, Object... params) throws JAXBException {
        source = "<span>" + source + "</span>";

        JAXBContext context = JAXBContext.newInstance(Element.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Element tag = (Element) unmarshaller.unmarshal(new StringReader(source));
        return tag.getFancyMessage();
    }

    /**
     * Escapes a String from the XML parser.
     *
     * @param s string to escape
     * @return escaped String
     */
    public static String escape(String s) {
        s = s.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        return s;
    }
}
