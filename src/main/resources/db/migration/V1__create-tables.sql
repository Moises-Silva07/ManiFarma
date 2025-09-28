CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    is_client BOOLEAN NOT NULL
);

CREATE TABLE clientes (
    id BIGINT PRIMARY KEY,
    cpf VARCHAR(14),
    endereco VARCHAR(255),
    telefone VARCHAR(20),
    FOREIGN KEY (id) REFERENCES users(id)
);

CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(255),
    salary DOUBLE PRECISION,
    shift VARCHAR(255)
);

CREATE TABLE produtos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco DOUBLE PRECISION NOT NULL,
    estoque INT NOT NULL
);

CREATE TABLE pedido (
    id BIGSERIAL PRIMARY KEY,
    descricao TEXT,
    status VARCHAR(50) NOT NULL,
    receita VARCHAR(255),
    cliente_id BIGINT,
    FOREIGN KEY (cliente_id) REFERENCES users(id)
);

CREATE TABLE pedido_produto (
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    PRIMARY KEY (pedido_id, produto_id),
    FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    FOREIGN KEY (produto_id) REFERENCES produtos(id)
);