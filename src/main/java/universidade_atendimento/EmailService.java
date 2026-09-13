package universidade_atendimento;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.remetente}")
    private String remetente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigoAcesso(String destinatario, String codigo) {

        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);

        mensagem.setSubject(
                "Código de acesso - Universidade"
        );

        mensagem.setText(
                "Seu código temporário de acesso é: "
                        + codigo
                        + "\n\n"
                        + "Este código é válido por 10 minutos."
                        + "\n\n"
                        + "Se você não solicitou este acesso, "
                        + "ignore esta mensagem."
        );

        mailSender.send(mensagem);
    }
}