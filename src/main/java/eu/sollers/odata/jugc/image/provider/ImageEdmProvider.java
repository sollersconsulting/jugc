package eu.sollers.odata.jugc.image.provider;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.springframework.stereotype.Service;

import eu.sollers.odata.jugc.core.provider.AbstractSpringEdmProvider;

@Service
public class ImageEdmProvider extends AbstractSpringEdmProvider {
    @Override
    protected List<CsdlActionImport> getActionImports() {
        return new ArrayList<>();
    }

    @Override
    protected List<CsdlFunctionImport> getFunctionImports() {
        return new ArrayList<>();
    }
}
