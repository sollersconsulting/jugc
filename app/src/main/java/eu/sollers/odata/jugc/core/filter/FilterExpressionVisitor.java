package eu.sollers.odata.jugc.core.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.sollers.odata.jugc.common.entity.JpaOdataEntity;
import eu.sollers.odata.jugc.core.util.JavaEdmConverter;

/**
 * $filter
 * Just 1 join supported in the current implementation.
 */
@Service
public class FilterExpressionVisitor implements ExpressionVisitor<String> {
    private static final Map<BinaryOperatorKind, String> BINARY_OPERATORS = new HashMap<BinaryOperatorKind, String>() {{
        put(BinaryOperatorKind.ADD, " + ");
        put(BinaryOperatorKind.AND, " AND ");
        put(BinaryOperatorKind.DIV, " / ");
        put(BinaryOperatorKind.EQ, " = ");
        put(BinaryOperatorKind.GE, " => ");
        put(BinaryOperatorKind.GT, " > ");
        put(BinaryOperatorKind.LE, " =< ");
        put(BinaryOperatorKind.LT, " < ");
        put(BinaryOperatorKind.MOD, " % ");
        put(BinaryOperatorKind.MUL, " * ");
        put(BinaryOperatorKind.NE, " <> ");
        put(BinaryOperatorKind.OR, " OR");
        put(BinaryOperatorKind.SUB, " -");
    }};

    // TODO: aliases should be generated and navProperties should be stored in the list
    private static final String firstAlias = "e1";
    private static final String secondAlias = "e2";
    private String usedNavProp;

    @Autowired
    private JavaEdmConverter converter;
    private EdmEntitySet entitySet;

    /**
     * Filter should always start with this instruction.
     */
    public FilterExpressionVisitor in(EdmEntitySet entitySet) {
        this.entitySet = entitySet;
        this.usedNavProp = null;
        return this;
    }

    @Override
    public String visitBinaryOperator(BinaryOperatorKind operator, String left, String right)
            throws ExpressionVisitException, ODataApplicationException {
        String strOperator = BINARY_OPERATORS.get(operator);

        if (strOperator == null) {
            throw new ODataApplicationException("Unsupported binary operation: " + operator.name(),
                    operator == BinaryOperatorKind.HAS ?
                            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode() :
                            HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
        }
        return left + strOperator + right;
    }

    @Override
    public String visitUnaryOperator(UnaryOperatorKind operator, String operand)
            throws ExpressionVisitException, ODataApplicationException {
        switch (operator) {
        case NOT:
            return "NOT " + operand;
        case MINUS:
            return "-" + operand;
        }
        throw new ODataApplicationException("Wrong unary operator: " + operator,
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public String visitMethodCall(MethodKind methodCall, List<String> parameters)
            throws ExpressionVisitException, ODataApplicationException {
        String firsEntityParam = firstAlias + "." + parameters.get(0);
        switch (methodCall) {
        case CONTAINS:
            return firsEntityParam + " LIKE '%" + extractFromStringValue(parameters.get(1)) + "%'";
        case STARTSWITH:
            return firsEntityParam + " LIKE '" + extractFromStringValue(parameters.get(1)) + "%'";
        case ENDSWITH:
            return firsEntityParam + " LIKE '%" + extractFromStringValue(parameters.get(1));
        case NOW:
            return "CURRENT_DATE";
        // TODO: joined entities
        case DAY:
            return "DAY(" + firsEntityParam + ")";
        case MONTH:
            return "MONTH(" + firsEntityParam + ")";
        case YEAR:
            return "YEAR(" + firsEntityParam + ")";
        }
        throw new ODataApplicationException("Method call " + methodCall + " not implemented",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public String visitLiteral(Literal literal) throws ExpressionVisitException, ODataApplicationException {
        String literalAsString = literal.getText();
        if (literal.getType() == null) {
            literalAsString = "NULL";
        }
        return literalAsString;
    }

    @Override
    public String visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {
        List<UriResource> resources = member.getResourcePath().getUriResourceParts();

        UriResource first = resources.get(0);

        // TODO: Enum and ComplexType; more joins
        if (resources.size() == 1 && first instanceof UriResourcePrimitiveProperty) {
            UriResourcePrimitiveProperty primitiveProperty = (UriResourcePrimitiveProperty) first;
            return firstAlias + "." + getJavaFieldNameForODataEntityAndFieldName(
                    entitySet.getEntityType().getFullQualifiedName(), primitiveProperty.getProperty().getName());
        } else if (resources.size() == 2 && first instanceof UriResourceNavigation) {
            UriResourcePrimitiveProperty second = (UriResourcePrimitiveProperty) resources.get(1);
            usedNavProp = second.getSegmentValue();
            return secondAlias + "." + getJavaFieldNameForODataEntityAndFieldName(
                    ((UriResourceNavigation) first).getType().getFullQualifiedName(), second.getProperty().getName());
        } else {
            throw new ODataApplicationException(
                    "Only direct navigation or primitive properties are implemented in filter expressions",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }
    }

    @Override
    public String visitEnum(EdmEnumType type, List<String> enumValues)
            throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Enums are not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
                Locale.ENGLISH);
    }

    @Override
    public String visitLambdaExpression(String lambdaFunction, String lambdaVariable, Expression expression)
            throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Lambda expressions are not implemented",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public String visitAlias(String aliasName) throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Aliases are not implemented",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public String visitTypeLiteral(EdmType type) throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Type literals are not implemented",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public String visitLambdaReference(String variableName) throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Lambda references are not implemented",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    public String prepareHQL(String filterHQL) {
        StringBuilder hql = new StringBuilder("SELECT ").append(firstAlias).append(" FROM ")
                                                        .append(entitySet.getEntityType().getFullQualifiedName())
                                                        .append(" ").append(firstAlias);

        // there is a join operation needed
        if (filterHQL.contains(secondAlias)) {
            hql.append(" JOIN ").append(firstAlias).append(".").append(usedNavProp).append(" ").append(secondAlias);
        }

        return hql.append(" WHERE").append(filterHQL).toString();
    }

    private String getJavaFieldNameForODataEntityAndFieldName(FullQualifiedName entityTypeFQN, String name)
            throws ExpressionVisitException {
        Class<?> clazz = converter.getJavaClassForFQN(entityTypeFQN);
        try {
            // TODO: better way for getting a field from class
            return ((JpaOdataEntity) clazz.newInstance()).getJavaFieldNameForODataName(name);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new ExpressionVisitException(
                    "Wrong field name [" + name + "] in filter for the entity: " + entityTypeFQN, e);
        }
    }

    private String extractFromStringValue(String val) {
        return val.substring(1, val.length() - 1);
    }
}