-- =====================================================================
--  Banco de dados do Caixa de Supermercado  -  MySQL 5.7 / 8.x
--
--  Como usar (MySQL Workbench):  File > Open SQL Script > escolha este
--  arquivo > clique no raio (Execute).
--  Pelo terminal:  mysql -u root -p < supermercado_mysql.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS supermercado
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE supermercado;

-- ---------------------------------------------------------------------
--  PRODUTO: cadastro de produtos e estoque
--  preco_centavos: 549 = R$ 5,49   |   estoque: unidades (ou kg se por_kg = 1)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS produto (
    codigo          VARCHAR(20)   NOT NULL,              -- código de barras
    nome            VARCHAR(100)  NOT NULL,
    preco_centavos  BIGINT        NOT NULL,
    por_kg          TINYINT(1)    NOT NULL DEFAULT 0,    -- 1 = vendido por quilo
    categoria       VARCHAR(30)   NOT NULL DEFAULT 'MERCEARIA',  -- FLV = frutas, legumes e verduras
    estoque         DECIMAL(12,3) NOT NULL DEFAULT 0,
    PRIMARY KEY (codigo),
    INDEX idx_produto_nome (nome),
    INDEX idx_produto_categoria (categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
--  VENDA: uma linha por compra finalizada
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS venda (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    data_hora            DATETIME     NOT NULL,
    cpf                  VARCHAR(11)  NULL,               -- só dígitos; NULL = sem CPF na nota
    forma_pagamento      VARCHAR(30)  NOT NULL,           -- Dinheiro, Cartão de débito, ...
    total_centavos       BIGINT       NOT NULL,
    valor_pago_centavos  BIGINT       NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_venda_data (data_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
--  ITEM_VENDA: itens de cada venda (guarda cópia do nome e do preço da
--  época, então excluir ou alterar um produto não muda o histórico)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS item_venda (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    venda_id             BIGINT        NOT NULL,
    codigo               VARCHAR(20)   NOT NULL,
    nome                 VARCHAR(100)  NOT NULL,
    quantidade           DECIMAL(12,3) NOT NULL,
    preco_unit_centavos  BIGINT        NOT NULL,
    total_centavos       BIGINT        NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_item_venda (venda_id),
    CONSTRAINT fk_item_venda FOREIGN KEY (venda_id) REFERENCES venda (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
--  PRODUTOS DE EXEMPLO (códigos de barras fictícios)
--  INSERT IGNORE: pode rodar o script de novo sem duplicar
-- ---------------------------------------------------------------------
INSERT IGNORE INTO produto (codigo, nome, preco_centavos, por_kg, categoria, estoque) VALUES
    ('7891000100103', 'Leite Integral 1L',        549, 0, 'MERCEARIA', 100),
    ('7891910000197', 'Açúcar Refinado 1kg',      499, 0, 'MERCEARIA', 100),
    ('7896005800027', 'Arroz Branco 5kg',        2690, 0, 'MERCEARIA', 100),
    ('7896045104000', 'Feijão Carioca 1kg',       879, 0, 'MERCEARIA', 100),
    ('7891000053508', 'Chocolate em Barra 90g',   650, 0, 'MERCEARIA', 100),
    ('7894900011517', 'Refrigerante Cola 2L',     999, 0, 'MERCEARIA', 100),
    ('7891149103102', 'Biscoito Recheado 130g',   325, 0, 'MERCEARIA', 100),
    ('7896036090558', 'Café Torrado 500g',       1890, 0, 'MERCEARIA', 100),
    ('7891150023501', 'Sabão em Pó 1kg',         1450, 0, 'MERCEARIA', 100),
    ('7898080640437', 'Pão de Forma 500g',        799, 0, 'MERCEARIA', 100),
    ('2000001',       'Banana Prata',             699, 1, 'FLV',        50),
    ('2000002',       'Maçã Fuji',                990, 1, 'FLV',        50),
    ('2000003',       'Tomate',                   749, 1, 'FLV',        50),
    ('2000004',       'Cebola',                   499, 1, 'FLV',        50),
    ('2000005',       'Batata',                   599, 1, 'FLV',        50),
    ('2000006',       'Cenoura',                  449, 1, 'FLV',        50),
    ('2000007',       'Laranja',                  399, 1, 'FLV',        50),
    ('2000008',       'Mamão Papaia',             890, 1, 'FLV',        50),
    ('2000009',       'Limão',                    550, 1, 'FLV',        50),
    ('2000010',       'Alface (unidade)',         350, 0, 'FLV',       100);

-- =====================================================================
--  CONSULTAS ÚTEIS (rode uma de cada vez, selecionando o trecho)
-- =====================================================================

-- Produtos com estoque baixo (menos de 10)
-- SELECT codigo, nome, estoque FROM produto WHERE estoque < 10 ORDER BY estoque;

-- Vendas de hoje
-- SELECT id, data_hora, forma_pagamento, total_centavos / 100 AS total_reais
--   FROM venda WHERE DATE(data_hora) = CURDATE() ORDER BY data_hora DESC;

-- Faturamento por dia
-- SELECT DATE(data_hora) AS dia, COUNT(*) AS vendas, SUM(total_centavos) / 100 AS total_reais
--   FROM venda GROUP BY DATE(data_hora) ORDER BY dia DESC;

-- Produtos mais vendidos
-- SELECT codigo, nome, SUM(quantidade) AS qtd_vendida, SUM(total_centavos) / 100 AS total_reais
--   FROM item_venda GROUP BY codigo, nome ORDER BY qtd_vendida DESC LIMIT 10;

-- Forma de pagamento mais usada
-- SELECT forma_pagamento, COUNT(*) AS vendas FROM venda GROUP BY forma_pagamento ORDER BY vendas DESC;

-- =====================================================================
--  (OPCIONAL) Usuário só para o programa, em vez do root
-- =====================================================================
-- CREATE USER 'caixa'@'localhost' IDENTIFIED BY 'troque_esta_senha';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON supermercado.* TO 'caixa'@'localhost';
-- FLUSH PRIVILEGES;
