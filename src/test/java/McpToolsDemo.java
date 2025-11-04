import com.dam.accesodatos.config.DatabaseConfig;
import com.dam.accesodatos.model.User;
import com.dam.accesodatos.model.UserCreateDto;
import com.dam.accesodatos.model.UserUpdateDto;
import com.dam.accesodatos.ra2.DatabaseUserServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Demostración de las herramientas MCP JDBC
 * Este programa muestra cómo funcionan los 5 métodos implementados
 */
public class McpToolsDemo {

    public static void main(String[] args) {
        System.out.println("=== DEMOSTRACIÓN MCP SERVER RA2 JDBC ===\n");

        // Inicializar la base de datos
        DatabaseConfig.initializeDatabase();

        // Crear instancia del servicio
        DatabaseUserServiceImpl service = new DatabaseUserServiceImpl();

        try {
            // 1. TEST CONNECTION - Prueba de conexión básica
            System.out.println("1️⃣  test_connection:");
            String connectionResult = service.testConnection();
            System.out.println("   " + connectionResult);
            System.out.println();

            // 2. FIND USER BY ID - Consultar usuario existente
            System.out.println("2️⃣  find_user_by_id (id=1):");
            User user1 = service.findUserById(1L);
            System.out.println("   ID: " + user1.getId());
            System.out.println("   Nombre: " + user1.getName());
            System.out.println("   Email: " + user1.getEmail());
            System.out.println("   Departamento: " + user1.getDepartment());
            System.out.println("   Rol: " + user1.getRole());
            System.out.println("   Activo: " + user1.getActive());
            System.out.println();

            // 3. CREATE USER - Insertar nuevo usuario
            System.out.println("3️⃣  create_user:");
            long timestamp = System.currentTimeMillis();
            UserCreateDto newUserDto = new UserCreateDto();
            newUserDto.setName("MCP Test User");
            newUserDto.setEmail("mcp.test." + timestamp + "@example.com");
            newUserDto.setDepartment("IT");
            newUserDto.setRole("Developer");

            User createdUser = service.createUser(newUserDto);
            System.out.println("   ✓ Usuario creado con ID: " + createdUser.getId());
            System.out.println("   Nombre: " + createdUser.getName());
            System.out.println("   Email: " + createdUser.getEmail());
            System.out.println();

            // 4. UPDATE USER - Actualizar usuario
            System.out.println("4️⃣  update_user (id=" + createdUser.getId() + "):");
            UserUpdateDto updateDto = new UserUpdateDto();
            updateDto.setName("MCP Test User UPDATED");
            updateDto.setEmail("mcp.updated." + timestamp + "@example.com");
            updateDto.setDepartment("IT");
            updateDto.setRole("Senior Developer");
            updateDto.setActive(true);

            User updatedUser = service.updateUser(createdUser.getId(), updateDto);
            System.out.println("   ✓ Usuario actualizado");
            System.out.println("   Nombre: " + updatedUser.getName());
            System.out.println("   Email: " + updatedUser.getEmail());
            System.out.println("   Rol: " + updatedUser.getRole());
            System.out.println();

            // 5. TRANSFER DATA - Demostración de transacciones
            System.out.println("5️⃣  transfer_data:");
            List<User> usersToTransfer = new ArrayList<>();
            User u1 = new User();
            u1.setName("Transfer User 1");
            u1.setEmail("transfer1." + timestamp + "@example.com");
            u1.setDepartment("Finance");
            u1.setRole("Analyst");
            u1.setActive(true);

            User u2 = new User();
            u2.setName("Transfer User 2");
            u2.setEmail("transfer2." + timestamp + "@example.com");
            u2.setDepartment("Finance");
            u2.setRole("Manager");
            u2.setActive(true);

            usersToTransfer.add(u1);
            usersToTransfer.add(u2);

            boolean transferResult = service.transferData(usersToTransfer);
            System.out.println("   ✓ Transacción " + (transferResult ? "exitosa" : "fallida"));
            System.out.println("   Usuarios transferidos: " + usersToTransfer.size());
            System.out.println();

            System.out.println("=== ✓ TODAS LAS HERRAMIENTAS MCP FUNCIONAN CORRECTAMENTE ===");
            System.out.println("\n📊 Herramientas disponibles:");
            System.out.println("   ✅ test_connection - Prueba de conexión JDBC");
            System.out.println("   ✅ create_user - Insertar nuevo usuario");
            System.out.println("   ✅ find_user_by_id - Consultar usuario por ID");
            System.out.println("   ✅ update_user - Actualizar datos de usuario");
            System.out.println("   ✅ transfer_data - Demostración de transacciones");
            System.out.println("\n⚠️  Herramientas TODO (para estudiantes):");
            System.out.println("   ❌ get_connection_info");
            System.out.println("   ❌ delete_user");
            System.out.println("   ❌ find_all_users");
            System.out.println("   ❌ find_users_by_department");
            System.out.println("   ❌ search_users");
            System.out.println("   ❌ find_users_with_pagination");
            System.out.println("   ❌ batch_insert_users");
            System.out.println("   ❌ get_database_info");
            System.out.println("   ❌ get_table_columns");
            System.out.println("   ❌ execute_count_by_department");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
