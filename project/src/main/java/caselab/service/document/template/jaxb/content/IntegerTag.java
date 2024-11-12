package caselab.service.document.template.jaxb.content;

import caselab.service.document.template.jaxb.Namespaces;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "int", namespace = Namespaces.templateNamespace)
@Getter
@Setter
@AllArgsConstructor
public class IntegerTag {
    @XmlAttribute(namespace = Namespaces.templateNamespace)
    private String title;
    @XmlValue
    private int value;
}
