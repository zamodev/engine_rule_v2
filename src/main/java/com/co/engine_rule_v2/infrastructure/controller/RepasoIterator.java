package com.co.engine_rule_v2.infrastructure.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/iterator")
public class RepasoIterator {
    @GetMapping("/iterar")
    public String iterar(){
        //Crear colección para iterar
        List<String> lista = new ArrayList<>();

        //Lleno datos de lista
        lista.add("Pera");
        lista.add("Manzana");
        lista.add("Uva");
        lista.add("Cereza");

        //Creo iterador para la lista
        Iterator<String> iterator = lista.iterator();

        //Usando un bucle While()
        while (iterator.hasNext()) {
            System.out.println("Element Value= " + iterator.next());
        }

        //usando el for
        for (Iterator<String> iterator2 = lista.iterator(); iterator2.hasNext();){
            System.out.println("Element value: " + iterator.next());
        }

        return "Hola";
    }
}
