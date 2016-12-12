package eu.sollers.odata.jugc.common.provider.csdl;

import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;

/**
 * Base for describing unbound operations in the way understandable for Provider.
 */
abstract public class AbstractCsdlOperation implements CsdlProvider {
    abstract public CsdlAction getCsdlAction();

    abstract public CsdlFunction getCsdlFunction();

    @Override
    final public List<CsdlAction> getCsdlActions() {
        return Arrays.asList(getCsdlAction());
    }

    @Override
    final public List<CsdlFunction> getCsdlFunctions() {
        return Arrays.asList(getCsdlFunction());
    }

    @Override
    final public CsdlEntitySet getCsdlEntitySet() {
        return null;
    }

    @Override
    final public CsdlEntityType getCsdlEntityType() {
        return null;
    }

    @Override
    final public CsdlEnumType getCsdlEnumType() {
        return null;
    }

    @Override
    final public CsdlComplexType getCsdlComplexType() {
        return null;
    }
}
