# Quiz Conhecimentos Gerais

Aplicativo Android desenvolvido em **Kotlin** e **Jetpack Compose**, com persistência de dados local utilizando **SQLite** (`SQLiteOpenHelper`). O aplicativo permite que os usuários respondam a perguntas de conhecimentos gerais, acompanhem suas pontuações e recordes históricos, e cadastrem novas perguntas diretamente no banco de dados local.

---

## Regras do Quiz

1. **Seleção de Perguntas:** O aplicativo carrega um conjunto de perguntas do banco de dados SQLite (incluindo perguntas iniciais e perguntas cadastradas pelo usuário).
2. **Múltipla Escolha:** Cada pergunta possui três alternativas (Opções A, B e C), sendo apenas uma correta.
3. **Pontuação:** Cada resposta correta adiciona **1 ponto** à pontuação total do jogador.
4. **Tela Final e Recordes:** Ao finalizar o quiz, a pontuação obtida é salva automaticamente na tabela de recordes. O aplicativo exibe a pontuação final e o **Seu Recorde Histórico** acumulado.
5. **Reinício e Cadastro:** O usuário pode reiniciar o quiz a qualquer momento ou cadastrar novas perguntas através do botão de cadastro no cabeçalho.

---

## Estrutura do Banco de Dados SQLite (`quiz_gerais.db` - Versão 2)

O banco de dados local possui duas tabelas principais:

### 1. Tabela `perguntas`
Armazena as perguntas do quiz exibidas aos usuários.
- `id` (INTEGER, Primary Key, AutoIncrement): Identificador único da pergunta.
- `enunciado` (TEXT, Not Null): Texto da pergunta.
- `opcao_a` (TEXT, Not Null): Texto da alternativa A.
- `opcao_b` (TEXT, Not Null): Texto da alternativa B.
- `opcao_c` (TEXT, Not Null): Texto da alternativa C.
- `correta` (INTEGER, Not Null): Índice da resposta correta (`0` para A, `1` para B, `2` para C).

### 2. Tabela `recordes`
Armazena o histórico de pontuações obtidas nas partidas.
- `id` (INTEGER, Primary Key, AutoIncrement): Identificador único do registro.
- `pontos` (INTEGER, Not Null): Pontuação alcançada pelo usuário ao finalizar o quiz.

---

## Tecnologias Usadas

- **Kotlin:** Linguagem de programação principal.
- **Jetpack Compose:** Toolkit moderno de UI declarativa do Android.
- **Material 3:** Sistema de design do Google para interfaces consistentes e modernas.
- **SQLite (`SQLiteOpenHelper`):** Banco de dados relacional local para armazenamento de perguntas e recordes.
- **Android SDK & Gradle:** Ferramentas de compilação e ciclo de vida do Android.
