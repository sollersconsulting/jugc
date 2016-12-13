package eu.sollers.odata.jugc.common.entity;

import javax.persistence.MappedSuperclass;

/**
 * Special Entity which handles files upload.
 */
@MappedSuperclass
public abstract class JpaOdataMediaEntity extends JpaOdataEntity {
    public static final String MEDIA_PROPERTY_NAME = "$value";

    public abstract byte[] getContent();

    public abstract void setContent(byte[] data);

    public abstract String getContentType();

    public abstract void setContentType(String paramString);
}