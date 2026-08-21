package universidade_atendimento;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

@Controller
public class FAQController {

    @GetMapping("/faq")
    public String faq(
            Model model,
            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        // Verifica se o usuário está logado
        if (usuario == null) {
            return "redirect:/login";
        }

        // A FAQ será destinada aos alunos
        if (!"ALUNO".equals(usuario.getPerfil())) {
            return "redirect:/";
        }

        List<PerguntaFAQ> perguntas = new ArrayList<>();

        perguntas.add(new PerguntaFAQ(
                "Financeiro",
                "Como emitir a segunda via do boleto?",
                "Acesse o portal financeiro da instituição, entre com seus dados e procure pela opção Boletos/Financeiro. Caso o boleto não esteja disponível, abra um atendimento para o setor financeiro."
        ));

        perguntas.add(new PerguntaFAQ(
                "Financeiro",
                "Como consultar minhas mensalidades?",
                "Acesse o portal do aluno e consulte a área Financeiro para visualizar mensalidades, vencimentos e pagamentos."
        ));

        perguntas.add(new PerguntaFAQ(
                "Financeiro",
                "Como faço para negociar uma mensalidade em atraso?",
                "Entre em contato com o setor financeiro da instituição para verificar as opções de negociação disponíveis."
        ));

        perguntas.add(new PerguntaFAQ(
                "Acadêmico",
                "Como solicitar uma declaração de matrícula?",
                "A declaração pode ser solicitada através do portal do aluno, na área de documentos acadêmicos."
        ));

        perguntas.add(new PerguntaFAQ(
                "Acadêmico",
                "Como solicitar documentos acadêmicos?",
                "Acesse o portal do aluno e procure pela área de Documentos ou Secretaria. Caso o documento desejado não esteja disponível, abra um atendimento."
        ));

        perguntas.add(new PerguntaFAQ(
                "Acadêmico",
                "Como faço minha rematrícula?",
                "Acesse o período de rematrícula informado pela instituição e siga as instruções disponíveis no portal acadêmico."
        ));

        perguntas.add(new PerguntaFAQ(
                "Acadêmico",
                "Como faço para trancar a matrícula?",
                "A solicitação de trancamento deve ser realizada conforme as regras e prazos acadêmicos da instituição. Entre em contato com a secretaria para verificar os procedimentos."
        ));

        perguntas.add(new PerguntaFAQ(
                "Acadêmico",
                "Como faço para cancelar minha matrícula?",
                "O cancelamento deve ser solicitado junto à instituição, seguindo os procedimentos definidos pela secretaria acadêmica."
        ));

        perguntas.add(new PerguntaFAQ(
                "Acadêmico",
                "Como solicitar transferência de curso?",
                "Procure a secretaria acadêmica para verificar os cursos disponíveis, requisitos e procedimentos para transferência."
        ));

        perguntas.add(new PerguntaFAQ(
                "Acadêmico",
                "Como consultar minhas notas?",
                "Acesse o portal acadêmico e entre na área de Notas ou Desempenho Acadêmico."
        ));

        perguntas.add(new PerguntaFAQ(
                "Acadêmico",
                "Onde consulto o calendário acadêmico?",
                "O calendário acadêmico pode ser consultado no portal ou nos canais oficiais da instituição."
        ));

        perguntas.add(new PerguntaFAQ(
                "Acadêmico",
                "Como consultar o horário das aulas?",
                "Acesse o portal do aluno e consulte a área de Horários ou Grade Horária."
        ));

        perguntas.add(new PerguntaFAQ(
                "Acadêmico",
                "Como saber a sala onde terei aula?",
                "Consulte sua grade horária no portal acadêmico. A sala geralmente aparece junto às informações da disciplina."
        ));

        perguntas.add(new PerguntaFAQ(
                "Acadêmico",
                "Como faço para entrar em contato com um professor?",
                "Utilize os canais acadêmicos disponibilizados pela instituição ou procure a coordenação do curso."
        ));

        perguntas.add(new PerguntaFAQ(
                "Acadêmico",
                "Como solicitar aproveitamento de disciplinas?",
                "A solicitação deve ser feita junto à secretaria ou coordenação do curso, normalmente mediante apresentação da documentação acadêmica necessária."
        ));

        perguntas.add(new PerguntaFAQ(
                "Acadêmico",
                "Como funciona a segunda chamada de uma prova?",
                "Verifique as regras acadêmicas da instituição e os prazos para solicitação de segunda chamada."
        ));

        perguntas.add(new PerguntaFAQ(
                "Estágio",
                "Como solicitar documentos para estágio?",
                "Entre em contato com o setor responsável por estágios ou consulte a área de estágio no portal acadêmico."
        ));

        perguntas.add(new PerguntaFAQ(
                "Tecnologia",
                "Como acessar as plataformas de estudo?",
                "Utilize seu login institucional para acessar o ambiente virtual de aprendizagem indicado pela faculdade."
        ));

        perguntas.add(new PerguntaFAQ(
                "Acesso",
                "Esqueci minha senha. Como recuperar?",
                "Na tela de login do portal, utilize a opção Esqueci minha senha e siga as instruções para recuperação."
        ));

        perguntas.add(new PerguntaFAQ(
                "Atendimento",
                "Minha dúvida não está na FAQ. O que faço?",
                "Não encontrou o que precisava? Abra um atendimento para que a equipe da universidade possa analisar sua solicitação."
        ));

        model.addAttribute("perguntas", perguntas);

        return "faq";
    }

    // Classe interna para representar uma pergunta da FAQ
    public static class PerguntaFAQ {

        private String categoria;
        private String pergunta;
        private String resposta;

        public PerguntaFAQ(
                String categoria,
                String pergunta,
                String resposta) {

            this.categoria = categoria;
            this.pergunta = pergunta;
            this.resposta = resposta;
        }

        public String getCategoria() {
            return categoria;
        }

        public String getPergunta() {
            return pergunta;
        }

        public String getResposta() {
            return resposta;
        }
    }
}