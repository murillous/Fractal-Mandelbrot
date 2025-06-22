import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Implementação Simplificada do Fractal de Mandelbrot com Zoom Centrado no Cursor
 * 
 * ESTRUTURA:
 * 1. PARTE MATEMÁTICA - Números complexos e algoritmo de Mandelbrot
 * 2. OTIMIZAÇÕES - Cache de imagem e cores pré-calculadas
 * 3. INTERFACE - Zoom centrado no cursor
 */
public class FractalMandelbrot extends JPanel implements MouseListener, MouseWheelListener, KeyListener {
    
    // === CONFIGURAÇÕES BÁSICAS ===
    private static final int LARGURA = 800;
    private static final int ALTURA = 600;
    private static final int MAX_ITERACOES = 256;
    
    // === PARTE MATEMÁTICA - NÚMEROS COMPLEXOS ===
    private double centroReal = -0.5;     // Parte real do centro do fractal
    private double centroImaginario = 0.0; // Parte imaginária do centro
    private double zoom = 1.0;            // Nível de zoom atual
    private final double FATOR_ZOOM = 1.5; // Multiplicador do zoom
    
    // === OTIMIZAÇÕES ===
    private BufferedImage imagemFractal;
    private boolean precisaRedesenhar = true;
    private Color[] paletaCores;          // Cores pré-calculadas para performance
    
    public FractalMandelbrot() {
        configurarInterface();
        criarPaletaCores();
        gerarFractal();
    }
    
    private void configurarInterface() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(Color.BLACK);
        setFocusable(true);
        
        addMouseListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        
        imagemFractal = new BufferedImage(LARGURA, ALTURA, BufferedImage.TYPE_INT_RGB);
    }
    
    /**
     * OTIMIZAÇÃO: Cria paleta de cores baseada nas cores especificadas
     * Evita cálculos repetidos durante a renderização
     */
    private void criarPaletaCores() {
        // Cores especificadas convertidas para Color
        Color[] coresBase = {
            new Color(0x000011), new Color(0x000044), new Color(0x000088),
            new Color(0x0044cc), new Color(0x0088ff), new Color(0x44ccff),
            new Color(0x88ffcc), new Color(0xccff88), new Color(0xffcc44),
            new Color(0xff8800), new Color(0xff4400), new Color(0xcc0000),
            new Color(0xffffff)
        };
        
        paletaCores = new Color[MAX_ITERACOES];
        
        // OTIMIZAÇÃO: Interpola cores para criar gradiente suave
        for (int i = 0; i < MAX_ITERACOES; i++) {
            float posicao = (float) i / MAX_ITERACOES;
            paletaCores[i] = interpolarCor(coresBase, posicao);
        }
    }
    
    /**
     * MATEMÁTICA: Interpola entre cores para criar transições suaves
     */
    private Color interpolarCor(Color[] cores, float posicao) {
        if (posicao >= 1.0f) return cores[cores.length - 1];
        if (posicao <= 0.0f) return cores[0];
        
        // Encontra segmento para interpolação
        float escalado = posicao * (cores.length - 1);
        int indice = (int) escalado;
        float fracao = escalado - indice;
        
        if (indice >= cores.length - 1) return cores[cores.length - 1];
        
        Color cor1 = cores[indice];
        Color cor2 = cores[indice + 1];
        
        // Interpola RGB
        int r = (int) (cor1.getRed() + fracao * (cor2.getRed() - cor1.getRed()));
        int g = (int) (cor1.getGreen() + fracao * (cor2.getGreen() - cor1.getGreen()));
        int b = (int) (cor1.getBlue() + fracao * (cor2.getBlue() - cor1.getBlue()));
        
        return new Color(r, g, b);
    }
    
    /**
     * ALGORITMO MATEMÁTICO DE MANDELBROT
     * Implementa: z(n+1) = z(n)² + c
     * onde z começa em 0 e c é o ponto complexo sendo testado
     */
    private int calcularIteracoesMandelbrot(double cReal, double cImaginario) {
        double zReal = 0.0;        // Parte real de z
        double zImaginario = 0.0;  // Parte imaginária de z
        
        for (int iteracao = 0; iteracao < MAX_ITERACOES; iteracao++) {
            // MATEMÁTICA COMPLEXA: z² = (a + bi)² = a² - b² + 2abi
            double zRealNovo = zReal * zReal - zImaginario * zImaginario + cReal;
            double zImaginarioNovo = 2.0 * zReal * zImaginario + cImaginario;
            
            // TESTE DE DIVERGÊNCIA: Se |z|² > 4, o ponto escapa
            if (zRealNovo * zRealNovo + zImaginarioNovo * zImaginarioNovo > 4.0) {
                return iteracao;
            }
            
            zReal = zRealNovo;
            zImaginario = zImaginarioNovo;
        }
        
        return MAX_ITERACOES; // Ponto pertence ao conjunto
    }
    
    /**
     * CONVERSÃO DE COORDENADAS: Tela → Plano Complexo
     * Transforma pixels da tela em coordenadas do plano complexo
     */
    private double[] pixelParaComplexo(int x, int y) {
        // Calcula dimensões do plano complexo baseado no zoom
        double larguraPlano = 4.0 / zoom;
        double alturaPlano = 3.0 / zoom;
        
        // Calcula offset para centralizar
        double offsetReal = centroReal - larguraPlano / 2.0;
        double offsetImaginario = centroImaginario - alturaPlano / 2.0;
        
        // Converte coordenadas
        double real = offsetReal + ((double) x / LARGURA) * larguraPlano;
        double imaginario = offsetImaginario + ((double) y / ALTURA) * alturaPlano;
        
        return new double[]{real, imaginario};
    }
    
    /**
     * GERAÇÃO DO FRACTAL
     * Calcula e renderiza o fractal completo
     */
    private void gerarFractal() {
        if (!precisaRedesenhar) return;
        
        System.out.println("Gerando fractal - Zoom: " + String.format("%.2e", zoom));
        
        // Percorre cada pixel da imagem
        for (int y = 0; y < ALTURA; y++) {
            for (int x = 0; x < LARGURA; x++) {
                // Converte pixel para coordenadas complexas
                double[] coords = pixelParaComplexo(x, y);
                
                // Calcula quantas iterações até divergir
                int iteracoes = calcularIteracoesMandelbrot(coords[0], coords[1]);
                
                // OTIMIZAÇÃO: Seleciona cor da paleta pré-calculada
                Color cor;
                if (iteracoes >= MAX_ITERACOES) {
                    cor = Color.BLACK; // Pontos do conjunto são pretos
                } else {
                    cor = paletaCores[iteracoes];
                }
                
                imagemFractal.setRGB(x, y, cor.getRGB());
            }
        }
        
        precisaRedesenhar = false;
    }
    
    /**
     * ZOOM CENTRADO NO CURSOR
     * Mantém o ponto sob o cursor fixo durante o zoom
     */
    private void aplicarZoomNoCursor(double fatorZoom, int xMouse, int yMouse) {
        // Converte posição do mouse para coordenadas complexas ANTES do zoom
        double[] coordsMouse = pixelParaComplexo(xMouse, yMouse);
        double pontoRealMouse = coordsMouse[0];
        double pontoImaginarioMouse = coordsMouse[1];
        
        // Aplica o zoom
        zoom *= fatorZoom;
        
        // MATEMÁTICA DO ZOOM CENTRADO:
        // Recalcula o centro para manter o ponto do mouse fixo
        double[] coordsMouseDepois = pixelParaComplexo(xMouse, yMouse);
        
        // Ajusta o centro pela diferença
        centroReal += pontoRealMouse - coordsMouseDepois[0];
        centroImaginario += pontoImaginarioMouse - coordsMouseDepois[1];
        
        precisaRedesenhar = true;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Gera fractal se necessário
        gerarFractal();
        
        // Desenha a imagem do fractal
        g.drawImage(imagemFractal, 0, 0, null);
        
        // Interface de informações
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Zoom: " + String.format("%.2e", zoom), 10, 20);
        g.drawString("Centro: " + String.format("(%.6f, %.6f)", centroReal, centroImaginario), 10, 35);
        g.drawString("Scroll: Zoom no cursor | Clique: Centralizar | R: Reset", 10, ALTURA - 20);
    }
    
    // === EVENTOS DE MOUSE ===
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // Zoom in ou zoom out baseado na direção do scroll
        double fator = (e.getWheelRotation() < 0) ? FATOR_ZOOM : 1.0 / FATOR_ZOOM;
        aplicarZoomNoCursor(fator, e.getX(), e.getY());
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        // Centraliza o fractal no ponto clicado
        double[] coords = pixelParaComplexo(e.getX(), e.getY());
        centroReal = coords[0];
        centroImaginario = coords[1];
        
        precisaRedesenhar = true;
        repaint();
    }
    
    // === EVENTOS DE TECLADO ===
    
    @Override
    public void keyPressed(KeyEvent e) {
        boolean mudou = false;
        
        switch (e.getKeyCode()) {
            case KeyEvent.VK_R:
                // Reset para visualização inicial
                centroReal = -0.5;
                centroImaginario = 0.0;
                zoom = 1.0;
                mudou = true;
                break;
                
            case KeyEvent.VK_PLUS:
            case KeyEvent.VK_EQUALS:
                zoom *= FATOR_ZOOM;
                mudou = true;
                break;
                
            case KeyEvent.VK_MINUS:
                zoom /= FATOR_ZOOM;
                mudou = true;
                break;
                
            // Navegação com setas
            case KeyEvent.VK_UP:
                centroImaginario -= 0.1 / zoom;
                mudou = true;
                break;
            case KeyEvent.VK_DOWN:
                centroImaginario += 0.1 / zoom;
                mudou = true;
                break;
            case KeyEvent.VK_LEFT:
                centroReal -= 0.1 / zoom;
                mudou = true;
                break;
            case KeyEvent.VK_RIGHT:
                centroReal += 0.1 / zoom;
                mudou = true;
                break;
        }
        
        if (mudou) {
            precisaRedesenhar = true;
            repaint();
        }
    }
    
    // Métodos não utilizados das interfaces
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
    
    /**
     * MÉTODO PRINCIPAL
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("Fractal de Mandelbrot - Zoom Centrado no Cursor");
            FractalMandelbrot fractal = new FractalMandelbrot();
            
            janela.add(fractal);
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setResizable(false);
            janela.pack();
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);
            
            fractal.requestFocusInWindow();
            
            System.out.println("=== FRACTAL DE MANDELBROT SIMPLIFICADO ===");
            System.out.println("MATEMÁTICA: z² + c para números complexos");
            System.out.println("OTIMIZAÇÃO: Cache de imagem + cores pré-calculadas");
            System.out.println("ZOOM: Centrado no cursor com transformações matemáticas");
            System.out.println();
            System.out.println("CONTROLES:");
            System.out.println("• Scroll do mouse: Zoom centrado no cursor");
            System.out.println("• Clique: Centralizar no ponto");
            System.out.println("• Setas: Navegar");
            System.out.println("• +/-: Zoom");
            System.out.println("• R: Reset");
        });
    }
}