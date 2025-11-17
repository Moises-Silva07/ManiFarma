package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.DTO.*;
import dev.java.ManiFarma.Entity.*;
import dev.java.ManiFarma.Repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final EmployeeRepository employeeRepository;
    private final ProdutoRepository produtoRepository;
    private final PaymentService paymentService;
    private final EmailService emailService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            EmployeeRepository employeeRepository,
            ProdutoRepository produtoRepository,
            PaymentService paymentService,
            EmailService emailService
    ) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.employeeRepository = employeeRepository;
        this.produtoRepository = produtoRepository;
        this.paymentService = paymentService;
        this.emailService = emailService;
    }


    @Transactional
    public PedidoResponseDTO criarPedidoMultipart(
            String descricao,
            StatusPedido status,
            Long clienteId,
            Long employeeId,
            MultipartFile receitaFile
    ) {
        //  Valida se a imagem foi enviada
        if (receitaFile == null || receitaFile.isEmpty()) {
            throw new IllegalArgumentException("A imagem da receita é obrigatória!");
        }

        //  Verifica se o cliente existe
        Cliente cliente = (Cliente) clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + clienteId));

        // Cria o novo pedido
        Pedido pedido = new Pedido();
        pedido.setDescricao(descricao);
        pedido.setStatus(StatusPedido.PENDENTE); // Sempre inicia como PENDENTE
        pedido.setCliente(cliente);
        pedido.setValorTotal(0.0);

        // Se houver funcionário associado
        if (employeeId != null) {
            User user = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado com ID: " + employeeId));

            if (!(user instanceof Employee)) {
                throw new ClassCastException("O usuário com ID " + user.getId() + " é um Cliente, não um Funcionário.");
            }
            pedido.setEmployee((Employee) user);
        }

        // Salva a imagem localmente e guarda o caminho
        String caminhoCompleto = salvarImagemReceita(receitaFile);
        pedido.setCaminhoReceita(caminhoCompleto);
        pedido.setReceita(receitaFile.getOriginalFilename());

        // Salva o pedido no banco
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // Isso será feito depois pelo método gerarLinkEEnviarEmail()
        System.out.println(" Pedido criado com sucesso! ID: " + pedidoSalvo.getId());

        //  Retorna o DTO
        return toDTO(pedidoSalvo);
    }


    //  MÉTODO PRIVADO - Salva a imagem no servidor COM VALIDAÇÕES

    private String salvarImagemReceita(MultipartFile arquivo) {
        try {
            // Valida o tipo do arquivo
            String contentType = arquivo.getContentType();
            if (contentType == null ||
                    (!contentType.equals("image/jpeg") &&
                            !contentType.equals("image/png") &&
                            !contentType.equals("image/jpg"))) {
                throw new IllegalArgumentException("Apenas imagens JPG, JPEG ou PNG são permitidas!");
            }

            // Valida o tamanho (máximo 5MB)
            if (arquivo.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("A imagem não pode ter mais de 5MB!");
            }

            // Define o diretório de upload
            String uploadDir = "uploads/receitas/";
            Path uploadPath = Paths.get(uploadDir);

            // Cria o diretório se não existir
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println(" Diretório criado: " + uploadPath.toAbsolutePath());
            }

            //  Gera nome único para o arquivo
            String nomeOriginal = arquivo.getOriginalFilename();
            String extensao = nomeOriginal != null ? nomeOriginal.substring(nomeOriginal.lastIndexOf(".")) : ".jpg";
            String nomeArquivo = System.currentTimeMillis() + extensao;

            //  Salva o arquivo
            Path caminhoCompleto = uploadPath.resolve(nomeArquivo);
            Files.copy(arquivo.getInputStream(), caminhoCompleto);

            System.out.println(" Imagem salva em: " + caminhoCompleto.toAbsolutePath());

            return caminhoCompleto.toString();

        } catch (IllegalArgumentException e) {
            // Repassa erros de validação
            throw e;
        } catch (Exception e) {
            // Erros de I/O
            System.err.println(" Erro ao salvar imagem: " + e.getMessage());
            throw new RuntimeException("Erro ao salvar a imagem da receita: " + e.getMessage());
        }
    }


    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO request) {
        Cliente cliente = (Cliente) clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + request.getClienteId()));

        Pedido pedido = new Pedido();
        pedido.setDescricao(request.getDescricao());
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setReceita(request.getReceita());
        pedido.setCliente(cliente);

        if (request.getEmployeeId() != null) {
            User user = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado com ID: " + request.getEmployeeId()));
            if (!(user instanceof Employee)) {
                throw new ClassCastException("O usuário com ID " + user.getId() + " é um Cliente, não um Funcionário.");
            }
            pedido.setEmployee((Employee) user);
        }

        double valorTotalPedido = 0.0;

        if (request.getItens() != null && !request.getItens().isEmpty()) {
            List<PedidoProduto> itens = new ArrayList<>();
            for (PedidoProdutoRequestDTO itemDTO : request.getItens()) {
                Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                        .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + itemDTO.getProdutoId()));
                valorTotalPedido += produto.getPreco() * itemDTO.getQuantidade();

                PedidoProduto item = new PedidoProduto();
                item.setPedido(pedido);
                item.setProduto(produto);
                item.setQuantidade(itemDTO.getQuantidade());
                itens.add(item);
            }
            pedido.setItens(itens);
        }

        pedido.setValorTotal(valorTotalPedido);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // Gera link e envia email
        String linkPagamento = paymentService.criarLinkDePagamento(pedidoSalvo);
        emailService.enviarEmailPagamento(pedidoSalvo.getCliente(), pedidoSalvo, linkPagamento);

        return toDTO(pedidoSalvo);
    }


    // MÉTODOS DE LISTAGEM

    public List<PedidoResponseDTO> getAllPedidos() {
        return pedidoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PedidoResponseDTO getPedidoById(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + id));
        return toDTO(pedido);
    }

    public List<PedidoResponseDTO> getPedidosPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ==========================================================
    // 1. NOVO MÉTODO ADICIONADO AQUI
    // ==========================================================
    public List<PedidoResponseDTO> getPedidosPorFuncionario(Long employeeId) {
        // Verifica se o funcionário existe (opcional, mas boa prática)
        if (!employeeRepository.existsById(employeeId)) {
             throw new EntityNotFoundException("Funcionário não encontrado com ID: " + employeeId);
        }
        
        return pedidoRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    // CONVERSÃO PARA DTO

    private PedidoResponseDTO toDTO(Pedido pedido) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setDescricao(pedido.getDescricao());
        dto.setStatus(pedido.getStatus());
        dto.setReceita(pedido.getReceita());
        dto.setValorTotal(pedido.getValorTotal() != null ? pedido.getValorTotal() : 0.0);

        dto.setClienteId(pedido.getCliente().getId());
        dto.setClienteNome(pedido.getCliente().getNome());
        dto.setClienteTelefone(pedido.getCliente().getTelefone());

        if (pedido.getEmployee() != null) {
            dto.setEmployeeId(pedido.getEmployee().getId());
            dto.setEmployeeNome(pedido.getEmployee().getNome());
        }

        dto.setLinkPagamento(pedido.getLinkPagamento());

        if (pedido.getItens() != null) {
            dto.setItens(pedido.getItens().stream().map(item -> {
                PedidoProdutoResponseDTO itemDto = new PedidoProdutoResponseDTO();
                if (item.getProduto() != null) {
                    itemDto.setProdutoId(item.getProduto().getId());
                    itemDto.setProdutoNome(item.getProduto().getNome());
                } else {
                    itemDto.setProdutoNome("Produto removido");
                }
                itemDto.setQuantidade(item.getQuantidade());
                return itemDto;
            }).collect(Collectors.toList()));
        }

        return dto;
    }


    // GERAR LINK DE PAGAMENTO E ENVIAR EMAIL (Feito pelo funcionário)

    @Transactional
    public void gerarLinkEEnviarEmail(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + pedidoId));
        
        // ADICIONADO: Validação para impedir geração de link sem valor
        if (pedido.getValorTotal() == null || pedido.getValorTotal() <= 0) {
            throw new RuntimeException("Não é possível gerar cotação: O pedido #" + pedidoId + " está com valor total R$0,00 ou nulo.");
        }

        // Gera o link de pagamento
        String linkPagamento = paymentService.criarLinkDePagamento(pedido);

        // Atualiza o pedido com o link e muda o status
        pedido.setLinkPagamento(linkPagamento);
        pedido.setStatus(StatusPedido.ENVIODECOTACAO);
        pedidoRepository.save(pedido);

        // Envia o email com o link
        emailService.enviarEmailPagamento(pedido.getCliente(), pedido, linkPagamento);

        System.out.println("📧 Email de cotação enviado para o pedido #" + pedidoId);
    }


    //  ATRIBUIR FUNCIONÁRIO AO PEDIDO
    @Transactional
    public void atribuirFuncionario(Long pedidoId, Long employeeId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + pedidoId));

        Employee employee = (Employee) employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado: " + employeeId));

        pedido.setEmployee(employee);
        pedidoRepository.save(pedido);

        System.out.println("👤 Funcionário " + employee.getNome() + " atribuído ao pedido #" + pedidoId);
    }


    // ALTERAR STATUS DO PEDIDO

    @Transactional
    public void alterarStatus(Long pedidoId, String novoStatus) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + pedidoId));

        String statusFormatado = novoStatus.trim().toUpperCase();

        try {
            StatusPedido statusEnum = StatusPedido.valueOf(statusFormatado);
            pedido.setStatus(statusEnum);
            pedidoRepository.save(pedido);

            System.out.println("📝 Status do pedido #" + pedidoId + " alterado para: " + statusEnum);

        } catch (IllegalArgumentException e) {
            // ERRO 400
            throw new IllegalArgumentException("Status inválido enviado: " + novoStatus +
                    ". Valores aceitos: PENDENTE, VALIDO, ENVIODECOTACAO, PAGO, CONCLUIDO, CANCELADO");
        }
    }

    // Lista os pedidos por Valido
    public List<PedidoResponseDTO> getPedidosPorStatus(String status) {
        StatusPedido statusEnum;
        try {
            statusEnum = StatusPedido.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            // ERRO 400
            throw new IllegalArgumentException("Status inválido: " + status);
        }

        return pedidoRepository.findAll().stream()
                .filter(p -> p.getStatus() == statusEnum)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Adiciona os itens no pedido
    @Transactional
    public PedidoResponseDTO adicionarItensAoPedido(Long pedidoId, List<PedidoProdutoRequestDTO> itens) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + pedidoId));

        double novoValor = pedido.getValorTotal() != null ? pedido.getValorTotal() : 0.0;
        List<PedidoProduto> novosItens = new ArrayList<>();

        for (PedidoProdutoRequestDTO itemDTO : itens) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + itemDTO.getProdutoId()));

            PedidoProduto novoItem = new PedidoProduto();
            novoItem.setPedido(pedido);
            novoItem.setProduto(produto);
            novoItem.setQuantidade(itemDTO.getQuantidade());
            novosItens.add(novoItem);

            novoValor += produto.getPreco() * itemDTO.getQuantidade();
        }

        // Adiciona os novos itens à lista existente (se houver)
        if (pedido.getItens() == null) {
            pedido.setItens(novosItens);
        } else {
            pedido.getItens().addAll(novosItens);
        }

        pedido.setValorTotal(novoValor);
        pedidoRepository.save(pedido);

        return toDTO(pedido);
    }
}