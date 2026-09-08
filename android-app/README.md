
# INOVAGAB - Plataforma de Inovação Corporativa 🚀

Aplicativo Android nativo desenvolvido para a gestão do funil de inovação do Grupo Águia Branca. O sistema implementa uma arquitetura reativa que conecta a base operacional (motoristas, cobradores) à liderança estratégica, permitindo o registro de dores, ideação, curadoria tática e acompanhamento de ROI em tempo real.

---

## 🛠️ Stack Tecnológico e Arquitetura

Este projeto foi construído utilizando as ferramentas e padrões mais modernos do ecossistema Android nativo:

* **Linguagem:** Kotlin
* **UI Toolkit:** Jetpack Compose (UI Declarativa) com Material Design 3
* **Arquitetura:** MVVM (Model-View-ViewModel) para separação clara de responsabilidades entre interface e lógica de negócios.
* **Gerenciamento de Estado (State Management):** `StateFlow` e `MutableStateFlow` nativos do Kotlin, garantindo que a UI (View) reaja instantaneamente a qualquer mutação na camada de dados.
* **Assincronismo:** Kotlin Coroutines (`suspend functions` e `viewModelScope`) para garantir que as requisições ao repositório não bloqueiem a Main Thread (UI Thread).
* **Navegação:** Jetpack Navigation Compose para roteamento de telas e passagem de estado.
* **Autenticação:** Firebase Authentication para controle de acesso.
* **Persistência de Dados:** Firebase Realtime Database para armazenamento de dados.

## 🚀 Como Executar e Configurar o Emulador

Para testar o aplicativo sem erros de renderização ou quebras visuais no Windows, é fundamental utilizar uma imagem de sistema Android estável.

1. Abra o Android Studio e acesse o **Device Manager**.
2. Clique em **Create Virtual Device** e escolha **Pixel 7** ou **Pixel 8**.
3. **[ATENÇÃO]** Na seleção de *System Image*, **NÃO utilize a API 37 (Pre-Release/Experimental)**, pois causa bugs de renderização (`black screen`) no host.
4. Escolha uma versão estável consolidada, como a **API 35 (Android 15)** ou **API 34 (Android 14)** com suporte a *Google Play Intel x86_64*.
5. Após finalizar a criação, inicie o AVD, aguarde o boot completo e execute o projeto (Run / `Shift + F10`).

---

## 🔑 Credenciais de Acesso (Testes)

O sistema possui controle de roteamento condicional baseado na triagem de perfil do usuário. Utilize as credenciais embutidas (Mock Login) para acessar as diferentes visões do app:

* **Perfil Operador (Base):** `operador@aguiabranca.com.br`
* **Perfil Gestor (Tático):** `gestor@aguiabranca.com.br`
* **Perfil Líder (Executivo):** `lider@aguiabranca.com.br`

**Senha padrão (Validação de Input):** `000000`

---

## 📂 Estrutura do Projeto e Responsabilidades

A codebase segue a estruturação modular focada no MVVM:

### 1. Camada de Dados (`data`)
* **`model/InnovationModels.kt`:** Data classes que mapeiam as entidades do sistema: `UserRole` (Enum), `InnovationIdea` (inputs da base operacional com data de criação e homologação), `StrategicGuideline` (diretrizes macro) e `CorporateProject` (projetos aprovados com consolidação financeira).
* **`dao/`:** Classes DAO que realizam as operções no banco de dados
* **`repository/`:** Classes intermidárias entre as classes DAO e a classe cliente, separando a camada de acesso aos dados das regras de negócio
* **`FirebaseProvedir.kt`:** Objeto que provê a conexão com o banco de dados Firebase Realtime Database

### 2. Camada Lógica e de Estado (`ui/viewmodel`)
* **`InnovationViewModel.kt`:** Classe que estende `ViewModel`. Centraliza a regra de negócio (como transformar uma "Ideia Aprovada" em um "Projeto" de forma transacional). Mantém os `StateFlows` públicos que são coletados (observados) pela UI através do `collectAsState()`.

### 3. Camada de Interface Declarativa (`ui/screens`)
* **`LoginScreen.kt`:** Componente Compose de entrada. Implementa validação de formulário (senha em branco/errada) controlada por estado, além de rotas de atalho (Deep links internos) para testes ágeis.
* **`OperadorDashboardScreen.kt`:** Painel operacional. Utiliza `LazyRow` para o componente de `FilterChip` (filtragem em tempo real da lista em memória por categoria) e `LazyColumn` com pesos (`weight`) distribuídos para listagem performática.
* **`GestorDashboardScreen.kt`:** Visão tática com duas `LazyColumns` assíncronas. Integra componentes do Material 3 (`AlertDialog`) acoplados ao ViewModel para realizar mutações de estado no painel de acompanhamento financeiro (ROI, Investimento).
* **`LiderDashboardScreen.kt`:** Dashboard executivo. Aplica lógicas de agregação matemática (`sumOf`, `.average()`) diretamente sobre os fluxos reativos da ViewModel, atualizando os KPIs de "Capital Alocado" e "Retorno Capturado" de forma síncrona aos inputs do Gestor.

### 4. Camada de Roteamento (`ui/navigation`)
* **`Screen.kt`:** Classe `sealed` utilizada para definir o contrato tipado das rotas de navegação (type-safety).
* **`InovaGabNavGraph.kt`:** Define o container `NavHost` do Navigation Compose, injetando o `NavController` e orquestrando as trocas de contexto sem perder a instância da ViewModel Global.

### 5. Configuração e Bootstrap (`Root` & `ui/theme`)
* **`Theme` (`Color.kt`, `Theme.kt`, `Type.kt`):** Tokens de design padronizados baseados no Material 3, com suporte estrutural a Dynamic Color e Dark/Light Mode.
* **`MainActivity.kt`:** O *Entry Point* do ciclo de vida Android. Inicializa o contexto principal (`setContent`), aplica a `Surface` base e provê a injeção manual da `InnovationViewModel` para o container de navegação.

```
