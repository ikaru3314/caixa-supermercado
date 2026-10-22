package br.com.caixa.db;

import br.com.caixa.model.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso à tabela "produto": consultar, adicionar, alterar e excluir. */
public class ProdutoDao {
    private static final String COLUNAS = "codigo, nome, preco_centavos, por_kg, categoria, estoque";

    // ---------------------------------------------------------------- consultas

    public Optional<Produto> buscarPorCodigo(String codigo) {
        String sql = "SELECT " + COLUNAS + " FROM produto WHERE codigo = ?";
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao buscar produto: " + e.getMessage(), e);
        }
    }

    public boolean existe(String codigo) {
        return buscarPorCodigo(codigo).isPresent();
    }

    public List<Produto> listarPorCategoria(String categoria) {
        String sql = "SELECT " + COLUNAS + " FROM produto WHERE categoria = ? ORDER BY nome";
        List<Produto> lista = new ArrayList<>();
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, categoria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar produtos: " + e.getMessage(), e);
        }
        return lista;
    }

    /** Todos os produtos; com {@code filtro} busca no nome ou no código. */
    public List<Produto> listarTodos(String filtro) {
        String f = filtro == null ? "" : filtro.trim();
        String sql = "SELECT " + COLUNAS + " FROM produto WHERE nome LIKE ? OR codigo LIKE ? ORDER BY nome";
        List<Produto> lista = new ArrayList<>();
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + f + "%");
            ps.setString(2, "%" + f + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar produtos: " + e.getMessage(), e);
        }
        return lista;
    }

    // ---------------------------------------------------------------- alterações

    public void inserir(Produto p) {
        String sql = "INSERT INTO produto(codigo, nome, preco_centavos, por_kg, categoria, estoque) VALUES (?,?,?,?,?,?)";
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.codigo());
            ps.setString(2, p.nome());
            ps.setLong(3, p.precoCentavos());
            ps.setInt(4, p.porKg() ? 1 : 0);
            ps.setString(5, p.categoria());
            ps.setDouble(6, p.estoque());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao adicionar produto: " + e.getMessage(), e);
        }
    }

    /** Altera o produto de mesmo código (o código em si não muda). */
    public void atualizar(Produto p) {
        String sql = "UPDATE produto SET nome = ?, preco_centavos = ?, por_kg = ?, categoria = ?, estoque = ? WHERE codigo = ?";
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.nome());
            ps.setLong(2, p.precoCentavos());
            ps.setInt(3, p.porKg() ? 1 : 0);
            ps.setString(4, p.categoria());
            ps.setDouble(5, p.estoque());
            ps.setString(6, p.codigo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao alterar produto: " + e.getMessage(), e);
        }
    }

    /** As vendas antigas guardam uma cópia do nome/preço, então excluir não quebra o histórico. */
    public void excluir(String codigo) {
        String sql = "DELETE FROM produto WHERE codigo = ?";
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao excluir produto: " + e.getMessage(), e);
        }
    }

    private Produto mapear(ResultSet rs) throws SQLException {
        return new Produto(
                rs.getString("codigo"),
                rs.getString("nome"),
                rs.getLong("preco_centavos"),
                rs.getInt("por_kg") == 1,
                rs.getString("categoria"),
                rs.getDouble("estoque"));
    }
}
