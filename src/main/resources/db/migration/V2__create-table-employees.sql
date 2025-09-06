CREATE TABLE employees (
    atendente_id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    usuario varchar (20)UNIQUE NOT NULL,
    senha varchar (20) NOT NULL,
    data_admissao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);