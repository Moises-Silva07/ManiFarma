package dev.java.ManiFarma.Service;

import dev.java.ManiFarma.DTO.PedidoProdutoRequestDTO;
import dev.java.ManiFarma.DTO.PedidoProdutoResponseDTO;
import dev.java.ManiFarma.DTO.PedidoRequestDTO;
import dev.java.ManiFarma.DTO.PedidoResponseDTO;
import dev.java.ManiFarma.Entity.*;
import dev.java.ManiFarma.Repository.ClienteRepository;
import dev.java.ManiFarma.Repository.EmployeeRepository;
import dev.java.ManiFarma.Repository.PedidoRepository;
import dev.java.ManiFarma.Repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final EmployeeRepository employeeRepository;
    private final ProdutoRepository produtoRepository;
    
    // <-- INJETANDO NOVOS SERVIÇOS
    private final PaymentService paymentService;
    private final EmailService emailService;

    // <-- ATUALIZANDO CONSTRUTOR
    public PedidoService(PedidoRepository pedidoRepository, 
                         ClienteRepository clienteRepository, 
                         EmployeeRepository employeeRepository, 
                         ProdutoRepository produtoRepository,
                         PaymentService paymentService, // <-- NOVO
                         EmailService emailService) {  // <-- NOVO
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.employeeRepository = employeeRepository;
        this.produtoRepository = produtoRepository;
        this.paymentService = paymentService; // <-- NOVO
        this.emailService = emailService;   // <-- NOVO
    }

    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO request) {
        // 1. Busca o cliente. Se não existir, lança uma exceção clara.
        Cliente cliente = (Cliente) clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + request.getClienteId()));

        // 2. Cria a entidade Pedido
        Pedido pedido = new Pedido();
        pedido.setDescricao(request.getDescricao());
        pedido.setStatus(StatusPedido.PENDENTE); // Define o status inicial como pendente
        pedido.setReceita(request.getReceita());
        pedido.setCliente(cliente);

        // 3. Associa o funcionário (com verificação de tipo)
        if (request.getEmployeeId() != null) {
            User user = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado com ID: " + request.getEmployeeId()));

            // <-- Correção de segurança para ClassCastException
            if (!(user instanceof Employee)) {
                throw new ClassCastException("O usuário com ID " + user.getId() + " é um Cliente, não um Funcionário.");
            }

            Employee employee = (Employee) user;
            pedido.setEmployee(employee);
        }

        double valorTotalPedido = 0.0; // Variável para o total

        // 4. Processa os itens do pedido
        if (request.getItens() != null && !request.getItens().isEmpty()) {
            List<PedidoProduto> itens = new ArrayList<>();
            for (PedidoProdutoRequestDTO itemDTO : request.getItens()) {
                Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                        .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + itemDTO.getProdutoId()));

                // Cálculo do valor
                valorTotalPedido += produto.getPreco() * itemDTO.getQuantidade();

                PedidoProduto item = new PedidoProduto();
                item.setPedido(pedido);
                item.setProduto(produto);
                item.setQuantidade(itemDTO.getQuantidade());
                itens.add(item);
            }
            pedido.setItens(itens);
        }

        pedido.setValorTotal(valorTotalPedido); // Atribui o total ao pedido

        // 5. Salva o pedido e seus itens no banco de dados
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // --- INÍCIO DA NOVA LÓGICA DE PAGAMENTO E EMAIL ---

        // 6. Gerar o link de pagamento
        String linkPagamento = paymentService.criarLinkDePagamento(pedidoSalvo);

        // 7. Enviar o e-mail (de forma assíncrona, graças ao @Async no EmailService)
        emailService.enviarEmailPagamento(pedidoSalvo.getCliente(), pedidoSalvo, linkPagamento);

        // --- FIM DA NOVA LÓGICA ---

        // 8. Retorna o DTO de resposta
        return toDTO(pedidoSalvo);
    }

    public List<PedidoResponseDTO> getAllPedidos() {
        return pedidoRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PedidoResponseDTO getPedidoById(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + id));
        return toDTO(pedido);
    }
    
    public List<PedidoResponseDTO> getPedidosPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    // Converte a entidade Pedido para um DTO de resposta de forma segura
    private PedidoResponseDTO toDTO(Pedido pedido) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setDescricao(pedido.getDescricao());
        dto.setStatus(pedido.getStatus());
        dto.setReceita(pedido.getReceita());
        dto.setValorTotal(pedido.getValorTotal() != null ? pedido.getValorTotal() : 0.0);

        if (pedido.getCliente() != null) dto.setClienteId(pedido.getCliente().getId());
        if (pedido.getEmployee() != null) dto.setEmployeeId(pedido.getEmployee().getId());

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

    @Transactional
    public void gerarLinkEEnviarEmail(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + pedidoId));

        String linkPagamento = paymentService.criarLinkDePagamento(pedido);

        // opcional — salva o link no pedido
        pedido.setLinkPagamento(linkPagamento);
        pedido.setStatus(StatusPedido.ENVIODECOTACAO); // 🔹 muda o status automaticamente
        pedidoRepository.save(pedido);

        // envia o e-mail
        emailService.enviarEmailPagamento(pedido.getCliente(), pedido, linkPagamento);
    }

    @Transactional
    public void atribuirFuncionario(Long pedidoId, Long employeeId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + pedidoId));

        Employee employee = (Employee) employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado: " + employeeId));

        pedido.setEmployee(employee);
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void alterarStatus(Long pedidoId, String novoStatus) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + pedidoId));

        // Limpa espaços e converte para maiúscula
        String statusFormatado = novoStatus.trim().toUpperCase();

        // Verifica se o status é válido
        try {
            StatusPedido statusEnum = StatusPedido.valueOf(statusFormatado);
            pedido.setStatus(statusEnum);
            pedidoRepository.save(pedido);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido enviado: " + novoStatus);
        }
    }
}