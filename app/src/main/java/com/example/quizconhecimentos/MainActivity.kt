package com.example.quizconhecimentos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF2F4F8)
                ) {
                    AppQuiz()
                }
            }
        }
    }
}

// Camada conectada ao Banco SQLite
@Composable
fun AppQuiz() {
    val context = LocalContext.current
    val banco = remember { BancoQuizHelper(context) }
    val listaPerguntas = remember { banco.buscarPerguntas() }

    if (listaPerguntas.isNotEmpty()) {
        TelaQuiz(perguntas = listaPerguntas)
    }
}

// Camada Visual (Design de Tela puro - compatível com @Preview)
@Composable
fun TelaQuiz(perguntas: List<Pergunta>) {
    var indiceAtual by remember { mutableStateOf(0) }
    var pontuacao by remember { mutableStateOf(0) }
    var opcaoSelecionada by remember { mutableStateOf<Int?>(null) }

    val quizFinalizado = indiceAtual >= perguntas.size

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Cabeçalho Estilizado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF283593))
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🧠 Quiz Geral",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (quizFinalizado) "Desafio concluído!" else "Pergunta ${indiceAtual + 1} de ${perguntas.size}",
                        fontSize = 14.sp,
                        color = Color(0xFFC5CAE9)
                    )
                }

                // Selo de Pontuação
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF3949AB)
                ) {
                    Text(
                        text = "⭐ $pontuacao pts",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 2. Barra de Progresso
        val progresso = if (quizFinalizado) 1f else (indiceAtual.toFloat() / perguntas.size.toFloat())
        LinearProgressIndicator(
            progress = { progresso },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = Color(0xFF00C853),
            trackColor = Color(0xFFDDE1E7)
        )

        // 3. Conteúdo Principal (Pergunta ou Tela Final)
        if (quizFinalizado) {
            TelaResultado(
                pontuacao = pontuacao,
                total = perguntas.size,
                onReiniciar = {
                    indiceAtual = 0
                    pontuacao = 0
                    opcaoSelecionada = null
                }
            )
        } else {
            val perguntaAtual = perguntas[indiceAtual]
            val letras = listOf("A", "B", "C")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Cartão do Enunciado
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = perguntaAtual.enunciado,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1D21),
                            lineHeight = 28.sp,
                            modifier = Modifier.padding(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Lista de Alternativas
                    perguntaAtual.opcoes.forEachIndexed { index, textoOpcao ->
                        val corFundo = when {
                            opcaoSelecionada == null -> Color.White
                            index == perguntaAtual.respostaCorreta -> Color(0xFFE8F5E9)
                            index == opcaoSelecionada -> Color(0xFFFFEBEE)
                            else -> Color.White
                        }

                        val corBorda = when {
                            opcaoSelecionada == null -> Color(0xFFD0D7DE)
                            index == perguntaAtual.respostaCorreta -> Color(0xFF2E7D32)
                            index == opcaoSelecionada -> Color(0xFFC62828)
                            else -> Color(0xFFD0D7DE)
                        }

                        OutlinedButton(
                            onClick = {
                                if (opcaoSelecionada == null) {
                                    opcaoSelecionada = index
                                    if (index == perguntaAtual.respostaCorreta) {
                                        pontuacao++
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(2.dp, corBorda),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = corFundo)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${letras[index]}) ",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF283593)
                                )
                                Text(
                                    text = textoOpcao,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1A1D21)
                                )
                            }
                        }
                    }
                }

                // Botão para avançar (aparece após responder)
                if (opcaoSelecionada != null) {
                    Button(
                        onClick = {
                            opcaoSelecionada = null
                            indiceAtual++
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283593))
                    ) {
                        Text(
                            text = if (indiceAtual + 1 == perguntas.size) "Ver Resultado Final 🏆" else "Próxima Pergunta ➡️",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TelaResultado(pontuacao: Int, total: Int, onReiniciar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🏆", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Fim de Jogo!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1D21)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Você acertou $pontuacao de $total perguntas",
                    fontSize = 18.sp,
                    color = Color(0xFF283593),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onReiniciar,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text(text = "Jogar Novamente 🔄", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTelaQuiz() {
    MaterialTheme {
        TelaQuiz(
            perguntas = listOf(
                Pergunta(1, "Qual é o maior planeta do Sistema Solar?", listOf("Saturno", "Júpiter", "Netuno"), 1)
            )
        )
    }
}