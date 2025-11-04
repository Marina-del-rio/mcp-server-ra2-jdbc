package org.springframework.ai.mcp.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación personalizada para marcar métodos como herramientas MCP.
 *
 * NOTA PEDAGÓGICA:
 * Esta es una implementación temporal mientras Spring AI MCP está en desarrollo.
 * Permite a los estudiantes ver cómo funciona el patrón de anotaciones personalizadas.
 *
 * Cuando Spring AI MCP esté disponible (versión estable), esta annotation
 * puede ser reemplazada por la oficial de Spring AI.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Tool {

    /**
     * Nombre de la herramienta MCP.
     * Si está vacío, se usa el nombre del método.
     */
    String name() default "";

    /**
     * Descripción de la herramienta para mostrar al LLM.
     * Debe explicar claramente qué hace la herramienta.
     */
    String description() default "";
}
