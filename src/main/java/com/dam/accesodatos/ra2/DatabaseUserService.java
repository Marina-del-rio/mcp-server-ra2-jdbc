package com.dam.accesodatos.ra2;

import com.dam.accesodatos.model.User;
import com.dam.accesodatos.model.UserCreateDto;
import com.dam.accesodatos.model.UserQueryDto;
import com.dam.accesodatos.model.UserUpdateDto;
import org.springframework.ai.mcp.server.annotation.Tool;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * Interface de servicio para operaciones JDBC con usuarios
 *
 * RA2: Desarrolla aplicaciones que gestionan información almacenada mediante conectores
 *
 * Esta interface define 13 herramientas MCP (métodos @Tool) que los estudiantes deben implementar
 * usando JDBC puro (Connection, PreparedStatement, ResultSet, etc.)
 *
 * Métodos organizados por criterios de evaluación:
 * - CE2.a: Conexión y gestión de conexiones (1 método)
 * - CE2.b: Operaciones CRUD básicas (5 métodos)
 * - CE2.c: Consultas avanzadas (2 métodos)
 * - CE2.d: Transacciones (2 métodos)
 * - CE2.e: Metadatos (2 métodos)
 * - CE2.f: Funciones de agregación (1 método)
 */
public interface DatabaseUserService {

    // ========== CE2.a: Connection Management ==========

    /**
     * CE2.a: Prueba la conexión a la base de datos
     *
     * Implementación requerida:
     * - Obtener Connection del DataSource
     * - Validar que la conexión esté abierta con !conn.isClosed()
     * - Ejecutar una query simple: SELECT 1
     * - Cerrar conexión con try-with-resources
     *
     * Clases JDBC requeridas:
     * - javax.sql.DataSource (inyectado por Spring)
     * - java.sql.Connection
     * - java.sql.Statement o PreparedStatement
     *
     * @return Mensaje indicando si la conexión fue exitosa y versión de la BD
     * @throws RuntimeException si no se puede conectar
     */
    @Tool(name = "test_connection",
            description = "Prueba la conexión a la base de datos H2 usando JDBC")
    String testConnection();


    // ========== CE2.b: CRUD Operations ==========

    /**
     * CE2.b: Crea un nuevo usuario en la base de datos
     *
     * Implementación requerida:
     * - Usar PreparedStatement con SQL INSERT
     * - Setear parámetros con setString(), setBoolean(), etc.
     * - Ejecutar con executeUpdate()
     * - Obtener ID generado con getGeneratedKeys()
     * - Usar Statement.RETURN_GENERATED_KEYS
     *
     * Clases JDBC requeridas:
     * - java.sql.Connection
     * - java.sql.PreparedStatement
     * - java.sql.ResultSet (para getGeneratedKeys)
     * - java.sql.Statement (RETURN_GENERATED_KEYS)
     *
     * @param dto DTO con datos del usuario a crear
     * @return Usuario creado con ID generado
     * @throws RuntimeException si hay error o email duplicado
     */
    @Tool(name = "create_user",
            description = "Inserta un nuevo usuario usando PreparedStatement y retorna el ID generado")
    User createUser(UserCreateDto dto);

    /**
     * CE2.b: Busca un usuario por su ID
     *
     * Implementación requerida:
     * - Usar PreparedStatement con SQL SELECT WHERE id = ?
     * - Navegar ResultSet con rs.next()
     * - Mapear columnas a objeto User usando rs.getLong(), rs.getString(), etc.
     * - Manejar caso cuando no existe (retornar null o lanzar excepción)
     *
     * Clases JDBC requeridas:
     * - java.sql.PreparedStatement
     * - java.sql.ResultSet
     * - Métodos: getLong(), getString(), getBoolean(), getTimestamp()
     *
     * @param id ID del usuario a buscar
     * @return Usuario encontrado o null si no existe
     * @throws RuntimeException si hay error de BD
     */
    @Tool(name = "find_user_by_id",
            description = "Busca un usuario por ID usando SELECT con PreparedStatement")
    User findUserById(Long id);

