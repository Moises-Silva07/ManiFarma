-- Remove tabelas existentes na ordem correta para evitar erros de dependência
DROP TABLE IF EXISTS pedido_produto;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS produtos;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS clientes;
DROP TABLE IF EXISTS users;

-- Cria a tabela base de usuários
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    is_client BOOLEAN NOT NULL,
    is_disabled BOOLEAN NOT NULL DEFAULT FALSE ,-- ADICIONE ESTA LINHA
    is_anonymized BOOLEAN NOT NULL DEFAULT FALSE
);

-- Tabela de clientes, usando o ID de users como chave primária e estrangeira
CREATE TABLE clientes (
    id BIGINT NOT NULL PRIMARY KEY,
    cpf VARCHAR(14),
    cep VARCHAR(255),
    rua VARCHAR(255),
    bairro VARCHAR(255),
    cidade VARCHAR(255),
    estado VARCHAR(255),
    telefone VARCHAR(20),
    CONSTRAINT fk_clientes_users FOREIGN KEY (id) REFERENCES users(id)
);

-- Tabela de funcionários, usando o ID de users como chave primária e estrangeira
CREATE TABLE employees (
    id BIGINT NOT NULL PRIMARY KEY,
    role VARCHAR(255),
    salary DOUBLE PRECISION,
    shift VARCHAR(255),
    CONSTRAINT fk_employees_users FOREIGN KEY (id) REFERENCES users(id)
);

-- Tabela de produtos (simplificada para corresponder à sua entidade Java)
CREATE TABLE produtos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    preco DOUBLE PRECISION NOT NULL
);

-- Tabela de pedidos
-- Corrigido: Adicionada a coluna employee_id e a chave estrangeira correspondente
CREATE TABLE pedido (
    id BIGSERIAL PRIMARY KEY,
    descricao TEXT,
    status VARCHAR(50) NOT NULL,
    receita VARCHAR(255),
    valor_total DOUBLE PRECISION, -- <--- ADICIONE ESTA LINHA
    cliente_id BIGINT NOT NULL, -- Um pedido sempre deve ter um cliente
    employee_id BIGINT, -- Um funcionário pode ser atribuído depois
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_pedido_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- Tabela de associação para os itens do pedido
CREATE TABLE pedido_produto (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    CONSTRAINT fk_pp_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    CONSTRAINT fk_pp_produto FOREIGN KEY (produto_id) REFERENCES produtos(id)
);