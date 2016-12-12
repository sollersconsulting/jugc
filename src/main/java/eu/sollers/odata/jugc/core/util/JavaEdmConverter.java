package eu.sollers.odata.jugc.core.util;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.sollers.odata.jugc.common.provider.csdl.CsdlProvider;
import eu.sollers.odata.jugc.common.provider.csdl.JpaCsdlEntity;

/**
 * Helper mainly for filtering. Stores connections between Java and EDM.
 */
@Service
public class JavaEdmConverter {
    @Autowired(required = false)
    private List<CsdlProvider> providers;

    /**
     * Returns original Java class name for the FullQualifiedName from the related OData entity.
     */
    public Class<?> getJavaClassForFQN(FullQualifiedName fqn) {
        for (CsdlProvider p : providers) {
            if (p instanceof JpaCsdlEntity) {
                return ((JpaCsdlEntity) p).getJavaClass();
            }
        }
        throw new IllegalArgumentException("Nothing with name: " + fqn.getFullQualifiedNameAsString());
    }
}
