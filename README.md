# Configurando Projeto

## Etapa 1: Iniciando as configurações

1. **Crie uma conta Google** (se ainda não tiver):  
   [https://accounts.google.com/signup](https://accounts.google.com/signup)   

2. **Faça o cadastro de pagamento** no Google Cloud:  
   [https://console.cloud.google.com/freetrial/signup](https://console.cloud.google.com/freetrial/signup)
   ![imagem](assets/01.png)

3. **Crie um novo projeto** no Google Cloud com o nome `Photo Plant`:  
   [https://console.cloud.google.com](https://console.cloud.google.com)
   ![imagem](assets/02.png)
   ![imagem](assets/03.png)
   ![imagem](assets/04.png)
---

## Etapa 2: Habilitando a API da Vertex AI

1. Acesse o link abaixo para ativar a API:  
   [Habilitar Vertex AI](https://console.cloud.google.com/flows/enableapi?apiid=aiplatform.googleapis.com&hl=pt-br)
   ![imagem](assets/05.png)
---

## Etapa 3: Criando credencial de serviço

1. Vá até:  
   [Contas de serviço](https://console.cloud.google.com/iam-admin/serviceaccounts)

2. Selecione o seu projeto criado.
   ![imagem](assets/06.png)

3. Clique em **"Criar conta de serviço"**.
   ![imagem](assets/07.png)

4. Na **Etapa 1 – Detalhes da conta de serviço**:
   - Nome: `vertex-ai-service`
   ![imagem](assets/08.png)

5. Na **Etapa 2 – Acessos ao projeto**:
   - Adicione os papéis:
     - `Usuário da Vertex AI`
     - `Criador do token da conta de serviço`   

6. Ignore a Etapa 3 e clique em **Concluir**.
   ![imagem](assets/09.png)

7. Após criada, clique no e-mail da conta de serviço criada.
   ![imagem](assets/010.png)

8. Vá até a aba **"Chaves"** → clique em **"Adicionar chave"** → **"Criar nova chave"**.
   ![imagem](assets/011.png)
   ![imagem](assets/012.png)

9. Escolha o tipo **JSON** e salve o arquivo.
   ![imagem](assets/013.png)

---

## Etapa 4: Adicionando credenciais ao projeto Android

1. Acesse:  
   [https://console.cloud.google.com](https://console.cloud.google.com)
   

2. Clique no projeto **"Photo Plant"**.
   ![imagem](assets/014.png)

3. Copie o **ID do projeto**.
   ![imagem](assets/015.png)

4. No Android Studio:
   - Abra o arquivo `GeminiApiService.java`
   - Substitua o valor da variável `ID_DO_PROJETO` pelo ID copiado
   ![imagem](assets/016.png)

5. Renomeie o arquivo **JSON** que você baixou para `service_account.json` e mova ele para `app\src\main\res\raw`:
   ![imagem](assets/017.png)
