package com.co.engine_rule_v2.infrastructure.controller;

import java.math.BigDecimal;
import java.util.List;

public class Records2 {
    public record Trx_Context (Trx trx, Acuerdo acuerdo, List<Tercero> terceros ){}
    public record Trx(BigDecimal valorVenta){}
    public record Acuerdo(String nombre){}
    public record Tercero (String nombre, String nit, List<Regla> contextoEjecucion){}
    public record Regla (String nombre, String tipo, List<Variables> variables){}
//    public record Variables (String nombre, BigDecimal valor){}

    // ✅ Un solo record que soporta ambos formatos (KV y RANGO)
    public record Variables (
            String nombre,          // KV
            BigDecimal valor,       // KV
            String condicion,       // RANGO
            String expresion        // RANGO
    ){}


}
