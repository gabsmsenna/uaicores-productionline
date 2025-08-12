package dev.senna.model.enums;

public enum Material {

    ADESIVO(1, "Adesivo"),
    ELETROSTATICO(2, "Eletrostático"),
    BRANCO_FOSCO(3, "Branco Fosco"),
    LONA(4, "Lona"),;

    private Integer id;
    private String nomeMaterial;

    Material(Integer id, String nomeMaterial) {
        this.id = id;
        this.nomeMaterial = nomeMaterial;
    }

    public Integer getId() {
        return id;
    }

    public String getNomeMaterial() {
        return nomeMaterial;
    }
}
