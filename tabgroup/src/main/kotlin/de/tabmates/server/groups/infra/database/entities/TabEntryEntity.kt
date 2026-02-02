package de.tabmates.server.groups.infra.database.entities

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.TabEntryId
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant
import java.util.Currency

@Entity
@Table(
    name = "tab_entries",
    schema = "group_service",
    indexes = [
        Index(
            name = "idx_tab_entry_group_id_created_at",
            columnList = "group_id,created_at DESC",
        ),
    ],
)
class TabEntryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: TabEntryId? = null,
    @Column(nullable = false)
    var title: String,
    @Lob
    @Column(nullable = false, length = 256)
    var description: String,
    @Column(nullable = false)
    var amount: Double,
    @Column(nullable = false, length = 3)
    @Convert(converter = CurrencyConverter::class)
    var currency: Currency,
    @Column(
        name = "group_id",
        nullable = false,
        updatable = false,
    )
    var groupId: GroupId,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "group_id",
        nullable = false,
        insertable = false,
        updatable = false,
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    var group: GroupEntity? = null,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "creator_id",
        nullable = false,
    )
    var creator: GroupParticipantEntity,
    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
)

@Converter(autoApply = false)
class CurrencyConverter : AttributeConverter<Currency, String> {
    override fun convertToDatabaseColumn(attribute: Currency?): String? = attribute?.currencyCode

    override fun convertToEntityAttribute(dbData: String?): Currency? = dbData?.let { Currency.getInstance(it) }
}
