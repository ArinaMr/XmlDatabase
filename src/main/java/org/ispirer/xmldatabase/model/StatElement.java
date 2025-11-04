package org.ispirer.xmldatabase.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "stat_element")
public class StatElement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String elementType;
    private String xmlId;
    private String xmlSchema;
    private String xmlName;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private StatElement parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StatElement> children = new ArrayList<>();

    @OneToMany(mappedBy = "element", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<StatAttribute> attributes = new ArrayList<>();
}
