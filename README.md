 Sistema de Estacionamento Inteligente

Este projeto simula o controle de um estacionamento inteligente de shopping utilizando **Java com interface gráfica (Swing)**. 
O sistema gerencia vagas para **pessoas com deficiência**, **idosos** e **público geral**, além de controlar a entrada, saída e desistência de carros.

👥 Integrantes do Grupo

Luca Samuel dos Santos e Maria Eduarda Batista Henrique

🚗 Regras do Estacionamento

- O estacionamento possui um número total `X` de vagas:
  - `X'` para pessoas com deficiência
  - `X''` para idosos
  - `X'''` para o público geral
  - Total: `X = X' + X'' + X'''`

- Uma quantidade de carros tenta acessar o estacionamento.
  - Se houver vaga compatível, o carro entra.
  - Se não houver vaga, ele aguarda.
  - Se esperar muito tempo, pode **desistir**.

🖥️ Funcionalidades da Interface Gráfica

- Mostrar quantidade de **vagas disponíveis e ocupadas** por tipo.
- Exibir **carros estacionados e suas posições**.
- Indicar **carros na fila de espera**.
- Contar **carros que saíram** e **carros que desistiram**.


👩‍💻 Tecnologias

- **Java**
 **Swing (GUI)**
 **Threading e sincronização com `synchronized` / `wait` / `notifyAll`**

## 👥 Integrantes do Grupo

- Maria Henrique  
- Luca Samuel 
