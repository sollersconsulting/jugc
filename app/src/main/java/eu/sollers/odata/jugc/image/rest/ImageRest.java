package eu.sollers.odata.jugc.image.rest;

import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageRest {
    @Autowired
    private CsdlEdmProvider edmProvider;

    // TODO: mapping as in https://github.com/apache/olingo-odata4/blob/master/fit/src/main/java/org/apache/olingo/fit/Services.java
}