    /**
     * CE2.b: Actualiza los datos de un usuario existente
     *
     * Implementación requerida:
     * - Primero verificar que el usuario existe (SELECT)
     * - Usar PreparedStatement con SQL UPDATE
     * - Actualizar solo campos no nulos del DTO
     * - Actualizar campo updated_at con CURRENT_TIMESTAMP
     * - Retornar usuario actualizado
     *
     * Clases JDBC requeridas:
     * - java.sql.PreparedStatement (SELECT y UPDATE)
     * - java.sql.ResultSet
     *
     * @param id ID del usuario a actualizar
     * @param dto DTO con datos a actualizar (campos opcionales)
     * @return Usuario actualizado
     * @throws RuntimeException si el usuario no existe o hay error
     */
    @Tool(name = "update_user",
            description = "Actualiza un usuario existente usando UPDATE statement")
    User updateUser(Long id, UserUpdateDto dto);

    /**
     * CE2.b: Elimina un usuario de la base de datos
     *
     * Implementación requerida:
     * - Usar PreparedStatement con SQL DELETE WHERE id = ?
     * - Ejecutar con executeUpdate()
     * - Verificar filas afectadas para confirmar eliminación
     *
     * Clases JDBC requeridas:
     * - java.sql.PreparedStatement
     * - executeUpdate() retorna int (filas afectadas)
     *
     * @param id ID del usuario a eliminar
     * @return true si se eliminó, false si no existía
     * @throws RuntimeException si hay error de BD
     */
    @Tool(name = "delete_user",
            description = "Elimina un usuario usando DELETE statement")
    boolean deleteUser(Long id);

    /**
     * CE2.b: Obtiene todos los usuarios
     *
     * Implementación requerida:
     * - Usar PreparedStatement o Statement con SELECT * FROM users
     * - Iterar ResultSet con while(rs.next())
     * - Mapear cada fila a objeto User
     * - Agregar a lista
     *
     * Clases JDBC requeridas:
     * - java.sql.Statement o PreparedStatement
     * - java.sql.ResultSet
     * - Métodos de navegación: next()
     *
     * @return Lista de todos los usuarios
     * @throws RuntimeException si hay error
     */
    @Tool(name = "find_all_users",
            description = "Obtiene todos los usuarios de la base de datos")
    List<User> findAll();

    // ========== CE2.c: Advanced Queries ==========

    /**
     * CE2.c: Busca usuarios por departamento
     *
     * Implementación requerida:
     * - Usar PreparedStatement con WHERE department = ?
     * - Opcionalmente filtrar por active = true
     * - Ordenar por name
     *
     * Clases JDBC requeridas:
     * - java.sql.PreparedStatement
     * - java.sql.ResultSet
     *
     * @param department Nombre del departamento
     * @return Lista de usuarios del departamento
     * @throws RuntimeException si hay error
     */
    @Tool(name = "find_users_by_department",
            description = "Busca usuarios por departamento con filtro WHERE")
    List<User> findUsersByDepartment(String department);

    /**
     * CE2.c: Busca usuarios con filtros dinámicos
     *
     * Implementación requerida:
     * - Construir query SQL dinámica según filtros presentes
     * - Usar PreparedStatement con múltiples placeholders
     * - Manejar filtros opcionales (department, role, active)
     * - Aplicar paginación con LIMIT y OFFSET
     *
     * Clases JDBC requeridas:
     * - java.sql.PreparedStatement
     * - StringBuilder para construir SQL dinámico
     *
     * @param query DTO con filtros opcionales y paginación
     * @return Lista de usuarios que cumplen los criterios
     * @throws RuntimeException si hay error
     */
    @Tool(name = "search_users",
            description = "Busca usuarios con múltiples filtros opcionales y paginación")
    List<User> searchUsers(UserQueryDto query);


    // ========== CE2.d: Transactions ==========

