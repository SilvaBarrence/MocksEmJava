package br.com.caelum.leilao;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamento;
import br.com.caelum.leilao.infra.relogio.Relogio;
import br.com.caelum.leilao.servico.Avaliador;
import br.com.caelum.leilao.servico.GeradorDePagamento;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class GeradorDePagamentoTest {

    private Usuario gabriel;
    private Usuario paulo;

    @Before
    public void CriarUsuario() {
        this.gabriel = new Usuario("Gabriel");
        this.paulo = new Usuario("Paulo");
    }

    @Test
    public void deveGerarPagamentoParaUmLeilaoEncerrado() {

        RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
        RepositorioDePagamento pagamentos = mock(RepositorioDePagamento.class);
        Avaliador avaliador = mock(Avaliador.class);

        Leilao leilao = new CriadorDeLeilao()
                .para("Playstation")
                .lance(new Usuario("José da Silva"), 2000.0)
                .lance(new Usuario("Maria Pereira"), 2500.0)
                .constroi();

        when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
        when(avaliador.getMaiorLance()).thenReturn(2500.0);

        GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, avaliador);
        gerador.gera();

        ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentos).salva(argumento.capture());
        Pagamento pagamentoGerado = argumento.getValue();
        assertEquals(2500.0, pagamentoGerado.getValor(), 0.00001);
    }

    @Test
    public void deveEmpurrarParaProximoDiaUtil() {
        RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
        RepositorioDePagamento pagamentos = mock(RepositorioDePagamento.class);
        Relogio relogio = mock(Relogio.class);

        Leilao leilao = new CriadorDeLeilao().para("Geladeira")
                .lance(paulo, 2000.0)
                .lance(gabriel, 2500.0)
                .constroi();

        when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));

        Calendar sabado = Calendar.getInstance();
        sabado.set(2020, Calendar.SEPTEMBER,19);

        when(relogio.Hoje()).thenReturn(sabado);

        GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, new Avaliador(), relogio);
        gerador.gera();

        ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentos).salva(argumento.capture());

        Pagamento pagamentoGerado = argumento.getValue();

        assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));
        assertEquals(21, pagamentoGerado.getData().get(Calendar.DAY_OF_MONTH));
    }
}
