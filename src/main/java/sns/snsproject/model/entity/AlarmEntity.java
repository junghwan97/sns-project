package sns.snsproject.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import sns.snsproject.model.AlarmArgs;
import sns.snsproject.model.AlarmType;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "\"alarm\"")
@Getter
@Setter
@SQLDelete(sql = "UPDATE alarm SET deleted_at = NOW() where id=?")
//@Where(clause = "deleted_at is NULL")
@SQLRestriction("deleted_at is NULL")
public class AlarmEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private AlarmArgs args;

    @Column(name = "registered_at")
    private Timestamp registeredAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    @PrePersist
    void registeredAT() {
        this.registeredAt = Timestamp.from(Instant.now());
    }

    @PreUpdate
    void updatedAt() {
        this.updatedAt = Timestamp.from(Instant.now());
    }

    public static AlarmEntity of(UserEntity user, AlarmType alarmType, AlarmArgs args) {
        AlarmEntity entity = new AlarmEntity();
        entity.setUser(user);
        entity.setAlarmType(alarmType);
        entity.setArgs(args);
        return entity;
    }
}
