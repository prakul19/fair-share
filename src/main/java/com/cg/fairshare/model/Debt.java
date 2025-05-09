package com.cg.fairshare.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"fromUser", "toUser", "group"})
@EqualsAndHashCode(exclude = {"fromUser", "toUser", "group"})
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_user_id")
    private User fromUser;

    @ManyToOne
    @JoinColumn(name = "to_user_id")
    private User toUser;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    private Double amount;

    @Builder.Default
    private boolean isActive = true;
}
