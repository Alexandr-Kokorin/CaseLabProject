package caselab.service.document.template.jaxb.content;

import caselab.service.document.template.jaxb.Namespaces;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement(name = "content", namespace = Namespaces.templateNamespace)
@Getter
@Setter
@NoArgsConstructor
public class ContentTag {
    List<TextTag> texts = new ArrayList<>();
    List<FloatTag> floats = new ArrayList<>();
    List<IntegerTag> integers = new ArrayList<>();
    List<UnknownTag> unknowns = new ArrayList<>();
}
