package com.co.engine_rule_v2.infrastructure.dto;

public record TrxInfoDto(String numero_contrato, String id_transaccion, String medio_pago, String producto,
                         String valor_total, String estado) {
}
