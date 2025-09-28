// package dev.java.ManiFarma.Controller;

// import dev.java.ManiFarma.DTO.UserRegisterRequestDTO;
// import dev.java.ManiFarma.DTO.UserResponseDTO;
// import dev.java.ManiFarma.Service.UserService;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/users" )
// public class UserController {

//     private final UserService userService;

//     public UserController(UserService userService) {
//         this.userService = userService;
//     }

//     // Endpoint para criar um usuário (se não for feito exclusivamente pelo AuthService)
//     @PostMapping
//     public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRegisterRequestDTO request) {
//         UserResponseDTO newUser = userService.createUser(request);
//         return new ResponseEntity<>(newUser, HttpStatus.CREATED);
//     }

//     @GetMapping
//     public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
//         List<UserResponseDTO> users = userService.findAllUsers();
//         return ResponseEntity.ok(users);
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
//         UserResponseDTO user = userService.findUserById(id);
//         return ResponseEntity.ok(user);
//     }

//     // Endpoint de atualização usando UserRegisterRequestDTO
//     @PutMapping("/{id}")
//     public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody UserRegisterRequestDTO request) {
//         UserResponseDTO updatedUser = userService.updateUser(id, request);
//         return ResponseEntity.ok(updatedUser);
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
//         userService.deleteUser(id);
//         return ResponseEntity.noContent().build();
//     }
// }
