package caselab.service.document.template;

import caselab.domain.entity.attribute.value.AttributeValue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.customXmlProperties.DatastoreItem;
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.model.datastorage.CustomXmlDataStorageImpl;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@Service
@RequiredArgsConstructor
public class DocxService {
    private final GenerateTemplateXmlService generateTemplateXmlService;

    public InputStream insertXml(InputStream document, Stream<AttributeValue> values) throws Docx4JException {
        InputStream customXml = generateTemplateXmlService.generateTemplate(values);

        Document customXmlDocument;
        WordprocessingMLPackage wordMLPackage = Docx4J.load(document);
        try {
            customXmlDocument = XmlUtils.getNewDocumentBuilder().parse(customXml);
        } catch (IOException | SAXException e) {  // Этих ошибок не должно возникнуть
            throw new RuntimeException(e);
        }

        CustomXmlDataStoragePart customXmlPart = new CustomXmlDataStoragePart();
        CustomXmlDataStorage storage = new CustomXmlDataStorageImpl();
        storage.setDocument(customXmlDocument);
        customXmlPart.setData(storage);

        CustomXmlDataStoragePropertiesPart props = new CustomXmlDataStoragePropertiesPart();
        DatastoreItem datastoreItem = new DatastoreItem();
        datastoreItem.setItemID(UUID.randomUUID().toString());
        props.setContents(datastoreItem);

        wordMLPackage.getMainDocumentPart().addTargetPart(customXmlPart);
        customXmlPart.addTargetPart(props);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Docx4J.save(wordMLPackage, outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