    /**
     * CE2.d: Transfiere múltiples usuarios en una transacción
     *
     * Implementación requerida:
     * - Desactivar auto-commit: conn.setAutoCommit(false)
     * - Insertar múltiples usuarios en bucle
     * - Si alguno falla, hacer rollback: conn.rollback()
     * - Si todos tienen éxito, hacer commit: conn.commit()
     * - Restaurar auto-commit al final
     *
     * Clases JDBC requeridas:
     * - java.sql.Connection
     * - Métodos: setAutoCommit(), commit(), rollback()
     * - java.sql.PreparedStatement
     *
     * @param users Lista de usuarios a insertar en transacción
     * @return true si la transacción fue exitosa
     * @throws RuntimeException si hay error y se hace rollback
     */
    @Tool(name = "transfer_data",
            description = "Inserta múltiples usuarios en una transacción con commit/rollback")
    boolean transferData(List<User> users);

    /**
     * CE2.d: Inserta múltiples usuarios usando batch operations
     *
     * Implementación requerida:
     * - Usar PreparedStatement.addBatch()
     * - Ejecutar con executeBatch()
     * - Procesar int[] resultados
     *
     * Clases JDBC requeridas:
     * - java.sql.PreparedStatement
     * - Métodos: addBatch(), executeBatch()
     *
     * @param users Lista de usuarios a insertar
     * @return Número de usuarios insertados exitosamente
     * @throws RuntimeException si hay error
     */
    @Tool(name = "batch_insert_users",
            description = "Inserta múltiples usuarios usando batch operations")
    int batchInsertUsers(List<User> users);

    // ========== CE2.e: Metadata ==========

    /**
     * CE2.e: Obtiene información de la base de datos usando DatabaseMetaData
     *
     * Implementación requerida:
     * - Obtener DatabaseMetaData: conn.getMetaData()
     * - Consultar: database product name, version, driver name, driver version
     * - Consultar: max connections, max tables, etc.
     *
     * Clases JDBC requeridas:
     * - java.sql.DatabaseMetaData
     * - Métodos: getDatabaseProductName(), getDatabaseProductVersion(), etc.
     *
     * @return Información detallada de la base de datos
     * @throws RuntimeException si hay error
     */
    @Tool(name = "get_database_info",
            description = "Obtiene metadatos de la base de datos usando DatabaseMetaData")
    String getDatabaseInfo();

    /**
     * CE2.e: Obtiene información de las columnas de una tabla
     *
     * Implementación requerida:
     * - Usar DatabaseMetaData.getColumns()
     * - Iterar ResultSet con información de columnas
     * - Obtener: nombre, tipo SQL, tamaño, nullable, etc.
     *
     * Clases JDBC requeridas:
     * - java.sql.DatabaseMetaData
     * - Métodos: getColumns(catalog, schema, tableName, columnPattern)
     * - java.sql.ResultSet para iterar columnas
     *
     * @param tableName Nombre de la tabla
     * @return Lista con información de cada columna
     * @throws RuntimeException si la tabla no existe o hay error
     */
    @Tool(name = "get_table_columns",
            description = "Obtiene metadatos de las columnas de una tabla usando ResultSetMetaData")
    List<Map<String, Object>> getTableColumns(String tableName);

    // ========== CE2.f: Funciones de Agregación ==========

    /**
     * CE2.f: Cuenta usuarios activos por departamento usando función COUNT
     *
     * Implementación requerida:
     * - Usar PreparedStatement con SELECT COUNT(*)
     * - Filtrar por department y active = true
     * - Retornar el número de usuarios encontrados
     *
     * Clases JDBC requeridas:
     * - java.sql.PreparedStatement
     * - java.sql.ResultSet
     * - Función SQL: COUNT(*)
     *
     * @param department Departamento a contar
     * @return Número de usuarios activos en el departamento
     * @throws RuntimeException si hay error
     */
    @Tool(name = "execute_count_by_department",
            description = "Cuenta usuarios activos por departamento usando COUNT")
    int executeCountByDepartment(String department);
}