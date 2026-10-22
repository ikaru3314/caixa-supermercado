package br.com.caixa.db;

import br.com.caixa.model.ItemVenda;
import br.com.caixa.model.Sessao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VendaDao {
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Grava a venda e seus itens numa única transação. Retorna o número da venda. */
    public long salvar(Sessao sessao) {
        try (Connection c = Database.conectar()) {
            c.setAutoCommit(false);
            try {
                long vendaId;
                String sqlVenda = "INSERT INTO venda(data_hora, cpf, forma_pagamento, total_centavos, valor_pago_centavos) VALUES (?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                    // formato "2026-10-22 07:42:00": aceito pelo SQLite (texto) e pelo MySQL (DATETIME)
                    ps.setString(1, LocalDateTime.now().format(DATA_HORA));
                    ps.setString(2, sessao.cpf());
                    ps.setString(3, sessao.formaPagamento());
                    ps.setLong(4, sessao.totalCentavos());
                    ps.setLong(5, sessao.valorPagoCentavos());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        vendaId = rs.getLong(1);
                    }
                }
                String sqlItem = "INSERT INTO item_venda(venda_id, codigo, nome, quantidade, preco_unit_centavos, total_centavos) VALUES (?,?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sqlItem)) {
                    for (ItemVenda i : sessao.itens()) {
                        ps.setLong(1, vendaId);
                        ps.setString(2, i.produto().codigo());
                        ps.setString(3, i.produto().nome());
                        ps.setDouble(4, i.quantidade());
                        ps.setLong(5, i.produto().precoCentavos());
                        ps.setLong(6, i.totalCentavos());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                // baixa no estoque, na mesma transação da venda
                String sqlEstoque = "UPDATE produto SET estoque = estoque - ? WHERE codigo = ?";
                try (PreparedStatement ps = c.prepareStatement(sqlEstoque)) {
                    for (ItemVenda i : sessao.itens()) {
                        ps.setDouble(1, i.quantidade());
                        ps.setString(2, i.produto().codigo());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
                return vendaId;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao gravar a venda: " + e.getMessage(), e);
        }
    }
}
