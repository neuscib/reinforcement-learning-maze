# Aprendizaje por Refuerzo: Q-Learning aplicado a un laberinto

## Descripción

Este proyecto implementa un sistema de **Aprendizaje por Refuerzo (Reinforcement Learning)** utilizando el algoritmo **Q-Learning**.

El agente debe aprender a desplazarse por un laberinto desde un punto de entrada hasta un objetivo, intentando encontrar una ruta eficiente mediante la experiencia obtenida durante diferentes episodios de entrenamiento.

La aplicación dispone de una interfaz gráfica desarrollada con **Java Swing**, que permite visualizar el proceso de aprendizaje en tiempo real.

## Objetivo

El objetivo del proyecto es demostrar de forma práctica cómo un agente puede aprender a resolver un problema de navegación mediante aprendizaje por refuerzo.

El agente comienza sin conocer cuál es la mejor ruta. Durante el entrenamiento:

1. Explora diferentes movimientos.
2. Recibe recompensas o penalizaciones.
3. Actualiza los valores de su tabla Q.
4. Reduce progresivamente la exploración.
5. Aprende qué acciones son más convenientes en cada posición.
6. Finalmente utiliza los conocimientos adquiridos para ejecutar la mejor ruta encontrada.

## Tecnologías utilizadas

* Java 21
* Java Swing
* Q-Learning
* Aprendizaje por Refuerzo
* Git
* GitHub
* Visual Studio Code

## Algoritmo Q-Learning

Q-Learning es un algoritmo de aprendizaje por refuerzo basado en valores.

Para cada estado y acción se almacena un valor Q que representa la utilidad estimada de realizar esa acción desde ese estado.

La actualización utilizada es:

```text
Q(s,a) = Q(s,a) + alpha * [r + gamma * max Q(s',a') - Q(s,a)]
```

Donde:

* `s` = estado actual.
* `a` = acción realizada.
* `r` = recompensa obtenida.
* `s'` = nuevo estado.
* `alpha` = tasa de aprendizaje.
* `gamma` = factor de descuento.

## Exploración y explotación

El agente utiliza una estrategia **epsilon-greedy**.

Durante las primeras etapas del entrenamiento existe una mayor probabilidad de explorar movimientos nuevos.

A medida que avanza el entrenamiento, el valor de `epsilon` disminuye y el agente utiliza cada vez más los conocimientos almacenados en la tabla Q.

De esta forma se busca equilibrar:

* **Exploración:** probar nuevas acciones.
* **Explotación:** utilizar las acciones que ya han demostrado buenos resultados.

## Laberinto

El laberinto está representado mediante una matriz.

Cada posición puede ser:

* `0` = camino transitable.
* `1` = pared.

El agente comienza en la posición inicial y debe alcanzar la posición objetivo.

En la interfaz:

* Verde = inicio.
* Rojo = objetivo.
* Azul = agente.
* Gris oscuro = pared.
* Línea azul clara = recorrido del episodio actual.

## Sistema de recompensas

El sistema utiliza diferentes recompensas según la acción realizada.

| Situación               | Recompensa |
| ----------------------- | ---------: |
| Llegar al objetivo      |       +100 |
| Movimiento normal       |      -1.20 |
| Chocar contra una pared |         -5 |

La recompensa positiva por alcanzar el objetivo incentiva al agente a encontrar una ruta válida.

Las penalizaciones hacen que el agente intente evitar movimientos innecesarios y choques contra las paredes.

## Parámetros

Los valores iniciales utilizados son:

| Parámetro                    | Valor |
| ---------------------------- | ----: |
| Episodios                    |   500 |
| Alpha                        |  0.70 |
| Gamma                        |  0.90 |
| Epsilon inicial              |  1.00 |
| Epsilon mínimo               |  0.05 |
| Máximo de pasos por episodio |   200 |

Los parámetros de episodios, alpha y gamma pueden modificarse desde la interfaz gráfica.

## Visualización del aprendizaje

La aplicación permite observar el aprendizaje en tiempo real.

Durante el entrenamiento se muestran:

* Episodio actual.
* Recompensa obtenida.
* Número de pasos.
* Valor de epsilon.
* Mejor recompensa encontrada.
* Mejor ruta encontrada.
* Evolución de las recompensas mediante una gráfica.
* Posición actual del agente dentro del laberinto.

Esto permite observar cómo el comportamiento del agente cambia durante el entrenamiento.

## Ejecución

### Requisitos

Se necesita tener instalado:

* Java JDK 21 o superior.
* Git.
* Un entorno de desarrollo como Visual Studio Code.

### Compilar

Desde la carpeta raíz del proyecto:

```bash
javac -d out src/Main.java
```

### Ejecutar

```bash
java -cp out Main
```

## Funcionamiento de la aplicación

Al iniciar el programa aparece el laberinto.

El usuario puede configurar:

* Número de episodios.
* Alpha.
* Gamma.

Después se pulsa:

**ENTRENAR AGENTE**

El agente comienza a explorar el laberinto y la interfaz muestra el progreso del aprendizaje.

Cuando termina el entrenamiento, se habilita:

**EJECUTAR MEJOR RUTA**

Esta opción permite visualizar la ruta que el agente ha aprendido como mejor recorrido encontrado durante el entrenamiento.

El botón:

**REINICIAR**

permite comenzar de nuevo el aprendizaje desde una tabla Q vacía.

## Estructura del proyecto

```text
reinforcement-learning-maze/
│
├── src/
│   └── Main.java
│
├── .gitignore
│
└── README.md
```

Los archivos compilados se generan en la carpeta `out/`, que no se incluye en Git gracias al archivo `.gitignore`.

## Resultados

Los resultados del entrenamiento se generan dinámicamente al ejecutar el programa.

La ruta encontrada puede variar entre ejecuciones debido a la exploración aleatoria utilizada durante el aprendizaje.

Por este motivo, no se fija en este documento una ruta o recompensa concreta como resultado universal del experimento.

## Conclusión

Este proyecto muestra una aplicación práctica de **Aprendizaje por Refuerzo mediante Q-Learning**.

A través de la interacción con el entorno, el agente aprende progresivamente qué acciones son más adecuadas para alcanzar un objetivo dentro de un laberinto.

La interfaz gráfica permite visualizar el proceso de aprendizaje y comprender de forma práctica conceptos como estados, acciones, recompensas, exploración, explotación, alpha, gamma y epsilon.
