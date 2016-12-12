package eu.sollers.odata.jugc.image.provider;

import eu.sollers.odata.jugc.common.exception.CsdlExtractException;
import eu.sollers.odata.jugc.common.provider.csdl.JpaCsdlEntity;
import eu.sollers.odata.jugc.core.annotation.CsdlProvider;
import eu.sollers.odata.jugc.image.entity.Image;

@CsdlProvider
public class ImageCsdl extends JpaCsdlEntity<Image> {
    public ImageCsdl() throws CsdlExtractException {
        super(Image.class);
    }
}
