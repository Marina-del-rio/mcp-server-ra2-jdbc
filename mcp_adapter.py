#!/usr/bin/env python3
"""
Adaptador MCP para el servidor de gestión JDBC.
Implementa el protocolo MCP estándar (JSON-RPC sobre stdio) y se comunica
con el servidor HTTP REST de Spring Boot.
"""

import json
import sys
import requests
import subprocess
import time
import os
import signal
from typing import Dict, List, Any, Optional

# URL del servidor Spring Boot
SERVER_URL = "http://localhost:8082/mcp"
spring_process = None

def log_error(message: str):
    """Escribe mensaje de error en stderr"""
    if os.environ.get('MCP_DEBUG', '').lower() in ['1', 'true', 'yes']:
        print(f"ERROR: {message}", file=sys.stderr, flush=True)

def check_server_running():
    """Verifica si el servidor Spring Boot ya está corriendo"""
    try:
        response = requests.get(f"{SERVER_URL}/health", timeout=2)
        return response.status_code == 200
    except:
        return False

def start_spring_server():
    """Inicia el servidor Spring Boot en segundo plano si no está corriendo"""
    global spring_process

    if check_server_running():
        log_error("Servidor Spring Boot ya está corriendo")
        return True

    log_error("Iniciando servidor Spring Boot...")

    # Obtener el directorio del proyecto
    project_dir = os.path.dirname(os.path.abspath(__file__))

    spring_process = subprocess.Popen(
        ["./gradlew", "bootRun"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        cwd=project_dir,
        preexec_fn=os.setsid
    )

    # Esperar a que el servidor esté listo
    max_retries = 60
    for i in range(max_retries):
        try:
            response = requests.get(f"{SERVER_URL}/health", timeout=2)
            if response.status_code == 200:
                log_error("Servidor Spring Boot iniciado correctamente")
                return True
        except:
            pass

        time.sleep(1)
        if i % 5 == 0:
            log_error(f"Esperando servidor... ({i+1}/{max_retries})")

    log_error("ERROR: No se pudo iniciar el servidor Spring Boot")
    return False

def stop_spring_server():
    """Detiene el servidor Spring Boot solo si fue iniciado por este proceso"""
    global spring_process
    if spring_process:
        log_error("Deteniendo servidor Spring Boot...")
        try:
            os.killpg(os.getpgid(spring_process.pid), signal.SIGTERM)
            spring_process.wait(timeout=5)
        except:
            pass

def get_tools() -> List[Dict[str, Any]]:
    """Obtiene la lista de herramientas disponibles del servidor"""
    try:
        response = requests.get(f"{SERVER_URL}/tools", timeout=5)
        if response.status_code == 200:
            data = response.json()
            return data.get("tools", [])
        return []
    except Exception as e:
        log_error(f"Error obteniendo herramientas: {e}")
        return []

def call_tool(tool_name: str, arguments: Dict[str, Any]) -> Any:
    """Ejecuta una herramienta en el servidor"""

    # Mapeo de nombres de herramientas a endpoints
    endpoint_map = {
        "test_connection": "/test_connection",
        "create_user": "/create_user",
        "find_user_by_id": "/find_user_by_id",
        "update_user": "/update_user",
        "delete_user": "/delete_user",
        "find_all_users": "/find_all_users",
        "find_users_by_department": "/find_users_by_department",
        "search_users": "/search_users",
        "find_users_with_pagination": "/find_users_with_pagination",
        "transfer_data": "/transfer_data",
        "batch_insert_users": "/batch_insert_users",
        "get_connection_info": "/get_connection_info",
        "get_database_info": "/get_database_info",
        "get_table_columns": "/get_table_columns",
        "execute_count_by_department": "/execute_count_by_department"
    }

    endpoint = endpoint_map.get(tool_name)
    if not endpoint:
        raise Exception(f"Herramienta desconocida: {tool_name}")

    try:
        response = requests.post(
            f"{SERVER_URL}{endpoint}",
            json=arguments,
            headers={"Content-Type": "application/json"},
            timeout=30
        )

        if response.status_code == 200:
            return response.json()
        else:
            error_data = response.json() if response.headers.get('content-type') == 'application/json' else {}
            raise Exception(error_data.get("error", f"Error HTTP {response.status_code}"))

    except requests.exceptions.RequestException as e:
        raise Exception(f"Error de conexión: {e}")

def handle_initialize(params: Dict[str, Any]) -> Dict[str, Any]:
    """Maneja la inicialización del protocolo MCP"""
    return {
        "protocolVersion": "2024-11-05",
        "capabilities": {
            "tools": {}
        },
        "serverInfo": {
            "name": "mcp-server-ra2-jdbc",
            "version": "1.0.0"
        }
    }

def handle_tools_list(params: Dict[str, Any]) -> Dict[str, Any]:
    """Maneja la lista de herramientas disponibles"""
    tools = get_tools()

    # Convertir al formato MCP estándar
    mcp_tools = []
    for tool in tools:
        mcp_tool = {
            "name": tool["name"],
            "description": tool["description"],
            "inputSchema": {
                "type": "object",
                "properties": {},
                "required": []
            }
        }

        # Definir esquemas de parámetros para cada herramienta
        if tool["name"] == "test_connection":
            pass  # No requiere parámetros

        elif tool["name"] == "create_user":
            mcp_tool["inputSchema"]["properties"] = {
                "name": {"type": "string", "description": "Nombre del usuario"},
                "email": {"type": "string", "description": "Email del usuario"},
                "department": {"type": "string", "description": "Departamento"},
                "role": {"type": "string", "description": "Rol del usuario"}
            }
            mcp_tool["inputSchema"]["required"] = ["name", "email", "department", "role"]

        elif tool["name"] in ["find_user_by_id", "delete_user"]:
            mcp_tool["inputSchema"]["properties"] = {
                "userId": {"type": "number", "description": "ID del usuario"}
            }
            mcp_tool["inputSchema"]["required"] = ["userId"]

        elif tool["name"] == "update_user":
            mcp_tool["inputSchema"]["properties"] = {
                "userId": {"type": "number", "description": "ID del usuario"},
                "name": {"type": "string", "description": "Nombre del usuario"},
                "email": {"type": "string", "description": "Email del usuario"},
                "department": {"type": "string", "description": "Departamento"},
                "role": {"type": "string", "description": "Rol del usuario"}
            }
            mcp_tool["inputSchema"]["required"] = ["userId", "name", "email", "department", "role"]

        elif tool["name"] == "find_users_by_department":
            mcp_tool["inputSchema"]["properties"] = {
                "department": {"type": "string", "description": "Departamento a buscar"}
            }
            mcp_tool["inputSchema"]["required"] = ["department"]

        elif tool["name"] == "find_all_users":
            pass  # No requiere parámetros

        elif tool["name"] == "get_connection_info":
            pass  # No requiere parámetros

        mcp_tools.append(mcp_tool)

    return {"tools": mcp_tools}

def handle_tools_call(params: Dict[str, Any]) -> Dict[str, Any]:
    """Maneja la llamada a una herramienta"""
    tool_name = params.get("name")
    arguments = params.get("arguments", {})

    try:
        result = call_tool(tool_name, arguments)

        # Formatear el resultado según el tipo de herramienta
        if isinstance(result, dict):
            if "result" in result:
                content = result["result"]
            else:
                content = result
        else:
            content = result

        # Convertir el contenido a string si es necesario
        if isinstance(content, (dict, list)):
            content_str = json.dumps(content, indent=2, ensure_ascii=False)
        else:
            content_str = str(content)

        return {
            "content": [
                {
                    "type": "text",
                    "text": content_str
                }
            ]
        }

    except Exception as e:
        return {
            "content": [
                {
                    "type": "text",
                    "text": f"Error: {str(e)}"
                }
            ],
            "isError": True
        }

def handle_request(request: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Maneja una solicitud JSON-RPC"""
    method = request.get("method")
    params = request.get("params", {})
    request_id = request.get("id")

    try:
        if method == "initialize":
            result = handle_initialize(params)
        elif method == "tools/list":
            result = handle_tools_list(params)
        elif method == "tools/call":
            result = handle_tools_call(params)
        else:
            raise Exception(f"Método no soportado: {method}")

        if request_id is not None:
            return {
                "jsonrpc": "2.0",
                "id": request_id,
                "result": result
            }

        return None

    except Exception as e:
        log_error(f"Error manejando {method}: {e}")
        if request_id is not None:
            return {
                "jsonrpc": "2.0",
                "id": request_id,
                "error": {
                    "code": -32603,
                    "message": str(e)
                }
            }
        return None

def main():
    """Función principal del adaptador MCP"""
    # Iniciar el servidor Spring Boot
    if not start_spring_server():
        print(json.dumps({
            "jsonrpc": "2.0",
            "error": {
                "code": -32603,
                "message": "No se pudo iniciar el servidor Spring Boot"
            }
        }), flush=True)
        sys.exit(1)

    try:
        # Procesar solicitudes desde stdin
        for line in sys.stdin:
            line = line.strip()
            if not line:
                continue

            try:
                request = json.loads(line)
                response = handle_request(request)

                if response:
                    print(json.dumps(response), flush=True)

            except json.JSONDecodeError as e:
                if os.environ.get('MCP_DEBUG'):
                    print(f"Error decodificando JSON: {e}", file=sys.stderr, flush=True)
                continue

    except KeyboardInterrupt:
        pass

    finally:
        stop_spring_server()

if __name__ == "__main__":
    main()
