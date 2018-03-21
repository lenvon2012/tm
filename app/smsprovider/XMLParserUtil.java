package smsprovider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * @author hongliang.dinghl DOM生成与解析XML文档
 */
public class XMLParserUtil {

    public final static Logger log = LoggerFactory.getLogger(XMLParserUtil.class);

    public static Document init(String msg) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(msg));

            Document document = builder.parse(is);
            return document;
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage(), e);
        } catch (SAXException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static String getNodeValue(Element info, String nodeName) {
        return ((Element) info.getElementsByTagName(nodeName).item(0)).getTextContent();
    }

    public void parserXml(String str) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(str));

            Document document = db.parse(is);
            NodeList rspNodes = document.getChildNodes();

            for (int i = 0; i < rspNodes.getLength(); i++) {
                Node resNode = rspNodes.item(i);
                NodeList messages = resNode.getChildNodes();

                for (int j = 0; j < messages.getLength(); j++) {
                    Node node = messages.item(j);
                    NodeList employeeMeta = node.getChildNodes();

                    for (int k = 0; k < employeeMeta.getLength(); k++) {
                        System.out.println(employeeMeta.item(k).getNodeName() + ":"
                                + employeeMeta.item(k).getTextContent());
                    }
                }
            }

        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage(), e);
        } catch (SAXException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
