package eu.sollers.odata.jugc.core.provider;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import eu.sollers.odata.jugc.common.provider.AbstractEdmProvider;
import eu.sollers.odata.jugc.common.provider.csdl.CsdlProvider;

/**
 * Spring used for autowiring CsdlProviders.
 */
public abstract class AbstractSpringEdmProvider extends AbstractEdmProvider {
    @Autowired
    private List<CsdlProvider> providers;

    @Override
    protected List<CsdlProvider> getCsdlProviders() {
        return providers;
    }
}
