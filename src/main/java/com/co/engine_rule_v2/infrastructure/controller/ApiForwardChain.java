package com.co.engine_rule_v2.infrastructure.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/engine_rule_forward_chain")
public class ApiForwardChain {

    //llaves contexto
    private static final String K_VALOR_VENTA = "valorVenta";
    private static final String K_VALOR_NETO  = "valorNeto";
    private static final String K_IVA         = "iva";

    //Strategy - Regla
    interface RuleStrategy {
        String id();
        boolean canExecute(Map<String, BigDecimal> ctx, Records2.Regla regla);
        String execute(Map<String, BigDecimal> ctx, Records2.Regla regla);
    }

    /**
     * Implementasción calculoComisionVariable:
     * - Requiere: valorVenta
     * - Parámetro: porcentajeComision
     * - Produce: valorNeto
     */
    static class CalculoComisionVariable implements RuleStrategy {

        @Override
        public String id() {
            return "calculoComisionVariable";
        }

        @Override
        public boolean canExecute(Map<String, BigDecimal> ctx, Records2.Regla regla) {
            // Ya puedo si existe valorVenta y aún no he producido valorNeto (evita re-ejecutar)
            return ctx.containsKey(K_VALOR_VENTA) && !ctx.containsKey(K_VALOR_NETO);
        }

        @Override
        public String execute(Map<String, BigDecimal> ctx, Records2.Regla regla) {
            BigDecimal valorVenta = ctx.get(K_VALOR_VENTA);

            BigDecimal porcentajeComision = getVar(regla, "porcentajeComision")
                    .orElseThrow(() -> new IllegalArgumentException("Falta variable 'porcentajeComision' en " + id()));

            // comision = valorVenta * porcentajeComision
            BigDecimal comision = valorVenta.multiply(porcentajeComision);

            // Publica output en el contexto: habilita IVA
            ctx.put(K_VALOR_NETO, comision);

            return "OK " + id() + " -> valorNeto=" + comision;
        }
    }

    /**
     * Implementacion calculoIVA:
     * - Requiere: valorNeto
     * - Parámetro opcional: valorPorcentaje (si no viene, usa 0.19)
     * - Produce: iva
     */
    static class CalculoIVA implements RuleStrategy {

        @Override
        public String id() {
            return "calculoIVA";
        }

        @Override
        public boolean canExecute(Map<String, BigDecimal> ctx, Records2.Regla regla) {
            // Ya puedo si existe valorNeto y aún no he calculado iva
            return ctx.containsKey(K_VALOR_NETO) && !ctx.containsKey(K_IVA);
        }

        @Override
        public String execute(Map<String, BigDecimal> ctx, Records2.Regla regla) {
            BigDecimal valorNeto = ctx.get(K_VALOR_NETO);

            BigDecimal tasaIVA = getVar(regla, "valorPorcentaje")
                    .orElse(new BigDecimal("0.19"));

            BigDecimal iva = valorNeto.multiply(tasaIVA);

            // Publica output en el contexto para reglas futuras
            ctx.put(K_IVA, iva);

            return "OK " + id() + " -> iva=" + iva + " (tasa=" + tasaIVA + ")";
        }
    }

    // Registry de reglas (nombre -> strategy)
    private final Map<String, RuleStrategy> registry = Map.of(
            "calculoComisionVariable", new CalculoComisionVariable(),
            "calculoIVA", new CalculoIVA()
    );



    @PostMapping("/evaluacion")
    public List<String> evaluate(@RequestBody Records2.Trx_Context data) {

        List<String> liquidationResults = new ArrayList<>();

        // Contexto BASE por request (se copia por tercero)
        Map<String, BigDecimal> ctxBase = new HashMap<>();
        ctxBase.put(K_VALOR_VENTA, data.trx().valorVenta());

        liquidationResults.add("Acuerdo: " + safe(data.acuerdo() != null ? data.acuerdo().nombre() : null));
        liquidationResults.add("ValorVenta: " + data.trx().valorVenta());

        // Por cada tercero, ejecuta su contexto con forward-chaining
        for (Records2.Tercero tercero : data.terceros()) {

            liquidationResults.add("Tercero: " + tercero.nombre() + " Nit: " + tercero.nit());

            // Contexto por tercero (para que cada uno tenga sus outputs sin mezclar)
            Map<String, BigDecimal> ctxTercero = new HashMap<>(ctxBase);

            // Ejecuta forward-chaining sobre la lista de reglas del tercero
            List<String> trace = executeForwardChaining(tercero.contextoEjecucion(), ctxTercero);

            liquidationResults.addAll(trace);

            // Resultado final del contexto del tercero (para ver outputs)
            liquidationResults.add("Contexto final keys: " + ctxTercero.keySet());
            liquidationResults.add("Contexto final valores: " + ctxTercero);
        }

        return liquidationResults;
    }


    // Motor Forward-chaining
    private List<String> executeForwardChaining(List<Records2.Regla> reglas, Map<String, BigDecimal> ctx) {

        List<String> out = new ArrayList<>();
        if (reglas == null) {
            out.add("No hay reglas para ejecutar.");
            return out;
        }

        // Pendientes: aquí está la magia: NO confías en el orden del JSON
        List<Records2.Regla> pending = new ArrayList<>(reglas);

        boolean progress;
        int round = 0;

        do {
            round++;
            progress = false;

        //out.add("---- Ronda " + round + " | pending=" + pending.stream().map(Records2.Regla::nombre).toList() + " | ctxKeys=" + ctx.keySet());

            Iterator<Records2.Regla> it = pending.iterator();
            while (it.hasNext()) {
                Records2.Regla regla = it.next();

                RuleStrategy strategy = registry.get(regla.nombre());
                if (strategy == null) {
                    // Para conceptualizar: falla rápido si viene una regla no soportada
                    throw new IllegalArgumentException("Regla no soportada: " + regla.nombre());
                }

                // La regla solo se ejecuta si ya tiene sus insumos
                if (strategy.canExecute(ctx, regla)) {
                    String res = strategy.execute(ctx, regla);
                    out.add(res);

                    it.remove();
                    progress = true;
                }
            }

        } while (progress && !pending.isEmpty());

        // Si quedaron pendientes y ya no hubo progreso, algo faltó o hay dependencia imposible
        if (!pending.isEmpty()) {
            out.add("Bloqueo: no se pudieron ejecutar reglas: "
                    + pending.stream().map(Records2.Regla::nombre).toList());
            out.add("Context keys actuales: " + ctx.keySet());
            out.add("Esto significa: faltan insumos o una regla requiere algo que ninguna produce.");
        } else {
            out.add("Todas las reglas ejecutadas correctamente.");
        }

        return out;
    }

    // Helpers
    private static Optional<BigDecimal> getVar(Records2.Regla regla, String name) {
        if (regla.variables() == null) return Optional.empty();
        return regla.variables().stream()
                .filter(v -> name.equals(v.nombre()))
                .map(Records2.Variables::valor)
                .findFirst();
    }
    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
