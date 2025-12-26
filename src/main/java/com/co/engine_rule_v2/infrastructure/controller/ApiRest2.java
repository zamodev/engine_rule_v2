package com.co.engine_rule_v2.infrastructure.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/engine_rule2")
public class ApiRest2 {

    @GetMapping("/health")
    public String health() {
        return "Health";
    }

    @PostMapping("/eval")
    public List<String> evaluate(@RequestBody Records.Context_Ex data){


        System.out.println("Transaccion" + data.trx());

        Records.Trx trx_data = data.trx();
        List<String> result = new ArrayList<>();

        for (Records.Regla rule: data.reglas()){
            System.out.println(rule.variables());
            String resu = calculo(rule, trx_data);
            result.add(resu);
            result.add(String.valueOf(trx_data.valorVenta()));
            System.out.println(resu);
        }

        return result;
    }
    public String calculo(Records.Regla rule, Records.Trx trx) {
        if (rule.nombre().equals("calculoUtilidad")){
            BigDecimal resultado;
            BigDecimal valorVenta = new BigDecimal(trx.valorVenta());
            BigDecimal variableValorUtilidad = new BigDecimal(rule.variables().valor());
            resultado = valorVenta.multiply(variableValorUtilidad);
           return "Valor calculo utilidad: "  + resultado;
        }
        return "no se puedo calcular";
    }
}
