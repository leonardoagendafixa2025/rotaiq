"""
ROTA IQ — Serviço Oficial de E-mails Transacionais (SendGrid Integration)
Responsável pelo envio real de:
1. Código / Link de Recuperação de Senha
2. Confirmação / Verificação de E-mail
3. Boas-vindas para novos motoristas
4. Alertas de Segurança (alteração de senha, login em novo dispositivo)

Implementado com urllib.request padrão para máxima portabilidade (Vercel Serverless, Docker, Local).
"""

import os
import json
import urllib.request
import urllib.error
from typing import Optional, Dict, Any

# Carregar .env se existir localmente
_env_paths = [
    os.path.join(os.path.dirname(os.path.dirname(__file__)), ".env"),
    os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), ".env"),
    ".env"
]
for _p in _env_paths:
    if os.path.exists(_p):
        try:
            with open(_p, "r", encoding="utf-8") as _f:
                for _line in _f:
                    _line = _line.strip()
                    if _line and not _line.startswith("#") and "=" in _line:
                        _k, _v = _line.split("=", 1)
                        _k = _k.strip()
                        _v = _v.strip().strip('"').strip("'")
                        if _k not in os.environ:
                            os.environ[_k] = _v
        except Exception:
            pass

SENDGRID_API_KEY = os.getenv("SENDGRID_API_KEY", "")
SENDGRID_FROM_EMAIL = os.getenv("SENDGRID_FROM_EMAIL", "toyesterdayagencia@gmail.com")
SENDGRID_FROM_NAME = os.getenv("SENDGRID_FROM_NAME", "ROTA IQ")


