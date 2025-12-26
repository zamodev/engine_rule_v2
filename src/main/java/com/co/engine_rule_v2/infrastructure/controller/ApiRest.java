package com.co.engine_rule_v2.infrastructure.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@RestController
@RequestMapping("/engine_rule")
public class ApiRest {

    @GetMapping("/health")
    public String health() {
        return "Health";
    }

    //Este codigo lista vecinos de los nodos mas no hace busqueda
    @PostMapping("/evaluate")
    public String  evaluateContext() {
        // Creo una lista de adyacencia en un map donde la clave es el nodo
        //y el valor que guarda este nodo es una lista
        Map<Integer, List<Integer>> grafo = new HashMap<>();

        //llenado del grafo
        grafo.put(1, Arrays.asList(2,3));
        grafo.put(2, Arrays.asList(4,3));
        grafo.put(3, Arrays.asList(5));

        for (Map.Entry<Integer, List<Integer>> entry: grafo.entrySet()) {
            System.out.println("Valor clave " + entry.getKey() + " Valor " + entry.getValue() + " Grado " + entry.getValue().size());
            for (Integer val: entry.getValue()){
                System.out.println(suma(val));
            }
        }
        return "test";
    }

    Integer suma(Integer a){
        return a + 2;
    }

    // Breadth First Search - Busqueda en amplitud
    /*
    * Algoritmo para recorrer o buscar en un grafo o árbol.
    * Se caracteriza por explorar nivel por nivel (por capa) desde un nodo inicial
    * antes de pasar al siguiente nivel
    *
    * Idea clave: Primero visita el nodo inicial, luego todos sus vecinos directos, después los vecinos de
    * esos vecinos y así sucesivamente.
    *
    * Estructura base: Se usa cola FIFO para mantener el orden de visita.
    *
    * Proposito
    *   * Encontrar el camino mas corto en grafos no ponderados.
    *   * Explorar todos los nodos alcanzables desde un nodo inicial.
    *   * Detectar conectividad, ciclos, componentes, etc.
    *
    * PASOS DEL ALGORITMO BFS
    *   1. Inicialización
    *       * Inicializar el grafo y llenar
    *       * Crear una estructura para marcar nodos visitados (ej. Set o boolean[])
    *       * Crear una cola para manejar el orden de visita.
    *       * Marcar el nodo inicial como visitado y encolar.
    *
    *   2. Iteración principal
    *       * Mientras la cola no está vacía:
    *           * Sacar el primer nodo de la cola (llamemoslo u)
    *           * Procesar u (ej. imprimir, acumular, calcular distancia)
    *           * Para cada vecino v de u
    *               * Si v no está visitado
    *                   * Marcar v como visitado
    *                   * Encolar v
    *   3. Finalización
    *       * Cuando la cola está vacía, todos los nodos alcanzables fueron visitados.
    * */

    @GetMapping("/bfs/{valor}")
    public Map<String, Object> busquedaBFS(@PathVariable Integer valor) {

        // Inicializar grafo (dirigido en este ejemplo)
        Map<Integer, List<Integer>> grafo = new HashMap<>();

        // Poblar el grafo
        grafo.put(1, Arrays.asList(4));
        grafo.put(4, Arrays.asList(3, 5));
        grafo.put(2, Arrays.asList(5));
        grafo.put(3, Arrays.asList(2));
        // Opcional: asegurar que todos los nodos tengan lista (aunque sea vacía)
        grafo.putIfAbsent(5, Collections.emptyList());

        // Estructura para marcar nodos visitados
        Set<Integer> visitados = new HashSet<>();

        // Estructura para manejar orden de visita
        Queue<Integer> cola = new ArrayDeque<>();

        // Resultado: orden BFS
        List<Integer> ordenVisita = new ArrayList<>();

        // Si el nodo inicial no existe, podemos decidir qué hacer:
        // Aquí lo permitimos: lo agregamos con lista vacía y seguimos.
        grafo.putIfAbsent(valor, Collections.emptyList());

        // Inicializar
        cola.add(valor);
        visitados.add(valor);

        // BFS
        while (!cola.isEmpty()) {
            Integer nodo = cola.poll();
            ordenVisita.add(nodo);

            // Obtener vecinos de forma segura
            List<Integer> vecinos = grafo.getOrDefault(nodo, Collections.emptyList());

            for (Integer vecino : vecinos) {
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    cola.add(vecino);
                }
            }
        }

        // Armar respuesta JSON
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("inicio", valor);
        respuesta.put("grafo", grafo);
        respuesta.put("ordenBFS", ordenVisita);
        respuesta.put("visitados", visitados);

        return respuesta;
    }



}
