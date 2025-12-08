//package dev.java.ManiFarma.Service;

//import dev.java.ManiFarma.DTO.LoginRequest;
//import dev.java.ManiFarma.DTO.UserRegisterRequestDTO;
//import dev.java.ManiFarma.DTO.UserResponseDTO;
//import dev.java.ManiFarma.Entity.Cliente;
//import dev.java.ManiFarma.Entity.Employee;
//import dev.java.ManiFarma.Entity.User;
//import dev.java.ManiFarma.Repository.UserRepository;
//import dev.java.ManiFarma.utils.JwtUtil;
//import org.springframework.stereotype.Service;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//import java.util.Optional;

//@Service
//public class AuthService {

//    private final UserRepository userRepository;
//    private final BCryptPasswordEncoder passwordEncoder;
//    private final JwtUtil jwtUtil;
//    private final AuthenticationManager authenticationManager;

//    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = new BCryptPasswordEncoder();
//        this.jwtUtil = jwtUtil;
//        this.authenticationManager = authenticationManager;
//    }

//    public UserResponseDTO register(UserRegisterRequestDTO request) {
//        if (userRepository.existsByEmail(request.getEmail())) {
            // CORRIGIDO: Lança uma exceção 400 (Bad Request)
//            throw new IllegalArgumentException("Email já cadastrado");
  //      }

    //    User newUser;

      //  if (request.isClient()) {
        //    Cliente cliente = new Cliente();
          //  cliente.setNome(request.getNome());
            //cliente.setEmail(request.getEmail());
//            cliente.setSenha(passwordEncoder.encode(request.getSenha()));
  //          cliente.setClient(true);
    //        cliente.setCpf(request.getCpf());
      //      cliente.setCep(request.getCep());
        //    cliente.setRua(request.getRua());
          //  cliente.setBairro(request.getBairro());
           // cliente.setCidade(request.getCidade());
          //  cliente.setEstado(request.getEstado());
           // cliente.setTelefone(request.getTelefone());
         //   newUser = cliente;
        //} else {
//            Employee employee = new Employee();
  //          employee.setNome(request.getNome());
    //        employee.setEmail(request.getEmail());
      //      employee.setSenha(passwordEncoder.encode(request.getSenha()));
        //    employee.setClient(false);
          //  employee.setRole(request.getRole());
            //employee.setSalary(request.getSalary());
//            employee.setShift(request.getShift());
  //          newUser = employee;
    //    }

      //  User savedUser = userRepository.save(newUser);
 //       return toUserResponseDTO(savedUser);
   // }

   // public UserResponseDTO login(LoginRequest request) {
        //para autenticar o usuário
        // Este método 'authenticate' lança AuthenticationException se o login falhar
     //   Authentication authentication = authenticationManager.authenticate(
    //            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
   //     );


     //   Optional<User> optUser = userRepository.findByEmail(request.getEmail());
      //  if (optUser.isEmpty()) {
        //    throw new RuntimeException("Usuário não encontrado após autenticação bem-sucedida. Isso não deveria acontecer.");
 //       }
   //     User loggedInUser = optUser.get();
     //   String token = jwtUtil.generateToken(loggedInUser.getEmail());

       // UserResponseDTO responseDTO = toUserResponseDTO(loggedInUser);
 //       responseDTO.setToken(token);
   //     return responseDTO;
  //  }

    //private UserResponseDTO toUserResponseDTO(User user) {
      //  UserResponseDTO dto = new UserResponseDTO();
        //dto.setId(user.getId());
  //      dto.setNome(user.getNome());
    //    dto.setEmail(user.getEmail());
      //  dto.setClient(user.isClient());

        //if (user instanceof Cliente) {
          //  Cliente cliente = (Cliente) user;
            //dto.setCpf(cliente.getCpf());
         //   dto.setCep(cliente.getCep());
           // dto.setRua(cliente.getRua());
  //          dto.setBairro(cliente.getBairro());
    //        dto.setCidade(cliente.getCidade());
      //      dto.setEstado(cliente.getEstado());
        //    dto.setTelefone(cliente.getTelefone());
 //       } else if (user instanceof Employee) {
   //         Employee employee = (Employee) user;
     //       dto.setRole(employee.getRole());
       //     dto.setSalary(employee.getSalary());
         //   dto.setShift(employee.getShift());
      //  }
  //      return dto;
 //   }
//}