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

@XmlRootElement(name = "content", namespace = Namespaces.TEMPLATE_NAMESPACE)
@XmlAccessorType(XmlAccessType.NONE)
@Getter
@Setter
@NoArgsConstructor
public class ContentTag {
    @XmlElement(name = Types.TEXT_TYPE, namespace = Namespaces.TEMPLATE_NAMESPACE)
    List<TextTag> texts = new ArrayList<>();
    @XmlElement(name = Types.FLOAT_TYPE, namespace = Namespaces.TEMPLATE_NAMESPACE)
    List<FloatTag> floats = new ArrayList<>();
    @XmlElement(name = Types.INT_TYPE, namespace = Namespaces.TEMPLATE_NAMESPACE)
    List<IntegerTag> integers = new ArrayList<>();
    @XmlElement(name = Types.UNKNOWN_TYPE, namespace = Namespaces.TEMPLATE_NAMESPACE)
    List<UnknownTag> unknowns = new ArrayList<>();
}
