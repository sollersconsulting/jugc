package eu.sollers.odata.jugc.image.entity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

import javax.imageio.ImageIO;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.format.ContentType;

import eu.sollers.odata.jugc.common.annotation.ODataEntity;
import eu.sollers.odata.jugc.common.annotation.ODataKey;
import eu.sollers.odata.jugc.common.annotation.ODataNavigationProperty;
import eu.sollers.odata.jugc.common.annotation.ODataOperation;
import eu.sollers.odata.jugc.common.annotation.ODataOperationParameter;
import eu.sollers.odata.jugc.common.annotation.ODataProperty;
import eu.sollers.odata.jugc.common.entity.JpaOdataMediaEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * Media entity for storing images.
 */
@Entity(name = "images")
@ODataEntity(name = "Image", entitySetName = "Images")
public class Image extends JpaOdataMediaEntity {
    private static final ContentType PNG = ContentType.parse("image/png");
    private static final ContentType GIF = ContentType.parse("image/gif");
    private static final ContentType JPEG = ContentType.parse("image/jpeg");

    private static final ContentType[] ALLOWED_TYPES = { PNG, GIF, JPEG };

    /**
     * "Real" value of the file content. {@link org.apache.olingo.commons.api.format.ContentType ContentType} instead of
     * String.
     */
    @Transient
    private ContentType contentType;

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
    @Column(name = "name")
    @ODataProperty(name = "Name", type = EdmPrimitiveTypeKind.String)
    private String name;

    @Getter
    @Setter
    @Column(name = "desc")
    @ODataProperty(name = "Description", type = EdmPrimitiveTypeKind.String)
    private String description;

    @Getter
    @Setter
    @Column(name = "published")
    @ODataProperty(name = "Published", type = EdmPrimitiveTypeKind.DateTimeOffset)
    private Timestamp published;

    @Getter
    @Setter
    @Column(name = "is_private")
    @ODataProperty(name = "IsPrivate", type = EdmPrimitiveTypeKind.Boolean)
    private Boolean isPrivate;

    /**
     * Bytes which represent the image.
     */
    @Lob
    @Column(name = "bytes", columnDefinition = "mediumblob")
    private byte[] $value;

    @Getter
    @Setter
    @Column(name = "user_id")
    @ODataProperty(name = "UserId", type = EdmPrimitiveTypeKind.Int64)
    private Long userId;

    @Getter
    @Setter
    @Column(name = "width")
    @ODataProperty(name = "Width", type = EdmPrimitiveTypeKind.Int32)
    private Integer width;

    @Getter
    @Setter
    @Column(name = "height")
    @ODataProperty(name = "Height", type = EdmPrimitiveTypeKind.Int32)
    private Integer height;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ODataNavigationProperty(name = "Category")
    public Category category;

    Image() {
    }

    /**
     * Entity can change its image size.
     *
     * @param newWidth
     *         new image width
     * @param newHeight
     *         new height of the image
     * @throws IOException
     *         when access to the bytes failed
     */
    @ODataOperation(name = "Resize", action = true)
    public void resize(@ODataOperationParameter(name = "Width") Integer newWidth,
            @ODataOperationParameter(name = "Height") Integer newHeight) throws IOException {

        BufferedImage oldImage = ImageIO.read(new ByteArrayInputStream($value));

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, oldImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(oldImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, contentType.getSubtype(), baos);

        setContent(baos.toByteArray());
        width = newWidth;
        height = newHeight;
    }

    @Override
    public byte[] getContent() {
        // return (byte[]) getProperty(MEDIA_PROPERTY_NAME).asPrimitive();
        return $value;
    }

    @Override
    public void setContent(byte[] data) {
        $value = data;
        this.getProperties().remove(this.getProperty(MEDIA_PROPERTY_NAME));
        this.addProperty(new Property(null, MEDIA_PROPERTY_NAME, ValueType.PRIMITIVE, data));
    }

    @Override
    public String getContentType() {
        return getMediaContentType();
    }

    @Override
    public void setContentType(String contentType) {
        boolean typeInAllowedTypes = false;
        for (ContentType ct : ALLOWED_TYPES) {
            if (contentType.equals(ct.toString())) {
                this.contentType = ct;
                typeInAllowedTypes = true;
                break;
            }
        }

        if (!typeInAllowedTypes) {
            throw new IllegalArgumentException("Wrong image content type: " + contentType);
        }

        this.setMediaContentType(contentType);
    }
}
