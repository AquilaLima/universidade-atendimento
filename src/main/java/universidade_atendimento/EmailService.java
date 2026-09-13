package universidade_atendimento;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${app.email.remetente}")
    private String remetente;

    @Value("${app.email.remetente.nome:Universidade}")
    private String nomeRemetente;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    private final HttpClient httpClient;

    public EmailService() {

        this.httpClient =
                HttpClient.newBuilder()
                        .build();
    }

    public void enviarCodigoAcesso(
            String destinatario,
            String codigo) {

        try {

            String json =
                    """
                    {
                      "sender": {
                        "name": "%s",
                        "email": "%s"
                      },
                      "to": [
                        {
                          "email": "%s"
                        }
                      ],
                      "subject": "Código de acesso - Universidade",
                      "htmlContent": "<html><body style='font-family:Arial,sans-serif;'><h2>Código de acesso</h2><p>Seu código temporário de acesso é:</p><h1 style='letter-spacing:6px;'>%s</h1><p>Este código é válido por 10 minutos.</p><p>Se você não solicitou este acesso, ignore esta mensagem.</p></body></html>"
                    }
                    """
                    .formatted(
                            escaparJson(nomeRemetente),
                            escaparJson(remetente),
                            escaparJson(destinatario),
                            escaparJson(codigo)
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            "https://api.brevo.com/v3/smtp/email"
                                    )
                            )
                            .header(
                                    "accept",
                                    "application/json"
                            )
                            .header(
                                    "content-type",
                                    "application/json"
                            )
                            .header(
                                    "api-key",
                                    brevoApiKey
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            json
                                    )
                            )
                            .build();

            long inicio =
                    System.currentTimeMillis();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            long fim =
                    System.currentTimeMillis();

            System.out.println(
                    "[BREVO API] Tempo de envio: "
                    + (fim - inicio)
                    + " ms"
            );

            System.out.println(
                    "[BREVO API] Status HTTP: "
                    + response.statusCode()
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                System.out.println(
                        "[BREVO API] Resposta de erro: "
                        + response.body()
                );

                throw new RuntimeException(
                        "Erro ao enviar e-mail pelo Brevo. HTTP "
                        + response.statusCode()
                );
            }

            System.out.println(
                    "[BREVO API] E-mail enviado com sucesso."
            );

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();

            throw new RuntimeException(
                    "Envio do e-mail foi interrompido.",
                    e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Erro ao enviar código de acesso pelo Brevo.",
                    e
            );
        }
    }

    private String escaparJson(
            String valor) {

        if (valor == null) {
            return "";
        }

        return valor
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}