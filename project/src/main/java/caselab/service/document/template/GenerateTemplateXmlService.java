package caselab.service.document.template;

import caselab.domain.entity.attribute.value.AttributeValue;
import caselab.service.document.template.jaxb.content.ContentTag;
import caselab.service.document.template.jaxb.content.FloatTag;
import caselab.service.document.template.jaxb.content.IntegerTag;
import caselab.service.document.template.jaxb.content.TextTag;
import caselab.service.document.template.jaxb.content.UnknownTag;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class GenerateTemplateXmlService {
    @SneakyThrows
    public ByteArrayInputStream generateTemplate(Stream<AttributeValue> values) {
        ContentTag contentTag = new ContentTag();
        values.forEach(attributeValue -> {
            String attrName = attributeValue.getAttribute().getName();
            String value = attributeValue.getAppValue();
            switch (attributeValue.getAttribute().getType()) {
                case "text" -> contentTag
                    .getTexts()
                    .add(new TextTag(attrName, value));
                case "int" -> contentTag
                    .getIntegers()
                    .add(new IntegerTag(attrName, Integer.parseInt(value)));
                case "float" -> contentTag
                    .getFloats()
                    .add(new FloatTag(attrName, Float.parseFloat(value)));
                default -> contentTag
                    .getUnknowns()
                    .add(new UnknownTag(attrName, value));
            }
        });

        // Эта штука кидает JAXBException, который в нормальной ситуации никогда не возникнет
        JAXBContext context = JAXBContext.newInstance(ContentTag.class);

        Marshaller marshaller = context.createMarshaller();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        marshaller.marshal(contentTag, outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
