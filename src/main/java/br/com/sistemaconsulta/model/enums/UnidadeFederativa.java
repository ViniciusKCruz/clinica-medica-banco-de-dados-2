package br.com.sistemaconsulta.model.enums;

public enum UnidadeFederativa {
    AC("Rio Branco"),
    AL("Maceió"),
    AP("Macapá"),
    AM("Manaus"),
    BA("Salvador"),
    CE("Fortaleza"),
    DF("Brasília"),
    ES("Vitória"),
    GO("Goiânia"),
    MA("São Luís"),
    MT("Cuiabá"),
    MS("Campo Grande"),
    MG("Belo Horizonte"),
    PA("Belém"),
    PB("João Pessoa"),
    PR("Curitiba"),
    PE("Recife"),
    PI("Teresina"),
    RJ("Rio de Janeiro"),
    RN("Natal"),
    RS("Porto Alegre"),
    RO("Porto Velho"),
    RR("Boa Vista"),
    SC("Florianópolis"),
    SP("São Paulo"),
    SE("Aracaju"),
    TO("Palmas");

    private final String capital;

    UnidadeFederativa(String capital) {
        this.capital = capital;
    }

    public String getCapital() {
        return capital;
    }
}
