package org.finsage.api.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Income {

    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, updatable = false, nullable = false)
    private UUID id;

    @Version
    private Integer version;

    private Double annualPostTaxIncome;

    private Integer incomeYear;

    @ManyToOne(fetch = FetchType.LAZY)
    private AppUser appUser;

}
