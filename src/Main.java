import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Main extends JFrame {

    // ============================================================
    // CONFIGURACION DEL LABERINTO
    // ============================================================

    private static final int FILAS = 10;
    private static final int COLUMNAS = 10;

    // 0 = camino
    // 1 = pared
    private final int[][] laberinto = {
            {0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {1, 1, 0, 1, 0, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 1, 1, 1, 0, 1, 0, 1, 1, 1},
            {0, 0, 0, 1, 0, 0, 0, 1, 0, 0},
            {1, 1, 0, 1, 1, 1, 0, 1, 0, 1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 0, 0}
    };

    private final int inicioFila = 0;
    private final int inicioColumna = 0;

    private final int objetivoFila = 9;
    private final int objetivoColumna = 9;

    // ============================================================
    // Q-LEARNING
    // ============================================================

    private static final int NUM_ACCIONES = 4;

    // 0 = arriba
    // 1 = derecha
    // 2 = abajo
    // 3 = izquierda

    private double[][] qTable = new double[FILAS * COLUMNAS][NUM_ACCIONES];

    private double alpha = 0.70;
    private double gamma = 0.90;

    private double epsilon = 1.00;
    private final double epsilonMin = 0.05;

    private final Random random = new Random();

    private boolean entrenando = false;

    private List<Point> mejorRuta = new ArrayList<>();

    private double mejorRecompensa = Double.NEGATIVE_INFINITY;
    private int mejorNumeroPasos = Integer.MAX_VALUE;

    private final List<Double> historialRecompensas = new ArrayList<>();

    // ============================================================
    // COMPONENTES DE LA INTERFAZ
    // ============================================================

    private LaberintoPanel laberintoPanel;

    private JLabel lblEpisodio;
    private JLabel lblRecompensa;
    private JLabel lblPasos;
    private JLabel lblEpsilon;
    private JLabel lblMejorRecompensa;
    private JLabel lblMejorRuta;

    private JProgressBar barraProgreso;

    private JTextArea areaLog;

    private GraficaPanel graficaPanel;

    private JSpinner spinnerEpisodios;
    private JSpinner spinnerAlpha;
    private JSpinner spinnerGamma;

    private JButton btnEntrenar;
    private JButton btnMejorRuta;
    private JButton btnReset;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public Main() {

        setTitle("Aprendizaje por Refuerzo - Q-Learning - Laberinto");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(1250, 800);

        setLocationRelativeTo(null);

        inicializarInterfaz();
    }

    // ============================================================
    // INTERFAZ
    // ============================================================

    private void inicializarInterfaz() {

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));

        panelPrincipal.setBorder(new EmptyBorder(10, 10, 10, 10));

        setContentPane(panelPrincipal);

        // --------------------------------------------------------
        // TITULO
        // --------------------------------------------------------

        JLabel titulo = new JLabel(
                "APRENDIZAJE POR REFUERZO - Q-LEARNING",
                SwingConstants.CENTER
        );

        titulo.setFont(new Font("Arial", Font.BOLD, 24));

        panelPrincipal.add(titulo, BorderLayout.NORTH);

        // --------------------------------------------------------
        // CENTRO
        // --------------------------------------------------------

        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));

        laberintoPanel = new LaberintoPanel();

        panelCentro.add(laberintoPanel, BorderLayout.CENTER);

        graficaPanel = new GraficaPanel();

        graficaPanel.setPreferredSize(new Dimension(360, 250));

        panelCentro.add(graficaPanel, BorderLayout.SOUTH);

        panelPrincipal.add(panelCentro, BorderLayout.CENTER);

        // --------------------------------------------------------
        // PANEL DERECHO
        // --------------------------------------------------------

        JPanel panelDerecho = new JPanel();

        panelDerecho.setLayout(new BoxLayout(panelDerecho, BoxLayout.Y_AXIS));

        panelDerecho.setPreferredSize(new Dimension(300, 0));

        // Parametros
        JPanel panelParametros = crearPanelParametros();

        panelDerecho.add(panelParametros);

        panelDerecho.add(Box.createVerticalStrut(10));

        // Resultados
        JPanel panelResultados = crearPanelResultados();

        panelDerecho.add(panelResultados);

        panelDerecho.add(Box.createVerticalStrut(10));

        // Botones
        JPanel panelBotones = crearPanelBotones();

        panelDerecho.add(panelBotones);

        panelDerecho.add(Box.createVerticalStrut(10));

        // Progreso
        JPanel panelProgreso = crearPanelProgreso();

        panelDerecho.add(panelProgreso);

        panelDerecho.add(Box.createVerticalStrut(10));

        // Log
        JPanel panelLog = crearPanelLog();

        panelDerecho.add(panelLog);

        panelPrincipal.add(panelDerecho, BorderLayout.EAST);

        actualizarEtiquetasIniciales();
    }

    // ============================================================
    // PANEL PARAMETROS
    // ============================================================

    private JPanel crearPanelParametros() {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBorder(
                BorderFactory.createTitledBorder("Parametros de aprendizaje")
        );

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Episodios

        gbc.gridx = 0;
        gbc.gridy = 0;

        panel.add(new JLabel("Episodios:"), gbc);

        spinnerEpisodios = new JSpinner(
                new SpinnerNumberModel(500, 10, 5000, 10)
        );

        gbc.gridx = 1;

        panel.add(spinnerEpisodios, gbc);

        // Alpha

        gbc.gridx = 0;
        gbc.gridy = 1;

        panel.add(new JLabel("Alpha:"), gbc);

        spinnerAlpha = new JSpinner(
                new SpinnerNumberModel(0.70, 0.01, 1.00, 0.05)
        );

        gbc.gridx = 1;

        panel.add(spinnerAlpha, gbc);

        // Gamma

        gbc.gridx = 0;
        gbc.gridy = 2;

        panel.add(new JLabel("Gamma:"), gbc);

        spinnerGamma = new JSpinner(
                new SpinnerNumberModel(0.90, 0.01, 1.00, 0.05)
        );

        gbc.gridx = 1;

        panel.add(spinnerGamma, gbc);

        return panel;
    }

    // ============================================================
    // PANEL RESULTADOS
    // ============================================================

    private JPanel crearPanelResultados() {

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setBorder(
                BorderFactory.createTitledBorder("Resultados en tiempo real")
        );

        lblEpisodio = crearEtiquetaResultado();

        lblRecompensa = crearEtiquetaResultado();

        lblPasos = crearEtiquetaResultado();

        lblEpsilon = crearEtiquetaResultado();

        lblMejorRecompensa = crearEtiquetaResultado();

        lblMejorRuta = crearEtiquetaResultado();

        panel.add(lblEpisodio);

        panel.add(Box.createVerticalStrut(5));

        panel.add(lblRecompensa);

        panel.add(Box.createVerticalStrut(5));

        panel.add(lblPasos);

        panel.add(Box.createVerticalStrut(5));

        panel.add(lblEpsilon);

        panel.add(Box.createVerticalStrut(5));

        panel.add(lblMejorRecompensa);

        panel.add(Box.createVerticalStrut(5));

        panel.add(lblMejorRuta);

        return panel;
    }

    private JLabel crearEtiquetaResultado() {

        JLabel label = new JLabel(" ");

        label.setFont(new Font("Arial", Font.PLAIN, 14));

        return label;
    }

    // ============================================================
    // PANEL BOTONES
    // ============================================================

    private JPanel crearPanelBotones() {

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setBorder(
                BorderFactory.createTitledBorder("Control")
        );

        btnEntrenar = new JButton("ENTRENAR AGENTE");

        btnEntrenar.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnEntrenar.addActionListener(this::entrenarAgente);

        panel.add(btnEntrenar);

        panel.add(Box.createVerticalStrut(8));

        btnMejorRuta = new JButton("EJECUTAR MEJOR RUTA");

        btnMejorRuta.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnMejorRuta.setEnabled(false);

        btnMejorRuta.addActionListener(e -> ejecutarMejorRuta());

        panel.add(btnMejorRuta);

        panel.add(Box.createVerticalStrut(8));

        btnReset = new JButton("REINICIAR");

        btnReset.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnReset.addActionListener(e -> reiniciar());

        panel.add(btnReset);

        return panel;
    }

    // ============================================================
    // PANEL PROGRESO
    // ============================================================

    private JPanel crearPanelProgreso() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBorder(
                BorderFactory.createTitledBorder("Progreso")
        );

        barraProgreso = new JProgressBar();

        barraProgreso.setStringPainted(true);

        barraProgreso.setMinimum(0);

        barraProgreso.setMaximum(500);

        barraProgreso.setValue(0);

        panel.add(barraProgreso, BorderLayout.CENTER);

        return panel;
    }

    // ============================================================
    // PANEL LOG
    // ============================================================

    private JPanel crearPanelLog() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBorder(
                BorderFactory.createTitledBorder("Registro")
        );

        areaLog = new JTextArea();

        areaLog.setEditable(false);

        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 11));

        JScrollPane scroll = new JScrollPane(areaLog);

        scroll.setPreferredSize(new Dimension(280, 170));

        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ============================================================
    // VALORES INICIALES
    // ============================================================

    private void actualizarEtiquetasIniciales() {

        lblEpisodio.setText("Episodio: 0/500");

        lblRecompensa.setText("Recompensa: -");

        lblPasos.setText("Pasos: -");

        lblEpsilon.setText("Epsilon: 1.000");

        lblMejorRecompensa.setText("Mejor recompensa: -");

        lblMejorRuta.setText("Mejor ruta: -");

        barraProgreso.setMaximum(500);

        barraProgreso.setValue(0);
    }

    // ============================================================
    // ENTRENAMIENTO
    // ============================================================

    private void entrenarAgente(ActionEvent event) {

        if (entrenando) {
            return;
        }

        int episodios = (Integer) spinnerEpisodios.getValue();

        alpha = ((Number) spinnerAlpha.getValue()).doubleValue();

        gamma = ((Number) spinnerGamma.getValue()).doubleValue();

        // Reiniciar aprendizaje
        qTable = new double[FILAS * COLUMNAS][NUM_ACCIONES];

        epsilon = 1.0;

        mejorRuta = new ArrayList<>();

        mejorRecompensa = Double.NEGATIVE_INFINITY;

        mejorNumeroPasos = Integer.MAX_VALUE;

        historialRecompensas.clear();

        graficaPanel.setValores(historialRecompensas);

        barraProgreso.setMaximum(episodios);

        barraProgreso.setValue(0);

        areaLog.setText("");

        escribirLog("======================================");

        escribirLog("NUEVO ENTRENAMIENTO");

        escribirLog("Episodios: " + episodios);

        escribirLog("Alpha: " + String.format("%.2f", alpha));

        escribirLog("Gamma: " + String.format("%.2f", gamma));

        escribirLog("======================================");

        entrenando = true;

        btnEntrenar.setEnabled(false);

        btnMejorRuta.setEnabled(false);

        spinnerEpisodios.setEnabled(false);

        spinnerAlpha.setEnabled(false);

        spinnerGamma.setEnabled(false);

        SwingWorker<Void, DatosEpisodio> worker =
                new SwingWorker<>() {

                    @Override
                    protected Void doInBackground() {

                        for (int episodio = 1; episodio <= episodios; episodio++) {

                            if (isCancelled()) {
                                break;
                            }

                            DatosEpisodio datos =
                                    ejecutarEpisodio(episodio, episodios);

                            historialRecompensas.add(
                                    datos.recompensa
                            );

                            // Mostrar el aprendizaje
                            // poco a poco en pantalla.
                            publish(datos);

                            // Pausa para que el usuario
                            // pueda observar el proceso.
                            try {

                                Thread.sleep(15);

                            } catch (InterruptedException e) {

                                Thread.currentThread().interrupt();

                                break;
                            }
                        }

                        return null;
                    }

                    @Override
                    protected void process(
                            List<DatosEpisodio> datos
                    ) {

                        if (datos.isEmpty()) {
                            return;
                        }

                        DatosEpisodio ultimo =
                                datos.get(datos.size() - 1);

                        actualizarInterfazDuranteEntrenamiento(
                                ultimo
                        );
                    }

                    @Override
                    protected void done() {

                        terminarEntrenamiento(
                                episodios
                        );
                    }
                };

        worker.execute();
    }

    // ============================================================
    // EJECUTAR UN EPISODIO
    // ============================================================

    private DatosEpisodio ejecutarEpisodio(
            int episodio,
            int totalEpisodios
    ) {

        int fila = inicioFila;

        int columna = inicioColumna;

        double recompensaTotal = 0;

        int pasos = 0;

        List<Point> ruta = new ArrayList<>();

        ruta.add(new Point(fila, columna));

        boolean llegoObjetivo = false;

        int maxPasos = 200;

        for (int paso = 0; paso < maxPasos; paso++) {

            int estado = obtenerEstado(fila, columna);

            int accion = elegirAccion(estado);

            Movimiento movimiento =
                    realizarMovimiento(
                            fila,
                            columna,
                            accion
                    );

            double recompensa =
                    calcularRecompensa(
                            movimiento.nuevaFila,
                            movimiento.nuevaColumna,
                            movimiento.chocoPared
                    );

            int nuevoEstado =
                    obtenerEstado(
                            movimiento.nuevaFila,
                            movimiento.nuevaColumna
                    );

            double maxQ =
                    obtenerMaxQ(nuevoEstado);

            double qActual =
                    qTable[estado][accion];

            double nuevoQ =
                    qActual
                            + alpha
                            * (
                            recompensa
                                    + gamma * maxQ
                                    - qActual
                    );

            qTable[estado][accion] = nuevoQ;

            recompensaTotal += recompensa;

            pasos++;

            fila = movimiento.nuevaFila;

            columna = movimiento.nuevaColumna;

            ruta.add(new Point(fila, columna));

            if (
                    fila == objetivoFila
                            && columna == objetivoColumna
            ) {

                llegoObjetivo = true;

                break;
            }
        }

        // Reducir exploracion progresivamente.
        epsilon = Math.max(
                epsilonMin,
                epsilon * 0.995
        );

        boolean esMejor = false;

        if (llegoObjetivo) {

            if (
                    recompensaTotal > mejorRecompensa
                            || (
                            Math.abs(
                                    recompensaTotal
                                            - mejorRecompensa
                            ) < 0.0001
                                    && pasos < mejorNumeroPasos
                    )
            ) {

                mejorRecompensa =
                        recompensaTotal;

                mejorNumeroPasos =
                        pasos;

                mejorRuta =
                        new ArrayList<>(ruta);

                esMejor = true;
            }
        }

        return new DatosEpisodio(
                episodio,
                totalEpisodios,
                recompensaTotal,
                pasos,
                epsilon,
                llegoObjetivo,
                esMejor,
                new ArrayList<>(ruta)
        );
    }

    // ============================================================
    // SELECCION DE ACCION
    // ============================================================

    private int elegirAccion(int estado) {

        // Exploracion
        if (random.nextDouble() < epsilon) {

            return random.nextInt(NUM_ACCIONES);
        }

        // Explotacion
        double[] valores =
                qTable[estado];

        double maximo =
                valores[0];

        for (int i = 1; i < NUM_ACCIONES; i++) {

            if (valores[i] > maximo) {

                maximo = valores[i];
            }
        }

        List<Integer> mejores =
                new ArrayList<>();

        for (int i = 0; i < NUM_ACCIONES; i++) {

            if (
                    Math.abs(
                            valores[i] - maximo
                    ) < 0.000001
            ) {

                mejores.add(i);
            }
        }

        return mejores.get(
                random.nextInt(mejores.size())
        );
    }

    // ============================================================
    // MOVIMIENTO
    // ============================================================

    private Movimiento realizarMovimiento(
            int fila,
            int columna,
            int accion
    ) {

        int nuevaFila = fila;

        int nuevaColumna = columna;

        if (accion == 0) {
            nuevaFila--;
        }

        if (accion == 1) {
            nuevaColumna++;
        }

        if (accion == 2) {
            nuevaFila++;
        }

        if (accion == 3) {
            nuevaColumna--;
        }

        // Fuera del tablero
        if (
                nuevaFila < 0
                        || nuevaFila >= FILAS
                        || nuevaColumna < 0
                        || nuevaColumna >= COLUMNAS
        ) {

            return new Movimiento(
                    fila,
                    columna,
                    true
            );
        }

        // Pared
        if (
                laberinto[nuevaFila][nuevaColumna]
                        == 1
        ) {

            return new Movimiento(
                    fila,
                    columna,
                    true
            );
        }

        return new Movimiento(
                nuevaFila,
                nuevaColumna,
                false
        );
    }

    // ============================================================
    // RECOMPENSA
    // ============================================================

    private double calcularRecompensa(
            int fila,
            int columna,
            boolean chocoPared
    ) {

        if (
                fila == objetivoFila
                        && columna == objetivoColumna
        ) {

            return 100.0;
        }

        if (chocoPared) {

            return -5.0;
        }

        // Penalizacion por cada paso.
        return -1.20;
    }

    // ============================================================
    // ESTADO
    // ============================================================

    private int obtenerEstado(
            int fila,
            int columna
    ) {

        return fila * COLUMNAS + columna;
    }

    // ============================================================
    // MAX Q
    // ============================================================

    private double obtenerMaxQ(int estado) {

        double maximo =
                qTable[estado][0];

        for (int i = 1; i < NUM_ACCIONES; i++) {

            if (
                    qTable[estado][i]
                            > maximo
            ) {

                maximo =
                        qTable[estado][i];
            }
        }

        return maximo;
    }

    // ============================================================
    // ACTUALIZAR INTERFAZ
    // ============================================================

    private void actualizarInterfazDuranteEntrenamiento(
            DatosEpisodio datos
    ) {

        lblEpisodio.setText(
                "Episodio: "
                        + datos.episodio
                        + "/"
                        + datos.totalEpisodios
        );

        lblRecompensa.setText(
                String.format(
                        "Recompensa: %.2f",
                        datos.recompensa
                )
        );

        lblPasos.setText(
                "Pasos: "
                        + datos.pasos
        );

        lblEpsilon.setText(
                String.format(
                        "Epsilon: %.3f",
                        datos.epsilon
                )
        );

        if (
                mejorRecompensa
                        == Double.NEGATIVE_INFINITY
        ) {

            lblMejorRecompensa.setText(
                    "Mejor recompensa: -"
            );

            lblMejorRuta.setText(
                    "Mejor ruta: aun no encontrada"
            );

        } else {

            lblMejorRecompensa.setText(
                    String.format(
                            "Mejor recompensa: %.2f",
                            mejorRecompensa
                    )
            );

            lblMejorRuta.setText(
                    "Mejor ruta: "
                            + mejorNumeroPasos
                            + " pasos"
            );
        }

        barraProgreso.setValue(
                datos.episodio
        );

        // Mostrar el recorrido del episodio actual.
        laberintoPanel.setRutaActual(
                datos.ruta
        );

        if (!datos.ruta.isEmpty()) {

            Point ultima =
                    datos.ruta.get(
                            datos.ruta.size() - 1
                    );

            laberintoPanel.setAgente(
                    ultima.x,
                    ultima.y
            );
        }

        graficaPanel.setValores(
                historialRecompensas
        );

        // Escribir en el log solo cada 10 episodios.
        if (
                datos.episodio % 10 == 0
                        || datos.episodio == 1
        ) {

            String resultado;

            if (datos.llegoObjetivo) {

                resultado = "OBJETIVO";

            } else {

                resultado = "no llega";
            }

            escribirLog(
                    "Ep "
                            + datos.episodio
                            + " | Reward: "
                            + String.format(
                            "%.1f",
                            datos.recompensa
                    )
                            + " | Pasos: "
                            + datos.pasos
                            + " | "
                            + resultado
            );
        }
    }

    // ============================================================
    // FINALIZAR ENTRENAMIENTO
    // ============================================================

    private void terminarEntrenamiento(
            int episodios
    ) {

        entrenando = false;

        btnEntrenar.setEnabled(true);

        spinnerEpisodios.setEnabled(true);

        spinnerAlpha.setEnabled(true);

        spinnerGamma.setEnabled(true);

        if (!mejorRuta.isEmpty()) {

            btnMejorRuta.setEnabled(true);

            escribirLog("");

            escribirLog(
                    "ENTRENAMIENTO FINALIZADO"
            );

            escribirLog(
                    "Mejor ruta encontrada: "
                            + mejorNumeroPasos
                            + " pasos"
            );

            escribirLog(
                    String.format(
                            "Mejor recompensa: %.2f",
                            mejorRecompensa
                    )
            );

            escribirLog(
                    "Ya puedes ejecutar la mejor ruta."
            );

        } else {

            escribirLog("");

            escribirLog(
                    "ENTRENAMIENTO FINALIZADO"
            );

            escribirLog(
                    "No se encontro una ruta."
            );
        }

        laberintoPanel.setRutaActual(
                mejorRuta
        );

        graficaPanel.setValores(
                historialRecompensas
        );

        barraProgreso.setValue(
                episodios
        );

        lblEpisodio.setText(
                "Episodio: "
                        + episodios
                        + "/"
                        + episodios
        );
    }

    // ============================================================
    // EJECUTAR MEJOR RUTA
    // ============================================================

    private void ejecutarMejorRuta() {

        if (
                mejorRuta == null
                        || mejorRuta.isEmpty()
                        || entrenando
        ) {

            return;
        }

        btnMejorRuta.setEnabled(false);

        escribirLog("");

        escribirLog(
                "Ejecutando mejor ruta..."
        );

        final int[] indice = {0};

        Timer timer =
                new Timer(
                        180,
                        null
                );

        timer.addActionListener(e -> {

            if (
                    indice[0]
                            >= mejorRuta.size()
            ) {

                timer.stop();

                btnMejorRuta.setEnabled(true);

                escribirLog(
                        "Ruta completada."
                );

                return;
            }

            Point punto =
                    mejorRuta.get(
                            indice[0]
                    );

            laberintoPanel.setAgente(
                    punto.x,
                    punto.y
            );

            indice[0]++;
        });

        timer.start();
    }

    // ============================================================
    // REINICIAR
    // ============================================================

    private void reiniciar() {

        if (entrenando) {

            return;
        }

        qTable =
                new double[
                        FILAS * COLUMNAS
                ][NUM_ACCIONES];

        epsilon = 1.0;

        mejorRuta =
                new ArrayList<>();

        mejorRecompensa =
                Double.NEGATIVE_INFINITY;

        mejorNumeroPasos =
                Integer.MAX_VALUE;

        historialRecompensas.clear();

        graficaPanel.setValores(
                historialRecompensas
        );

        laberintoPanel.setAgente(
                inicioFila,
                inicioColumna
        );

        laberintoPanel.setRutaActual(
                new ArrayList<>()
        );

        areaLog.setText("");

        actualizarEtiquetasIniciales();

        btnMejorRuta.setEnabled(false);

        escribirLog(
                "Sistema reiniciado."
        );
    }

    // ============================================================
    // LOG
    // ============================================================

    private void escribirLog(
            String texto
    ) {

        areaLog.append(
                texto
                        + "\n"
        );

        areaLog.setCaretPosition(
                areaLog.getDocument().getLength()
        );
    }

    // ============================================================
    // CLASE DATOS EPISODIO
    // ============================================================

    private static class DatosEpisodio {

        int episodio;

        int totalEpisodios;

        double recompensa;

        int pasos;

        double epsilon;

        boolean llegoObjetivo;

        boolean esMejor;

        List<Point> ruta;

        DatosEpisodio(
                int episodio,
                int totalEpisodios,
                double recompensa,
                int pasos,
                double epsilon,
                boolean llegoObjetivo,
                boolean esMejor,
                List<Point> ruta
        ) {

            this.episodio =
                    episodio;

            this.totalEpisodios =
                    totalEpisodios;

            this.recompensa =
                    recompensa;

            this.pasos =
                    pasos;

            this.epsilon =
                    epsilon;

            this.llegoObjetivo =
                    llegoObjetivo;

            this.esMejor =
                    esMejor;

            this.ruta =
                    ruta;
        }
    }

    // ============================================================
    // CLASE MOVIMIENTO
    // ============================================================

    private static class Movimiento {

        int nuevaFila;

        int nuevaColumna;

        boolean chocoPared;

        Movimiento(
                int nuevaFila,
                int nuevaColumna,
                boolean chocoPared
        ) {

            this.nuevaFila =
                    nuevaFila;

            this.nuevaColumna =
                    nuevaColumna;

            this.chocoPared =
                    chocoPared;
        }
    }

    // ============================================================
    // PANEL DEL LABERINTO
    // ============================================================

    private class LaberintoPanel
            extends JPanel {

        private int agenteFila =
                inicioFila;

        private int agenteColumna =
                inicioColumna;

        private List<Point> rutaActual =
                new ArrayList<>();

        LaberintoPanel() {

            setPreferredSize(
                    new Dimension(
                            650,
                            500
                    )
            );

            setBackground(
                    Color.WHITE
            );
        }

        void setAgente(
                int fila,
                int columna
        ) {

            agenteFila =
                    fila;

            agenteColumna =
                    columna;

            repaint();
        }

        void setRutaActual(
                List<Point> ruta
        ) {

            if (ruta == null) {

                rutaActual =
                        new ArrayList<>();

            } else {

                rutaActual =
                        new ArrayList<>(
                                ruta
                        );
            }

            repaint();
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g.create();

            int ancho =
                    getWidth();

            int alto =
                    getHeight();

            int tamCelda =
                    Math.min(
                            ancho / COLUMNAS,
                            alto / FILAS
                    );

            int inicioX =
                    (
                            ancho
                                    - tamCelda
                                    * COLUMNAS
                    ) / 2;

            int inicioY =
                    (
                            alto
                                    - tamCelda
                                    * FILAS
                    ) / 2;

            // ----------------------------------------------------
            // RUTA
            // ----------------------------------------------------

            if (
                    rutaActual != null
                            && rutaActual.size() > 1
            ) {

                g2.setStroke(
                        new BasicStroke(
                                5,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND
                        )
                );

                g2.setColor(
                        new Color(
                                150,
                                200,
                                255
                        )
                );

                for (
                        int i = 0;
                        i < rutaActual.size() - 1;
                        i++
                ) {

                    Point a =
                            rutaActual.get(i);

                    Point b =
                            rutaActual.get(i + 1);

                    int ax =
                            inicioX
                                    + a.y * tamCelda
                                    + tamCelda / 2;

                    int ay =
                            inicioY
                                    + a.x * tamCelda
                                    + tamCelda / 2;

                    int bx =
                            inicioX
                                    + b.y * tamCelda
                                    + tamCelda / 2;

                    int by =
                            inicioY
                                    + b.x * tamCelda
                                    + tamCelda / 2;

                    g2.drawLine(
                            ax,
                            ay,
                            bx,
                            by
                    );
                }
            }

            // ----------------------------------------------------
            // CELDAS
            // ----------------------------------------------------

            for (
                    int fila = 0;
                    fila < FILAS;
                    fila++
            ) {

                for (
                        int columna = 0;
                        columna < COLUMNAS;
                        columna++
                ) {

                    int x =
                            inicioX
                                    + columna
                                    * tamCelda;

                    int y =
                            inicioY
                                    + fila
                                    * tamCelda;

                    if (
                            laberinto[fila][columna]
                                    == 1
                    ) {

                        g2.setColor(
                                new Color(
                                        45,
                                        45,
                                        45
                                )
                        );

                        g2.fillRect(
                                x,
                                y,
                                tamCelda,
                                tamCelda
                        );

                    } else {

                        g2.setColor(
                                Color.WHITE
                        );

                        g2.fillRect(
                                x,
                                y,
                                tamCelda,
                                tamCelda
                        );
                    }

                    g2.setColor(
                            new Color(
                                    210,
                                    210,
                                    210
                            )
                    );

                    g2.drawRect(
                            x,
                            y,
                            tamCelda,
                            tamCelda
                    );
                }
            }

            // ----------------------------------------------------
            // INICIO
            // ----------------------------------------------------

            int inicioXCelda =
                    inicioX
                            + inicioColumna
                            * tamCelda;

            int inicioYCelda =
                    inicioY
                            + inicioFila
                            * tamCelda;

            g2.setColor(
                    new Color(
                            80,
                            200,
                            100
                    )
            );

            g2.fillRect(
                    inicioXCelda + 4,
                    inicioYCelda + 4,
                    tamCelda - 8,
                    tamCelda - 8
            );

            g2.setColor(
                    Color.BLACK
            );

            g2.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            12
                    )
            );

            g2.drawString(
                    "START",
                    inicioXCelda + 5,
                    inicioYCelda
                            + tamCelda / 2
                            + 5
            );

            // ----------------------------------------------------
            // OBJETIVO
            // ----------------------------------------------------

            int objetivoX =
                    inicioX
                            + objetivoColumna
                            * tamCelda;

            int objetivoY =
                    inicioY
                            + objetivoFila
                            * tamCelda;

            g2.setColor(
                    new Color(
                            230,
                            80,
                            80
                    )
            );

            g2.fillRect(
                    objetivoX + 4,
                    objetivoY + 4,
                    tamCelda - 8,
                    tamCelda - 8
            );

            g2.setColor(
                    Color.BLACK
            );

            g2.drawString(
                    "GOAL",
                    objetivoX + 5,
                    objetivoY
                            + tamCelda / 2
                            + 5
            );

            // ----------------------------------------------------
            // AGENTE
            // ----------------------------------------------------

            int agenteX =
                    inicioX
                            + agenteColumna
                            * tamCelda;

            int agenteY =
                    inicioY
                            + agenteFila
                            * tamCelda;

            int margen =
                    Math.max(
                            7,
                            tamCelda / 6
                    );

            g2.setColor(
                    new Color(
                            40,
                            100,
                            220
                    )
            );

            g2.fillOval(
                    agenteX + margen,
                    agenteY + margen,
                    tamCelda
                            - 2 * margen,
                    tamCelda
                            - 2 * margen
            );

            g2.setColor(
                    Color.WHITE
            );

            g2.setStroke(
                    new BasicStroke(2)
            );

            g2.drawOval(
                    agenteX + margen,
                    agenteY + margen,
                    tamCelda
                            - 2 * margen,
                    tamCelda
                            - 2 * margen
            );

            g2.dispose();
        }
    }

    // ============================================================
    // PANEL DE GRAFICA
    // ============================================================

    private static class GraficaPanel
            extends JPanel {

        private List<Double> valores =
                new ArrayList<>();

        GraficaPanel() {

            setBorder(
                    BorderFactory.createTitledBorder(
                            "Evolucion de la recompensa"
                    )
            );

            setBackground(
                    Color.WHITE
            );
        }

        void setValores(
                List<Double> valores
        ) {

            this.valores =
                    new ArrayList<>(
                            valores
                    );

            repaint();
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            super.paintComponent(g);

            if (
                    valores == null
                            || valores.size() < 2
            ) {

                g.setColor(
                        Color.GRAY
                );

                g.drawString(
                        "Esperando datos de entrenamiento...",
                        20,
                        35
                );

                return;
            }

            Graphics2D g2 =
                    (Graphics2D) g.create();

            int margenIzquierdo = 50;

            int margenDerecho = 20;

            int margenSuperior = 25;

            int margenInferior = 35;

            int ancho =
                    getWidth()
                            - margenIzquierdo
                            - margenDerecho;

            int alto =
                    getHeight()
                            - margenSuperior
                            - margenInferior;

            double minimo =
                    Collections.min(
                            valores
                    );

            double maximo =
                    Collections.max(
                            valores
                    );

            if (
                    Math.abs(
                            maximo - minimo
                    ) < 0.0001
            ) {

                maximo += 1;

                minimo -= 1;
            }

            // Ejes
            g2.setColor(
                    Color.DARK_GRAY
            );

            g2.drawLine(
                    margenIzquierdo,
                    margenSuperior,
                    margenIzquierdo,
                    margenSuperior
                            + alto
            );

            g2.drawLine(
                    margenIzquierdo,
                    margenSuperior
                            + alto,
                    margenIzquierdo
                            + ancho,
                    margenSuperior
                            + alto
            );

            // Etiquetas
            g2.setFont(
                    new Font(
                            "Arial",
                            Font.PLAIN,
                            10
                    )
            );

            g2.drawString(
                    String.format(
                            "%.0f",
                            maximo
                    ),
                    5,
                    margenSuperior + 5
            );

            g2.drawString(
                    String.format(
                            "%.0f",
                            minimo
                    ),
                    5,
                    margenSuperior
                            + alto
            );

            // Grafica
            g2.setStroke(
                    new BasicStroke(2)
            );

            g2.setColor(
                    new Color(
                            50,
                            120,
                            220
                    )
            );

            for (
                    int i = 1;
                    i < valores.size();
                    i++
            ) {

                double valorAnterior =
                        valores.get(i - 1);

                double valorActual =
                        valores.get(i);

                int x1 =
                        margenIzquierdo
                                + (
                                i - 1
                        )
                                * ancho
                                / Math.max(
                                1,
                                valores.size() - 1
                        );

                int x2 =
                        margenIzquierdo
                                + i
                                * ancho
                                / Math.max(
                                1,
                                valores.size() - 1
                        );

                int y1 =
                        margenSuperior
                                + alto
                                - (int) (
                                (
                                        valorAnterior
                                                - minimo
                                )
                                        / (
                                        maximo
                                                - minimo
                                )
                                        * alto
                        );

                int y2 =
                        margenSuperior
                                + alto
                                - (int) (
                                (
                                        valorActual
                                                - minimo
                                )
                                        / (
                                        maximo
                                                - minimo
                                )
                                        * alto
                        );

                g2.drawLine(
                        x1,
                        y1,
                        x2,
                        y2
                );
            }

            g2.setColor(
                    Color.DARK_GRAY
            );

            g2.drawString(
                    "Episodios",
                    margenIzquierdo
                            + ancho / 2
                            - 25,
                    getHeight() - 8
            );

            g2.dispose();
        }
    }

    // ============================================================
    // MAIN
    // ============================================================

    public static void main(
            String[] args
    ) {

        SwingUtilities.invokeLater(
                () -> {

                    Main ventana =
                            new Main();

                    ventana.setVisible(
                            true
                    );
                }
        );
    }
}