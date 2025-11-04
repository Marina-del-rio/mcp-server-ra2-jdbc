package org.springframework.ai.mcp.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para habilitar el servidor MCP en la aplicación Spring Boot.
 *
 * NOTA PEDAGÓGICA:
 * Esta es una implementación temporal tipo "marker annotation".
 * En este proyecto, no hace nada funcional (solo marca la aplicación).
 *
 * Cuando Spring AI MCP esté disponible, esta annotation activaría
 * la auto-configuración de Spring AI MCP Server.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableMcpServer {
}
