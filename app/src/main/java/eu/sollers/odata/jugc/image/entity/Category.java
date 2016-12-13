package eu.sollers.odata.jugc.image.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;

import eu.sollers.odata.jugc.common.annotation.ODataEntity;
import eu.sollers.odata.jugc.common.annotation.ODataKey;
import eu.sollers.odata.jugc.common.annotation.ODataNavigationProperty;
import eu.sollers.odata.jugc.common.annotation.ODataProperty;
import eu.sollers.odata.jugc.common.entity.JpaOdataEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@ODataEntity(name = "Category", entitySetName = "Categories")
public class Category extends JpaOdataEntity {
    @Id
    @Getter
    @Setter
    @ODataKey
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ODataProperty(name = "Id", type = EdmPrimitiveTypeKind.Int64)
    private Long ID;

    @Getter
    @Setter
    @Column(name = "tag")
    @ODataProperty(name = "Tag", type = EdmPrimitiveTypeKind.String)
    private String tag;

    @Getter
    @Setter
    @Column(name = "description")
    @ODataProperty(name = "Description", type = EdmPrimitiveTypeKind.String)
    private String description;

    @Getter
    @Setter
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "category")
    @ODataNavigationProperty(name = "Images", collection = true)
    private List<Image> images;
}
