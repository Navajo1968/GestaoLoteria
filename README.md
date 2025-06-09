# Gestão de Loterias

Este projeto é um sistema desktop Java para cadastro e gestão de loterias, incluindo suas faixas de premiação. O sistema utiliza JavaFX para interface gráfica, Java 17+, Maven para build e organização em pacotes seguindo boas práticas.

## Funcionalidades Implementadas

- **Menu Principal Moderno**  
  O sistema possui uma tela principal com botões grandes e modernos: “Cadastros”, “Processos” e “Sair”.

- **Cadastro de Loterias Completo**
  - Tabela com as loterias já cadastradas, exibindo nome e descrição.
  - Botões de ação por linha: **Editar** e **Excluir**.
  - Botão global: **Novo** para cadastrar uma nova loteria.
  - Botão **Voltar** para retornar ao menu principal.
  - Ao clicar em “Novo” ou “Editar”, abre a tela de formulário para cadastro/edição da loteria e suas faixas de premiação.
  - Permite adicionar/remover múltiplas faixas de premiação para cada loteria.
  - Validação dos campos do formulário.

- **Fluxo de navegação simples**
  - Toda tela tem botão de retorno ao menu ou à lista principal da funcionalidade.

## Estrutura do Projeto

- `src/main/java/com/gestaoloteria/loteria/Main.java`  
  Ponto de entrada da aplicação, gerencia a navegação entre as telas.

- `src/main/java/com/gestaoloteria/loteria/MainMenu.java`  
  Tela principal moderna com botões de navegação.

- `src/main/java/com/gestaoloteria/loteria/LoteriaListaView.java`  
  Tela de listagem das loterias, com botões de ação, novo cadastro e voltar.

- `src/main/java/com/gestaoloteria/loteria/LoteriaCadastroView.java`  
  Tela de cadastro/edição de loterias, incluindo gestão das faixas de premiação.

- `src/main/java/com/gestaoloteria/loteria/dao/LoteriaDAO.java`  
  Classe de acesso a dados, com todos os métodos CRUD para loterias e faixas.

- `src/main/java/com/gestaoloteria/loteria/model/Loteria.java`  
  Modelo da entidade Loteria.

- `src/main/java/com/gestaoloteria/loteria/model/FaixaPremiacao.java`  
  Modelo da entidade FaixaPremiacao.

- `src/main/java/com/gestaoloteria/loteria/ConexaoBanco.java`  
  Classe utilitária para conexão com o banco de dados.

## Fluxo de Conversa e Decisões

- **Começamos com uma tela de cadastro que misturava formulário e tabela.**
- Evoluímos para separar a listagem (tabela de loterias) do formulário de edição/cadastro, melhorando a experiência do usuário.
- Ajustamos o menu principal, removendo o MenuButton e usando botões modernos alinhados.
- Foram feitas validações para garantir que cada classe pública está em seu próprio arquivo, conforme boas práticas Java.
- Sempre que solicitado, os arquivos completos foram fornecidos para garantir fácil integração e manutenção.
- O sistema foi desenhado para ser expansível, podendo receber outros cadastros e módulos facilmente.

## Como rodar

1. Certifique-se de ter Java 17+ e Maven instalados.
2. Configure o banco de dados e o arquivo `db.properties` em `src/main/resources`.
3. Compile e rode pelo Maven:
   ```sh
   mvn clean package
   mvn javafx:run
   ```
   Ou pelo Eclipse: clique com o direito em `Main.java` → Run As → Java Application.

## Observações

- Para evoluções futuras (outros cadastros, relatórios, integrações etc.), siga o padrão de separar listagem e formulário para cada entidade.
- O sistema já está pronto para produção em ambiente desktop Java.

---
Este README foi gerado automaticamente para resumir toda a conversa e decisões deste projeto.# GestaoLoteria

Este repositório está pronto para o desenvolvimento de um novo sistema de gestão de loterias.

## Como começar

1. Clone o repositório:
   ```sh
   git clone https://github.com/Navajo1968/GestaoLoteria.git
   cd GestaoLoteria
   ```

2. Crie a estrutura de pastas e arquivos conforme a necessidade do novo projeto.

3. Siga as boas práticas de versionamento e documentação.

## Sugestões iniciais

- Defina a stack de tecnologia e registre neste README.
- Utilize uma branch `develop` para desenvolvimento contínuo.
- Documente comandos úteis, dependências e instruções de build.

## Licença

Coloque informações de licença do novo projeto aqui, se necessário.