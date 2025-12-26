package com.co.engine_rule_v2.infrastructure.controller;

import java.util.List;

public class Records {
    public record Context_Ex (Trx trx, List<Regla> reglas){}
    public record Trx(String valorVenta){}
    public record Regla (String nombre, Variable variables){}
    public record Variable (String nombre, String valor){}
}
