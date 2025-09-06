CREATE TABLE clientes (
    cliente_id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    telefone VARCHAR(20),
    endereco VARCHAR(255),
    cep int(7),
    rg int(8),
    cpf int(10),
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);