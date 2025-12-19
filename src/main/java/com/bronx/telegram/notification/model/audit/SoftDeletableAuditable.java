package com.bronx.telegram.notification.model.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class SoftDeletableAuditable<T extends Serializable> extends Auditable<T> {

    @SoftDelete(columnName = "deleted_at")
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @LastModifiedBy
    @Column(name = "deleted_by")
    private String deletedBy;
}
