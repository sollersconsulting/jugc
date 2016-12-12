package eu.sollers.odata.jugc.image.provider;

import eu.sollers.odata.jugc.common.exception.CsdlExtractException;
import eu.sollers.odata.jugc.common.provider.csdl.JpaCsdlEntity;
import eu.sollers.odata.jugc.core.annotation.CsdlProvider;
import eu.sollers.odata.jugc.image.entity.Category;

@CsdlProvider
public class CategoryCsdl extends JpaCsdlEntity<Category> {
    public CategoryCsdl() throws CsdlExtractException {
        super(Category.class);
    }
}
