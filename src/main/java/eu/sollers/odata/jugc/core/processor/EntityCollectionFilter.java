package eu.sollers.odata.jugc.core.processor;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.sollers.odata.jugc.common.entity.JpaOdataEntity;
import eu.sollers.odata.jugc.core.filter.FilterExpressionVisitor;

/**
 * Class which supports $filter processing. Should be used with EntityCollectionProcessor.
 */
@Service
public class EntityCollectionFilter {
    @Autowired(required = false)
    EntityManager em;

    @Autowired
    FilterExpressionVisitor filterExpressionVisitor;

    public EntityCollection getFilteredEntityCollection(UriInfo uriInfo)
            throws NullPointerException, ODataApplicationException, ExpressionVisitException {

        EntityCollection result = new EntityCollection();

        EdmEntitySet edmEntitySet = ((UriResourceEntitySet) uriInfo.getUriResourceParts().get(0)).getEntitySet();
        String hql = filterExpressionVisitor
                .prepareHQL(uriInfo.getFilterOption().getExpression().accept(filterExpressionVisitor.in(edmEntitySet)));

        result.getEntities().addAll(em.createQuery(hql, JpaOdataEntity.class).getResultList());

        return result;
    }
}
