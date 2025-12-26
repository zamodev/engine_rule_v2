package com.co.engine_rule_v2.infrastructure.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/engine_rule3")
public class ApiRest3 {

    //Esto es una conceptualizacion de motor de reglas
    @GetMapping("/health")
    public String health() {
        return "Health";
    }
    Map<String, BigDecimal> contextoGlobal = new HashMap<>();

    @PostMapping("/evaluacion")
    public List<String> evaluate(@RequestBody Records2.Trx_Context data){
        Records2.Trx trx_data = data.trx();
        //cargo en contexto valor venta
        contextoGlobal.put("valorVenta", trx_data.valorVenta());
        //
        Records2.Acuerdo acuerdo_data = data.acuerdo();
        List<String> liquidationResults = new ArrayList<>();

        for (Records2.Tercero tercero : data.terceros()){
            System.out.println("Tercero: " + tercero.nombre() + " Nit: " + tercero.nit());
            liquidationResults.add(tercero.nombre());
            liquidationResults.add(tercero.nit());
            for (Records2.Regla regla : tercero.contextoEjecucion()){
                liquidationResults.add(calculoliquidacion(regla));
            }
        }
        return liquidationResults;
    }

    public String calculoliquidacion(Records2.Regla regla){
        if (regla.nombre().equals("calculoComisionVariable")){
            return calculoComisionVariable(regla);
        }
        if (regla.nombre().equals("calculoIVA")){
            return calculoIVA();
        }
        return "no se puedo calcular";
    }

    //---------------------- Metodos de calculo -------------------------//
    public String calculoComisionVariable(Records2.Regla regla){
        if(contextoGlobal.containsKey("valorVenta")){
            BigDecimal valorVenta = contextoGlobal.get("valorVenta");
            Optional<Records2.Variables> variableComisionOpt = regla.variables().stream()
                    .filter(var -> var.nombre().equals("porcentajeComision"))
                    .findFirst();

            if (variableComisionOpt.isPresent()) {
                BigDecimal porcentajeComision = new BigDecimal(String.valueOf(variableComisionOpt.get().valor()));
                BigDecimal comision = valorVenta.multiply(porcentajeComision);

                contextoGlobal.put("valorNeto", comision);
                return "Valor comision variable: " + comision;
            } else {
                return "Variable porcentajeComision no encontrada";
            }
        }
        return "comision variable";
    }

    //
    public String calculoIVA(){
        if (contextoGlobal.containsKey("valorNeto")){
            BigDecimal valorNeto = contextoGlobal.get("valorNeto");
            BigDecimal tasaIVA = new BigDecimal("0.19");
            BigDecimal iva = valorNeto.multiply(tasaIVA);
            return "Valor IVA: " + iva;
        }
        return "IVA no calculado";
    }
//    public String calculoComisionFija(){}
//    public String calculoRefetefuente(){}
//    public String calculoDescuento(){}

}
