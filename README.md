# IF Eventos 

**Java 21 · Spring Boot 4.1.1 · Gradle · JPA · H2/MySQL · HTML/CSS/JavaScript**

## Objetivo

Construir um sistema de eventos em que pessoas podem cadastrar usuários, cadastrar eventos e vincular usuários a eventos por meio de inscrições.

### Entidades

- `Usuario`: `id`, `nome`, `email`, `idade`.
- `Evento`: `id`, `titulo`, `descricao`, `data`, `local`, `capacidade`.
- `Inscricao`: `id`, `usuario`, `evento`, `dataInscricao`. Liga uma pessoa a um evento.

Relacionamento: **Usuario (1) ← (N) Inscricao (N) → (1) Evento**.

Uma inscrição não exige autenticação. Um usuário pode participar de vários eventos e um evento pode ter diversos usuários.

### Regras de negócio

1. O mesmo usuário não pode se inscrever duas vezes no mesmo evento.
2. Um evento não pode ter mais inscritos que sua capacidade.
3. A capacidade não pode ser reduzida para menos que o número de inscritos.
4. Usuários ou eventos com inscrições não podem ser excluídos antes do cancelamento dessas inscrições.
5. Quando uma regra impede uma operação, a API devolve HTTP `409 Conflict` com uma mensagem curta.

## Como abrir no IntelliJ IDEA (Windows)

1. Instale o [JDK 21](https://www.azul.com/downloads/?version=java-21-lts&package=jdk) e o [IntelliJ IDEA](https://www.jetbrains.com/idea/download/).
2. Extraia este ZIP e abra o IntelliJ. Escolha **Open** e selecione a pasta que contém `build.gradle`.
3. Se solicitado, confirme **Trust Project** se você confia na origem dos arquivos.
4. Configure `File > Project Structure > Project SDK` para **JDK 21**.
5. Em `Settings > Build, Execution, Deployment > Build Tools > Gradle`, selecione **Gradle JVM = JDK 21**.
6. Aguarde a sincronização das dependências. É necessário ter internet na primeira execução.
7. Abra `src/main/java/com/ifsul/backend/aula/AulaApplication.java` e execute pelo botão verde ao lado do `main`.

O perfil padrão é **H2**, portanto não precisa instalar banco para iniciar.

## Endereços

- Front-end: <http://localhost:8080/>
- Usuários: <http://localhost:8080/usuarios>
- Eventos: <http://localhost:8080/eventos>
- H2 Console: <http://localhost:8080/h2-console>

Configuração do H2: JDBC URL `jdbc:h2:mem:backend_aula;DB_CLOSE_DELAY=-1`, usuário `sa`, senha vazia.

Ao parar a aplicação, o banco H2 em memória perde os registros.

## Endpoints REST

| Verbo | Rota | Função |
|---|---|---|
| GET | `/usuarios` | Listar usuários |
| GET | `/usuarios/{id}` | Consultar usuário |
| POST | `/usuarios` | Cadastrar usuário |
| PUT | `/usuarios/{id}` | Atualizar usuário |
| DELETE | `/usuarios/{id}` | Excluir usuário sem inscrições |
| GET | `/eventos` | Listar eventos, inscritos e vagas |
| GET | `/eventos/{id}` | Consultar evento |
| POST | `/eventos` | Cadastrar evento |
| PUT | `/eventos/{id}` | Atualizar evento |
| DELETE | `/eventos/{id}` | Excluir evento sem inscrições |
| GET | `/inscricoes/evento/{eventoId}` | Consultar participantes de um evento |
| POST | `/inscricoes` | Inscrever usuário em evento |
| DELETE | `/inscricoes/{id}` | Cancelar uma inscrição |

### JSON para criar evento

```json
{
  "titulo": "Oficina de Spring Boot",
  "descricao": "Introdução às APIs REST",
  "data": "2026-11-25",
  "local": "Laboratório 1",
  "capacidade": 20
}
```

### JSON para inscrever uma pessoa

```json
{
  "usuarioId": 1,
  "eventoId": 1
}
```


## MySQL (opcional)

O arquivo `application-mysql.yaml` foi preservado do projeto-base. Crie o banco pelo SQL em `database/criar_banco_mysql.sql` e execute com o perfil `mysql`:

```powershell
$env:DB_PASSWORD="SUA_SENHA_LOCAL"
.\gradlew.bat bootRun --args="--spring.profiles.active=mysql"
```

Nunca publique senhas reais no GitHub.

## Material complementar

### Collection Postman

Importe `postman/IF_Eventos.postman_collection.json` pelo menu **Import** do Postman. 