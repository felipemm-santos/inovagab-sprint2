# Aplicativo Android INOVAGAB

O aplicativo da Sprint 2 usa Kotlin, Jetpack Compose, Material Design 3, MVVM, StateFlow, Coroutines e Navigation Compose. Os fluxos de autenticação, diretrizes, ideias, priorização, projetos, indicadores e sugestão da IA usam o backend Spring Boot. Firebase não é necessário.

## Executar

1. Inicie MongoDB e backend conforme o [README principal](../README.md).
2. Abra esta pasta no Android Studio com Android SDK instalado e execute o módulo `app` em um emulador.
3. O cliente REST em `app/src/main/java/br/com/fiap/inovagab/data/api/ApiClient.kt` usa `http://10.0.2.2:8080/api/v1` para o emulador local. Ajuste esse endereço para dispositivo físico ou outro ambiente; use HTTPS fora do desenvolvimento local.
4. Entre com uma conta acadêmica do backend. Os atalhos do login servem para testes visuais das telas e não concedem acesso a dados protegidos.

Para compilar um APK de depuração:

```powershell
.\gradlew.bat assembleDebug
```

## Estrutura

- `data/repository`: operações consumidas pelo ViewModel, incluindo autenticação.
- `data/api`: chamadas REST, sessão JWT em memória e mapeamento dos contratos da API.
- `data/model`: modelos usados nas telas.
- `ui/viewmodel/InnovationViewModel.kt`: estado e operações assíncronas por fluxo.
- `ui/screens`: telas Compose dos três perfis e login.
- `ui/navigation`: navegação dos perfis e logout.

O JWT é descartado no logout e não é persistido. Ao reiniciar o app, faça login novamente. A sugestão da IA é opcional e aparece separada da avaliação manual do Gestor. Quando a chave Gemini estiver ausente no backend, os demais fluxos continuam utilizáveis.