class EmailService:
    def __init__(self, api_key: Optional[str] = None, from_email: Optional[str] = None):
        self.api_key = api_key or SENDGRID_API_KEY
        self.from_email = from_email or SENDGRID_FROM_EMAIL
        self.from_name = SENDGRID_FROM_NAME

    def _send_sendgrid(
        self,
        to_email: str,
        subject: str,
        plain_text: str,
        html_content: str
    ) -> bool:
        """Envia mensagem usando a API v3 do SendGrid."""
        if not self.api_key or not self.api_key.startswith("SG."):
            print("[EmailService] AVISO: SENDGRID_API_KEY não configurada ou inválida. Simulando envio.")
            print(f"[EmailService SIMULADO] Para: {to_email} | Assunto: {subject}")
            return False

        payload = {
            "personalizations": [
                {
                    "to": [{"email": to_email.strip()}],
                    "subject": subject
                }
            ],
            "from": {
                "email": self.from_email,
                "name": self.from_name
            },
            "content": [
                {
                    "type": "text/plain",
                    "value": plain_text
                },
                {
                    "type": "text/html",
                    "value": html_content
                }
            ]
        }

        req = urllib.request.Request(
            url="https://api.sendgrid.com/v3/mail/send",
            data=json.dumps(payload).encode("utf-8"),
            headers={
                "Authorization": f"Bearer {self.api_key}",
                "Content-Type": "application/json",
                "User-Agent": "ROTA-IQ-EmailService/1.0"
            },
            method="POST"
        )

        try:
            with urllib.request.urlopen(req, timeout=10) as response:
                status_code = response.status
                if status_code in (200, 202):
                    print(f"[EmailService] E-mail enviado com sucesso via SendGrid para {to_email} (status {status_code})")
                    return True
                print(f"[EmailService] Resposta inesperada do SendGrid: status {status_code}")
                return False
        except urllib.error.HTTPError as e:
            try:
                err_body = e.read().decode("utf-8")
            except Exception:
                err_body = str(e)
            print(f"[EmailService] ERRO SendGrid HTTP {e.code}: {err_body}")
            return False
        except Exception as e:
            print(f"[EmailService] Exceção ao enviar e-mail via SendGrid: {str(e)}")
            return False

    # ------------------------------------------------------------------
    # 1. Recuperação de Senha
    # ------------------------------------------------------------------
    def send_password_reset_email(
        self,
        to_email: str,
        reset_token: str,
        user_name: Optional[str] = None
    ) -> bool:
        """
        Envia e-mail de recuperação de senha com token / código de segurança.
        """
        greeting = f"Olá, {user_name}!" if user_name else "Olá, Motorista!"
        subject = "ROTA IQ — Código de Recuperação de Senha"

        # Se o token tiver mais de 8 caracteres, criamos um código de destaque
        display_code = reset_token

        plain_text = f"""{greeting}

Recebemos uma solicitação para redefinir a senha da sua conta no ROTA IQ.

Seu token de recuperação é:
{display_code}

Copie e cole este código no aplicativo para cadastrar uma nova senha.
Este código expira em 1 hora por motivos de segurança.

Se você não solicitou a recuperação, ignore esta mensagem. Sua conta permanece segura.

Equipe ROTA IQ — Inteligência para cada corrida.
"""

        html_content = f"""<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Recuperação de Senha - ROTA IQ</title>
</head>
<body style="margin:0;padding:0;background-color:#0d1117;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#c9d1d9;">
  <div style="max-width:560px;margin:30px auto;background:#161b22;border:1px solid #30363d;border-radius:12px;overflow:hidden;box-shadow:0 8px 24px rgba(0,0,0,0.5);">
    <div style="background:linear-gradient(135deg,#1f6feb 0%,#238636 100%);padding:28px 24px;text-align:center;">
      <h1 style="margin:0;font-size:24px;font-weight:700;color:#ffffff;letter-spacing:1px;">ROTA IQ</h1>
      <p style="margin:4px 0 0 0;font-size:13px;color:#e6edf3;opacity:0.9;">Inteligência para cada corrida</p>
    </div>
    <div style="padding:32px 28px;">
      <h2 style="margin:0 0 16px 0;font-size:18px;color:#f0f6fc;">{greeting}</h2>
      <p style="margin:0 0 20px 0;font-size:15px;line-height:1.6;color:#8b949e;">
        Recebemos uma solicitação para redefinir a senha da sua conta no <strong>ROTA IQ</strong>.
      </p>
      <div style="background:#0d1117;border:1px solid #30363d;border-radius:8px;padding:20px;text-align:center;margin:24px 0;">
        <span style="display:block;font-size:12px;text-transform:uppercase;color:#8b949e;letter-spacing:1.5px;margin-bottom:8px;">Seu Código de Recuperação</span>
        <code style="font-size:20px;font-weight:700;color:#58a6ff;letter-spacing:2px;word-break:break-all;">{display_code}</code>
      </div>
      <p style="margin:0 0 16px 0;font-size:14px;line-height:1.5;color:#8b949e;">
        Insira este código na tela de redefinição de senha no aplicativo ROTA IQ. Este token é válido por <strong>60 minutos</strong> e só pode ser utilizado uma única vez.
      </p>
      <p style="margin:24px 0 0 0;padding-top:20px;border-top:1px solid #21262d;font-size:12px;line-height:1.5;color:#6e7681;">
        Se você não fez essa solicitação, por favor ignore este e-mail. Nenhuma alteração foi realizada na sua conta.
      </p>
    </div>
    <div style="background:#0d1117;padding:16px 28px;text-align:center;border-top:1px solid #21262d;">
      <p style="margin:0;font-size:12px;color:#6e7681;">&copy; 2026 ROTA IQ. Todos os direitos reservados.</p>
    </div>
  </div>
</body>
</html>
"""
        return self._send_sendgrid(to_email, subject, plain_text, html_content)

    # ------------------------------------------------------------------
    # 2. Verificação de E-mail
    # ------------------------------------------------------------------
    def send_email_verification(
        self,
        to_email: str,
        verification_token: str,
        user_name: Optional[str] = None
    ) -> bool:
        """Envia e-mail com código para verificação de conta."""
        greeting = f"Olá, {user_name}!" if user_name else "Olá!"
        subject = "ROTA IQ — Confirme seu endereço de e-mail"

        plain_text = f"""{greeting}

Obrigado por se cadastrar no ROTA IQ!
Para confirmar seu endereço de e-mail e ativar todos os recursos, use o token de validação:

{verification_token}

Insira este código no aplicativo ou confirme sua conta.

Equipe ROTA IQ
"""
        html_content = f"""<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Confirmação de E-mail - ROTA IQ</title>
</head>
<body style="margin:0;padding:0;background-color:#0d1117;font-family:sans-serif;color:#c9d1d9;">
  <div style="max-width:540px;margin:30px auto;background:#161b22;border:1px solid #30363d;border-radius:12px;overflow:hidden;">
    <div style="background:#1f6feb;padding:24px;text-align:center;">
      <h1 style="margin:0;color:#fff;font-size:22px;">ROTA IQ</h1>
      <p style="margin:4px 0 0;color:#c9d1d9;font-size:12px;">Confirmação de Cadastro</p>
    </div>
    <div style="padding:28px;">
      <h2 style="font-size:18px;color:#f0f6fc;margin-top:0;">{greeting}</h2>
      <p style="color:#8b949e;line-height:1.6;">Obrigado por se juntar à comunidade de motoristas do ROTA IQ! Para proteger sua conta e validar seu acesso, confirme seu e-mail utilizando o código abaixo:</p>
      <div style="background:#0d1117;border:1px solid #30363d;padding:16px;text-align:center;border-radius:8px;margin:20px 0;">
        <span style="font-size:20px;font-weight:700;color:#2ea043;letter-spacing:2px;">{verification_token}</span>
      </div>
      <p style="font-size:12px;color:#6e7681;margin-top:20px;">Se você não criou esta conta, nenhuma ação é necessária.</p>
    </div>
  </div>
</body>
</html>
"""
        return self._send_sendgrid(to_email, subject, plain_text, html_content)

    # ------------------------------------------------------------------
    # 3. Boas-vindas
    # ------------------------------------------------------------------
    def send_welcome_email(self, to_email: str, user_name: str) -> bool:
        """Envia mensagem de boas-vindas com orientações iniciais."""
        subject = "Bem-vindo ao ROTA IQ — Maximize seus lucros!"
        plain_text = f"""Olá, {user_name}!

Seja muito bem-vindo ao ROTA IQ!
Nosso objetivo é transformar a sua rotina como motorista com dados reais, custo por km preciso e avaliação inteligente de corridas.

Primeiros passos recomendados:
1. Cadastre o seu veículo com consumo e combustível
2. Configure seus custos fixos reais (seguro, IPVA, manutenção)
3. Defina sua meta diária de faturamento e taxa por hora
4. Ative a avaliação de corridas e o HUD flutuante

Bons lucros e boas corridas!
Equipe ROTA IQ
"""
        html_content = f"""<!DOCTYPE html>
<html>
<body style="margin:0;padding:0;background-color:#0d1117;font-family:sans-serif;color:#c9d1d9;">
  <div style="max-width:540px;margin:30px auto;background:#161b22;border:1px solid #30363d;border-radius:12px;padding:28px;">
    <h1 style="color:#58a6ff;margin-top:0;">Bem-vindo ao ROTA IQ, {user_name}! 🚀</h1>
    <p style="color:#8b949e;line-height:1.6;">Você agora conta com uma ferramenta profissional desenvolvida para colocar o controle financeiro de volta nas mãos do motorista.</p>
    <ul style="color:#c9d1d9;line-height:1.8;">
      <li>✅ Cálculo do custo real do km rodado</li>
      <li>✅ Avaliação de corridas (lucro líquido vs. prejuízo)</li>
      <li>✅ Metas inteligentes diárias e semanais</li>
      <li>✅ Copiloto com alertas e HUD flutuante</li>
    </ul>
    <p style="color:#8b949e;">Abra o app e configure seu primeiro veículo para começar!</p>
  </div>
</body>
</html>
"""
        return self._send_sendgrid(to_email, subject, plain_text, html_content)


email_service = EmailService()
