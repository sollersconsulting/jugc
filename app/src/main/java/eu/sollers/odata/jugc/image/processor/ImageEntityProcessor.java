package eu.sollers.odata.jugc.image.processor;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.MediaEntityProcessor;
import org.apache.olingo.server.api.uri.UriInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.sollers.odata.jugc.core.processor.EntityCollectionFilter;

/**
 * OData way for handling entity requests.
 */
@Service
public class ImageEntityProcessor implements EntityProcessor, EntityCollectionProcessor, MediaEntityProcessor {

    @Autowired
    private EntityCollectionFilter filterUtil;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
            ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void readMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
            ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {
    }

    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
            ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
            ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
            throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void createMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
            ContentType requestFormat, ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void updateMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
            ContentType requestFormat, ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void deleteMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
            throws ODataApplicationException, ODataLibraryException {

    }
}
