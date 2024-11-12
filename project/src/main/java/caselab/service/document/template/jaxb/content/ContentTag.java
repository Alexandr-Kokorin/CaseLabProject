package caselab.service.document.template.jaxb.content;

import caselab.service.document.template.jaxb.Namespaces;
import caselab.service.document.template.jaxb.Types;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement(name = "content", namespace = Namespaces.templateNamespace)
@XmlAccessorType(XmlAccessType.NONE)
@Getter
@Setter
@NoArgsConstructor
public class ContentTag {
    @XmlElement(name = Types.textType, namespace = Namespaces.templateNamespace)
    List<TextTag> texts = new ArrayList<>();
    @XmlElement(name = Types.floatType, namespace = Namespaces.templateNamespace)
    List<FloatTag> floats = new ArrayList<>();
    @XmlElement(name = Types.intType, namespace = Namespaces.templateNamespace)
    List<IntegerTag> integers = new ArrayList<>();
    @XmlElement(name = Types.unknownType, namespace = Namespaces.templateNamespace)
    List<UnknownTag> unknowns = new ArrayList<>();
}
