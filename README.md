# Fractal de Mandelbrot

Uma implementação em Java do famoso fractal de Mandelbrot com interface gráfica interativa e zoom centrado no cursor.

## O que é

O **Conjunto de Mandelbrot** é um fractal matemático definido pela iteração da função complexa `z² + c`, onde `z` começa em 0 e `c` é o ponto complexo sendo testado. Pontos que não divergem após um número máximo de iterações pertencem ao conjunto (aparecem em preto), enquanto pontos que divergem são coloridos baseado na velocidade de escape.

## Como funciona

- **Algoritmo**: Implementa a fórmula `z(n+1) = z(n)² + c` para números complexos
- **Visualização**: Cada pixel da tela representa um ponto no plano complexo
- **Cores**: Gradiente colorido baseado no número de iterações até a divergência
- **Zoom**: Sistema de zoom centrado no cursor mantém o ponto sob o mouse fixo durante o zoom
- **Otimização**: Cache de imagem e paleta de cores pré-calculada para melhor performance

## Controles

- **Scroll do mouse**: Zoom in/out centrado no cursor
- **Clique do mouse**: Centraliza o fractal no ponto clicado
- **Setas do teclado**: Navegar pelo fractal
- **+/-**: Zoom in/out
- **R**: Reset para visualização inicial

## Como rodar

### Pré-requisitos
- Java 8 ou superior instalado

### Executando
1. Compile o arquivo:
   ```bash
   javac FractalMandelbrot.java
   ```

2. Execute o programa:
   ```bash
   java FractalMandelbrot
   ```

3. Uma janela será aberta com o fractal. Use os controles descritos acima para explorar!

## Recursos

- Interface gráfica responsiva com Swing
- Zoom suave centrado no cursor
- Paleta de cores personalizada
- Navegação intuitiva
- Informações em tempo real (nível de zoom e coordenadas do centro)