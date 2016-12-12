package eu.sollers.odata.jugc.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;

import eu.sollers.odata.jugc.common.annotation.ODataEntity;
import eu.sollers.odata.jugc.common.annotation.ODataKey;
import eu.sollers.odata.jugc.common.annotation.ODataProperty;
import eu.sollers.odata.jugc.common.entity.JpaOdataEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Tolerate;

/**
 * Users entity.
 */
@Builder
@Entity(name = "users")
@ODataEntity(name = "User", entitySetName = "Users")
public class User extends JpaOdataEntity {

    @Id
    @Getter
    @Setter
    @ODataKey
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ODataProperty(name = "Id", type = EdmPrimitiveTypeKind.Int64, valueType = ValueType.PRIMITIVE)
    private Long ID;

    @Getter
    @Setter
    @Column(name = "username", unique = true, nullable = false)
    @ODataProperty(name = "Username", type = EdmPrimitiveTypeKind.String, valueType = ValueType.PRIMITIVE)
    private String username;

    @Setter
    @Column(name = "password")
    private String password;

    @Getter
    @Setter
    @Column(name = "mail")
    @ODataProperty(name = "Email", type = EdmPrimitiveTypeKind.String, valueType = ValueType.PRIMITIVE)
    private String mail;

    @Tolerate
    User() {
    }

    public AuthenticatedUser extractAuthenticatedUser() {
        return new AuthenticatedUser(username, password);
    }
}
