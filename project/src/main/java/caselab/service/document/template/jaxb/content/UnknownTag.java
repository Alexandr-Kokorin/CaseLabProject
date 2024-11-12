package caselab.service.document.template.jaxb.content;

import caselab.service.document.template.jaxb.Namespaces;
import caselab.service.document.template.jaxb.Types;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement(name = Types.unknownType, namespace = Namespaces.templateNamespace)
@XmlAccessorType(XmlAccessType.NONE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnknownTag {
    @XmlAttribute(namespace = Namespaces.templateNamespace)
    private String title;
    @XmlValue
    private String value;
}
